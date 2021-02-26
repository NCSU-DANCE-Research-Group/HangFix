package typeDataContent;
/*
 * InvokeStaticInstrumenter inserts count instructions before
 * INVOKESTATIC bytecode in a program. The instrumented program will
 * report how many static invocations happen in a run.
 *
 * Goal:
 *   Insert counter instruction before static invocation instruction.
 *   Report counters before program's normal exit point.
 *
 * Approach:
 *   1. Create a counter class which has a counter field, and
 *      a reporting method.
 *   2. Take each method body, go through each instruction, and
 *      insert count instructions before INVOKESTATIC.
 *   3. Make a call of reporting method of the counter class.
 *
 * Things to learn from this example:
 *   1. How to use Soot to examine a Java class.
 *   2. How to insert profiling instructions in a class.
 */

/* InvokeStaticInstrumenter extends the abstract class BodyTransformer,
 * and implements <pre>internalTransform</pre> method.
 */
import soot.*;
import soot.javaToJimple.AnonInitBodyBuilder;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.*;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JEqExpr;
import soot.jimple.internal.JIfStmt;
import soot.jimple.internal.JNewArrayExpr;
import soot.jimple.internal.JSpecialInvokeExpr;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.util.*;
import utils.Condition;
import utils.ExtraCondition;
import utils.ExtractLoopMetrics;
import utils.ExtractStmts;
import utils.ExtractVars;
import utils.IdentifyField;
import utils.LoopPath;
import utils.LoopPathFinder;
import utils.MethodTagTransformer;
import utils.MethodUtils;
import utils.PathExit;
import ioMethodLib.IOFuncRetRange;

import java.util.*;
import java.util.Map.Entry;

import dataflowAnalysis.MyForwardDependency;

public class InvokeStaticInstrumenter extends BodyTransformer{


  /* internalTransform goes through a method body and inserts
   * counter instructions before an INVOKESTATIC instruction
   */
  protected void internalTransform(Body body, String phase, Map options) { 
    LoopPathFinder loopfinder = new LoopPathFinder();
    loopfinder.transform(body);
    HashMap<Integer, List<LoopPath>> nonExitPaths = loopfinder.getNonExitPath();
    SootMethod ioMethod = null;
    InvokeExpr ioExpr = null;
    List<Value> strides = new ArrayList<>();
    for(Entry<Integer, List<LoopPath>> nonEPathEntry : nonExitPaths.entrySet()){
    	int loopId = nonEPathEntry.getKey();
    	G.v().out.println("At the " + loopId + " loop.........");
    	List<LoopPath> nonEPaths = nonEPathEntry.getValue();
    	G.v().out.println("nonEPaths.size = " + nonEPaths.size()+"!!!!!!");
        for(LoopPath path : nonEPaths){
        	List<Stmt> pathStmts = path.getpathStmt();
        	Value retIOVal = null;
            boolean foundRetVal = false;
            
            Value stride = ExtractLoopMetrics.extractLoopStride(path);
        	G.v().out.println("stride = " + stride);
        	strides.add(stride);
        	
            for(Stmt stmt : pathStmts){        	
            	if(!foundRetVal) {//currently, consider the i/o invocation is on the loop path
            		// there are many kinds of statements, here we are only interested in statements containing InvokeStatic
            		// NOTE: there are two kinds of statements may contain invoke expression: InvokeStmt, and AssignStmt
                    if (!stmt.containsInvokeExpr()) {
                      continue;
                    }

                    // take out the invoke expression
                    InvokeExpr expr = (InvokeExpr)stmt.getInvokeExpr();
//                    String invokeMethodStr = expr.getMethod().getDeclaringClass().toString();
//            	  	G.v().out.println(invokeMethodStr);
//                    boolean isIOClass = invokeMethodStr.equals("java.io.InputStream");
                    String invokeMethodClassStr = expr.getMethod().getDeclaringClass().toString();
                    boolean isIOClass = IOFuncRetRange.ioFuncRetLib.keySet().contains(invokeMethodClassStr);
              	  	if(isIOClass && stmt.getDefBoxes().size() == 1){
              	  		retIOVal = stmt.getDefBoxes().get(0).getValue();
              	  		foundRetVal = true;
              	  		ioMethod = expr.getMethod();
              	  		ioExpr = expr;
              	  		G.v().out.println("retIOVal = " + retIOVal.toString());
//              		G.v().out.println(ioMethod.getSubSignature());
//              	  		continue;
              	  	}
                } 
            }
            
//            if(ioMethod == null) continue;            
//            G.v().out.println("foundAssign!!!"); 		
            getDataArgAndInvokeInstrumentation(ioMethod, ioExpr, body);
    		
        }
     }
    
    //for some cases, e.g., Hadoop-15424, the i/o operation is not inside the loop,
    //they are inside the method itself before the loop.
    if(ioMethod == null){
    	Value retIOVal = null;
        boolean foundRetVal = false;
        
        //conduct data dependency analysis
    	UnitGraph graph = new ExceptionalUnitGraph(body);	  
    	MyForwardDependency depAnaly = new MyForwardDependency(graph, true);//here, we consider the intermediated dependency
  	  	G.v().out.println("-----------Object data-dependency-----------");
  	  	for (Map.Entry<Object, Set<Object>> entry : depAnaly.map_merged.entrySet()) {
  			G.v().out.println(entry.getKey() + " " + entry.getValue().toString());
  	  	}
  	  	G.v().out.println("--------------------------------------------");
        for(Value stride : strides){//each stride is corresponding to each loop path
        	Set<Object> dependencees = depAnaly.map_merged.get(stride);
        	//travese the method body to get the io assignment stmt
    		for(Stmt stmt : MethodUtils.getMethodAllStmts(body)){        	
            	if(!foundRetVal) {//currently, consider the i/o invocation is on the loop path
            		// there are many kinds of statements, here we are only interested in statements containing InvokeStatic
            		// NOTE: there are two kinds of statements may contain invoke expression: InvokeStmt, and AssignStmt
                    if (!stmt.containsInvokeExpr()) {
                      continue;
                    }
                    // take out the invoke expression
                    InvokeExpr expr = (InvokeExpr)stmt.getInvokeExpr();
                    String invokeMethodClassStr = expr.getMethod().getDeclaringClass().toString();
                    boolean isIOClass = IOFuncRetRange.ioFuncRetLib.keySet().contains(invokeMethodClassStr);
              	  	if(isIOClass && stmt.getDefBoxes().size() == 1){
              	  		retIOVal = stmt.getDefBoxes().get(0).getValue();
              	  		if(!dependencees.contains(retIOVal))
              	  			continue;
              	  		ioMethod = expr.getMethod();
              	  		foundRetVal = true;
              	  		ioExpr = expr;
              	  		G.v().out.println("retIOVal = " + retIOVal.toString());
              	  	}
                } 
            }
        	
//    		for(Object dependentee : dependencees){
//        		G.v().out.println("dependentee = " + dependentee + " type = " + dependentee.getClass());
//        	}
    		
    		getDataArgAndInvokeInstrumentation(ioMethod, ioExpr, body);
        	
        }
    	
        
    }
    
    
  }
  
  private void getDataArgAndInvokeInstrumentation(SootMethod ioMethod,InvokeExpr ioExpr, Body body){
	  if(ioMethod == null || ioExpr == null || body == null) return;
	  G.v().out.println(ioMethod.getDeclaringClass().toString());
	  G.v().out.println(ioMethod.getSubSignature());
	  int argIndex = IOFuncRetRange.ioFuncArgLib.get(ioMethod.getDeclaringClass().toString()).get(ioMethod.getSubSignature().toString());
		Value dataArg = ioExpr.getArg(argIndex);
		G.v().out.println(dataArg + " type : " + dataArg.getType());
		Value dataArg2 = null;
		//conduct the data dependency analysis here to simply switching the data argument
		for(Stmt stmt : MethodUtils.getMethodAllStmts(body)){
			if(stmt instanceof JAssignStmt && ((JAssignStmt)stmt).getLeftOp().equals(dataArg)){
				Value right = ((JAssignStmt)stmt).getRightOp();
				if(right instanceof JNewArrayExpr){//e.g., r5 = newarray (byte)[$i1], then $i1 should be the data arg instead of r5
					 dataArg2 = ((JNewArrayExpr)right).getSize();
					 break;
				 }
			}
		}
		if(dataArg2 != null)
			G.v().out.println(dataArg2 + " type : " + dataArg2.getType());    		
		
		SootField field = IdentifyField.getField(MethodUtils.getMethodAllStmts(body), dataArg);
		if(field == null && dataArg2 != null){
			field = IdentifyField.getField(MethodUtils.getMethodAllStmts(body), dataArg2);
			if(field != null) dataArg = dataArg2;
		}
		
//		SootField field = IdentifyField.getField(path, dataArg);
		if(field != null){//the data variable is a class field 
			G.v().out.println("Data var is a class field.");
			String fieldName = field.getSignature();
			G.v().out.println(dataArg + " name is " + fieldName);
			SootClass fieldClass = null;
			if(!field.getDeclaringClass().equals(body.getMethod().getDeclaringClass())){
				//The field declaring class is not the same as the current method's class 
				String className = field.getDeclaringClass().toString();
				soot.options.Options.v().set_whole_program(true);
				PhaseOptions.v().setPhaseOption("tag.ln", "on");
				PhaseOptions.v().setPhaseOption("cg.spark","ignore-types:true");
				Scene.v().forceResolve(className, SootClass.SIGNATURES);	
				fieldClass = Scene.v().getSootClass(className);
				if(fieldClass == null) return;
  			for(SootMethod methodAnother : fieldClass.getMethods()){
					invokeAnotherClassAnalysis methodSearch = new invokeAnotherClassAnalysis(fieldName, ioMethod, body.getMethod().getSignature(), 0);
					if(!methodAnother.hasActiveBody()) methodAnother.retrieveActiveBody();
					methodSearch.transform((JimpleBody)methodAnother.getActiveBody());
				}
				
			} else {
				//neither of the following two branches work on hadoop-15417 case
				if(false){
					String className = body.getMethod().getDeclaringClass().toString();
					invokeSceneAnalysis classSearch = new invokeSceneAnalysis(fieldName, ioMethod, body.getMethod().getSignature(), 0, className);
					PackManager.v().getPack("wjtp").add(new Transform("wjtp.continuation", classSearch));
					classSearch.transform();
				} else {
					fieldClass = body.getMethod().getDeclaringClass();
					if(fieldClass == null) return;
	    			for(SootMethod methodAnother : fieldClass.getMethods()){
						invokeAnotherClassAnalysis methodSearch = new invokeAnotherClassAnalysis(fieldName, ioMethod, body.getMethod().getSignature(), 0);						
						if(!methodAnother.hasActiveBody()) methodAnother.retrieveActiveBody();
						methodSearch.transform((JimpleBody)methodAnother.getActiveBody());
					}
				}
			}
//			if(fieldClass == null) continue;
//			for(SootMethod methodAnother : fieldClass.getMethods()){
//				invokeAnotherClassAnalysis methodSearch = new invokeAnotherClassAnalysis(fieldName, ioMethod, body.getMethod().getSignature(), 0);
////				Options.v().setPhaseOption("jtp", );
////				try{
////					PackManager.v().getPack("jap").add(new Transform("jap.continuation", methodSearch));
////				}catch (Exception e){}
////				methodSearch.transform(methodAnother.getActiveBody(), "jtp.instrumenter");
//				
//				if(!methodAnother.hasActiveBody()) methodAnother.retrieveActiveBody();
//				methodSearch.transform((JimpleBody)methodAnother.getActiveBody());
////				AnonInitBodyBuilder java2JimpleBuilder = new AnonInitBodyBuilder();
////				methodSearch.transform(java2JimpleBuilder.createBody(methodAnother));
////				methodSearch.transform(methodAnother.retrieveActiveBody());
//			}
			
		} else {
			G.v().out.println("Data var is not a field.");
			if(body.getMethod().isStatic()){ 
				//current function is a static function, which means any function can call this method
				//thus, the fixing should be inside current function.
				invokeAnotherClassAnalysis methodSearch = new invokeAnotherClassAnalysis(dataArg.toString(), ioMethod, body.getMethod().getSignature(), 1);
				methodSearch.transform(body);//in current function
			}
		}
  }
}

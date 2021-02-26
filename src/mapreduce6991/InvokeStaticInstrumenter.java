package mapreduce6991;
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
import soot.javaToJimple.LocalGenerator;
import soot.jimple.*;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JEqExpr;
import soot.jimple.internal.JIfStmt;
import soot.jimple.internal.JSpecialInvokeExpr;
import soot.tagkit.Tag;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.util.*;
import utils.Condition;
import utils.ExtraCondition;
import utils.ExtractStmts;
import utils.ExtractVars;
import utils.LoopPath;
import utils.LoopPathFinder;
import utils.LoopTag;
import utils.MethodUtils;
import utils.PathExit;
import ioMethodLib.IOFuncRetRange;
import ioMethodLib.callStackLib;

import java.util.*;
import java.util.Map.Entry;

import mapreduce7089.invokeAnotherClassAnalysis;

public class InvokeStaticInstrumenter extends BodyTransformer{

	/* some internal fields */
	  static SootClass flagClass;
	  static SootMethod setFlag, getFlag;

	  static {
	    flagClass    = Scene.v().loadClassAndSupport("mapreduce6991.MyFlag");
	    setFlag 	 = flagClass.getMethod("void set()");
	    getFlag  	 = flagClass.getMethod("boolean get()");
	  }

  /* internalTransform goes through a method body and inserts
   * counter instructions before an INVOKESTATIC instruction
   */
  protected void internalTransform(Body body, String phase, Map options) { 
    LoopPathFinder loopfinder = new LoopPathFinder();
    loopfinder.transform(body);
//    Set<PathExit> exitPaths = loopfinder.getExitPath();
//    List<LoopPath> nonExitPaths = loopfinder.getNonExitPath();
    HashMap<Integer, List<LoopPath>> nonExitPaths = loopfinder.getNonExitPath();
//    List<LoopPath> allPaths = loopfinder.getAllPath();
//    Map<Value, SootMethod> valMethodMap = new HashMap<Value, SootMethod>();
    SootMethod ioMethod = null;
//    G.v().out.println("nonExitPaths.size = " + nonExitPaths.size()+"!!!!!!");
    Chain units = body.getUnits();
    LocalGenerator lg = new LocalGenerator(body);
    
    List<Stmt> methodStmts = MethodUtils.getMethodAllStmts(body);
    
//    Stmt outLoopStmt2 = null;
//    for(Stmt stmt : methodStmts){
//    	boolean isLoopStmt = false;
//    	for(Tag tag :stmt.getTags()){
//    		if(tag instanceof LoopTag){
//    			isLoopStmt = true; 
//    			break;
//    		}
//    	}
//    	if(!isLoopStmt){
//    		outLoopStmt2 = stmt;
//    		break;
//    	}
//    }
//    G.v().out.println("outLoopStmt2 = " + outLoopStmt2);
    
    for(Entry<Integer, List<LoopPath>> nonEPathEntry : nonExitPaths.entrySet()){
    	int loopId = nonEPathEntry.getKey();
    	G.v().out.println("At the " + loopId + " loop.........");
    	List<LoopPath> nonEPaths = nonEPathEntry.getValue();
    	G.v().out.println("nonEPaths.size = " + nonEPaths.size()+"!!!!!!");
      for(LoopPath path : nonEPaths){
    	List<Stmt> pathStmts = path.getpathStmt();
    	Value retIOVal = null;
        boolean foundRetVal = false;
        boolean foundAssign = false;
//    	List<Stmt> stmtsInvokeNAssignIndex = new ArrayList<Stmt>();//store the stmts which are in between the invocation stmt and the assign the i/o ret to loop index
    	
        int outLoopIndex = methodStmts.indexOf(pathStmts.get(pathStmts.size()-1));
        Stmt outLoopStmt = null;
        for(int i = outLoopIndex; i < methodStmts.size(); i++){
        	boolean isLoopStmt = false;
        	for(Tag tag : methodStmts.get(i).getTags()){
        		if(tag instanceof LoopTag){
        			isLoopStmt = true; 
        			break;
        		}
        	}
        	if(!isLoopStmt){
        		outLoopStmt = methodStmts.get(i);
        		break;
        	}
        }
        if(outLoopStmt == null) continue;
//        G.v().out.println("outLoopIndex = " + methodStmts.get(outLoopIndex));
//        G.v().out.println("outLoopIndex = " + outLoopIndex);
//        Stmt outLoopStmt = (Stmt) units.getSuccOf(pathStmts.get(pathStmts.size()-1));
    	Stmt afterPatchStmt = null;
        
    	boolean containOtherTypes = false;
    	boolean isTargetLoop = false;
    	//only invoke boundChecking API
        for(Stmt stmt : pathStmts){
        	if(!foundRetVal) {//currently, consider the i/o invocation is on the loop path
        		// there are many kinds of statements, here we are only interested in statements containing InvokeStatic
        		// NOTE: there are two kinds of statements may contain invoke expression: InvokeStmt, and AssignStmt
                if (!stmt.containsInvokeExpr()) {
                  continue;
                }
                
                

                // take out the invoke expression
                InvokeExpr expr = (InvokeExpr)stmt.getInvokeExpr();
                String className = expr.getMethod().getDeclaringClass().toString();
//        	  	G.v().out.println(invokeMethodStr);
                boolean isIOClass = className.equals("java.io.File");
          	  	if(isIOClass){
          	  		isTargetLoop = true;
          	  		afterPatchStmt = stmt;
          	  		ioMethod = expr.getMethod(); 
          	  		String methodName = expr.getMethod().getSubSignature();
          	  		int apiType = IOFuncRetRange.checkAPIType(methodName, IOFuncRetRange.invokedAPIs.get(className));
          	  		if(apiType != IOFuncRetRange.BOUND_CHECKING){
          	  			containOtherTypes = true;
          	  			break;
          	  		}
          	  	}
        	}
        }
        
        if(!isTargetLoop || containOtherTypes) return; //we currenly only consider the loop body only has boundchecking API case.
        
        
        RefType flagType = RefType.v("boolean");
        Local flagLocal = lg.generateLocal(flagType);
        Value FlagSetVal = IntConstant.v(1);
        
        // 1. make invoke expression of MyCounter.report()
        InvokeExpr reportExpr = Jimple.v().newStaticInvokeExpr(getFlag.makeRef());
        AssignStmt getFlagStmt = Jimple.v().newAssignStmt(flagLocal, reportExpr); 
        // 2. then, make a invoke statement
        G.v().out.println("outLoopStmt = " + outLoopStmt + "=========================================================================");
        IfStmt ifstmt = Jimple.v().newIfStmt(Jimple.v().newEqExpr(FlagSetVal, flagLocal), outLoopStmt);
        
        G.v().out.println("afterPatchStmt = " + afterPatchStmt);
        Stmt succAfterPatchStmt = (Stmt) units.getSuccOf(afterPatchStmt);
        
        units.insertAfter(getFlagStmt, afterPatchStmt);
        units.insertAfter(ifstmt, getFlagStmt);
        
        Stmt gotoStmt = Jimple.v().newGotoStmt(succAfterPatchStmt);
        units.insertAfter(gotoStmt, ifstmt);
        
        
        String currMethod = body.getMethod().getSignature();
        String currClass = body.getMethod().getDeclaringClass().toString();
        String[] bugNames = currClass.split("\\.");
        if(bugNames == null || bugNames.length == 0) return;
//        G.v().out.println("============================");
//        G.v().out.println(bugNames[0]);
//        G.v().out.println("============================");
        String bugName = bugNames[0];
//        String bugName = "mapreduce6991";
        if(!callStackLib.callStackMap.containsKey(bugName)) return;
        
//        G.v().out.println("currMethod = " + currMethod + "===================" + "currMethodClass = " + currClass);
        String className2 = "";
        String methodName2 = "";
        String callerMethodName = "";
        String callerClassName = "";
        HashMap<String, ArrayList<String>> callMap = callStackLib.callStackMap.get(bugName);
        for(Map.Entry<String, ArrayList<String>> entry : callMap.entrySet()){
        	String caller = entry.getKey();
        	List<String> callees = entry.getValue();
        	boolean foundCaller = false;
        	for(String callee : callees){
        		if(currMethod.contains(callee)) foundCaller = true;
        	}
        	if(!foundCaller) continue;
        	
        	callerClassName = caller.split(":")[0].trim();
        	callerClassName = bugName + "." + callerClassName;
        	callerMethodName = caller.split(":")[1].trim();
        	
        	for(String callee : callees){
        		String tmpClassName = callee.split(":")[0].trim();
        		tmpClassName = bugName + "." + tmpClassName;
//        		G.v().out.println("tmpClassName = " + tmpClassName + "===============");
        		if(!tmpClassName.equals(currClass) && className2 == ""){ 
        			className2 = tmpClassName;
        			methodName2 = callee.split(":")[1].trim();
        		}
        	}
        	
        }
        
        
        
        //we knows that for this bug, it happens in another class
        if(callerClassName.equals("") || callerMethodName.equals("") || className2.equals("") || methodName2.equals("")) return;
        
        G.v().out.println("className2 = " + className2 + ", methodName2 = " + methodName2 + "=====================");
        
        
        //we need to insert checker in method2
        SootClass method2Class = null;
        if(className2.equals(body.getMethod().getDeclaringClass().toString())){
        	method2Class = body.getMethod().getDeclaringClass();
        } else {
        	soot.options.Options.v().set_whole_program(true);
    		PhaseOptions.v().setPhaseOption("tag.ln", "on");
    		PhaseOptions.v().setPhaseOption("cg.spark","ignore-types:true");
    		Scene.v().forceResolve(className2, SootClass.SIGNATURES);
    		method2Class = Scene.v().getSootClass(className2);
        }
        if(method2Class == null) continue;
//        //For this bug, we already know that the create file function is in another class
////        className2 = "mapreduce6991.TestProcfsBasedProcessTree$RogueTaskThread";
//		
//		soot.options.Options.v().set_whole_program(true);
//		PhaseOptions.v().setPhaseOption("tag.ln", "on");
//		PhaseOptions.v().setPhaseOption("cg.spark","ignore-types:true");
//		
////		Scene.v().loadNecessaryClasses();
//		Scene.v().forceResolve(className2, SootClass.SIGNATURES);
//		SootClass anotherClass = Scene.v().getSootClass(className2);
		for(SootMethod methodAnother : method2Class.getMethods()){
//			invokeAnotherClassAnalysis classSearch = new invokeAnotherClassAnalysis(fieldName, ioMethod);
//			classSearch.transform(methodAnother.getActiveBody());
//			G.v().out.println(methodAnother.getSignature() + "===============================================");
			if(!methodAnother.getSignature().contains(methodName2)) continue;
//			if(methodAnother.getSubSignature().equals("run()")){ //we need this input in somewhere. user specified
				Body body2 = methodAnother.getActiveBody();
				Chain units2 = body2.getUnits();
				UnitGraph graph2 = new ExceptionalUnitGraph(body2);
				for(Stmt stmt : MethodUtils.getMethodAllStmts(body2)){
					if(stmt.containsInvokeExpr()){
						InvokeExpr expr = stmt.getInvokeExpr();
						SootMethod invokeMethod = expr.getMethod();
						String invokeMethodName = invokeMethod.getSubSignature();
						String invokeClassName = invokeMethod.getDeclaringClass().toString();
//						G.v().out.println("invokeMethodName = " + invokeMethodName + ", invokeClassName = " + invokeClassName+ "=================");
	          	  		int apiType = IOFuncRetRange.checkAPIType(invokeMethodName, IOFuncRetRange.invokedAPIs.get(invokeClassName));
	          	  		if(apiType != IOFuncRetRange.INDEX_FORWARD){
	          	  			continue;
	          	  		}
	          	  		
	          	  		for (Trap trap : body2.getTraps()) {
	          	  			boolean foundCorrectTrap = false;
	          	  			if(trap.getBeginUnit() instanceof Stmt  && trap.getEndUnit() instanceof Stmt && trap.getHandlerUnit() instanceof Stmt ){
	          	  				for(Unit afterUnit : graph2.getPredsOf(trap.getHandlerUnit())){
	          	  					if(afterUnit instanceof Stmt){
	          	  						Stmt afterStmt = (Stmt) afterUnit;
	          	  						if(afterStmt.equals(stmt)){
	          	  							foundCorrectTrap = true;
	          	  						}
	          	  					}
	          	  				}
	          	  				if(!foundCorrectTrap) continue;
	          	  			
	          	  				G.v().out.println("===trap====");
	          	  				G.v().out.println("try-begin: " + trap.getBeginUnit());
	          	  				G.v().out.println("try-end:   " + trap.getEndUnit());
	          	  				G.v().out.println("catch:     " + trap.getHandlerUnit());
	          	  				G.v().out.println("===========");
	          	  				
	          	  				// 1. first, make a new invoke expression
	          	  				InvokeExpr setExpr= Jimple.v().newStaticInvokeExpr(setFlag.makeRef());
	          	  				// 2. then, make a invoke statement
	            			    Stmt setStmt = Jimple.v().newInvokeStmt(setExpr);
	            			    // 3. insert new statement into the chain(we are mutating the unit chain).
	            			    Stmt previousStmt = (Stmt) units2.getSuccOf(trap.getHandlerUnit());
	            			    if(previousStmt != null && previousStmt.toString().equals(setStmt.toString())) continue; //we don't want to insert the set statement twice
	            			    units2.insertAfter(setStmt, trap.getHandlerUnit());
	          	  			}
	          	  		}
					}
				}
			
		}
        
        //now we need to insert checker in the caller
		SootClass callerClass = null;
        if(callerClassName.equals(body.getMethod().getDeclaringClass().toString())){
        	callerClass = body.getMethod().getDeclaringClass();
        } else {
        	soot.options.Options.v().set_whole_program(true);
    		PhaseOptions.v().setPhaseOption("tag.ln", "on");
    		PhaseOptions.v().setPhaseOption("cg.spark","ignore-types:true");
    		Scene.v().forceResolve(className2, SootClass.SIGNATURES);
    		callerClass = Scene.v().getSootClass(className2);
        }
        if(callerClass == null) continue;
		for(SootMethod callerMethod : callerClass.getMethods()){
			if(!callerMethod.getSignature().contains(callerMethodName)) continue;
			Body callerBody = callerMethod.getActiveBody();
			Chain callerUnits = callerBody.getUnits();
			UnitGraph callerGraph = new ExceptionalUnitGraph(callerBody);
			for(Stmt stmt : MethodUtils.getMethodAllStmts(callerBody)){
				if(stmt.containsInvokeExpr()
						&& stmt.getInvokeExpr().getMethod().getSignature().equals(body.getMethod().getSignature())){
					if(stmt instanceof JAssignStmt){
						Value left = ((JAssignStmt)stmt).getLeftOp();
						//add checker here
						
						//insert exception stmt
						RefType exceptionType = RefType.v("java.io.IOException");
						SootMethodRef cref = Scene.v().makeConstructorRef(exceptionType.getSootClass(),
								Collections.<Type>singletonList(RefType.v("java.lang.String")));
						LocalGenerator callerLG = new LocalGenerator(callerBody);
					    Local exceptionLocal = callerLG.generateLocal(exceptionType);
					    AssignStmt assignStmt = Jimple.v().newAssignStmt(exceptionLocal, Jimple.v().newNewExpr(exceptionType));
					    
					    G.v().out.println("exceptionLocal = " + exceptionLocal + ", type = " + exceptionLocal.getType());
					    String errorMsg = body.getMethod().getSubSignature() + " returns null";
					    SpecialInvokeExpr constructorInvokeExpr = Jimple.v().newSpecialInvokeExpr(exceptionLocal, cref,
								StringConstant.v(errorMsg));
						InvokeStmt invokeStmt = Jimple.v().newInvokeStmt(constructorInvokeExpr);
						ThrowStmt throwStmt = Jimple.v().newThrowStmt(exceptionLocal);
						
						Stmt afterCalleeStmt = (Stmt) callerUnits.getSuccOf(stmt);
						//insert if stmt
						IfStmt callerIfstmt = Jimple.v().newIfStmt(Jimple.v().newNeExpr(left, NullConstant.v()),
								afterCalleeStmt);
						
						
						callerUnits.add(callerIfstmt);
						callerUnits.insertAfter(assignStmt, callerIfstmt);
						callerUnits.insertAfter(invokeStmt, assignStmt);
						callerUnits.insertAfter(throwStmt, invokeStmt);
						
						Stmt gotoInsertStmt = Jimple.v().newGotoStmt(callerIfstmt);
						
						Stmt gotoOrgStmt = Jimple.v().newGotoStmt(afterCalleeStmt);
						
						callerUnits.insertAfter(gotoInsertStmt, stmt);
						callerUnits.insertAfter(gotoOrgStmt, throwStmt);
						
						
						
						/////////////////////
						break;
					}
				}
			}
		}
        
        
        break;
        
        
    }   
    }
  }
}

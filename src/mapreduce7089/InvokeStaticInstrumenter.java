package mapreduce7089;
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
import soot.util.*;
import utils.Condition;
import utils.ExtraCondition;
import utils.ExtractStmts;
import utils.ExtractVars;
import utils.IdentifyField;
import utils.LoopPath;
import utils.LoopPathFinder;
import utils.MethodUtils;
import utils.PathExit;
import ioMethodLib.IOFuncRetRange;

import java.util.*;
import java.util.Map.Entry;

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
//        	List<Stmt> stmtsInvokeNAssignIndex = new ArrayList<Stmt>();//store the stmts which are in between the invocation stmt and the assign the i/o ret to loop index
        	Stmt afterPatchStmt = null;
        	
            for(Stmt stmt : pathStmts){        	
            	if(!foundRetVal) {//currently, consider the i/o invocation is on the loop path
            		// there are many kinds of statements, here we are only interested in statements containing InvokeStatic
            		// NOTE: there are two kinds of statements may contain invoke expression: InvokeStmt, and AssignStmt
                    if (!stmt.containsInvokeExpr()) {
                      continue;
                    }

                    // take out the invoke expression
                    InvokeExpr expr = (InvokeExpr)stmt.getInvokeExpr();
                    String invokeMethodStr = expr.getMethod().getDeclaringClass().toString();
//            	  	G.v().out.println(invokeMethodStr);
                    boolean isIOClass = invokeMethodStr.equals("java.io.InputStream");
              	  	if(isIOClass){
              	  		retIOVal = stmt.getDefBoxes().get(0).getValue();
              	  		foundRetVal = true;
              	  		ioMethod = expr.getMethod();
              	  		ioExpr = expr;
              	  		G.v().out.println("retIOVal = " + retIOVal.toString());
//              		G.v().out.println(ioMethod.getSubSignature());
              	  		continue;
              	  	}
                } else if(foundRetVal & !foundAssign){
//                	stmtsInvokeNAssignIndex.add(stmt);
                	if(stmt instanceof JAssignStmt){
              		  boolean useRetIO = false, useitself = false;
              		  Value assignVal = stmt.getDefBoxes().get(0).getValue();
              		  for(ValueBox vb : stmt.getUseBoxes()){
              			  for(Value retIOsameVal : ExtractVars.extractSameVars(path, retIOVal)){
              				  if(vb.getValue().equals(retIOsameVal)){
              					  useRetIO = true;
              					  break;
              				  }
              			  }
              			  if(vb.getValue().equals(assignVal)){
              				  useitself = true;
              			  }
              		  }
              		  
              		  boolean isIOtoBound = false;
              		  if(useRetIO & useitself){ //check whether it is a bound variable appearing in exit conditions
              			  G.v().out.println("assignVal = " + assignVal.toString());
              			  List<Condition> pathConds = new ArrayList<Condition>();
              			  pathConds = ExtraCondition.extractEqualConditions(path, null);
              			  for(Condition cond : pathConds){
              				G.v().out.println("cond = " + cond.cond.toString());
            				  if(cond.cond.getOp1().equals(assignVal) || cond.cond.getOp2().equals(assignVal)){	
            					isIOtoBound = true;
            					break;
            				  }
              			  }
              		  }
              		  if(isIOtoBound){
              			  foundAssign = true;
              			  afterPatchStmt = stmt;
              		  }
              	  }
               } 
            }
            
            if(!foundAssign) continue;
            
            G.v().out.println("foundAssign!!!");
    		
    		int argIndex = IOFuncRetRange.ioFuncArgLib.get(ioMethod.getDeclaringClass().toString()).get(ioMethod.getSubSignature().toString());
    		Value dataArg = ioExpr.getArg(argIndex);
    		G.v().out.println("type : " + dataArg.getType());
    		SootField field = IdentifyField.getField(path, dataArg);
    		if(field != null){
    			String fieldName = field.getSignature();
    			if(!field.getDeclaringClass().equals(body.getMethod().getDeclaringClass())){//if the field declaring class is not the same as the current method's class 
    				String className = field.getDeclaringClass().toString();
    				
    				soot.options.Options.v().set_whole_program(true);
    				PhaseOptions.v().setPhaseOption("tag.ln", "on");
    				PhaseOptions.v().setPhaseOption("cg.spark","ignore-types:true");
    				Scene.v().forceResolve(className, SootClass.SIGNATURES);
    				SootClass fieldClass = Scene.v().getSootClass(className);
    				for(SootMethod methodAnother : fieldClass.getMethods()){
    					invokeAnotherClassAnalysis classSearch = new invokeAnotherClassAnalysis(fieldName, ioMethod);
    					classSearch.transform(methodAnother.getActiveBody());
    				}
    			}
    		}
        }
     }
  }
}

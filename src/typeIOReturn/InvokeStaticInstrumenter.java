package typeIOReturn;

import soot.*;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.*;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JEqExpr;
import soot.jimple.internal.JIfStmt;
import soot.jimple.internal.JLeExpr;
import soot.jimple.internal.JSpecialInvokeExpr;
import soot.util.*;
import utils.Condition;
import utils.ExtraCondition;
import utils.ExtractStmts;
import utils.ExtractVars;
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
//	    Set<PathExit> exitPaths = loopfinder.getExitPath();
	    HashMap<Integer, List<LoopPath>> nonExitPaths = loopfinder.getNonExitPath();
//	    List<LoopPath> allPaths = loopfinder.getAllPath();
//	    Map<Value, SootMethod> valMethodMap = new HashMap<Value, SootMethod>();
	    SootMethod ioMethod = null;
//	    G.v().out.println("nonExitPaths.size = " + nonExitPaths.size()+"!!!!!!");
	    for(Entry<Integer, List<LoopPath>> nonEPathEntry : nonExitPaths.entrySet()){
	    	int loopId = nonEPathEntry.getKey();
	    	G.v().out.println("At the " + loopId + " loop.........");
	    	List<LoopPath> nonEPaths = nonEPathEntry.getValue();
	    	G.v().out.println("nonEPaths.size = " + nonEPaths.size()+"!!!!!!");
	        for(LoopPath path : nonEPaths){
	        	List<Stmt> pathStmts = path.getpathStmt();
	        	G.v().out.println("pathStmts.size = " + pathStmts.size());
	        	Value retIOVal = null;
	            boolean foundRetVal = false;
	            boolean foundAssign = false;
	        	Stmt ioInvokeStmt = null;
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
	                    String invokeMethodClassStr = expr.getMethod().getDeclaringClass().toString();
	                    G.v().out.println(expr);
	            	  	G.v().out.println(invokeMethodClassStr);
//	                    boolean isIOClass = invokeMethodClassStr.equals("java.io.InputStream");
	                    boolean isIOClass = IOFuncRetRange.ioFuncRetLib.keySet().contains(invokeMethodClassStr);
	              	  	if(isIOClass){
	              	  		ioInvokeStmt = stmt;
	              	  		G.v().out.println("ioInvokeStmt = " + ioInvokeStmt);
	              	  		retIOVal = stmt.getDefBoxes().get(0).getValue();
	              	  		foundRetVal = true;
	              	  		ioMethod = expr.getMethod();
	              	  		G.v().out.println("retIOVal = " + retIOVal.toString());
//	              		G.v().out.println(ioMethod.getSubSignature());
	              	  		continue;
	              	  	}
	                } else if(foundRetVal & !foundAssign){
	                	if(afterPatchStmt == null)
	                		afterPatchStmt = stmt;
//	                	stmtsInvokeNAssignIndex.add(stmt);
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
//	              			  if(vb.getValue().equals(retIOVal)){
//	              				  useRetIO = true;
//	              			  } else 
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
	              			  
	              		  }
	              	  }
	               } 
	            }
	            
//	            for(Stmt stmt :  MethodUtils.getMethodAllStmts(body)){
//	            	 if (!stmt.containsInvokeExpr()) {
//	                     continue;
//	            	 }
//	                 // take out the invoke expression
//	                 InvokeExpr expr = (InvokeExpr)stmt.getInvokeExpr();
//	                 if(expr instanceof JSpecialInvokeExpr){
//	                	 G.v().out.println("methodRef = " + ((JSpecialInvokeExpr)expr).getMethodRef());
//	                	 G.v().out.println("Base = " + ((JSpecialInvokeExpr)expr).getBase());
//	                	 G.v().out.println("arg = " + ((JSpecialInvokeExpr)expr).getArgBox(0));
//	                 }
//	            }
	            
	            if(foundAssign){
	            	G.v().out.println("foundAssign!!!");
	            	List<Condition> pathConds = new ArrayList<Condition>();
	    			pathConds = ExtraCondition.extractEqualConditions(path, null);
	    			List<ConditionExpr> dicCondList = IOFuncRetRange.ioFuncRetLib.get(ioMethod.getDeclaringClass().toString()).get(ioMethod.getSubSignature().toString());
//	    			int argIndex = IOFuncRetRange.ioFuncArgLib.get(ioMethod.getDeclaringClass().toString()).get(ioMethod.getSubSignature().toString());
	    			List<ConditionExpr> patchCondList = new ArrayList<ConditionExpr>();
	    			for(ConditionExpr cond2 : dicCondList){
	    				G.v().out.println("diccond = " + cond2);
	    				boolean containornot = false;
	    				boolean notChecking = true;
	    				for(Condition cond : pathConds){
//	    					G.v().out.println("pathcond = " + cond.cond);
	    					if(cond.cond.getOp1().equals(retIOVal)){
	    						notChecking = false;
	    						G.v().out.println("pathcond = " + cond.cond);
	    						if(IOFuncRetRange.containCond(cond.cond, cond2)){
	    							G.v().out.println("pathcond contains diccond");
	    							containornot = true;
	    							break;
	    						}
	    					}
	    				}
	    				//if 1) the path condition contains the dictionary condition, 
	    				//or 2) the path condition doesn't even checking the retValue, which means it (i.e., no constraint) must contains the dictionary condition,
	    				//should add extra checking for this condition in the original loop path to avoid hang.
	    				if(containornot || notChecking) 
	    					patchCondList.add(cond2);
	    			}
	    			if(patchCondList.size() == 2){
	    				ConditionExpr cond = IOFuncRetRange.mergeConds(patchCondList.get(0), patchCondList.get(1));
	    				G.v().out.println("patchCondList.size = " + patchCondList.size());
	    				G.v().out.println("ioVariable: " + retIOVal.toString());
	    				G.v().out.println("patchCond = " + cond.toString());
	    				G.v().out.println("afterPatchStmt = " + afterPatchStmt);
	    				insertNewCode(cond, body, afterPatchStmt, ioInvokeStmt, retIOVal, ioMethod);
	    			}
	    			if(patchCondList.size() == 1){
	    				ConditionExpr cond = patchCondList.get(0);
	    				G.v().out.println("patchCondList.size = " + patchCondList.size());
	    				G.v().out.println("ioVariable: " + retIOVal.toString());
	    				G.v().out.println("patchCond = " + cond.toString());
	    				G.v().out.println("afterPatchStmt = " + afterPatchStmt);
	    				insertNewCode(cond, body, afterPatchStmt, ioInvokeStmt, retIOVal, ioMethod);		
	    			}
	            }
	    	 }
	    }
	    
	    
	    //at last, we want to print out all the stmt in the current function.
	    G.v().out.println("===============After patching=========================================");
	    G.v().out.println(body.getMethod().getSubSignature()+"==");
	    for(Stmt stmt : MethodUtils.getMethodAllStmts(body)){
	    	G.v().out.println(stmt);
	    }
	    G.v().out.println("=======================================================================");
  }
  
  private void insertNewCode(ConditionExpr cond, Body body, Stmt afterPatchStmt, Stmt ioInvokeStmt, Value retIOVal, SootMethod ioMethod){
	  if(!(cond instanceof JEqExpr || cond instanceof JLeExpr)) return; //currently, we only consider these two types of condition expression
	//insert exception stmt
		RefType runtimeExceptionType = RefType.v("java.io.IOException");
		SootMethodRef cref = Scene.v().makeConstructorRef(runtimeExceptionType.getSootClass(),
				Collections.<Type>singletonList(RefType.v("java.lang.String")));
//		G.v().out.println("cref = " + cref);
		LocalGenerator lg = new LocalGenerator(body);
	    Local exceptionLocal = lg.generateLocal(runtimeExceptionType);
	    AssignStmt assignStmt = Jimple.v().newAssignStmt(exceptionLocal, Jimple.v().newNewExpr(runtimeExceptionType));
	    
	    G.v().out.println("exceptionLocal = " + exceptionLocal + ", type = " + exceptionLocal.getType());
	    String errorMsg = IOFuncRetRange.ioFuncErrMsgLib.get(ioMethod.getDeclaringClass().toString()).get(ioMethod.getSubSignature().toString());
	    SpecialInvokeExpr constructorInvokeExpr = Jimple.v().newSpecialInvokeExpr(exceptionLocal, cref,
				StringConstant.v(errorMsg));
		InvokeStmt invokeStmt = Jimple.v().newInvokeStmt(constructorInvokeExpr);
		ThrowStmt throwStmt = Jimple.v().newThrowStmt(exceptionLocal);
		
		//sometimes, the afterPathStmt is not necessarily the first stmt in a label
//		afterPatchStmt = ExtractStmts.extractFirstStmtInALabel(path, afterPatchStmt);
//		G.v().out.println("afterPatchStmt = " + afterPatchStmt);
		//insert if stmt
//		GotoStmt gotoStmt2 = Jimple.v().newGotoStmt(afterPatchStmt);
		IfStmt ifstmt = null;
		if(cond instanceof JEqExpr){
			ifstmt = Jimple.v().newIfStmt(Jimple.v().newNeExpr(retIOVal, cond.getOp2()), afterPatchStmt);
		} else if(cond instanceof JLeExpr){
			ifstmt = Jimple.v().newIfStmt(Jimple.v().newGtExpr(retIOVal, cond.getOp2()), afterPatchStmt);
		}
		Chain units = body.getUnits();
//		G.v().out.println("ifstmt = " + ifstmt);
		units.add(ifstmt); //using add function can create a new label for the new stmts.
		units.add(assignStmt);
		units.add(invokeStmt);
		units.add(throwStmt);
		G.v().out.println("insert the patch stmt in new label");
		
		GotoStmt gotoStmt1 = Jimple.v().newGotoStmt(ifstmt);
		units.insertAfter(gotoStmt1, ioInvokeStmt);
//		units.insertBefore(gotoStmt2, afterPatchStmt);
//		units.insertAfter(gotoStmt2, throwStmt);
		G.v().out.println("=======================");
		G.v().out.println(ifstmt);
		G.v().out.println(assignStmt);
		G.v().out.println(invokeStmt);
		G.v().out.println(throwStmt);
		G.v().out.println("=======================");	
		
  }
}

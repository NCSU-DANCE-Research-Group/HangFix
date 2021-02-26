package typeMissingTimeout;
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
import soot.jimple.internal.AbstractDefinitionStmt;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JEqExpr;
import soot.jimple.internal.JIdentityStmt;
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
import utils.IdentifyField;
import utils.LoopPath;
import utils.LoopPathFinder;
import utils.LoopTag;
import utils.MethodUtils;
import utils.PathExit;
import ioMethodLib.IOFuncRetRange;
import ioMethodLib.IOFuncRetRange.IndexBoundAPIs;

import java.util.*;
import java.util.zip.Inflater;



public class InvokeStaticInstrumenter extends BodyTransformer{

	
	  /* some internal fields */
	  static SootClass threadClass;
	  static SootMethod decompressWTO;
	  static SootMethod decompressWTO2;
	  static List<SootMethod> hangMethods = new ArrayList<SootMethod>();

	  static {
	    threadClass    = Scene.v().loadClassAndSupport("typeMissingTimeout.MyCallableThread");
	    decompressWTO  = threadClass.getMethod("int decompressWTO(java.util.zip.Inflater,byte[])");
	    decompressWTO2 = threadClass.getMethod("int decompressWTO(java.util.zip.Inflater,byte[],int,int)");
	    hangMethods.add(decompressWTO);
	    hangMethods.add(decompressWTO2);
	  }
	
	
  /* internalTransform goes through a method body and inserts
   * counter instructions before an INVOKESTATIC instruction
   */
  protected void internalTransform(Body body, String phase, Map options) { 
    SootClass ioClass = null;
    SootClass ioClassSuper = null;//if ioClass does not have a super I/O class, then ioClass == ioClassSuper
    SootField ioField = null;
    IndexBoundAPIs invokedAPIs = null;
    Value ioRetValue = null;
    Stmt ioInvokeStmt = null;
    String arguments = "";
//    G.v().out.println("nonExitPaths.size = " + nonExitPaths.size()+"!!!!!!");
    
    
    List<Stmt> stmts = MethodUtils.getMethodAllStmts(body);
    for(Stmt stmt : stmts){
    	if(stmt instanceof JAssignStmt){
    		Value left = ((JAssignStmt)stmt).getLeftOp();
    		Value right = ((JAssignStmt)stmt).getRightOp();
    		if(right instanceof NewExpr){
    			RefType rightType = ((NewExpr)right).getBaseType();
    			String className = rightType.getClassName();
    			if(ioMethodLib.HangOps.classMethodMap.containsKey(className)){
    				ioClass = Scene.v().getSootClass(className);
    				ioClassSuper = ioClass.getSuperclass();
    				break;
    			}
    		}
    	}
    }
    
    if(ioClass == null) return;
    
    G.v().out.println("ioClass = " + ioClass);
    G.v().out.println("ioClassSuper = " + ioClassSuper);
    
    for(Stmt stmt : stmts){
    	if(stmt instanceof JAssignStmt && stmt.containsInvokeExpr()){
    		Value left = ((JAssignStmt)stmt).getLeftOp();
    		InvokeExpr expr = stmt.getInvokeExpr();
    		
    		arguments = "";
    		for(Value arg : expr.getArgs()){
    			arguments += arg.getType().toString() + ",";
    		}
    		if(arguments != "") arguments = arguments.substring(0, arguments.length()-1); //remove the last comma
//    		G.v().out.println("method = " + expr.getMethod().getSubSignature() + " in " + stmt);
    		
    		boolean isTargetClass = expr.getMethod().getDeclaringClass().equals(ioClassSuper) 
    				|| expr.getMethod().getDeclaringClass().equals(ioClass);
    		if(!isTargetClass) continue;
    		boolean isTargetMethod = ioMethodLib.HangOps.classMethodMap.get(ioClass.toString()).contains(expr.getMethod().getSubSignature())
    				|| ioMethodLib.HangOps.classMethodMap.get(ioClassSuper.toString()).contains(expr.getMethod().getSubSignature());
    		if(!isTargetMethod) continue;
    		ioRetValue = left;	
    		ioInvokeStmt = stmt;
    		break;
    	}
    }
    
    if(ioRetValue == null || ioInvokeStmt == null) return;
    
    G.v().out.println("===============Before patching=========================================");
    G.v().out.println(body.getMethod().getSubSignature()+"==");
    for(Stmt stmt : MethodUtils.getMethodAllStmts(body)){
    	G.v().out.println(stmt);
    }
    G.v().out.println("=======================================================================");
    
    if(ioInvokeStmt.getInvokeExpr() instanceof InstanceInvokeExpr){
    	Value base = ((InstanceInvokeExpr)ioInvokeStmt.getInvokeExpr()).getBase();
    	List<Value> args = ioInvokeStmt.getInvokeExpr().getArgs();
//    	G.v().out.println("base type = " + base.getType());
//    	String argStr = base.getType().toString();
    	String argStr = "";//the base variable type can be a subclass, but we need the super class type.
    	for(Value arg : args){
    		argStr += ","+arg.getType().toString();
    	}
    	args.add(0, base);//base is the first argument
    	for(SootMethod hangMethod : hangMethods){
//    		for(Object argType : hangMethod.getParameterTypes()){
//    			G.v().out.println(hangMethod.getSubSignature() + "arg: " + argType);
//    		}    		
    		String ioInvokeClassName = hangMethod.getParameterType(0).toString();
    		boolean isTargetClass2Instrument = ioMethodLib.HangOps.classMethodMap.containsKey(ioInvokeClassName);
    		//the base variable type can be a subclass, but we need the super class type.
    		//thus, we leverage the HangOpo dic to store the super-sub class relation info
    		if(!isTargetClass2Instrument) continue;
    		
    		G.v().out.println("argStr = " + argStr);
    		G.v().out.println("hangMethod = " + hangMethod.getSubSignature());
    		boolean isTargetMethod = hangMethod.getSubSignature().contains(argStr+")");
    		if(!isTargetMethod) continue;
    		InvokeExpr decompressExpr =Jimple.v().newStaticInvokeExpr(hangMethod.makeRef(), args);
    		AssignStmt decompressStmt = Jimple.v().newAssignStmt(ioRetValue, decompressExpr);
    		Chain units = body.getUnits();
    		Stmt preStmt = (Stmt) units.getPredOf(ioInvokeStmt);
    		
    		UnitGraph graph = new ExceptionalUnitGraph(body);
    		for(Unit unit : graph.getPredsOf(ioInvokeStmt)){
    			G.v().out.println("pre : " + unit);
    			if(unit instanceof JIfStmt){
    				Value condition = ((JIfStmt)unit).getCondition();
        			IfStmt ifStmt = Jimple.v().newIfStmt(condition, decompressStmt);
        			units.insertBefore(ifStmt, unit);//check the new ifStmt first
        			units.remove(unit);
    			}
    		}
    		for(Unit unit : graph.getSuccsOf(ioInvokeStmt)){
    			G.v().out.println("pos : " + unit);
    		}
    		Stmt posStmt = (Stmt) units.getSuccOf(ioInvokeStmt);
    		units.insertAfter(decompressStmt, preStmt);
    		GotoStmt gotoStmt = Jimple.v().newGotoStmt(posStmt);
    		units.insertAfter(gotoStmt, decompressStmt);
    		units.remove(ioInvokeStmt);
    	}	
    }
    
    G.v().out.println("===============After patching=========================================");
    G.v().out.println(body.getMethod().getSubSignature()+"==");
    for(Stmt stmt : MethodUtils.getMethodAllStmts(body)){
    	G.v().out.println(stmt);
    }
    G.v().out.println("=======================================================================");
    
  }
}

package cassandra9881;
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



public class InvokeStaticInstrumenter extends BodyTransformer{

	
	  /* some internal fields */
	  static SootClass counterClass;
	  static SootMethod increaseCounter, reportCounter;

	  static {
	    counterClass    = Scene.v().loadClassAndSupport("cassandra9881.MyCounter");
	    increaseCounter = counterClass.getMethod("void increase(int)");
	    reportCounter   = counterClass.getMethod("int report()");
	  }
	
	
  /* internalTransform goes through a method body and inserts
   * counter instructions before an INVOKESTATIC instruction
   */
  protected void internalTransform(Body body, String phase, Map options) { 
//    LoopPathFinder loopfinder = new LoopPathFinder();
//    loopfinder.transform(body);
//    Set<PathExit> exitPaths = loopfinder.getExitPath();
//    List<LoopPath> nonExitPaths = loopfinder.getNonExitPath();
//    G.v().out.println("there are " + nonExitPaths.size() + " loop paths");
//    List<LoopPath> allPaths = loopfinder.getAllPath();
//    Map<Value, SootMethod> valMethodMap = new HashMap<Value, SootMethod>();
//    SootMethod ioMethod = null;
    SootClass ioClass = null;
    SootClass ioClassSuper = null;//if ioClass does not have a super I/O class, then ioClass == ioClassSuper
    SootField ioField = null;
    IndexBoundAPIs invokedAPIs = null;
//    G.v().out.println("nonExitPaths.size = " + nonExitPaths.size()+"!!!!!!");
    
    //suppose we have at most one field is io variable
    for(SootField field : body.getMethod().getDeclaringClass().getFields()){ 	
    	String fieldClassName = field.getType().toString();
    	if(!fieldClassName.startsWith("cassandra")
    			|| fieldClassName.startsWith("java.io")
    			|| fieldClassName.startsWith("java.nio")) 
    		continue; //we only care about the application classes, or java io classes 
    			
		soot.options.Options.v().set_whole_program(true);
		PhaseOptions.v().setPhaseOption("tag.ln", "on");
		PhaseOptions.v().setPhaseOption("cg.spark","ignore-types:true");
	
		Scene.v().forceResolve(fieldClassName, SootClass.SIGNATURES);
		SootClass fieldClass = Scene.v().getSootClass(fieldClassName);
    	
    	SootClass fieldSuperClass = fieldClass.getSuperclass();
    	
    	G.v().out.println("field: " + field.getSignature() 
    			+ ", class = " + fieldClass.toString() 
    			+ ", superClass = " + fieldSuperClass.toString());
    	
    	G.v().out.println(IOFuncRetRange.invokedAPIs.keySet());
    	
    	if(IOFuncRetRange.invokedAPIs.containsKey(fieldClass.toString())){
        	ioClass = fieldClass;
        	ioClassSuper = fieldClass;
        	ioField = field;
        	invokedAPIs = IOFuncRetRange.invokedAPIs.get(ioClass.toString());
        	break;
        } else if(IOFuncRetRange.invokedAPIs.containsKey(fieldSuperClass.toString())){
        	ioClass = fieldClass;
        	ioClassSuper = fieldSuperClass;
        	ioField = field;
        	invokedAPIs = IOFuncRetRange.invokedAPIs.get(ioClassSuper.toString());
        	break;
        }
	}

    
    if(ioClass == null || ioClassSuper == null) {
//    	G.v().out.println("ioClass is empty...");
    	return;
    }
    
//    if(nonExitPaths.size() <= 0) return;
    
    List<Stmt> onlyLoopStmts = new ArrayList<Stmt>();
    for(Stmt stmt : MethodUtils.getMethodAllStmts(body)){
		for (Tag tag : stmt.getTags()) {
			if (tag instanceof LoopTag) {
				onlyLoopStmts.add(stmt);
				break;
			}
		}
    }
    
    G.v().out.println("onlyLoopStmt.size = " + onlyLoopStmts.size());

    
    if(onlyLoopStmts.size() <= 1) return;
    
    //the following make sure that we do not need the LoopPathFinder, which is expensive for loops-with-exception-handling.
    UnitGraph graph = new ExceptionalUnitGraph(body);
    boolean containsLoopPath = false;
    for(Unit predsUnit : graph.getPredsOf(onlyLoopStmts.get(0))){
    	if(predsUnit instanceof Stmt){
    		if(onlyLoopStmts.contains((Stmt)predsUnit)){
    			containsLoopPath = true;
    			break;
    		}
    	}
    }
    
    if(!containsLoopPath) return;
    
    
    boolean hasBoundCheck = false,
    		hasBoundUpdate = false,
    		hasIndexForward = false,
    		hasIndexBackward = false,
    		hasIndexReset = false;
    /////////////////////////////////////////////
    //the following conditions can cause infinite loop
    //1. hasBoundCheck == true, hasIndexForward is skipped, but the others do not exist
    ////////////////////////////////////////////
    
    List<Stmt> IndexForwardStmts = new ArrayList<Stmt>();
    
    List<Value> fieldAssignedVars = IdentifyField.getAssignFieldVars(onlyLoopStmts, ioField);
    
    G.v().out.println("fieldAssignedVars = " + fieldAssignedVars);
    for(Stmt stmt : onlyLoopStmts){
    	if(stmt.containsInvokeExpr()){
			InvokeExpr expr = (InvokeExpr)stmt.getInvokeExpr();
			//either the io variable invokes some apis
			if(expr instanceof InstanceInvokeExpr){
            	Value inst = ((InstanceInvokeExpr)expr).getBase();
//            	G.v().out.println(expr + ", base = " + inst);
            	if(fieldAssignedVars.contains(inst)){
            		String methodName = expr.getMethod().getSubSignature();
            		int retCode = IOFuncRetRange.checkAPIType(methodName, invokedAPIs);
            		if(retCode == IOFuncRetRange.BOUND_CHECKING)
            			hasBoundCheck = true;
            		else if(retCode == IOFuncRetRange.BOUND_UPDATE)
            			hasBoundUpdate = true;
            		else if(retCode == IOFuncRetRange.INDEX_FORWARD){
            			hasIndexForward = true;
            			IndexForwardStmts.add(stmt);
            		}
            		else if(retCode == IOFuncRetRange.INDEX_BACKWARD)
            			hasIndexBackward = true;
            		else if(retCode == IOFuncRetRange.INDEX_RESET)
            			hasIndexReset = true;
            	}
			}
//			//or the io variable is an argument of another functions which indirectly invokes apis
//			for(Value arg : expr.getArgs()){
//				if(fieldAssignedVars.contains(arg)){
//					//here, we need to conduct inter-procedural analysis, but for now, we just add it into the list for simplicity
//					IndexForwardStmts.add(stmt);
//				}
//			}
		}
    }
    
    int IndexForwardNum = IndexForwardStmts.size();
    
    G.v().out.println("The indexforwarding stmts are : " + IndexForwardStmts);
    
    if(!(hasBoundCheck && hasIndexForward)) return; //currently, we only consider this case....but need extension later
    
    
    Chain units = body.getUnits();
    LocalGenerator lg = new LocalGenerator(body);
    RefType counterType = RefType.v("int");
    Local counterLocal = lg.generateLocal(counterType);
    Value IndexForwardNumVal = IntConstant.v(0);
    
    //the operations which can throw exceptions must be in the traps
    //for each trap, we currently only consider the first io invoke which can throws exception
    for (Trap trap : body.getTraps()) {
    	if(trap.getBeginUnit() instanceof Stmt && onlyLoopStmts.contains((Stmt)trap.getBeginUnit()) 
    			&& trap.getEndUnit() instanceof Stmt && onlyLoopStmts.contains((Stmt)trap.getEndUnit())
    			&& trap.getHandlerUnit() instanceof Stmt && onlyLoopStmts.contains((Stmt)trap.getHandlerUnit())){
    		G.v().out.println("===trap====");
    		G.v().out.println("try-begin: " + trap.getBeginUnit());
    		G.v().out.println("try-end:   " + trap.getEndUnit());
    		G.v().out.println("catch:     " + trap.getHandlerUnit());
    		G.v().out.println("===========");
    		Stmt stmt = (Stmt)trap.getBeginUnit();
    		while(!stmt.equals((Stmt)trap.getEndUnit())){
    			if(IndexForwardStmts.contains(stmt)){
    				IndexForwardStmts.remove(stmt);
    				// 1. first, make a new invoke expression
    			    InvokeExpr incExpr= Jimple.v().newStaticInvokeExpr(increaseCounter.makeRef(),IntConstant.v(1));
    			    // 2. then, make a invoke statement
    			    Stmt incStmt = Jimple.v().newInvokeStmt(incExpr);
    			    // 3. insert new statement into the chain(we are mutating the unit chain).
    			    units.insertAfter(incStmt, stmt);
    			}
    			int nextIndex = onlyLoopStmts.indexOf(stmt) + 1;
    			stmt = onlyLoopStmts.get(nextIndex);
    		}
    		
    		//after traversing each trap, we check whether the forward APIs are all skipped.
    		//If yes, then we just generate the patch there
    		if(IndexForwardStmts.isEmpty()){
    			
    			// 1. make invoke expression of MyCounter.report()
    	        InvokeExpr reportExpr = Jimple.v().newStaticInvokeExpr(reportCounter.makeRef());
    	        AssignStmt getCountStmt = Jimple.v().newAssignStmt(counterLocal, reportExpr); 
    	        // 2. then, make a invoke statement
    	        Stmt afterPatchStmt = (Stmt) units.getSuccOf(trap.getHandlerUnit());
    	        G.v().out.println("===" + trap.getHandlerUnit());
    	        IfStmt ifstmt = Jimple.v().newIfStmt(Jimple.v().newNeExpr(IndexForwardNumVal, counterLocal),afterPatchStmt);
    	        G.v().out.println(getCountStmt);
    	        G.v().out.println(ifstmt);
    	        G.v().out.println("===" + afterPatchStmt);
    	          
    			//when exception happens, we should throw a new exception there.
    			G.v().out.println("The IndexForwardStmts are empty now.....We should insert a new exception here");
    			RefType runtimeExceptionType = RefType.v("java.io.IOException");
				SootMethodRef cref = Scene.v().makeConstructorRef(runtimeExceptionType.getSootClass(),
						Collections.<Type>singletonList(RefType.v("java.lang.String")));				
			    Local exceptionLocal = lg.generateLocal(runtimeExceptionType);
			    AssignStmt assignStmt = Jimple.v().newAssignStmt(exceptionLocal, Jimple.v().newNewExpr(runtimeExceptionType));
			    
			    G.v().out.println("exceptionLocal = " + exceptionLocal + ", type = " + exceptionLocal.getType());
			    String errorMsg = "";
			    if(IOFuncRetRange.ioFuncErrMsgLib.containsKey(ioClass.toString())){
			    	errorMsg = IOFuncRetRange.ioFuncErrMsgLib.get(ioClass.toString()).get("FORWARD");
			    } else if(IOFuncRetRange.ioFuncErrMsgLib.containsKey(ioClassSuper.toString())){
			    	errorMsg = IOFuncRetRange.ioFuncErrMsgLib.get(ioClassSuper.toString()).get("FORWARD");
			    }
			    
			    SpecialInvokeExpr constructorInvokeExpr = Jimple.v().newSpecialInvokeExpr(exceptionLocal, cref,
						StringConstant.v(errorMsg));
				InvokeStmt invokeStmt = Jimple.v().newInvokeStmt(constructorInvokeExpr);
				ThrowStmt throwStmt = Jimple.v().newThrowStmt(exceptionLocal);

				G.v().out.println("before instrumentation...");
    	        units.add(getCountStmt);
    	        units.add(ifstmt);
//				units.insertBefore(getCountStmt, afterPatchStmt);
//				units.insertAfter(ifstmt, getCountStmt);
				units.add(assignStmt);
				units.add(invokeStmt);
				units.add(throwStmt);
				
				GotoStmt gotoStmt = Jimple.v().newGotoStmt(getCountStmt);
				units.insertAfter(gotoStmt,trap.getHandlerUnit());
				G.v().out.println("after instrumentation...");
				break;
				
    		}
    	}
    }
    
    
    
    
//    for(LoopPath path : nonExitPaths){
//    	
//    	List<Value> fieldAssignedVars = IdentifyField.getAssignFieldVars(path, ioField);
//    	
//    	List<Stmt> pathStmts = path.getpathStmt();
//    	
//    	List<String> invokedMethods = new ArrayList<>();
//    	
//        for(Stmt stmt : pathStmts){
//        	if (!stmt.containsInvokeExpr()) {
//                continue;
//            }
//
//            // take out the invoke expression
//            InvokeExpr expr = (InvokeExpr)stmt.getInvokeExpr();
//            
//            if(expr instanceof InstanceInvokeExpr){
//            	Value inst = ((InstanceInvokeExpr)expr).getBase();
//            	if(fieldAssignedVars.contains(inst)){
//            		expr.getMethod().toString();
//            	}
//            }
//        }
//	 }
  }
}

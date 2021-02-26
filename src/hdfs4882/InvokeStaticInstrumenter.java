package hdfs4882;
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
import soot.jimple.internal.JInterfaceInvokeExpr;
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
import java.util.Map.Entry;



public class InvokeStaticInstrumenter extends BodyTransformer{

		
	
  /* internalTransform goes through a method body and inserts
   * counter instructions before an INVOKESTATIC instruction
   */
  protected void internalTransform(Body body, String phase, Map options) { 
    LoopPathFinder loopfinder = new LoopPathFinder();
    loopfinder.transform(body);
//    Set<PathExit> exitPaths = loopfinder.getExitPath();
    HashMap<Integer, List<LoopPath>> nonExitPaths = loopfinder.getNonExitPath();
//    List<LoopPath> nonExitPaths = loopfinder.getNonExitPath();
    G.v().out.println("there are " + nonExitPaths.size() + " loop paths");
//    List<LoopPath> allPaths = loopfinder.getAllPath();
    Map<Value, SootMethod> valMethodMap = new HashMap<Value, SootMethod>();
    SootMethod ioMethod = null;
    SootClass ioClass = null;
    SootClass ioClassSuper = null;//if ioClass does not have a super I/O class, then ioClass == ioClassSuper
    SootField targetField = null;
    IndexBoundAPIs invokedAPIs = null;
//    G.v().out.println("nonExitPaths.size = " + nonExitPaths.size()+"!!!!!!");
    
    
    Set<Value> targetVars = new HashSet<Value>();
    Set<Stmt> targetVarStmts = new HashSet<Stmt>();
    for(Stmt stmt : MethodUtils.getMethodAllStmts(body)){
    	if(stmt.containsFieldRef() && stmt instanceof JAssignStmt){
    		SootField field = stmt.getFieldRef().getField();
    		String fieldClassName = field.getType().toString();
        	if(!fieldClassName.equals("java.util.SortedSet")) 
        		continue; //we only care about the application classes, or java io classes 
        	if(targetField == null)
        		targetField = field;
        	else {
        		if(!targetField.equals(field))
        			return; //currently, we only consider there is one java.util.SortedSet variable
        	}
        	G.v().out.println("fieldName = " + field);
        	Value targetVar = ((JAssignStmt)stmt).getLeftOp();
        	targetVars.add(targetVar);
        	targetVarStmts.add(stmt);
    	}
    }
    
//    if(targetField == null) return;
    if(targetVars.size() <= 0) return;
    
    boolean foundInvokeBound = false;
    boolean foundInvokeFakeStride = false;
    Stmt replaceStartStmt = null;
    Stmt replaceEndStmt = null;
    for(Entry<Integer, List<LoopPath>> nonEPathEntry : nonExitPaths.entrySet()){
    	int loopId = nonEPathEntry.getKey();
    	G.v().out.println("At the " + loopId + " loop.........");
    	List<LoopPath> nonEPaths = nonEPathEntry.getValue();
    	G.v().out.println("nonEPaths.size = " + nonEPaths.size()+"!!!!!!");
        for(LoopPath path : nonEPaths){
    //We check whether the pattern is invoked <size, first>
//    for(LoopPath path : nonExitPaths){
    	for(Stmt stmt : path.getpathStmt()){
    		if (!stmt.containsInvokeExpr()) {
                continue;
            }
    		InvokeExpr expr = (InvokeExpr)stmt.getInvokeExpr();
            if(expr instanceof InstanceInvokeExpr){
            	Value inst = ((InstanceInvokeExpr)expr).getBase();
            	if(targetVars.contains(inst)){
            		String methodName = expr.getMethod().getSubSignature();
            		if(methodName.equals("int size()")){
            			foundInvokeBound = true;
            			replaceStartStmt = stmt;
            		} else if(methodName.contains(" first()")){
            			foundInvokeFakeStride = true;
            			replaceEndStmt = stmt;
            		}
            	}
            }
    	}
    	
    	if(!(foundInvokeBound & foundInvokeFakeStride)) continue;
    	
    	Chain units = body.getUnits();    	
        //we found the pattern <size, first>
        //now, we need to replace this pattern with another pattern <it.hasNext, it.next>
    	//    java.util.Iterator it = sortedLeases.iterator();
        //    while(it.hasNext()){
        //    	final Lease oldest = sortedLeases.first();
    	
    	
    	LocalGenerator lg = new LocalGenerator(body);
    	
//    	RefType sortedSetType = RefType.v("java.util.SortedSet");
//    	Local sortedSetLocal = lg.generateLocal(sortedSetType);
//    	AssignStmt getIteratorStmt = Jimple.v().newAssignStmt(sortedSetLocal, targetField);
    	
    	
        RefType iteratorType = RefType.v("java.util.Iterator");
        Local iteratorLocal = lg.generateLocal(iteratorType);
       
        RefType booleanType = RefType.v("boolean");
        Local hasNextLocal = lg.generateLocal(booleanType);
        
        Value hasNextVal = IntConstant.v(0);
        
        RefType objectType = RefType.v("java.lang.Object");
        Local nextLocal = lg.generateLocal(objectType);
        
        Type castType = null;
        Local castLocal = null;
        Stmt castStmt = (Stmt) units.getSuccOf(replaceEndStmt);
        G.v().out.println("castStmt = " + castStmt);
        if(castStmt instanceof JAssignStmt){
        	Value castExpr = ((JAssignStmt)castStmt).getRightOp();
        	G.v().out.println("castExpr = " + castExpr);
        	if(castExpr instanceof CastExpr){
        		castType = ((CastExpr)castExpr).getCastType();
        		castLocal = (Local) ((JAssignStmt)castStmt).getLeftOp();
        	}
        }
        
        
        Stmt ifStmt = (Stmt)units.getSuccOf(replaceStartStmt);
        Stmt elseBranchStmt = null;
        if(ifStmt instanceof JIfStmt){
        	elseBranchStmt = (Stmt) units.getSuccOf(ifStmt);
        }
        
        G.v().out.println("castType = " + castType);
        G.v().out.println("castLocal = " + castLocal);
        
        SootClass sortedSetClass = Scene.v().loadClassAndSupport("java.util.SortedSet");
        SootClass iteratorClass = Scene.v().loadClassAndSupport("java.util.Iterator");
        
        SootClass setClass = Scene.v().loadClassAndSupport("java.util.Set");
        
        SootMethod iteratorMethod = setClass.getMethod("java.util.Iterator iterator()");
        SootMethod hasNextMethod = iteratorClass.getMethod("boolean hasNext()");
        SootMethod nextMethod = iteratorClass.getMethod("java.lang.Object next()");
        
        Stmt castSuccStmt = (Stmt) units.getSuccOf(castStmt);
    	
        Stmt getFieldStmt = (Stmt) units.getPredOf(replaceStartStmt);
        
        
        
//        InvokeExpr newBoundExpr = null;
        InvokeExpr boundExpr = replaceStartStmt.getInvokeExpr();
        if(boundExpr instanceof JInterfaceInvokeExpr){
        	Value boundBase = ((JInterfaceInvokeExpr)boundExpr).getBase();
        	InvokeExpr getInteratorExpr = Jimple.v().newInterfaceInvokeExpr((Local)boundBase, iteratorMethod.makeRef());
        	AssignStmt getIteratorStmt = Jimple.v().newAssignStmt(iteratorLocal, getInteratorExpr); 
        	
        	InvokeExpr getHasNextExpr = Jimple.v().newInterfaceInvokeExpr(iteratorLocal, hasNextMethod.makeRef());
        	AssignStmt getHasNextStmt = Jimple.v().newAssignStmt(hasNextLocal, getHasNextExpr);
        	
        	InvokeExpr getNextExpr = Jimple.v().newInterfaceInvokeExpr(iteratorLocal, nextMethod.makeRef());
        	AssignStmt getNextStmt = Jimple.v().newAssignStmt(nextLocal, getNextExpr);
        	
        	CastExpr castExpr = Jimple.v().newCastExpr(nextLocal, castType);
        	AssignStmt castStmtNew = Jimple.v().newAssignStmt(castLocal, castExpr);
        	
        	IfStmt ifstmt = Jimple.v().newIfStmt(Jimple.v().newEqExpr(hasNextLocal, hasNextVal), elseBranchStmt);
//        	GotoStmt gotoStmt3 = Jimple.v().newGotoStmt(replaceStartStmt);
        	
        	GotoStmt gotoStmt2 = Jimple.v().newGotoStmt(castSuccStmt);
        	
        	units.insertBefore(getIteratorStmt, replaceStartStmt);
        	units.insertAfter(getHasNextStmt, getIteratorStmt);
        	units.insertAfter(getNextStmt, getHasNextStmt);
        	units.insertBefore(ifstmt, getNextStmt);
        	units.insertAfter(castStmtNew, getNextStmt);
        	units.insertAfter(gotoStmt2, castStmtNew);
        	
//        	units.add(getNextStmt);
//        	units.add(castStmtNew);
//        	units.insertAfter(gotoStmt2, castStmtNew);
//        	units.add(getHasNextStmt);
//        	units.insertAfter(gotoStmt, getIteratorStmt);
//        	units.insertAfter(ifstmt, getHasNextStmt);
        	
        	//redirect the path to the 
        	for(Stmt stmt :  MethodUtils.getMethodAllStmts(body)){
				if(stmt instanceof JIfStmt && path.getpathStmt().contains(stmt)){
					if(((JIfStmt)stmt).getTarget().equals(getFieldStmt)){
//						G.v().out.println("the old ifstmt = " + stmt);
						IfStmt ifstmtRep = Jimple.v().newIfStmt(((JIfStmt)stmt).getCondition(),
								getHasNextStmt);
						units.insertBefore(ifstmtRep, stmt);
						units.remove(stmt);//replaces the old if stmt
					}
				}
				
				if(path.getpathStmt().contains(stmt)){
					if(units.getSuccOf(stmt).equals(getFieldStmt)){
						GotoStmt gotoStmt = Jimple.v().newGotoStmt(getHasNextStmt);
						units.insertAfter(gotoStmt, stmt);
					}
				}
			}
        
        	
        	
        	break;
        	
        }
    }
  }
  }
}

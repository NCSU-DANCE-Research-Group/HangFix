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
import soot.jimple.internal.JNewArrayExpr;
import soot.jimple.internal.JSpecialInvokeExpr;
import soot.options.Options;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ArrayPackedSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.LocalUnitPair;
import soot.util.*;
import utils.Condition;
import utils.ExtraCondition;
import utils.ExtractStmts;
import utils.ExtractVars;
import utils.LoopPath;
import utils.LoopPathFinder;
import utils.MethodUtils;
import utils.PathExit;
import utils.Pair;
import utils.UseConfiguration;
import ioMethodLib.IOFuncRetRange;

import java.util.*;
import java.util.Map.Entry;

import dataflowAnalysis.MyForwardDependency;
import utils.IntPair;

public class invokeAnotherClassAnalysis extends BodyTransformer{
	
	private String datavalStr;
	private SootMethod ioMethod;
	private Object dataField;
	
	
	invokeAnotherClassAnalysis(String datavalName, SootMethod iomethod){
		datavalStr = datavalName;
		ioMethod = iomethod;
	}

  /* internalTransform goes through a method body and inserts
   * counter instructions before an INVOKESTATIC instruction
   */
  @SuppressWarnings("unused")
protected void internalTransform(Body body, String phase, Map options) { 
//    LoopPathFinder loopfinder = new LoopPathFinder();
//    loopfinder.transform(body);
//    Set<PathExit> exitPaths = loopfinder.getExitPath();
//    List<LoopPath> nonExitPaths = loopfinder.getNonExitPath();
//    List<LoopPath> allPaths = loopfinder.getAllPath();
//    Map<Value, SootMethod> valMethodMap = new HashMap<Value, SootMethod>();
//    SootMethod ioMethod = null;
//    G.v().out.println("nonExitPaths.size = " + nonExitPaths.size()+"!!!!!!");
	  SootMethod method = body.getMethod();
	  G.v().out.println("inside method: " + method.getSignature());
	  
//	  if(method.isAbstract()) return;
//	  if(method.isConstructor()) G.v().out.println("is constructor");
//	  
//	  Chain units = body.getUnits();
//	  G.v().out.println("unit size = " + units.size());
	  
//	  Iterator stmtIt = units.snapshotIterator();
//	  while (stmtIt.hasNext()) {
//		  G.v().out.println(stmtIt.next());
//	  }	  
	  
	  List<Stmt> stmts = MethodUtils.getMethodAllStmts(body);
	  G.v().out.println("stmt size = " + stmts.size());
//	  Stmt beforePatchStmt = null;
	  boolean foundAssignDataVal = false;
	  // typical while loop for iterating over each statement
	  for(Stmt stmt : stmts){
//		  G.v().out.println(stmt);
		  if(stmt instanceof JAssignStmt){
				Value lhs = ((JAssignStmt)stmt).getLeftOp();
//				G.v().out.println("lhs = " + lhs + " in stmt: " + stmt);
				if(stmt.containsFieldRef() 
						&& lhs instanceof FieldRef 
						&& ((FieldRef)lhs).getField().getSignature().equals(datavalStr)){
					foundAssignDataVal = true;
//					beforePatchStmt = stmt;
					dataField = ((FieldRef)lhs).getField();
					break;
				}
			}
	  }
	  G.v().out.println("dataValStr = " + datavalStr);
//	  G.v().out.println("beforePatchStmt = " + beforePatchStmt);
	  
	  
	  if(!foundAssignDataVal) return;
	  //now should add stmt
	  G.v().out.println("found the assignment stmt of the dataval");
	  
	  //use backforward dataflow analysis to check whether the data variable is related to configurations.
	  UnitGraph graph = new ExceptionalUnitGraph(body);
//	  UseMyFlowAnalysis flowdep = new UseMyFlowAnalysis(graph);
//	  
//	  G.v().out.println("========================================");
//	  Iterator<Entry<Unit, List>> itBefore = flowdep.unitToLocalsBefore.entrySet().iterator();
//	  while(itBefore.hasNext()){
//		  Entry<Unit, List> entry = itBefore.next();
//		  G.v().out.println(entry.getKey());
//		  G.v().out.println("\tbefore: " + entry.getValue() + " after: " + flowdep.unitToLocalsAfter.get(entry.getKey()));
//	  }
//	  G.v().out.println("======================================= ");
//	  
	  
//	  MyReachingDefinition(graph);
//	  Iterator<Entry<LocalUnitPair, List>> itLocalUnitPairToDefs = localUnitPairToDefs.entrySet().iterator();
//	  while(itLocalUnitPairToDefs.hasNext()){
//		  Entry<LocalUnitPair, List> entry = itLocalUnitPairToDefs.next();
//		  G.v().out.println(entry.getKey().getLocal() +"\t" +  entry.getKey().getUnit()
//				  + "===> " + entry.getValue());
//	  }
	  
	  MyForwardDependency depAnaly = new MyForwardDependency(graph, false);
//	  G.v().out.println("-----------Object data-dependency-----------");
//	  for (Map.Entry<Object, Set<Object>> entry : depAnaly.map_merged.entrySet()) {
//			G.v().out.println(entry.getKey() + " " + entry.getValue().toString());
//	  }
	  
	  if(!depAnaly.map_merged.containsKey(dataField)) return;
	  
	  Set<Object> dependencees = depAnaly.map_merged.get(dataField);
	  
	  boolean isReadFromConf = false;
	  for(Object dependentee : dependencees){
		  if(dependentee instanceof ParameterRef){
			  Type depenType = ((ParameterRef)dependentee).getType();
			  if(UseConfiguration.invokeConfiguration(depenType.toString())){
				  isReadFromConf = true;
			  }
		  }
	  }
	  
	  if(!isReadFromConf) return;
	  
	  G.v().out.println("reading configuration to configure the " + datavalStr);
	  //now, need to find the variable of the bufferSize
//	  IOFuncRetRange.ioFuncRetLib.get(ioMethod.getDeclaringClass().toString()).get(ioMethod.getSubSignature().toString());
	  
	  List<Object> equalObjs = new ArrayList<Object>();
	  for(int i = stmts.size()-1; i >= 0; i--){
		  Stmt stmt = stmts.get(i);
		  Object equalObj = null;
		  if(stmt instanceof JAssignStmt){
			  Value lhs = ((JAssignStmt)stmt).getLeftOp();
			  if(equalObj == null){
				  if(lhs instanceof FieldRef && ((FieldRef)lhs).getField().equals(dataField)){
					  Object rhs = ((JAssignStmt)stmt).getRightOp();
					  if(rhs instanceof FieldRef){
						  equalObj = ((FieldRef)rhs).getField();
					  } else { //currently, we didn't think about other type of ref, maybe we should expend the branches in future
						  equalObj = rhs;
					  }
					  equalObjs.add(equalObj);
			  	  }
			  } else {
				  if(lhs instanceof FieldRef){
					  if(((FieldRef)lhs).getField().equals(equalObj)){
						  Object rhs = ((JAssignStmt)stmt).getRightOp();
						  if(rhs instanceof FieldRef){
							  equalObj = ((FieldRef)rhs).getField();
						  } else { //currently, we didn't think about other type of ref, maybe we should expend the branches in future
							  equalObj = rhs;
						  }
						  equalObjs.add(equalObj);
					  }
				  } else {
					  if(lhs.equals(equalObj)){
						  Object rhs = ((JAssignStmt)stmt).getRightOp();
						  if(rhs instanceof FieldRef){
							  equalObj = ((FieldRef)rhs).getField();
						  } else { //currently, we didn't think about other type of ref, maybe we should expend the branches in future
							  equalObj = rhs;
						  }
						  equalObjs.add(equalObj);
					  }
				  }
			  }
		  }
	  }
	  
	  G.v().out.println(datavalStr + " equals to " + equalObjs);
	  
	  Stmt afterPatchStmt = null;
	  Value sizeVar = null;
	  for(Stmt stmt : MethodUtils.getMethodAllStmts(body)){
		  if(stmt instanceof JAssignStmt){
			  if(equalObjs.contains(((JAssignStmt)stmt).getLeftOp())){
				  for (ValueBox vb : stmt.getUseBoxes()){
					  if(vb.getValue() instanceof JNewArrayExpr){
						  JNewArrayExpr expr = ((JNewArrayExpr)vb.getValue());
						  sizeVar = expr.getSize();
						  afterPatchStmt = stmt;
						  //G.v().out.println("sizeVar = " + sizeVar);
					  }
				  }
			  }
		  }
	  }
	  
	  if(afterPatchStmt == null || sizeVar == null) return;
	  
	  //add if condition and throw exception stmt
	  List<ConditionExpr> dicCondList = IOFuncRetRange.ioFuncRetLib.get(ioMethod.getDeclaringClass().toString()).get(ioMethod.getSubSignature().toString());
	  List<ConditionExpr> patchCondList = dicCondList;
		if(patchCondList.size() > 0){
			G.v().out.println("patchCondList.size = " + patchCondList.size());
//			G.v().out.println("ioVariable: " + retIOVal.toString());
			for(ConditionExpr cond : patchCondList){
				G.v().out.println("patchCond = " + cond.toString());
				G.v().out.println("afterPatchStmt = " + afterPatchStmt);
				if(cond instanceof JEqExpr){
					//insert exception stmt
					RefType runtimeExceptionType = RefType.v("java.io.IOException");
					SootMethodRef cref = Scene.v().makeConstructorRef(runtimeExceptionType.getSootClass(),
							Collections.<Type>singletonList(RefType.v("java.lang.String")));
//					G.v().out.println("cref = " + cref);
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
					G.v().out.println("afterPatchStmt = " + afterPatchStmt);
					//insert if stmt
					IfStmt ifstmt = Jimple.v().newIfStmt(Jimple.v().newNeExpr(sizeVar, cond.getOp2()),
							afterPatchStmt);
					
					
					//sometimes, the afterPathStmt is not necessarily the first stmt in a label
//					Stmt testPatchStmt = ExtractStmts.extractFirstStmtInALabel(stmts, afterPatchStmt);
//					G.v().out.println("testPatchStmt = " + testPatchStmt);
					
					
					Chain units = body.getUnits();
					G.v().out.println("ifstmt = " + ifstmt);
					units.add(ifstmt); //using add function can create a new label for the new stmts.
					units.add(assignStmt);
					units.add(invokeStmt);
					units.add(throwStmt);
//					units.insertBefore(gotoStmt, afterPatchStmt);
//					units.insertBefore(ifstmt, gotoStmt);
////					units.insertAfter(ifstmt2, ifstmt);
//					units.insertAfter(assignStmt, ifstmt);
//					units.insertAfter(invokeStmt, assignStmt);
//					units.insertAfter(throwStmt, invokeStmt);
					
					Stmt beforePatchStmt = (Stmt) units.getPredOf(afterPatchStmt);
					GotoStmt gotoStmt = Jimple.v().newGotoStmt(ifstmt);
					units.insertAfter(gotoStmt, beforePatchStmt);
					

//					units.insertBefore(throwStmt, afterPatchStmt);
//					units.insertBefore(invokeStmt, throwStmt);
//					units.insertBefore(assignStmt, invokeStmt);
//					units.insertBefore(ifstmt, assignStmt);
					G.v().out.println("=================insert the patch stmt in new label===============");
					G.v().out.println(ifstmt);
					G.v().out.println(assignStmt);
					G.v().out.println(invokeStmt);
					G.v().out.println(throwStmt);
					G.v().out.println("==================================================================");
//					units.insertBefore(initStmt, afterPatchStmt);//this can cause new stmt and old stmt use the same label, resulting an infinite loop
//					units.insertBefore(ifstmt, initStmt);
					
					//checking whether there are other stmts jump to the afterPatchStmt, then redirect them
//					for(Stmt stmt :  stmts){
//						if(stmt instanceof JIfStmt){
//							if(((JIfStmt)stmt).getTarget().equals(afterPatchStmt)){
////								G.v().out.println("the old ifstmt = " + stmt);
//								IfStmt ifstmtRep = Jimple.v().newIfStmt(((JIfStmt)stmt).getCondition(),
//										ifstmt);
//								units.insertBefore(ifstmtRep, stmt);
//								units.remove(stmt);//replaces the old if stmt
//							}
//						}
//					}
				}
			}		
		}  
  }
}

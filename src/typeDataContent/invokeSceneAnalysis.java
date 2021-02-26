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
import soot.javaToJimple.LocalGenerator;
import soot.jimple.*;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JEqExpr;
import soot.jimple.internal.JIdentityStmt;
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

public class invokeSceneAnalysis extends SceneTransformer{
	
	private String datavalStr;
	private SootMethod ioMethod;
	private Object dataField;
	private String hangMethod;
	private int varType; //0: field, 1: parameter
	private String targetClassName;
	
	invokeSceneAnalysis(String datavalName, SootMethod iomethod, String hangmethod, int type, String analysisClass){
		datavalStr = datavalName;
		ioMethod = iomethod;
		hangMethod = hangmethod;
		varType = type;
		targetClassName = analysisClass;
	}

  /* internalTransform goes through a method body and inserts
   * counter instructions before an INVOKESTATIC instruction
   */
  @SuppressWarnings("unused")
  protected void internalTransform(String phase, Map options) {
	  
	  for(SootClass sclass : Scene.v().getApplicationClasses()){
		  if(!sclass.toString().equals(targetClassName)) continue;
		  for(SootMethod method : sclass.getMethods()){
			  G.v().out.println("body type is " + method.getActiveBody().getClass());
			  instrument(method.getActiveBody());
		  }
	  }
	  
	  
	  
  }
  
  
  private void instrument(Body body){
	  SootMethod method = body.getMethod();
	  G.v().out.println("inside method: " + method.getSignature()); 
	  G.v().out.println("body size is " + body.getUnits().size());
//	  for(Unit unit : body.getUnits()){
//		  G.v().out.println(unit);
//	  }
//	  UnitGraph g = new ExceptionalUnitGraph(body);
//	  G.v().out.println("graph size = " + g.size());	  
	  List<Stmt> stmts = MethodUtils.getMethodAllStmts(body);
	  G.v().out.println("stmt size = " + stmts.size());
	  boolean foundAssignDataVal = false;
	  // typical while loop for iterating over each statement
	  for(Stmt stmt : stmts){
//		  G.v().out.println(stmt);
		  if(varType == 0){
			  if(stmt instanceof JAssignStmt){
				Value lhs = ((JAssignStmt)stmt).getLeftOp();
				if(stmt.containsFieldRef() 
						&& lhs instanceof FieldRef 
						&& ((FieldRef)lhs).getField().getSignature().equals(datavalStr)){
					foundAssignDataVal = true;
					dataField = ((FieldRef)lhs).getField();
					break;
				}
			  } else if(stmt instanceof JIdentityStmt){
				  Value lhs = ((JIdentityStmt)stmt).getLeftOp();
				  if(stmt.containsFieldRef() 
							&& lhs instanceof FieldRef 
							&& ((FieldRef)lhs).getField().getSignature().equals(datavalStr)){
						foundAssignDataVal = true;
						dataField = ((FieldRef)lhs).getField();
						break;
				  }
			  }
		  }
		  if(varType == 1 && stmt instanceof JAssignStmt){//first find the data variable, second find whether the data variable depend on any parameter
			  Value lhs = ((JAssignStmt)stmt).getLeftOp();
			  if(lhs.toString().equals(datavalStr)){
				  foundAssignDataVal = true;
				  dataField = lhs;
				  break;
			  }
		  }
	  }
//	  G.v().out.println("dataValStr = " + datavalStr);
//	  G.v().out.println("beforePatchStmt = " + beforePatchStmt);
	  
	  
	  if(!foundAssignDataVal) return;
	  //now should add stmt
	  G.v().out.println("found the assignment stmt of the dataval");
	  
	  //use backforward dataflow analysis to check whether the data variable is related to configurations.
	  UnitGraph graph = new ExceptionalUnitGraph(body);	  
	  MyForwardDependency depAnaly = new MyForwardDependency(graph, false);
//	  G.v().out.println("-----------Object data-dependency-----------");
//	  for (Map.Entry<Object, Set<Object>> entry : depAnaly.map_merged.entrySet()) {
//			G.v().out.println(entry.getKey() + " " + entry.getValue().toString());
//	  }
	  
	  if(!depAnaly.map_merged.containsKey(dataField)) return;
	  
	  Set<Object> dependencees = depAnaly.map_merged.get(dataField);
	  
	  boolean isReadFromConf = false;
	  ParameterRef para = null;
	  for(Object dependentee : dependencees){
		  if(dependentee instanceof ParameterRef){
			  Type depenType = ((ParameterRef)dependentee).getType();
			  if(UseConfiguration.invokeConfiguration(depenType.toString())){
				  isReadFromConf = true;
			  }
			  para = (ParameterRef) dependentee;
		  }
	  }
	  
	  
//	  if(!isReadFromConf) return; //if not read from configuration files, it is possibly passed in from argument
	  
	  String extraMsg = "";
	  if(isReadFromConf) {
		  extraMsg = "Reading from corrupted configuration files."; 
		  G.v().out.println("reading configuration to configure the " + datavalStr);
	  }
	  //now, need to find the variable of the bufferSize
//	  IOFuncRetRange.ioFuncRetLib.get(ioMethod.getDeclaringClass().toString()).get(ioMethod.getSubSignature().toString());
	  
	  Stmt afterDataAssignStmt = null;
	  Stmt dataVarAssignStmt = null;
	  Value sizeVar = null;
	  if(varType == 0){
		//extract the values which are the same as dataField
		  List<Object> equalObjs = ExtractVars.extractSameObjs(stmts, dataField);
		  
		  G.v().out.println(datavalStr + " equals to " + equalObjs);
		  
		  
		  for(Stmt stmt : MethodUtils.getMethodAllStmts(body)){
			  if(stmt instanceof JAssignStmt){
				  Value left = ((JAssignStmt)stmt).getLeftOp();
				  G.v().out.println("left = " + left);
				  if(equalObjs.contains(left)){
					  sizeVar = left;
					  dataVarAssignStmt = stmt;
					  continue;
				  }
			  }
			  if(sizeVar != null && afterDataAssignStmt == null){
				  afterDataAssignStmt = stmt;
			  }
		  }
		  
		  if(afterDataAssignStmt == null || sizeVar == null) return;
	  } else if (varType == 1){
		  //for corruption from parameter, we focus on the parameter now (parameter:bufferSize), 
		  //not the argument in io operations (e.g., byte[])
		  G.v().out.println("para = " + para.toString());
		  for(Stmt stmt : stmts){
			  if(stmt instanceof JIdentityStmt){
				  Value rhs = ((JIdentityStmt)stmt).getRightOp();
//				  G.v().out.println(rhs.toString());
				  if(rhs instanceof ParameterRef && rhs.toString().equals(para.toString())){
					  sizeVar = ((JIdentityStmt)stmt).getLeftOp();
					  dataVarAssignStmt = stmt;
					  G.v().out.println("sizeVar = " + sizeVar);
					  continue;
				  }  
			  }
			  if(sizeVar != null && afterDataAssignStmt == null){
				  afterDataAssignStmt = stmt;
			  }
		  }
	  }
	  
	  
	  //add if condition and throw exception stmt
	  List<ConditionExpr> dicCondList = IOFuncRetRange.ioFuncRetLib.get(ioMethod.getDeclaringClass().toString()).get(ioMethod.getSubSignature().toString());
	  List<ConditionExpr> patchCondList = dicCondList;
		if(patchCondList.size() > 0){
			G.v().out.println("patchCondList.size = " + patchCondList.size());
//			G.v().out.println("ioVariable: " + retIOVal.toString());
			for(ConditionExpr cond : patchCondList){
				G.v().out.println("patchCond = " + cond.toString());
				G.v().out.println("afterDataAssignStmt = " + afterDataAssignStmt);
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
				    errorMsg = errorMsg.replace("METHOD", hangMethod);
				    errorMsg = extraMsg + " " + errorMsg;
				    SpecialInvokeExpr constructorInvokeExpr = Jimple.v().newSpecialInvokeExpr(exceptionLocal, cref,
							StringConstant.v(errorMsg));
					InvokeStmt invokeStmt = Jimple.v().newInvokeStmt(constructorInvokeExpr);
					ThrowStmt throwStmt = Jimple.v().newThrowStmt(exceptionLocal);
					
					//sometimes, the afterPathStmt is not necessarily the first stmt in a label
//					G.v().out.println("afterDataAssignStmt = " + afterDataAssignStmt);
					//insert if stmt
					IfStmt ifstmt = Jimple.v().newIfStmt(Jimple.v().newNeExpr(sizeVar, cond.getOp2()), afterDataAssignStmt);
										
					Chain units = body.getUnits();
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
					
//					Stmt beforePatchStmt = (Stmt) units.getPredOf(afterDataAssignStmt);
					GotoStmt gotoStmt = Jimple.v().newGotoStmt(ifstmt);
					units.insertAfter(gotoStmt, dataVarAssignStmt);
					

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
					
				}
			}		
		}  
  }

}

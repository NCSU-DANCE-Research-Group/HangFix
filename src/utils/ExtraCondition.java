package utils;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import soot.G;
import soot.Value;
import soot.ValueBox;
import soot.jimple.CastExpr;
import soot.jimple.Stmt;
import soot.jimple.internal.JAssignStmt;

public class ExtraCondition {
	
	public static List<Condition> extractEqualConditions(LoopPath path, PrintStream out){
		List<Condition> retConds = new ArrayList<Condition>();
		List<Condition> conditions = path.getconditions();
		List<Stmt> stmts = path.getpathStmt();
		//1. Replace those conditions which contains "cmp"
		List<Condition> conditionsWOcmp = new ArrayList<Condition>();
		for(Condition condition : conditions){
			boolean replaceCond = false;
			Value conVar = condition.cond.getOp1();
			for(Stmt stmt : stmts){
				if(stmt instanceof JAssignStmt){
					JAssignStmt jstmt = (JAssignStmt) stmt;
					if(jstmt.getLeftOp().equals(conVar)){
						boolean foundcmp = false;
						for(ValueBox vb : stmt.getUseBoxes()){
							if(vb.getValue().toString().contains("cmp")){
//								G.v().out.println("conVar = " + conVar + ", stmt = " + stmt);
								foundcmp = true;
								break;
							}
						}
//						for(ValueBox vb : stmt.getUseBoxes()){
//							G.v().out.println(vb.getValue());
//						}
						if(foundcmp == true  //if it contains l1 cmp l0, then the 1st usebox is l1 cmp l0, the 2nd is l1, the 3rd is l0
								&& stmt.getUseBoxes().size() == 3 ){
							
							Value replaceVar1 = null;
							Value replaceVar2 = null;
							
							for(ValueBox vb : stmt.getUseBoxes()){
								if(vb.getValue().toString().contains("cmp"))
									continue;
								if(replaceVar1 == null){
									replaceVar1 = vb.getValue();
								} else {
									replaceVar2 = vb.getValue();
								}
							}
							
							Condition newCond = new Condition(condition, replaceVar1, replaceVar2);
							conditionsWOcmp.add(newCond);
							replaceCond = true;
							break;
						}
					}
				}
			}
			if(!replaceCond)
				conditionsWOcmp.add(condition);
		}
		
//		for(Condition cond : conditionsWOcmp){
//			G.v().out.println("cond = " + cond.cond.toString());
//		}
		
		//2. Find those variables which are actually the same in the cast expr
		Map<Value, List<Value>> sameVarMap = new HashMap<Value, List<Value>>(); 
		for(Stmt stmt : stmts){
			if(stmt instanceof JAssignStmt){
				JAssignStmt jstmt = (JAssignStmt) stmt;
				for(ValueBox vb : stmt.getUseBoxes()){
					Value val = vb.getValue();
					if(val instanceof CastExpr){
						Value rhs = ((CastExpr) val).getOp();
						Value lhs = jstmt.getLeftOp();
						if(sameVarMap.containsKey(lhs)){
							List<Value> sameVars = sameVarMap.get(lhs);
							sameVars.add(rhs);
							sameVarMap.put(lhs, sameVars);
						} else {
							List<Value> sameVars = new ArrayList<Value>();
							sameVars.add(rhs);
							sameVarMap.put(lhs, sameVars);
						}
						break;
					}
				}
			}
		}
		
		//2. Find those variables which are actually the same, assign one variable to another
		for(Stmt stmt : stmts){
			if(stmt instanceof JAssignStmt){
				JAssignStmt jstmt = (JAssignStmt) stmt;
				if(jstmt.getUseBoxes()!= null && jstmt.getUseBoxes().size() == 1){
					for(ValueBox vb : stmt.getUseBoxes()){
						Value rhs = vb.getValue();
						//Value rhs = ((CastExpr) val).getOp();
						Value lhs = jstmt.getLeftOp();
						if(sameVarMap.containsKey(lhs)){
							List<Value> sameVars = sameVarMap.get(lhs);
							sameVars.add(rhs);
							sameVarMap.put(lhs, sameVars);
						} else {
							List<Value> sameVars = new ArrayList<Value>();
							sameVars.add(rhs);
							sameVarMap.put(lhs, sameVars);
						}
						break;
					}
				}
				
			}
		}
		
		//2. Find those variables which are actually the same with the same assignment
		for(int i = 0; i < stmts.size(); i++){
			Stmt stmt = stmts.get(i);
			for(int j = 1; j < stmts.size(); j++){
				Stmt stmt2 = stmts.get(j);
				if(stmt instanceof JAssignStmt && stmt2 instanceof JAssignStmt){
					String stmtAssign = ((JAssignStmt)stmt).rightBox.getValue().toString();
					String stmt2Assign = ((JAssignStmt)stmt2).rightBox.getValue().toString();
//					List<ValueBox> useBox1 = stmt.getUseBoxes();
//					List<ValueBox> useBox2 = stmt2.getUseBoxes();
//					if(useBox1.containsAll(useBox2) && useBox2.contains(useBox1)){
					if(stmtAssign.equals(stmt2Assign)){
						Value lhs = ((JAssignStmt)stmt).leftBox.getValue();
						Value rhs = ((JAssignStmt)stmt2).leftBox.getValue();
						if(sameVarMap.containsKey(lhs)){
							List<Value> sameVars = sameVarMap.get(lhs);
							sameVars.add(rhs);
							sameVarMap.put(lhs, sameVars);
						} else {
							List<Value> sameVars = new ArrayList<Value>();
							sameVars.add(rhs);
							sameVarMap.put(lhs, sameVars);
						}
						if(sameVarMap.containsKey(rhs)){
							List<Value> sameVars = sameVarMap.get(rhs);
							sameVars.add(lhs);
							sameVarMap.put(rhs, sameVars);
						} else {
							List<Value> sameVars = new ArrayList<Value>();
							sameVars.add(lhs);
							sameVarMap.put(rhs, sameVars);
						}
					}
				}
			}
		}
		
//		out.println("sameVarMap:");
//		Iterator<Entry<Value, List<Value>>> it = sameVarMap.entrySet().iterator();
//		Entry<Value, List<Value>> entry = null;
//		while(it.hasNext()){
//			entry = it.next();
//			out.print(entry.getKey().toString() + " : ");
//			for(Value same : entry.getValue()){
//				out.print(same.toString() + " ");
//			}
//			out.println();
//		}
		
		//3. add extra conditions
		for(Condition condition : conditionsWOcmp){
			retConds.add(condition);
			Value condVar = condition.cond.getOp1();
			if(sameVarMap.containsKey(condVar)){
				List<Value> sameVars = sameVarMap.get(condVar);
				for(Value sameVar : sameVars){
					if(!sameVar.equals(condVar)){
						Condition newCond = new Condition(condition, sameVar, 1);
						retConds.add(newCond);
					}
				}
			}
			Value condVar2 = condition.cond.getOp2();
			if(sameVarMap.containsKey(condVar2)){
				List<Value> sameVars = sameVarMap.get(condVar2);
				for(Value sameVar : sameVars){
					if(!sameVar.equals(condVar2)){
						Condition newCond = new Condition(condition, sameVar, 2);
						retConds.add(newCond);
					}
				}
			}
		}		
		return retConds;
	}
	
}

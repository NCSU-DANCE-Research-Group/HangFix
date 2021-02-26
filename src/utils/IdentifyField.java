package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import soot.G;
import soot.SootClass;
import soot.SootField;
import soot.Value;
import soot.grimp.NewInvokeExpr;
import soot.jimple.DynamicInvokeExpr;
import soot.jimple.FieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JIdentityStmt;

public class IdentifyField {
	
	public static SootField getField(LoopPath path, Value val){
		SootField field = null;
		for(Stmt stmt : path.getpathStmt()){
			if(stmt instanceof JAssignStmt){
				Value lhs = ((JAssignStmt)stmt).getLeftOp();
				if(stmt.containsFieldRef()){
					G.v().out.println("--" + stmt + " " + lhs);
					if(lhs.equals(val))
						return stmt.getFieldRef().getField();
				}
			}
		}
		return field;
	}
	
	
	public static SootField getField(List<Stmt> stmts, Value val){
		List<SootField> fields = new ArrayList<>();
		List<Value> fieldVars = new ArrayList<>();
		HashMap<Value, SootField> var2FieldMap = new HashMap<>();
		
		for(Stmt stmt : stmts){
			if(stmt instanceof JAssignStmt){
				Value left = ((JAssignStmt)stmt).getLeftOp();
				Value right = ((JAssignStmt)stmt).getRightOp();
				if(right instanceof FieldRef){
					SootField field = ((FieldRef)right).getField();
					var2FieldMap.put(left, field);
				}
			}
			else if(stmt instanceof JIdentityStmt){
				Value left = ((JIdentityStmt)stmt).getLeftOp();
				Value right = ((JIdentityStmt)stmt).getRightOp();
				if(right instanceof FieldRef){
					SootField field = ((FieldRef)right).getField();
					var2FieldMap.put(left, field);
				}
			}
		}
		return var2FieldMap.get(val);
//		for(Stmt stmt : stmts){
//			if(stmt.containsFieldRef()){
//				SootField field = stmt.getFieldRef().getField();
////				fields.add(field);
//				Value left = null;
//				if(stmt instanceof JAssignStmt){
//					left = ((JAssignStmt)stmt).getLeftOp();
////					fieldVars.add(left);
//				} else if(stmt instanceof JIdentityStmt){
//					left = ((JIdentityStmt)stmt).getLeftOp();
////					fieldVars.add(left);
//				}
//				if(left != null) var2FieldMap.put(left, field);
//			}
//		}
//		for(Stmt stmt : stmts){
//			if(stmt instanceof JAssignStmt){
//				Value right = ((JAssignStmt)stmt).getRightOp();
//				if(right instanceof FieldRef){
//					G.v().out.println("--stmt " + right + " is FieldRef");
//					SootField field = ((FieldRef)right).getField();
//				}
////				if(right instanceof DynamicInvokeExpr){
////					G.v().out.println("--stmt " + right + " is DynamicInvokeExpr");
////				} 
////				if(right instanceof InstanceInvokeExpr){
////					G.v().out.println("--stmt " + right + " is InstanceInvokeExpr");
////				} 
////				if(right instanceof InterfaceInvokeExpr){
////					G.v().out.println("--stmt " + right + " is InterfaceInvokeExpr");
////				} 
////				if(right instanceof NewInvokeExpr){
////					G.v().out.println("--stmt " + right + " is NewInvokeExpr");
////				} 
////				if(right instanceof SpecialInvokeExpr){
////					G.v().out.println("--stmt " + right + " is SpecialInvokeExpr");
////				} 
////				if(right instanceof StaticInvokeExpr){
////					G.v().out.println("--stmt " + right + " is StaticInvokeExpr");
////				} 
////				if(right instanceof VirtualInvokeExpr){
////					G.v().out.println("--stmt " + right + " is VirtualInvokeExpr");
////				}
//			}
//		}
		
	}
	
	public static List<Value> getAssignFieldVars(List<Stmt> stmts, SootField field){
		List<Value> fieldAssignedVars = new ArrayList<Value>();
		for(Stmt stmt : stmts){
			if(stmt instanceof JAssignStmt){
				Value lhs = ((JAssignStmt)stmt).getLeftOp();
				if(stmt.containsFieldRef()){
					if(stmt.getFieldRef().getField().equals(field)
							&& !fieldAssignedVars.contains(lhs)){
						fieldAssignedVars.add(lhs);
					}
				}
			}
		}
		return fieldAssignedVars;
	}
	
	
}

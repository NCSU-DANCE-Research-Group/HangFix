package utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import soot.Value;
import soot.ValueBox;
import soot.jimple.CastExpr;
import soot.jimple.FieldRef;
import soot.jimple.Stmt;
import soot.jimple.internal.JAssignStmt;

public class ExtractVars {
	public static Set<Value> extractSameVars(LoopPath path, Value var){
		Set<Value> sameVars = new HashSet<Value>();
		sameVars.add(var);
		for(Stmt stmt : path.getpathStmt()){
			if(stmt instanceof JAssignStmt){
				JAssignStmt jstmt = (JAssignStmt) stmt;
				for(ValueBox vb : stmt.getUseBoxes()){
					Value val = vb.getValue();
					if(val instanceof CastExpr){
						Value rhs = ((CastExpr) val).getOp();
						Value lhs = jstmt.getLeftOp();
						if(rhs.equals(var)){
							sameVars.add(lhs);
						}
					}
				}
			}
		}
		return sameVars;
	}
	
	
	@SuppressWarnings("unused")
	public static List<Object> extractSameObjs(List<Stmt> stmts, Object dataField){
		List<Object> equalObjs = new ArrayList<Object>();
//		  equalObjs.add(dataField);
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
		  return equalObjs;
	}
}

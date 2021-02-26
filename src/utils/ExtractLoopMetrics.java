package utils;

import java.util.ArrayList;
import java.util.List;

import soot.Value;
import soot.ValueBox;
import soot.jimple.Stmt;
import soot.jimple.internal.JAssignStmt;

public class ExtractLoopMetrics {
	
	public static Value extractLoopStride(LoopPath path){
		List<Value> condVals = new ArrayList<>();
		for(Condition cond : path.getconditions()){
			Value op1 = cond.cond.getOp1();
			Value op2 = cond.cond.getOp2();
			condVals.add(op1);
			condVals.add(op2);
		}
		
		Value stride = null;
		for(Stmt stmt : path.getpathStmt()){
			if(stmt instanceof JAssignStmt){
				boolean foundIndex = false;
				Value left = ((JAssignStmt)stmt).getLeftOp();
				for(ValueBox vb : stmt.getUseBoxes()){
					Value use = vb.getValue();
					if(use.equals(left)){
						foundIndex = true;
					} else {
						stride = use;
					}
					if(foundIndex && stride != null && condVals.contains(left))
						return stride;
				}
			}
		}
		return stride;
		
	}
}

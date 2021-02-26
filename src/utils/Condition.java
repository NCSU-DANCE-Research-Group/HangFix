package utils;

import soot.Value;
import soot.jimple.ConditionExpr;
import soot.jimple.EqExpr;
import soot.jimple.GeExpr;
import soot.jimple.GtExpr;
import soot.jimple.LeExpr;
import soot.jimple.LtExpr;
import soot.jimple.NeExpr;
import soot.jimple.internal.JEqExpr;
import soot.jimple.internal.JGeExpr;
import soot.jimple.internal.JGtExpr;
import soot.jimple.internal.JIfStmt;
import soot.jimple.internal.JLeExpr;
import soot.jimple.internal.JLtExpr;
import soot.jimple.internal.JNeExpr;

public class Condition {
	public JIfStmt stmt;
	public ConditionExpr cond;
	public boolean cond_v;

	Condition(JIfStmt stmt, boolean value) {
		this.stmt = stmt;
		cond = (ConditionExpr) stmt.getCondition();
		cond_v = value;
		transform();
	}
	
	Condition(Condition condition, Value replaceVar){
		this.stmt = condition.stmt;
		cond = condition.cond;
		cond_v = condition.cond_v;
		replace(replaceVar);
	}
	
	Condition(Condition condition, Value replaceVar, int pos){
		this.stmt = condition.stmt;
		cond = condition.cond;
		cond_v = condition.cond_v;
		replace(replaceVar, pos);
	}
	


	Condition(Condition condition, Value replaceVar1, Value replaceVar2){
		this.stmt = condition.stmt;
		cond = condition.cond;
		cond_v = condition.cond_v;
		replace(replaceVar1, replaceVar2);
	}

//	public String toString() {
//		return "Condition Stmt Tag: "
//				// + ((StringTag)
//				// (stmt.getTag("StringTag"))).getInfo().toString()
//				+ LoopUtils.stmt_toString(stmt) + "		Condition: "
//				+ cond.toString();
//	}

	public String getExpr() {
		return cond.toString();
	}

	void transform() { // if the cond_expr is a>0 ,the value is false, it will transform it into a<=0, value is true
		if (!cond_v) { // if cond_v false
			if (cond instanceof EqExpr) {
				EqExpr eq = (EqExpr) cond;
				cond = new JNeExpr(eq.getOp1(), eq.getOp2());
			} else if (cond instanceof NeExpr) {
				NeExpr eq = (NeExpr) cond;
				cond = new JEqExpr(eq.getOp1(), eq.getOp2());
			} else if (cond instanceof GeExpr) {
				GeExpr eq = (GeExpr) cond;
				cond = new JLtExpr(eq.getOp1(), eq.getOp2());
			} else if (cond instanceof GtExpr) {
				GtExpr eq = (GtExpr) cond;
				cond = new JLeExpr(eq.getOp1(), eq.getOp2());
			} else if (cond instanceof LeExpr) {
				LeExpr eq = (LeExpr) cond;
				cond = new JGtExpr(eq.getOp1(), eq.getOp2());
			} else if (cond instanceof LtExpr) {
				LtExpr eq = (LtExpr) cond;
				cond = new JGeExpr(eq.getOp1(), eq.getOp2());
			}
			cond_v = true;
		}
	}
	
	private void replace(Value replaceVar, int pos) {
		if(pos == 1){
			if (cond instanceof EqExpr) {
				EqExpr eq = (EqExpr) cond;
				cond = new JEqExpr(replaceVar, eq.getOp2());
			} else if (cond instanceof NeExpr) {
				NeExpr eq = (NeExpr) cond;
				cond = new JNeExpr(replaceVar, eq.getOp2());
			} else if (cond instanceof GeExpr) {
				GeExpr eq = (GeExpr) cond;
				cond = new JGeExpr(replaceVar, eq.getOp2());
			} else if (cond instanceof GtExpr) {
				GtExpr eq = (GtExpr) cond;
				cond = new JGtExpr(replaceVar, eq.getOp2());
			} else if (cond instanceof LeExpr) {
				LeExpr eq = (LeExpr) cond;
				cond = new JLeExpr(replaceVar, eq.getOp2());
			} else if (cond instanceof LtExpr) {
				LtExpr eq = (LtExpr) cond;
				cond = new JLtExpr(replaceVar, eq.getOp2());
			}
		} else if(pos == 2){
			if (cond instanceof EqExpr) {
				EqExpr eq = (EqExpr) cond;
				cond = new JEqExpr(eq.getOp1(), replaceVar);
			} else if (cond instanceof NeExpr) {
				NeExpr eq = (NeExpr) cond;
				cond = new JNeExpr(eq.getOp1(), replaceVar);
			} else if (cond instanceof GeExpr) {
				GeExpr eq = (GeExpr) cond;
				cond = new JGeExpr(eq.getOp1(), replaceVar);
			} else if (cond instanceof GtExpr) {
				GtExpr eq = (GtExpr) cond;
				cond = new JGtExpr(eq.getOp1(), replaceVar);
			} else if (cond instanceof LeExpr) {
				LeExpr eq = (LeExpr) cond;
				cond = new JLeExpr(eq.getOp1(), replaceVar);
			} else if (cond instanceof LtExpr) {
				LtExpr eq = (LtExpr) cond;
				cond = new JLtExpr(eq.getOp1(), replaceVar);
			}
		}
		
	}
	
	void replace(Value replaceVar) {
		if (cond instanceof EqExpr) {
			EqExpr eq = (EqExpr) cond;
			cond = new JEqExpr(replaceVar, eq.getOp2());
		} else if (cond instanceof NeExpr) {
			NeExpr eq = (NeExpr) cond;
			cond = new JNeExpr(replaceVar, eq.getOp2());
		} else if (cond instanceof GeExpr) {
			GeExpr eq = (GeExpr) cond;
			cond = new JGeExpr(replaceVar, eq.getOp2());
		} else if (cond instanceof GtExpr) {
			GtExpr eq = (GtExpr) cond;
			cond = new JGtExpr(replaceVar, eq.getOp2());
		} else if (cond instanceof LeExpr) {
			LeExpr eq = (LeExpr) cond;
			cond = new JLeExpr(replaceVar, eq.getOp2());
		} else if (cond instanceof LtExpr) {
			LtExpr eq = (LtExpr) cond;
			cond = new JLtExpr(replaceVar, eq.getOp2());
		}
	}
	
	void replace(Value replaceVar1, Value replaceVar2) {
		if (cond instanceof EqExpr) {
			EqExpr eq = (EqExpr) cond;
			cond = new JEqExpr(replaceVar1, replaceVar2);
		} else if (cond instanceof NeExpr) {
			NeExpr eq = (NeExpr) cond;
			cond = new JNeExpr(replaceVar1, replaceVar2);
		} else if (cond instanceof GeExpr) {
			GeExpr eq = (GeExpr) cond;
			cond = new JGeExpr(replaceVar1, replaceVar2);
		} else if (cond instanceof GtExpr) {
			GtExpr eq = (GtExpr) cond;
			cond = new JGtExpr(replaceVar1, replaceVar2);
		} else if (cond instanceof LeExpr) {
			LeExpr eq = (LeExpr) cond;
			cond = new JLeExpr(replaceVar1, replaceVar2);
		} else if (cond instanceof LtExpr) {
			LtExpr eq = (LtExpr) cond;
			cond = new JLtExpr(replaceVar1, replaceVar2);
		}
	}

	public boolean equals(Object condition) {
		if (condition == null || (!(condition instanceof Condition))) {
			return false;
		}

		Condition cond = (Condition) condition;

		if (this.stmt.equals(cond.stmt) && this.cond_v == cond.cond_v) {
			return true;
		} else if (this.getExpr().equals(cond.getExpr())) {
			return true;
		}

		return false;

	}
	
	public int hashCode(){
		return getExpr().hashCode();
	}
}

package utils;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import soot.Value;
import soot.jimple.ConditionExpr;
import soot.jimple.EqExpr;
import soot.jimple.GeExpr;
import soot.jimple.GtExpr;
import soot.jimple.LeExpr;
import soot.jimple.LtExpr;
import soot.jimple.NeExpr;
import soot.jimple.Stmt;
import soot.jimple.internal.JEqExpr;
import soot.jimple.internal.JGeExpr;
import soot.jimple.internal.JGtExpr;
import soot.jimple.internal.JIdentityStmt;
import soot.jimple.internal.JIfStmt;
import soot.jimple.internal.JLeExpr;
import soot.jimple.internal.JLtExpr;
import soot.jimple.internal.JNeExpr;
import soot.tagkit.IntegerConstantValueTag;

public class LoopPath {
	public List<Stmt> p;
	public List<Condition> conds;
	public List<ExceptionStmt> exps;
	// boolean hasException;

	LoopGraph lg;

    boolean isExit;
    int id;

	LoopPath(int size) {
		p = new ArrayList<Stmt>(size);
	}

	LoopPath(Stack<Stmt> list) {
		p = new ArrayList<Stmt>(list);
//		p = list;
	}

	LoopPath(int size, LoopGraph lg) {
		p = new ArrayList<Stmt>(size);
		this.lg = lg;
	}

	void setLoopGraph(LoopGraph lg) {
		this.lg = lg;
	}

	LoopPath() {
		p = new ArrayList<Stmt>();
	}
	void setId(int id){
		this.id=id;
	}

//	void add(Stmt s) {
//		if (s instanceof JIdentityStmt) {
//			if (((JIdentityStmt) s).getRightOp() instanceof JCaughtExceptionRef) {
//				this.add(new ExceptionStmt((JIdentityStmt) s));
//			}
//
//		}
//
//		p.add(s);
//
//	}

	void add(Condition c) {
		if (conds == null) {
			conds = new ArrayList<Condition>();
		}
		conds.add(c);
	}

	void add(ExceptionStmt e) {
		if (exps == null) {
			exps = new ArrayList<ExceptionStmt>();
		}
		// G.v().out.println("insert exception stmt");
		exps.add(e);
	}


	public boolean isExit() {
//		if (p.get(0) == p.get(p.size() - 1)) {
//			return false; // jump back to header";
//		} else {
//			return true; // jump out of the loops";
//		}
		return isExit;
	}

	public String toString() {
		String s = "";
		int i;
		for (i = 0; i < p.size(); i++) {
			Stmt stmt = p.get(i);
			s += ((IntegerConstantValueTag) (stmt.getTag("IntegerConstantValueTag"))).getIntValue();
			if (i + 1 < p.size()) {
				s += "--->";
			}
		}
//		if (p.get(0) == p.get(i - 1)) {
//			s += "	//jump back to header";
//		} else {
//			s += "	//jump out of the loops";
//		}
		if (!isExit) {
			s += "	//jump back to header";
		} else {
			s += "	//jump out of the loops";
		}
		return s;
	}
	
	public List<Stmt> getpathStmt(){
		return p;
	}

	public List<Condition> getconditions(){
		return conds;
	}
	
	void printConditions_Exceptions(PrintStream out) {
		if (conds != null && conds.size() > 0) {
			// out.println("Conditions: "+conds.size());
			for (Condition c : conds) {
				out.println(c.toString());
			}
		}

		if (exps != null && exps.size() > 0) {
			for (ExceptionStmt e : exps) {
				out.println(e.toString());
			}
		}

	}

	void printConditions(PrintStream out) {
		if (conds != null) {
			for (Condition c : conds) {
				out.println(c.toString());
			}
		}
	}

	void printExceptions(PrintStream out) {
		if (exps != null && exps.size() > 0) {
			for (ExceptionStmt e : exps) {
				out.println(e.toString());
			}
		}
	}
}






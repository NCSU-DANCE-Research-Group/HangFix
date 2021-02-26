package utils;

import soot.jimple.Stmt;
import soot.jimple.internal.JIdentityStmt;

public class ExceptionStmt {
	JIdentityStmt exception_identify;
	Stmt exception_throw; // which statement throw this exception

	ExceptionStmt(JIdentityStmt s1, Stmt s2) {
		exception_identify = s1;
		exception_throw = s2;
	}

	ExceptionStmt(JIdentityStmt s1) {
		exception_identify = s1;
		exception_throw = null;
	}

//	public String toString() {
//		return "Exception Stmt Tag: "
//				// + ((StringTag) (exception_identify.getTag("StringTag")))
//				// .getInfo().toString()
//				+ LoopUtils.stmt_toString(exception_identify) + "		Exception: "
//				+ exception_identify.getLeftOp().getType().toString();
//	}

	public String getExpr() {
		return exception_identify.getLeftOp().getType().toString();
	}

	public boolean equals(Object exception) {
		if (exception==null || (!(exception instanceof ExceptionStmt))) {
			return false;
		}

		ExceptionStmt exp = (ExceptionStmt) exception;

		if (this.exception_identify.equals(exp.exception_identify)) {
			if(exception_throw==null && exp.exception_throw==null){
				return true;
			}else if(exception_throw.equals(exp.exception_throw)){
				return true;
			}
			
		} else if (this.getExpr().equals(exp.getExpr())) {
			return true;
		}

		return false;

	}
	
	public int hashCode(){
		return getExpr().hashCode();
	}
}

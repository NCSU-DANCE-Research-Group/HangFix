package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.internal.JCaughtExceptionRef;
import soot.jimple.internal.JIdentityStmt;
import soot.jimple.internal.JIfStmt;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JVirtualInvokeExpr;
import soot.tagkit.IntegerConstantValueTag;
import soot.tagkit.Tag;
import soot.toolkits.exceptions.ThrowableSet;
import soot.toolkits.exceptions.UnitThrowAnalysis;

public class LoopUtils {
	static public String stmt_toString(Unit u) {
		List<Tag> tags = u.getTags();
		String s = "";

		for (Tag t : tags) {
			if (t instanceof LoopTag) {
				s += ((LoopTag) t).getInfo() + ": ";
			} else if (t instanceof IntegerConstantValueTag) {
				s += ((IntegerConstantValueTag) t).getIntValue() + ": ";
			}
		}
		return (s + u.toString() + "	Type: " + u.getClass().toString());
	}
	
	static int stmtTag(Unit u){
		List<Tag> tags = u.getTags();
		for (Tag t : tags) {
			if (t instanceof IntegerConstantValueTag) {
				return ((IntegerConstantValueTag) t).getIntValue();
			}
		}
		return -1;
	}

	static String stmt_Additional(JIfStmt s) {
		return "IfStmt TARGET: " + stmt_toString(s.getTarget());
	}

	static String stmt_Additional(JIdentityStmt s) {
		String str = "";
		str += "branches():" + s.branches();

		Value v = s.getRightOp();
		str += "Right：" + v.getType().toString();
		str += "Right：" + v.getClass().toString();

		if (v instanceof JCaughtExceptionRef) { // exception caught statement
			JCaughtExceptionRef caught = (JCaughtExceptionRef) v;
			str += "Right：" + caught.getType().toString();

			v = s.getLeftOp();
			str += "Left：" + v.getType().toString();
			str += "Left：" + v.getClass().toString();
		}

		return str;
	}

	static String stmt_Additional(JInvokeStmt s) {
		String str = "";

		InvokeExpr ie = s.getInvokeExpr();

		if (ie instanceof InstanceInvokeExpr) {
			InstanceInvokeExpr iie = (InstanceInvokeExpr) ie;
			Value v = iie.getBase();
			str += ("TYPE: " + v.getType().toString());

			if (iie instanceof JVirtualInvokeExpr) {
				SootMethod target = ((JVirtualInvokeExpr) iie).getMethod();
				if (target.getSignature().equals(
						"<java.lang.System: void exit(int)>")) {
					/* insert printing statements here */
				}
			}

			ThrowableSet throwExceptions = UnitThrowAnalysis.v().mightThrow(s);

			str += throwExceptions.toString();
		} else {
			str += "Expr TYPE:" + ie.getType().toString();
		}

		return str;
	}


}

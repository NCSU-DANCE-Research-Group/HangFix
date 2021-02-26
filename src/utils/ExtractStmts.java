package utils;

import java.util.List;

import soot.Body;
import soot.G;
import soot.jimple.GotoStmt;
import soot.jimple.Stmt;
import soot.jimple.internal.JGotoStmt;
import soot.jimple.internal.JIfStmt;
import soot.util.Chain;

public class ExtractStmts {
	public static Stmt extractFirstStmtInALabel(LoopPath path, Stmt targetStmt){
		Stmt firstStmt = targetStmt;
		List<Stmt> pathStmts = path.getpathStmt();
//		for(int i = pathStmts.size()-1; i >= 0; i--){
//			G.v().out.println("pathStmt = " + pathStmts.get(i));
//		}
		boolean foundTarg = false;
		for(int i = pathStmts.size()-1; i >= 0; i--){
			if(pathStmts.get(i).equals(targetStmt)){
				foundTarg = true;
			} else if(foundTarg){
				if(!(pathStmts.get(i) instanceof JGotoStmt) && !(pathStmts.get(i) instanceof JIfStmt)){ //in a label, there is no goto or if stmt.
					firstStmt = pathStmts.get(i);
				} else
					break;
			}
		}
		return firstStmt;
	}
	
	//When the stmts don't contain branches/labels, the return stmt is stmts.get(0).
	public static Stmt extractFirstStmtInALabel(List<Stmt> stmts, Stmt targetStmt){
		Stmt firstStmt = targetStmt;
//		for(int i = pathStmts.size()-1; i >= 0; i--){
//			G.v().out.println("pathStmt = " + pathStmts.get(i));
//		}
		boolean foundTarg = false;
		for(int i = stmts.size()-1; i >= 0; i--){
			if(stmts.get(i).equals(targetStmt)){
				foundTarg = true;
			} else if(foundTarg){
				if(!(stmts.get(i) instanceof JGotoStmt) && !(stmts.get(i) instanceof JIfStmt)){ //in a label, there is no goto or if stmt.
					firstStmt = stmts.get(i);
				} else
					break;
			}
		}
		return firstStmt;
	}
}

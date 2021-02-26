package utils;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import soot.Value;

public class PathExit {
	List<Condition> conds;
	List<ExceptionStmt> exps;

	public PathExit(List<Condition> c, List<ExceptionStmt> e) {
		conds = c;
		exps = e;
	}

	
	public Set<Value> getVariables(){
		Set<Value> vars=new HashSet<Value>();
		
		if(conds!=null){
			for(Condition cond:conds){
				vars.add(cond.cond.getOp1());
				vars.add(cond.cond.getOp2());
			}
		}
		return vars;
	}
	
	public boolean equals(Object pe) {
		if (pe == null || (!(pe instanceof PathExit))) {
			return false;
		}

		PathExit pexit = (PathExit) pe;
		
		
		boolean e_conds=false;
		boolean e_exps=false;
		
		if(conds!=null && pexit.conds!=null){
			if (conds.containsAll(pexit.conds) && pexit.conds.containsAll(conds)) {
				e_conds=true;
			}
		}else if(conds==null && pexit.conds==null){
			e_conds=true;
		}
		
		if(this.exps!=null && pexit.exps!=null){
			if (exps.containsAll(pexit.exps) && pexit.exps.containsAll(exps)) {
				e_exps=true;
			}
		}else if(exps==null&&pexit.exps==null){
			e_exps=true;
		}
		
		return (e_conds&&e_exps);

	}
	
	public int hashCode(){
		return toString().hashCode();
	}

	public String toString() {
		String s = "[";
		if (conds == null || conds.size() < 1) {
			s += "No condition and ";
		} else {
			for (Condition condition : conds) {
				s += condition.getExpr();
				s += " and ";
			}
		}

		if (exps == null || exps.size() < 1) {
			s += "No Exception thrown";
		} else {
			s += exps.get(0).getExpr();
			for (int i = 1; i < exps.size(); i++) {
				s += "and";
				s += exps.get(i).getExpr();
			}
		}

		s += "]";

		return s;
	}
}

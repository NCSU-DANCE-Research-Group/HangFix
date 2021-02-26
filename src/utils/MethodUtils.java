package utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import soot.Body;
import soot.G;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.UnitBox;
import soot.Value;
import soot.ValueBox;
import soot.baf.internal.BFieldPutInst;
import soot.baf.internal.BLoadInst;
import soot.baf.internal.BNewArrayInst;
import soot.baf.internal.BPushInst;
import soot.jimple.Stmt;
import soot.tagkit.IntegerConstantValueTag;
import soot.tagkit.Tag;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.util.Chain;


public class MethodUtils {
	
	
	/*
	 * This is to get all the stmts in the current class.
	 * */
	public static Map<String, List<Stmt>> getClassAllStmts(Body body){
		Map<String, List<Stmt>> allClassStmts = new HashMap<String, List<Stmt>>();
		for(SootMethod method : body.getMethod().getDeclaringClass().getMethods()){
			List<Stmt> methodStmts = getMethodAllStmts(method.getActiveBody());
			allClassStmts.put(method.getSubSignature(), methodStmts);
		}
		return allClassStmts;
	}
	
	
	public static List<Stmt> getClassAllStmts2(Body body){
		List<Stmt> allClassStmts = new ArrayList<Stmt>();
		for(SootMethod method : body.getMethod().getDeclaringClass().getMethods()){
			List<Stmt> methodStmts = getMethodAllStmts(method.getActiveBody());
			allClassStmts.addAll(methodStmts);
		}
		return allClassStmts;
	}
	
	
	public static List<List<Stmt>> getClassIntilizerStmts(Body body, PrintStream out){
		List<List<Stmt>> allClassStmts = new ArrayList<List<Stmt>>();
		for(SootMethod method : body.getMethod().getDeclaringClass().getMethods()){
//			out.println(method.getSignature());
			if(method.getName().toString().contains("<init>")){
				List<Stmt> methodStmts = getMethodAllStmts2(method.getActiveBody(), out);
				out.println(method.getSignature() + " " + methodStmts.size());
				allClassStmts.add(methodStmts);
			}
		}
		return allClassStmts;
	}
	
	

	
	/*
	 * This is get all the statements of the method/function.
	 * Including all the loop statements and non-loop statements.
	 * */
	public static List<Stmt> getMethodAllStmts(Body body){
		Chain<Unit> units = body.getUnits();
		List<Stmt> statements = new ArrayList<Stmt>();
		
//		for(Unit unit : units){
//			try{
//				statements.add((Stmt) unit);
//			} catch (Exception e){
//				continue;
//			}
//		}
		
		for (Iterator<Unit> stmts = units.snapshotIterator();stmts.hasNext();) {
			Unit cell = stmts.next();
			if(cell instanceof Stmt){
				Stmt stmt = (Stmt)cell;
				statements.add(stmt);
			}
			// Remove all the definitions.
			/*for (Iterator boxes = stmt.getDefBoxes().iterator(); boxes.hasNext();) {
				ValueBox box = (ValueBox)boxes.next();
				Value value = box.getValue();
				if (value instanceof FieldRef) {
					FieldRef ref = (FieldRef)value;
					units.remove(stmt);
				}
			}*/
		}
		return statements;
	}
	
	public static List<Stmt> getMethodAllStmts2(Body body,PrintStream out){
		List<Stmt> statements = new ArrayList<Stmt>();
		UnitGraph g = new ExceptionalUnitGraph(body);
		Iterator<Unit> it = g.iterator();
		while(it.hasNext()){
			Unit cell = it.next();
			out.println(cell.toString()+ " " + cell.getClass().toString());
//			if(cell instanceof Stmt){
				Stmt stmt = (Stmt)cell;
				statements.add(stmt);
//			}
		}
		return statements;
	}
	
	
}

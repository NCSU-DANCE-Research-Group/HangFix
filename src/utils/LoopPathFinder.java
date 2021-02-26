package utils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import soot.Body;
import soot.BodyTransformer;
import soot.G;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.Constant;
import soot.jimple.FieldRef;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;
import soot.jimple.toolkits.annotation.logic.Loop;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.LoopNestTree;
import soot.toolkits.graph.UnitGraph;
import soot.util.Chain;

public class LoopPathFinder extends BodyTransformer {
	private HashMap<Integer, Set<PathExit>> exits =  new HashMap<Integer, Set<PathExit>>();
	private HashMap<Integer, List<LoopPath>> nonExitPaths = new HashMap<Integer, List<LoopPath>>();
	private HashMap<Integer, List<LoopPath>> allPaths = new HashMap<Integer, List<LoopPath>>();
	
	public HashMap<Integer, Set<PathExit>> getExitPath(){
		return this.exits;
	}
	
	public HashMap<Integer, List<LoopPath>> getNonExitPath(){
		return this.nonExitPaths;
	}
	
	public HashMap<Integer, List<LoopPath>> getAllPath(){
		return this.allPaths;
	}
	
	
	@Override
	protected void internalTransform(Body body, String phaseName, Map options) {
		// TODO Auto-generated method stub
		String classname = body.getMethod().getDeclaringClass().getName();
		// use signature instead to avoid same-name functions
		String methodname = body.getMethod().getSubSignature();
		
//		String method_declare=body.getMethod().getDeclaration();
//		String class_name=body.getMethod().getDeclaringClass().getType().toString();
		
		G.v().out.println("LoopPathFinder: "+classname + ": " + methodname);
//		G.v().out.println("LoopPathFinder: "+class_name + ": " + method_declare);
		
		UnitGraph g = new ExceptionalUnitGraph(body);
		G.v().out.println("LoopPathFinder Size: " + g.size());

		LoopNestTree loopNestTree = new LoopNestTree(body);
		Iterator<Loop> iterator = loopNestTree.iterator();
		//Map<Integer, List<Integer>> loopTopoMap = MethodUtils.getLoopTopology(body);
		Map<String, List<Stmt>> allClassStmts = MethodUtils.getClassAllStmts(body);
		
		int loop_id = 0;
		while (iterator.hasNext()) {
			Loop l = iterator.next();

			LoopGraph lg = new LoopGraph(classname, methodname, allClassStmts, l , g, loop_id++);
			
			PrintStream out = null;
			try{
				String fname = ConfUtils.getFileName(classname, methodname,"_Loop"+lg.loop_id+".txt");
				File f = new File(fname);
				if(f.exists() && !f.isDirectory()) { 
					f.delete();
				}
				G.v().out.println(fname);
				out = new PrintStream(new FileOutputStream(fname));
			} catch(FileNotFoundException e){
				e.printStackTrace();
			}
			
			if(l.loopsForever()){
				out.println("This is an infinite loop.");
			} else {
				lg.setPrintStream(out);
				
				lg.printLoop(out);
				
			
				lg.pathSearch(out);
				
				lg.pathPrune(); // pruned the path that contains "a == b && a != b"
				
//				int pathsize = lg.printPaths(out);

//				lg.printPathConditions_Exceptions(out); //for every path
				Set<PathExit> exitPaths = lg.printLoopExitConditions_Exceptions(out); //for the whole loop
				this.exits.put(lg.loop_id, exitPaths);

//				int loopID = lg.getLoopID();
				
				List<LoopPath> nonEPaths = lg.getCandidateNonExitLoops(out); // for non-exit paths
				this.nonExitPaths.put(lg.loop_id, nonEPaths);
//				this.nonExitPaths.addAll(lg.getCandidateNonExitLoops(out)); // for non-exit paths
//				List<Stmt> loopAllstmts = lg.getLoopStmt();
				
				List<LoopPath> APaths = lg.getAllPaths(); //get all paths
				this.allPaths.put(lg.loop_id, APaths);
//				G.v().out.println("exits.size = " + exits.size() +"!!!inside LoopPathFinder");
//				G.v().out.println("nonExitPaths.size = " + nonExitPaths.size() +"!!!inside LoopPathFinder");
			}	

		}
	}
	

}
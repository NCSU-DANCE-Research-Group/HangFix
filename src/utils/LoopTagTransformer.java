package utils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.G;
import soot.Unit;
import soot.jimple.Stmt;
import soot.jimple.toolkits.annotation.logic.Loop;
import soot.tagkit.Tag;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.LoopNestTree;
import soot.toolkits.graph.UnitGraph;
import utils.LoopTag;
import utils.LoopUtils;

public class LoopTagTransformer extends BodyTransformer {

	@Override
	protected void internalTransform(Body body, String phaseName, Map options) {
		// TODO Auto-generated method stub
		String classname = body.getMethod().getDeclaringClass().getName();
		// use signature instead to avoid same-name functions
		String methodname = body.getMethod().getSubSignature();
		G.v().out.println("LoopTagTransformer: " + classname + ": " + methodname);

		// if(!LoopUtils.isPrint(classname)){ //if it is not the targeted
		// function, then do nothing
		// return;
		// }

//		if (!LoopUtils.isPrint(classname) || LoopUtils.isSkippedFunction(body.getMethod().getName())) { 
//			// if it is not the targeted function(filtered class, specified method written in LoopUtils), then do nothing
//			return;
//		}

		//UnitGraph g = new ExceptionalUnitGraph(body);
		// Iterator<Unit> it = g.iterator();
		
		LoopNestTree loopNestTree = new LoopNestTree(body);
		// boolean isNested = loopNestTree.hasNestedLoops();
		Iterator<Loop> iterator = loopNestTree.iterator();

		int loop_id = 0;
		while (iterator.hasNext()) {
			Loop l = iterator.next();
			//comment the following if branch for testing
//			if (l.getLoopStatements().size() <= 3) { // prevent soot-generated false loop
//													//--keyword: synchronized, try-catch-finally
//				continue;
//			}

			List<Stmt> loopStatements = l.getLoopStatements();

			int loop_stmt_id = 0;
			for (Stmt s : loopStatements) {
				//s.addTag(new LoopTag(loop_id, loop_stmt_id));
				s.addTag(new LoopTag(loop_id));
				loop_stmt_id++;
			}

			loop_id++;
		}

		G.v().out.println("LoopTagTransformer loops: " + loop_id);

		// print method
		if (isPrint && loop_id > 0) { // only print if the the method has at least one loop
			// if (LoopUtils.isPrint(classname)) {
			// if(LoopUtils.isRequiredFunction(body.getMethod().getName())){
			PrintStream out = null;

			String fname = ConfUtils.getFileName(classname, methodname, "_Loop.txt");
			//check whether file exists or not
			File f = new File(fname);
			if(f.exists() && !f.isDirectory()) { 
			    // do something
				//this.isExist = true;
				return;
			}
			
			G.v().out.println(fname);
			//File f = new File(fname);
			try {
				out = new PrintStream(new FileOutputStream(f));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// out=new PrintStream(new
			// FileOutputStream("/home/peipei/Downloads/"+classname+"_"+methodname+"_Loop.txt"));

			if (out != null) {
				UnitGraph g = new ExceptionalUnitGraph(body);
				Iterator<Unit> it_p = g.iterator();
				while (it_p.hasNext()) {
					Unit u = it_p.next();
					out.println(LoopUtils.stmt_toString(u));

					for (Unit stmt : g.getSuccsOf(u)) { //successor of the current statement
						out.println("---->" + LoopUtils.stmt_toString(stmt));
					}
				}
			}
		}
	}

	public void setLoopPrint(boolean isPrint) {
		this.isPrint = isPrint;
	}
	
	//boolean getExist(){
	//	return isExist;
	//}

	boolean isPrint = false;
	//boolean isExist = false;
}


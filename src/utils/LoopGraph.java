package utils;


import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import datastruct.tree.ArrayMultiTreeNode;
import datastruct.tree.TreeNode;
import soot.G;
import soot.Trap;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.BinopExpr;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.CmpExpr;
import soot.jimple.ConditionExpr;
import soot.jimple.Constant;
import soot.jimple.EqExpr;
import soot.jimple.GeExpr;
import soot.jimple.GtExpr;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.IntConstant;
import soot.jimple.LeExpr;
import soot.jimple.LtExpr;
import soot.jimple.NeExpr;
import soot.jimple.NumericConstant;
import soot.jimple.Stmt;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JCaughtExceptionRef;
import soot.jimple.internal.JEqExpr;
import soot.jimple.internal.JGeExpr;
import soot.jimple.internal.JGotoStmt;
import soot.jimple.internal.JGtExpr;
import soot.jimple.internal.JIdentityStmt;
import soot.jimple.internal.JIfStmt;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JLeExpr;
import soot.jimple.internal.JLtExpr;
import soot.jimple.internal.JNeExpr;
import soot.jimple.toolkits.annotation.logic.Loop;
import soot.tagkit.IntegerConstantValueTag;
import soot.tagkit.Tag;
import soot.toolkits.graph.UnitGraph;
import soot.util.Chain;
import utils.LoopUtils;

public class LoopGraph {
	protected Loop loop;
	protected UnitGraph g;
	public int loop_id;

	protected boolean hasNestedLoops = false;
	protected int nested_loop_id;

	protected Stmt header;
	protected Collection<Stmt> exits = null;
	protected List<Stmt> stmts = null;
	protected Stmt back_edge=null;
	protected HashMap<Integer,Stmt> stmts_OutOfLoops = null;
	protected HashMap<Integer,ExceptionStmt> stmts_exceptions = null;

	protected Chain<Trap> traps;

	protected List<LoopPath> paths = new ArrayList<LoopPath>();

	protected Map<Unit, Integer> isVisited = null;
	protected Stack<Stmt> stack = null;
	private PrintStream out;
	
	private ArrayMultiTreeNode<Stmt> root = null;
	String className = null;
	String methodName = null;
	Map<String, List<Stmt>> allClassStmts = null;
	
	private List<Value> dataVars = null;

	// protected TreeMap<Edge,Condition> conditions;

	// should handle compressed condition values

	public LoopGraph(String className, String methodName, Map<String, List<Stmt>> allClassStmts, Loop loop, UnitGraph g, int id) {
		this.loop = loop;
		this.g = g;
		this.loop_id = id;
		this.header = loop.getHead();
		this.exits = loop.getLoopExits();
		this.stmts = loop.getLoopStatements();
		this.back_edge=loop.getBackJumpStmt();
		this.hasNestedLoops = hasNestedLoop();
		this.root = new ArrayMultiTreeNode<>(header);
		this.className = className;
		this.methodName = methodName;
		this.allClassStmts = allClassStmts;
		
	}

	public void setTraps(Chain<Trap> traps) {
		this.traps = traps;
	}
	
	public int getLoopID(){
		return loop_id;
	}

	private boolean hasNestedLoop() {
		for (Stmt stmt : stmts) {
			List<Tag> tags = stmt.getTags();
			for (Tag tag : tags) {
				if (tag instanceof LoopTag) {
					int nested_id = ((LoopTag) tag).getLoopId();

					if (nested_id < loop_id) {
						if (nested_id > nested_loop_id) {
							nested_loop_id = nested_id;
						}

						return true;
					}
				}
			}
		}

		return false;
	}

	
	public boolean hasInnerLoop(){
		for(Stmt stmt : stmts){
			List<Tag> tags = stmt.getTags();
			boolean foundlooptag = false;
			for(Tag tag : tags){
				if(tag instanceof LoopTag){
					int id = ((LoopTag) tag).getLoopId();
					if(id == loop_id){
						foundlooptag = true;
					} else {
						if(foundlooptag == true){
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	 
	
	public boolean isNested() {
		return hasNestedLoops;
	}
	
	public List<Stmt> getLoopStmt(){
		return stmts;
	}
	
	public void printLoop(PrintStream out) {
		
		out.println("**********LOOP BODY SIZE: "+stmts.size()+"**********");
		for (Stmt s : stmts) {
			out.println(LoopUtils.stmt_toString(s));

			// additional handling for special stmt
			if (s instanceof JInvokeStmt) {
				JInvokeStmt newstmt = (JInvokeStmt) s;
				out.println(LoopUtils.stmt_Additional(newstmt));
			} else if (s instanceof JIdentityStmt) {
				JIdentityStmt newstmt = (JIdentityStmt) s;
				out.println(LoopUtils.stmt_Additional(newstmt));
			} else if (s instanceof JIfStmt) {
				JIfStmt newstmt = (JIfStmt) s;
				out.println(LoopUtils.stmt_Additional(newstmt));
			}

			// stmt successors
			for (Unit stmt : g.getSuccsOf(s)) {
				if(stmt.hasTag("IntegerConstantValueTag")){
					out.println("---->"
							+ ((IntegerConstantValueTag) stmt.getTag("IntegerConstantValueTag")).getIntValue() 
							+ ":" + stmt.toString());

				}
				
				if (!stmts.contains(stmt)) {
					addStmtOutOfLoops((Stmt) stmt);
				}
			}
		}
		out.println("**********END OF LOOP BODY***************");

		// print header, exits, ...
		out.println("**********LOOP Header, Loop Exits, Back Edge, OutOfLoopStmts**********");
		// header
		out.println("Header: " + LoopUtils.stmt_toString(header));
		printExits(out);// exits
		//back edge
		out.println("BackEdge:"+ LoopUtils.stmt_toString(back_edge));
		
		// Stmt OutOfLoop
		if (stmts_OutOfLoops != null) {
			for (Stmt stmt : stmts_OutOfLoops.values()) {
				out.println("OutOfLoop: " + LoopUtils.stmt_toString(stmt));
			}
		}
		// out.println("-------- Loop Traps--------");
		// Iterator trapsIt = traps.iterator();
		// while (trapsIt.hasNext()) {
		// Trap t = (Trap) trapsIt.next();
		// out.println(t.getException());
		// }

	}
	
	private void printExits(PrintStream out) {
		if (exits != null && exits.size() != 0) {
			out.println("The number of exits: " + exits.size());
			out.print("Exit: ");
			for (Stmt exit : exits) {
				out.println(LoopUtils.stmt_toString(exit));
			}
		}
	}
	
	public int printPaths(PrintStream out) {
		if (paths != null && paths.size() != 0) {
			out.println("**********The number of paths: " + paths.size() + "**********");
//			int i = 0;
			for (LoopPath p : paths) {
				out.print("Path " + p.id + ":");
				out.println(p.toString());
			}
			return paths.size();
		}
		return 0;
	}
	
	public List<LoopPath> getAllPaths(){
		return paths;
	}

	public void printPathConditions_Exceptions(PrintStream out) {
		if (paths != null && paths.size() != 0) {
			out.println("**********Path conditions & exceptions **********");
//			int i = 0;
			for (LoopPath p : paths) {
//				i++;
				if (p.conds != null) {
					out.println("Path " + p.id + ":	conditions: " + p.conds.size());
					p.printConditions(out);
				}else{
					out.println("Path " + p.id + ":	conditions: 0");
				}
				
				if (p.exps != null) {
					out.println("Path " + p.id + ":	exceptions: " + p.exps.size());
					p.printExceptions(out);
				}else{
					out.println("Path " + p.id + ":	exceptions: 0");
				}
			}
			out.println();
		}
	}

	public List<LoopPath> getCandidateNonExitLoops(PrintStream out){
		List<LoopPath> nonExitPaths = new ArrayList<LoopPath>();
		int pathId = 0;
		if (paths != null && paths.size() != 0) {
			//List<PathExit> list = new ArrayList<PathExit>();
			out.println("==============Loop non-exit paths & conditions & exceptions==============");
			for (LoopPath p : paths){
				if(!p.isExit()){ //restore the non-exit path information
					nonExitPaths.add(p);
					List<Stmt> pathstmt = p.getpathStmt();
					PathExit pathcond = new PathExit(p.conds, p.exps);
					out.println("Path " + pathId + ": "+ pathcond.toString());
					String s =  "        ";
					for (int i = 0; i < pathstmt.size(); i++) {
						Stmt stmt = pathstmt.get(i);
						s += ((IntegerConstantValueTag) (stmt.getTag("IntegerConstantValueTag"))).getIntValue();
						if (i + 1 < pathstmt.size()) {
							s += "--->";
						}
					}
					out.println(s);
					pathId++;
				}
			}
		}
		return nonExitPaths;
	}
	
	public Set<PathExit> printLoopExitConditions_Exceptions(PrintStream out) {
		if (paths != null && paths.size() != 0) {
			Set<PathExit> set = new HashSet<PathExit>();
			//Set<PathExit> set2 = new HashSet<PathExit>();
			for (LoopPath p : paths) {
				if(p.isExit()){   //Print only Exit paths
					set.add(new PathExit(p.conds, p.exps));
				}
			}
			out.println("**********Loop exit conditions & exceptions **********");
			if(set.size() > 0){
				out.println("Size: " + set.size());
				out.println("{");
				//boolean first = false;		
				Iterator<PathExit> it = set.iterator();
				while(it.hasNext()){
					PathExit pe = it.next();
					out.println("	" + pe.toString());
				}
				out.println("}");
			}else{
				out.println("All Paths cannot exit!!!");
			}
			return set;
		}
		return null;
	}

	
	public boolean checkDataRelated(PrintStream out, Set<PathExit> set, Map<Value, Set<Value>> map_merged, Set<Value> data_related, Set<Value> loop_counter){
		if(!set.isEmpty()){
			Iterator<PathExit> it = set.iterator();
			int count = 0;
			while(it.hasNext()){
				PathExit pe = it.next();
				Set<Value> vars = pe.getVariables();
				
				boolean is_data_related = false;
				for(Value v:vars){
					if(data_related.contains(v)){
						is_data_related = true;
						continue;
					}else if(loop_counter.contains(v)){
						continue;
					}
				}
				
				if(is_data_related){
					count++;
				}
				out.println("	" + pe.toString() + "  is data related: " + is_data_related);
			}
			
			if(count > 0){
				out.println("Loop Exit Condition is Data Related");
				return true;
			}else{
				out.println("Loop Exit Condition is NOT Data Related");
				return false;
			}
		}
		return false;
	}
	
	//Consider ALL the exit conditions are data-related.
	public void checkDataRelated2(PrintStream out, Set<PathExit> set, Map<Value, Set<Value>> map_merged, Set<Value> data_related, Set<Value> loop_counter){
		if(!set.isEmpty()){
			Iterator<PathExit> it = set.iterator();
			boolean is_data_related = true;
			boolean hasDataVar = false;
			while(it.hasNext()){
				PathExit pe = it.next();
				Set<Value> vars = pe.getVariables();
				
				for(Value v:vars){
					hasDataVar = true;
					if(!data_related.contains(v)){
						is_data_related = false;
						break;
					}
				}
				
				if(!is_data_related){
					break;
				}
			}
			
			if(hasDataVar && is_data_related){
				out.println("Loop Exit Condition is Data Related");
			}else{
				out.println("Loop Exit Condition is NOT Data Related");
			}
		}
	}
	
	
	private boolean olddesign = true;
	
	public void pathSearch(PrintStream out) {
		if(olddesign){
			Map<Stmt, Boolean> hashmap = new HashMap<Stmt, Boolean>();
			Stack<Stmt> path_list = new Stack<Stmt>();
			DFS(header, hashmap, path_list, null); //this func cannot generate all the paths
		} else {
			//we use multi-node tree to retrieve all the paths
//			search4PathBFS(header, root, out);
			search4Path(header, root, out);
			out.println("finish search4Path...");
//			printTreeBFS();
			printTreeH();
			generatePath(this.subHeaders);
		}
	}

	public void setPrintStream(PrintStream out){
		this.out = out;
	}
	
	private LoopPath buildPath(Stack<Stmt> list, boolean isExit, int id) {
		LoopPath p = new LoopPath(list);
		for (int i = 0; i < list.size()-1; i++) {    //does not consider the OutOfLoopStmt
			//i < size -1 this is because header is the last element, it may also be a JIfStmt;
			// if outofLoop statement is the last element, we do not care it is JIfStmt or not
			Stmt stmt = list.get(i);
			if (stmt instanceof JIfStmt) {
				Stmt target = ((JIfStmt) stmt).getTarget();
				Condition c;
				Stmt nextStmt = list.get(i + 1);
				if (nextStmt.equals(target)) {
					c = new Condition((JIfStmt) stmt, true);
				} else {
					c = new Condition((JIfStmt) stmt, false);
				}
				p.add(c);
			} else if (stmt instanceof JIdentityStmt) {
				if (((JIdentityStmt) stmt).getRightOp() instanceof JCaughtExceptionRef) {
					int m_tag=((IntegerConstantValueTag) stmt.getTag("IntegerConstantValueTag")).getIntValue();
					addStmtExceptions(stmt);
					p.add(stmts_exceptions.get(m_tag));
				}
			}
		}
		//Consider the LoopOutOfStmt 
		Stmt stmt2 = list.lastElement();
		if (stmt2 instanceof JIdentityStmt && ((JIdentityStmt) stmt2).getRightOp() instanceof JCaughtExceptionRef) {
				int m_tag=((IntegerConstantValueTag) stmt2.getTag("IntegerConstantValueTag")).getIntValue();
				addStmtExceptions(stmt2);
				p.add(stmts_exceptions.get(m_tag));
		}
		p.isExit=isExit;
        p.setId(id);
//        out.print("Path " + id + ":");
//        out.println(p.toString());
		return p;
	}
	
	
	private void DFS(Stmt cur, Map<Stmt, Boolean> hashmap, Stack<Stmt> list, Stmt prev) {
//		G.v().out.println("DFS list push:"+LoopUtils.stmt_toString(cur));
//		list.add(cur);
		list.push(cur);
		if(!cur.equals(header)){
			hashmap.put(cur,true);  //do not mark header because we may revisit it.
		}
		/*For the nested loop, it will not jump to the outer loop header, it might jump to the inner loop head,
		 * Thus, this func missed those non-exit paths*/
		int m_tag = ((IntegerConstantValueTag)cur.getTag("IntegerConstantValueTag")).getIntValue(); 
		if (stmts_OutOfLoops.containsKey(m_tag)) { // build Path, reach outofloop stmt
//			G.v().out.println("Add path jump of loop id:"+paths.size());
			paths.add(buildPath(list,true,paths.size()));
		} else if (cur.equals(header) && list.size() > 1) { // jump back to header
			paths.add(buildPath(list,false,paths.size()));
//			G.v().out.println("find path jump back to header");
		} else {
			for(Unit u : g.getSuccsOf(cur)){
				Stmt next = (Stmt)u;
				
				// no same element along the path
				if((!hashmap.containsKey(next)) || (!hashmap.get(next))){ //not visited or not in hashmap  
					DFS(next, hashmap, list, cur);
//					list.remove(next);
//					list.remove(list.size()-1);
					list.pop();
//					G.v().out.println("DFS list pop:"+LoopUtils.stmt_toString(next));
				}
				
//				else if(next.equals(header)){ // jump back to header
//					DFS(next,hashmap,list,cur);
//					list.remove(next);
//				}
			}
		}
		hashmap.put(cur,false);//mark it as unvisted
//		while (hashmap.containsKey(cur) && !hashmap.get(cur).isEmpty()) {
//			Stmt next=(Stmt) hashmap.get(cur).remove(0);
//			
//			
//			list.remove(next);
//			hashmap.get(cur).add(next);
//			G.v().out.println("remove last element of list: "+LoopUtils.stmt_toString(next));
//		}
	}
	
	private void printTree(){
		int MaxHeight = 0;
		Iterator<TreeNode<Stmt>> iterator = this.root.iterator();
		while (iterator.hasNext()) {
			TreeNode<Stmt> node = iterator.next();
			MaxHeight = (MaxHeight > node.height()) ? MaxHeight : node.height(); 
		}
		for(int i = MaxHeight; i >= 0; i--){
			iterator = this.root.iterator();
			boolean hasNode = false;
			while (iterator.hasNext()) {
				TreeNode<Stmt> node = iterator.next();
				if(node.height() == i){
					hasNode = true;
					int p_tag = -1; //root node has no parent
					if(!node.isRoot()){
						TreeNode<Stmt> parentNode = node.parent();
						p_tag = ((IntegerConstantValueTag)parentNode.data().getTag("IntegerConstantValueTag")).getIntValue();
					}
					int m_tag = ((IntegerConstantValueTag)node.data().getTag("IntegerConstantValueTag")).getIntValue();
					
					out.print(m_tag + "("+p_tag+") ");
				}
			}
			if(hasNode)
				out.println();
		}
	}
	
    private void printTreeH() {
    	printTreeH(this.root,"", true);
    }

    private void printTreeH(TreeNode<Stmt> node, String prefix, boolean isTail) {
    	int name = ((IntegerConstantValueTag)node.data().getTag("IntegerConstantValueTag")).getIntValue();
        out.println(prefix + (isTail ? "└── " : "├── ") + name);
        List<TreeNode<Stmt>> children = getChildren(node);
        for (int i = 0; i < children.size() - 1; i++) {
        	printTreeH(children.get(i), prefix + (isTail ? "    " : "│   "), false);
        }
        if (children.size() > 0) {
        	printTreeH(children.get(children.size() - 1), prefix + (isTail ?"    " : "│   "), true);
        }
    }
    
    private List<TreeNode<Stmt>> getChildren(TreeNode<Stmt> curNode){
    	List<TreeNode<Stmt>> children = new ArrayList<TreeNode<Stmt>>();
    	Iterator<TreeNode<Stmt>> iterator = this.root.iterator();
        while (iterator.hasNext()) {
        	TreeNode<Stmt> node = iterator.next();
        	if( !node.isRoot() && node.parent().equals(curNode)){
        		children.add(node);
        	}
		}
        return children;
    }
	
	private void printTreeBFS(){
        LinkedList<TreeNode<Stmt>> queue2 = new LinkedList<TreeNode<Stmt>>();
        queue2.add(this.root);
        int header_tag = ((IntegerConstantValueTag)this.header.getTag("IntegerConstantValueTag")).getIntValue();
        out.print(header_tag);
        while (queue2.size() != 0)
        {
            TreeNode<Stmt> curNode = queue2.poll();
            Stmt curStmt = curNode.data();
            int curStmt_tag = ((IntegerConstantValueTag)curStmt.getTag("IntegerConstantValueTag")).getIntValue();
            if(curStmt.equals(this.header)){
            	out.println();
            } else {
            	out.print(curStmt_tag + " ");
            }
            boolean needPrint = false;
//            int childNum = 0;
            Iterator<TreeNode<Stmt>> iterator = this.root.iterator();
            while (iterator.hasNext()) {
            	TreeNode<Stmt> node = iterator.next();
            	if( !node.isRoot() && node.parent().equals(curNode)){
            		Stmt childStmt = node.data();
                    int childStmt_tag = ((IntegerConstantValueTag)childStmt.getTag("IntegerConstantValueTag")).getIntValue();
//                    out.print(childStmt_tag + " ");
                    queue2.add(node);
                    needPrint = true;
//                    childNum++;
            	}
			}
//            out.print(curStmt_tag+"'s childNum = " + childNum);
            if(needPrint){
            	//out.println();
            	TreeNode<Stmt> newNode = new ArrayMultiTreeNode<>(this.header);
            	queue2.add(newNode);
            }
        }
	}
	
	// using BFS instead of DFS
    private void search4PathBFS(Stmt cur, ArrayMultiTreeNode<Stmt> root, PrintStream out)
    {
        LinkedList<Stmt> queue = new LinkedList<Stmt>();
        LinkedList<ArrayMultiTreeNode<Stmt>> queue2 = new LinkedList<ArrayMultiTreeNode<Stmt>>();
        boolean visited[] = new boolean[g.size()]; //g.size is the total number of stmt in the current method
        int cur_tag = ((IntegerConstantValueTag)cur.getTag("IntegerConstantValueTag")).getIntValue();
        if(stmts_OutOfLoops.containsKey(cur_tag)){
			return;
		}
        visited[cur_tag]=true;
        queue.add(cur);
        queue2.add(root);
        while (queue.size() != 0 && queue2.size() != 0)
        {
            Stmt curStmt = queue.poll();
            ArrayMultiTreeNode<Stmt> curNode = queue2.poll();
            //get all the successor stmts of curStmt
            for(Unit u : g.getSuccsOf(curStmt)){
    			if(u instanceof Stmt){
    				Stmt next = (Stmt)u;
    				int curStmt_tag = ((IntegerConstantValueTag)curStmt.getTag("IntegerConstantValueTag")).getIntValue();
    				int m_tag = ((IntegerConstantValueTag)next.getTag("IntegerConstantValueTag")).getIntValue();
    				if(curStmt_tag == m_tag){
    					continue;
    				} else if(stmts_OutOfLoops.containsKey(m_tag)){
//    					continue;
    					ArrayMultiTreeNode<Stmt> child = new ArrayMultiTreeNode<>(next);
    					curNode.add(child); // just create an exit leaf
    				}else {
//    					out.println("curTag:"+curStmt_tag+"------print tree------------");
//    					print();
//    					Iterator<TreeNode<Stmt>> iterator = this.root.iterator();
//    					boolean foundSameNode = false;
//    					while (iterator.hasNext()) {
//    						TreeNode<Stmt> node = iterator.next();
//    						if(node.data().equals(next)){
//    							foundSameNode = true;
//    							break;
//    						}
//    					}
    					ArrayMultiTreeNode<Stmt> child = new ArrayMultiTreeNode<>(next);
    					curNode.add(child);
//    					if(!foundSameNode){
//    						queue.add(next);
//    						queue2.add(child);
//    					}
    					if(!visited[m_tag]){
    						visited[m_tag] = true;
    						queue.add(next);
    						queue2.add(child);
    					}
    				}
    			}
            }
        }
    }
	


	private Set<Stmt> subHeaders = new HashSet<Stmt>();
	private void search4Path(Stmt cur, ArrayMultiTreeNode<Stmt> root, PrintStream out){
		for(Unit u : g.getSuccsOf(cur)){
			if(u instanceof Stmt){
				Stmt next = (Stmt)u;
				int cur_tag = ((IntegerConstantValueTag)cur.getTag("IntegerConstantValueTag")).getIntValue();
				int m_tag = ((IntegerConstantValueTag)next.getTag("IntegerConstantValueTag")).getIntValue();
				if(stmts_OutOfLoops.containsKey(cur_tag)){
					return;
				}
				if(!cur.containsInvokeExpr() && isErrorHandlingStmt(next)){//if the current statement does not invoke function all
					continue;                                              //the next statement cannot be error handling statement,
				}                                                          //Soot naively considers all the statement in try block can throw exception
				if(cur_tag == m_tag){
					continue;
				} else if(stmts_OutOfLoops.containsKey(m_tag)){
					ArrayMultiTreeNode<Stmt> child = new ArrayMultiTreeNode<>(next);
					root.add(child); // just create an exit leaf
				} else {
					Iterator<TreeNode<Stmt>> iterator = this.root.iterator();
					boolean foundSameNode = false;
					TreeNode<Stmt> sameNode = null;
					while (iterator.hasNext()) {
						TreeNode<Stmt> node = iterator.next();
						if(node.data().equals(next)){
							foundSameNode = true;
							sameNode = node;
							if(!next.equals(header)){
								subHeaders.add(next);
							}
							break;
						}
					}
					ArrayMultiTreeNode<Stmt> child = new ArrayMultiTreeNode<>(next);
					root.add(child);
					if(!foundSameNode){//the node is not on the tree yet
						search4Path(next, child, out);
					} 
//					else { //the node is already on the tree
//						if(sameNode != null){
//							child.add(sameNode);//add the sameNode as itself's child
//						}
//					}
				}
			}
		}
	}

	/*
	 * it shares the same subPath's headers with the search4Path function.
	 * */
	private void search4NormalPath(Stmt cur, ArrayMultiTreeNode<Stmt> root, PrintStream out){
		for(Unit u : g.getSuccsOf(cur)){
			if(u instanceof Stmt){
				Stmt next = (Stmt)u;
				if(isErrorHandlingStmt(next)){ //for normal path, we don't consider the error-handling code at all.
					continue;
				}
				int cur_tag = ((IntegerConstantValueTag)cur.getTag("IntegerConstantValueTag")).getIntValue();
				int m_tag = ((IntegerConstantValueTag)next.getTag("IntegerConstantValueTag")).getIntValue();
				if(stmts_OutOfLoops.containsKey(cur_tag)){
					return;
				}
				if(cur_tag == m_tag){
					continue;
				} else if(stmts_OutOfLoops.containsKey(m_tag)){
					ArrayMultiTreeNode<Stmt> child = new ArrayMultiTreeNode<>(next);
					root.add(child); // just create an exit leaf
				} else {
					Iterator<TreeNode<Stmt>> iterator = this.root.iterator();
					boolean foundSameNode = false;
					TreeNode<Stmt> sameNode = null;
					while (iterator.hasNext()) {
						TreeNode<Stmt> node = iterator.next();
						if(node.data().equals(next)){
							foundSameNode = true;
							sameNode = node;
							if(!next.equals(header)){
								subHeaders.add(next);
							}
							break;
						}
					}
					ArrayMultiTreeNode<Stmt> child = new ArrayMultiTreeNode<>(next);
					root.add(child);
					if(!foundSameNode){
						search4NormalPath(next, child, out);
					}
				}
			}
		}
	}
	
	//This set only stores the subpath's header, this subpath is the Error Handling path, not normal path
	private Set<Stmt> subHeadersWEH = new HashSet<Stmt>();
	private void search4ErrorHandlingPath(Stmt cur, ArrayMultiTreeNode<Stmt> root, PrintStream out, Value dataVar){
		boolean useDataVar = false;
		boolean hasErrorHandling = false;
		for(ValueBox vb : cur.getUseBoxes()){
			Value use = vb.getValue();
			if(use.equals(dataVar)){
				useDataVar = true;
				break;
			}
		}
		for(Unit u : g.getSuccsOf(cur)){
			if(u instanceof Stmt){
				Stmt next = (Stmt)u;
				if(isErrorHandlingStmt(next)){ //for normal path, we don't consider the error-handling code at all.
					hasErrorHandling = true;
					break;
				}
			}
		}
		int satisfy = -1;
		Stmt target = null; //if branch's next stmt
		if(cur instanceof JIfStmt){
			List<Stmt> toCurStmts = extractConditions2CurrentStmt(cur, root);
			JIfStmt ifstmt = (JIfStmt) cur;
			ConditionExpr cond = (ConditionExpr) ifstmt.getCondition();
			target = ifstmt.getTarget();
			satisfy = checkSatisfy(cond, toCurStmts);
		}
		for(Unit u : g.getSuccsOf(cur)){
			if(u instanceof Stmt){
				Stmt next = (Stmt)u;
				if(useDataVar && hasErrorHandling){
					if(!isErrorHandlingStmt(next)){ //if use this data variable, we assume it can only go to the error handling path
						continue;
					}
				}
				if(cur instanceof JIfStmt){
					if(satisfy == 1){//if branch
						if(!next.equals(target)){
							continue;
						}
					} else if(satisfy == 0){//else branch
						if(next.equals(target)){
							continue;
						}
					}
				}
				int cur_tag = ((IntegerConstantValueTag)cur.getTag("IntegerConstantValueTag")).getIntValue();
				int m_tag = ((IntegerConstantValueTag)next.getTag("IntegerConstantValueTag")).getIntValue();
				if(stmts_OutOfLoops.containsKey(cur_tag)){
					return;
				}
				if(cur_tag == m_tag){
					continue;
				} else if(stmts_OutOfLoops.containsKey(m_tag)){
					ArrayMultiTreeNode<Stmt> child = new ArrayMultiTreeNode<>(next);
					root.add(child); // just create an exit leaf
				} else {
					Iterator<TreeNode<Stmt>> iterator = this.root.iterator();
					boolean foundSameNode = false;
					while (iterator.hasNext()) {
						TreeNode<Stmt> node = iterator.next();
						if(node.data().equals(next)){
							foundSameNode = true;
							if(!next.equals(header)){
								subHeadersWEH.add(next);
							}
							break;
						}
					}
					ArrayMultiTreeNode<Stmt> child = new ArrayMultiTreeNode<>(next);
					root.add(child);
					if(!foundSameNode){
						search4ErrorHandlingPath(next, child, out, dataVar);
					}
				}
			}
		}
	}
	
	
	/* If it returns 0, then it means this if branch is definitely not gonna be in the path
	 * So, the path should only consider the else branch.
	 * If it returns 1, it means go to if branch.
	 * If it returns -1, it means currently we can not determine which branch it need to go, so include two branches*/
	private int checkSatisfy(ConditionExpr cond, List<Stmt> stmts){
		if (cond instanceof EqExpr || cond instanceof NeExpr 
				|| cond instanceof GeExpr || cond instanceof GtExpr
				|| cond instanceof LeExpr || cond instanceof LtExpr) {
			BinopExpr eq = (BinopExpr) cond;
			Value left = eq.getOp1();
			Value leftTemp = eq.getOp1();
			Value right = eq.getOp2();
			if(leftTemp instanceof CmpExpr){ //convert "var3 = var1 cmp var2", "var3 == 0" into "var1 == var2"
				left = ((CmpExpr) leftTemp).getOp1();
				right = ((CmpExpr) leftTemp).getOp2();
			}
			Value leftValue = lastestValue(left, stmts);
			Value rightValue = lastestValue(right, stmts);
			if(leftValue instanceof Constant && rightValue instanceof Constant){
				Constant leftC = (Constant) leftValue;
				Constant rightC = (Constant) rightValue;
				if(cond instanceof EqExpr){
					if(leftC.equivTo(rightC)){
						return 1;
					} else {
						return 0;
					}
				} else if(cond instanceof NeExpr){
					if(leftC.equivTo(rightC)){
						return 0;
					} else {
						return 1;
					}
				} 
			}
			if(leftValue instanceof NumericConstant && rightValue instanceof NumericConstant){
				NumericConstant leftN = (NumericConstant) leftValue;
				NumericConstant rightN = (NumericConstant) rightValue;
				if(cond instanceof GeExpr){
					if(((IntConstant) leftN.greaterThanOrEqual(rightN)).value == 1){
						return 1;
					} else{
						return 0;
					}
				} else if(cond instanceof GtExpr){
					if(((IntConstant) leftN.greaterThan(rightN)).value == 1){
						return 1;
					} else{
						return 0;
					}
				} else if(cond instanceof LeExpr){
					if(((IntConstant) leftN.lessThanOrEqual(rightN)).value == 1){
						return 1;
					} else{
						return 0;
					}
				} else if(cond instanceof LtExpr){
					if(((IntConstant) leftN.lessThan(rightN)).value == 1){
						return 1;
					} else{
						return 0;
					}
				}
			}
		}
		return -1;
	}
	
	
	private Value lastestValue(Value val, List<Stmt> stmts){
		if(stmts == null){
			return val;
		}
		for(int i = stmts.size()-1; i>=0; i--){
			Stmt stmt = stmts.get(i);
			if(stmt instanceof JAssignStmt){
				JAssignStmt agstmt = (JAssignStmt) stmt;
				Value def = agstmt.getLeftOp();
				if(def.equals(val)){
					Value use = agstmt.getRightOp();
					return use;
				}
			}
		}
		return val;
	}
	
	
	private List<Stmt> extractConditions2CurrentStmt(Stmt cur, ArrayMultiTreeNode<Stmt> node){
		List<Stmt> allStmt = new ArrayList<Stmt>();
		Collection<? extends TreeNode<Stmt>> path = root.path(node);
		for(TreeNode<Stmt> pathNode : path){
			Stmt stmt = pathNode.data();
			allStmt.add(stmt);
		}
		return allStmt;
	}
	
	//we first generate the path whose leaf is either outofloop or loop header
	//we then consider the path whose leaf is a subheader
	private void generatePath(Set<Stmt> subHeaders){
		out.println("Start generating paths...");
		List<TreeNode<Stmt>> allLeaves = new ArrayList<TreeNode<Stmt>>();
		for (TreeNode<Stmt> node : root) {
			if(node.isLeaf()){
				allLeaves.add(node);
			}
		}
		Set<Collection<? extends TreeNode<Stmt>>> allsubPathswithLeaf = new HashSet<Collection<? extends TreeNode<Stmt>>>();
		Set<Collection<? extends TreeNode<Stmt>>> allsubPathswithSubHeader = new HashSet<Collection<? extends TreeNode<Stmt>>>();
		for(int id = 0; id < allLeaves.size(); id++){
			TreeNode<Stmt> leaf = allLeaves.get(id);
			Collection<? extends TreeNode<Stmt>> path = root.path(leaf);
			
			Stack<Stmt> list = new Stack<Stmt>();
			Stmt leafStmt = leaf.data();
			int m_tag = ((IntegerConstantValueTag)leafStmt.getTag("IntegerConstantValueTag")).getIntValue();
			
			for(TreeNode<Stmt> pathnode : path){
				list.add(pathnode.data());
				if(subHeaders.contains(pathnode.data()) && path.size()>1 && !stmts_OutOfLoops.containsKey(m_tag)){
					//the subpath is not an exit path, and this path contains more than 1 node
					Collection<? extends TreeNode<Stmt>> subpath = pathnode.path(leaf);
					if(leafStmt.equals(header)){
//						printTreeH(pathnode,"", true);
						allsubPathswithLeaf.add(subpath);
					}
					else if(subHeaders.contains(leafStmt)){
//						printTreeH(pathnode,"", true);
						allsubPathswithSubHeader.add(subpath);
					}
				}
			}
			boolean isExit = false; //default for non-exit
			//For current loop, there are only two types of path,
			//either jump out of the loop or jump back to the loop header.
			if(stmts_OutOfLoops.containsKey(m_tag)){
				isExit = true;
				paths.add(buildPath(list,isExit,id));
			} else if(leafStmt.equals(header)){
				isExit = false;
				paths.add(buildPath(list,isExit,id));
			}
		}
		out.println("Generated " + paths.size() + " paths");
		Set<List<Stmt>> allReplacedSubPath = mergeSubPath(allsubPathswithLeaf,allsubPathswithSubHeader);
		out.println("There are " + allReplacedSubPath.size() + " subPaths");
		for(int id = 0; id < allLeaves.size(); id++){
			TreeNode<Stmt> leaf = allLeaves.get(id);
			Collection<? extends TreeNode<Stmt>> path = root.path(leaf);
			
			Stack<Stmt> list = new Stack<Stmt>();
			Stmt leafStmt = leaf.data();			
			for(TreeNode<Stmt> pathnode : path){
				list.add(pathnode.data());
			}
			boolean isExit = false; //default for non-exit
			if(subHeaders.contains(leafStmt)){
				for(List<Stmt> subpath : allReplacedSubPath){
					if(subpath.size()>1 && subpath.get(0).equals(leafStmt)){
						for(int i = 1; i < subpath.size(); i++){
							list.add(subpath.get(i));
						}
						if(subpath.get(subpath.size()-1).equals(header)){
							isExit = false;
							paths.add(buildPath(list,isExit,id));
						}
					}
				}
			}
		}
		
	}
	
	private Set<List<Stmt>> mergeSubPath(Set<Collection<? extends TreeNode<Stmt>>> allsubPathswithLeaf,
			Set<Collection<? extends TreeNode<Stmt>>> allsubPathswithSubHeader){
		Set<List<Stmt>> listSubPathswithLeaf = new HashSet<List<Stmt>>();//the tail of the subpath is the loop header
		Set<List<Stmt>> listSubPathswithSubHeader = new HashSet<List<Stmt>>();
		for(Collection<? extends TreeNode<Stmt>> path : allsubPathswithLeaf){
			List<Stmt> stmts = new ArrayList<Stmt>();
			for(TreeNode<Stmt> pathnode : path){
				stmts.add(pathnode.data());
			}
			listSubPathswithLeaf.add(stmts);
		}
		for(Collection<? extends TreeNode<Stmt>> path : allsubPathswithSubHeader){
			List<Stmt> stmts = new ArrayList<Stmt>();
			for(TreeNode<Stmt> pathnode : path){
				stmts.add(pathnode.data());
			}
			listSubPathswithSubHeader.add(stmts);
		}
		Set<List<Stmt>> allReplaced = new HashSet<List<Stmt>>();
		allReplaced.addAll(listSubPathswithLeaf);
		out.println("the subPathwithHeader's size is " + listSubPathswithLeaf.size());
		out.println("the subPathwithSubHeader's size is " + listSubPathswithSubHeader.size());
		for(List<Stmt> subPathwithSubHeader : listSubPathswithSubHeader){
			try{
				Set<List<Stmt>> replaced = replaceSubPath(subPathwithSubHeader, listSubPathswithLeaf,listSubPathswithSubHeader);
				allReplaced.addAll(replaced);
			}catch(Exception | Error e){
				e.printStackTrace();
			}
		}
		return allReplaced;
	}
	
	
	private Set<List<Stmt>> replaceSubPath(List<Stmt> subPathwithSubHeader, 
			Set<List<Stmt>> listSubPathswithLeaf,
			Set<List<Stmt>> listSubPathswithSubHeader){
		Stmt leafStmt = subPathwithSubHeader.get(subPathwithSubHeader.size()-1);
		Set<List<Stmt>> replacedPaths = new HashSet<List<Stmt>>();
		for(List<Stmt> listsubpath : listSubPathswithLeaf){
			if(listsubpath.get(0).equals(leafStmt)){
				List<Stmt> newPath = new ArrayList<Stmt>();
				for(int i = 0; i < subPathwithSubHeader.size()-1; i++){
					newPath.add(subPathwithSubHeader.get(i));
				}
				for(int i = 0; i < listsubpath.size(); i++){
					newPath.add(listsubpath.get(i));
				}
				replacedPaths.add(newPath);
			}
		}
		for(List<Stmt> listsubpath : listSubPathswithSubHeader){
			if(!listsubpath.equals(subPathwithSubHeader) && listsubpath.get(0).equals(leafStmt)){
				Stmt lastStmt = listsubpath.get(listsubpath.size()-1);
				int m_tag = ((IntegerConstantValueTag)lastStmt.getTag("IntegerConstantValueTag")).getIntValue();
				if(listSubPathswithSubHeader.contains(lastStmt)){//the path routes back, skip the circle
					continue;
				} else if(stmts_OutOfLoops.containsKey(m_tag)){ //we don't need to consider extra exit path, it's redundant
					continue;
				} else {
					List<Stmt> newPath = new ArrayList<Stmt>();
					for(int i = 0; i < subPathwithSubHeader.size()-1; i++){
						newPath.add(subPathwithSubHeader.get(i));
					}
					for(int i = 0; i < listsubpath.size(); i++){
						newPath.add(listsubpath.get(i));
					}
					Set<List<Stmt>> replaced = replaceSubPath(newPath, listSubPathswithLeaf, listSubPathswithSubHeader);
					replacedPaths.addAll(replaced);
				}
			}
		}
		return replacedPaths;
	}
	
	
	public void buildDifferentPaths(PrintStream out){
		if(!containErrorHandlinginLoop()){ //just build the normal path, don't need to consider the try-catch
			search4Path(header, root, out);
			generatePath(this.subHeaders);
			printTreeH();
		} else { //consider two types of paths: just normal path and the error-handling path
			//build the normal path
			out.println("Start building normal paths...");
			search4NormalPath(header, root, out);
			generatePath(this.subHeaders);
			printTreeH();
			//build the error-handling path
			//currently we only consider that there is only one data variable is corrupted.
			if(dataVars != null && dataVars.size()>0){
				for(Value dataVar : dataVars){
					//each time, needs to clean the previous tree.
					root = null;
					out.println("Start building error-handling paths...");
					search4ErrorHandlingPath(header, root, out, dataVar);
					generatePath(this.subHeadersWEH);
					printTreeH();
				}
			}
		}
	}
	
	
	private Map<TreeNode<Stmt>, Collection<? extends TreeNode<Stmt>>> subPaths = new HashMap<TreeNode<Stmt>, Collection<? extends TreeNode<Stmt>>>();
	private Map<Stack<Stmt>, Boolean> allPaths = new ConcurrentHashMap<Stack<Stmt>, Boolean>();
	private List<TreeNode<Stmt>> needReplaceLeaves = new ArrayList<TreeNode<Stmt>>();
	private void generatePaths(){
		if(this.root.isLeaf()){
			return;
		}
		List<TreeNode<Stmt>> allLeaves = new ArrayList<TreeNode<Stmt>>();
		for (TreeNode<Stmt> node : root) {
			if(node.isLeaf()){
				allLeaves.add(node);
			}
		}
		for(int id = 0; id < allLeaves.size(); id++){
			TreeNode<Stmt> leaf = allLeaves.get(id);
			Stmt stmt = leaf.data();
//			out.println("-------for testing start--------");
			int m_tag = ((IntegerConstantValueTag)stmt.getTag("IntegerConstantValueTag")).getIntValue();
//			if(m_tag == 269){
//				Collection<? extends TreeNode<Stmt>> path = this.root.path(leaf);
//				for(TreeNode<Stmt> pathnode : path){
//					int tagNum = ((IntegerConstantValueTag)pathnode.data().getTag("IntegerConstantValueTag")).getIntValue();
//					out.print(tagNum + " ");
//				}
//				out.println();
//			}
//			out.println("-------for testing end--------");
			if(stmt.equals(this.header)){//if it jump to the loop header, then this path belongs to this loop, otherwise, it belongs to inner loop
				Collection<? extends TreeNode<Stmt>> path = this.root.path(leaf);
				Stack<Stmt> list = new Stack<Stmt>();
				for(TreeNode<Stmt> pathnode : path){
					list.add(pathnode.data());
				}
				allPaths.put(list, false);//false for non-exit path
			} else if(stmts_OutOfLoops.containsKey(m_tag)){//if it jump out of loop
				Collection<? extends TreeNode<Stmt>> path = this.root.path(leaf);
				Stack<Stmt> list = new Stack<Stmt>();
				for(TreeNode<Stmt> pathnode : path){
					list.add(pathnode.data());
				}
				allPaths.put(list, true);//false for non-exit path
			} else {
				boolean hasSubCircle = false;
				Collection<? extends TreeNode<Stmt>> path = this.root.path(leaf.parent());// to the parent node
				for(TreeNode<Stmt> pathnode : path){
					if(pathnode.data().equals(leaf.data())){
						hasSubCircle = true;
						Collection<? extends TreeNode<Stmt>> subpath = pathnode.path(leaf);// to the child node
						subPaths.put(pathnode, subpath);
						break;
					}
				}
				if(!hasSubCircle){
					Iterator<TreeNode<Stmt>> iterator = this.root.iterator();
					while (iterator.hasNext()) {
						TreeNode<Stmt> node = iterator.next();
//						if(!node.isLeaf()){
//							m_tag = ((IntegerConstantValueTag)node.data().getTag("IntegerConstantValueTag")).getIntValue();
//							if(m_tag == 269){
//								out.println();
//							}
//						}
						if(!node.isLeaf() && node.data().equals(leaf.data())){
							//need replace.
							needReplaceLeaves.add(leaf);
						}
					}
				}
			}
		}
		out.println("before\tallPaths.size = " + allPaths.size() + "\tsubPaths.size = " + subPaths.size() + "\tneedReplaceLeaves.size = " + needReplaceLeaves.size());
		if(allPaths.size() > 100 
				&& containErrorHandlinginLoop() 
				&& (subPaths.size() > 0 || needReplaceLeaves.size()>0)){
			out.println("There is error handling code in the loop");
		} else {
			
		}
		
		while(generateMorePaths()){//if it is changed
			//stop when all the path are generated
		}
		out.println("add subCirclePath\tallPaths.size = " + allPaths.size() + "\tsubPaths.size = " + subPaths.size());
		while(generateMorePaths2()){//if it is changed
			//stop when all the path are generated
		}
		out.println("add replaceLeaves\tallPaths.size = " + allPaths.size() + "\tneedReplaceLeaves.size = " + needReplaceLeaves.size());
		out.println("get out of while loop...");
		Iterator<Entry<Stack<Stmt>, Boolean>> pathIt = allPaths.entrySet().iterator();
		Entry<Stack<Stmt>, Boolean> pathEntry = null;
		int nonPathID = 0;
		while(pathIt.hasNext()){
			pathEntry = pathIt.next();
			Stack<Stmt> lists = pathEntry.getKey();
			paths.add(buildPath(lists,pathEntry.getValue(),nonPathID));
			nonPathID++;
		}
	}
	
	private boolean containErrorHandlinginLoop(){
		for(Stmt stmt : stmts){
			if(isErrorHandlingStmt(stmt)){
				return true;
			}
		}
		return false;
	}
	
	private boolean isErrorHandlingStmt(Stmt stmt){
		if(stmt instanceof IdentityStmt){
			if( ((IdentityStmt) stmt).getRightOp() instanceof CaughtExceptionRef){
				return true;
			}
		}
		return false;
	}
	
	private boolean generateMorePaths(){
		Iterator<Entry<Stack<Stmt>, Boolean>> pathIt = allPaths.entrySet().iterator();
		Entry<Stack<Stmt>, Boolean> pathEntry = null;
		boolean hasChanged = false;
		while(pathIt.hasNext()){
			pathEntry = pathIt.next();
			Stack<Stmt> lists = pathEntry.getKey();
			Stmt replaceStmt = null;
			for(Stmt stmt : lists){
				if(subPaths.containsKey(stmt)){ //has the target stmt, which can be replaced
					Collection<? extends TreeNode<Stmt>> subpath = subPaths.get(stmt);
					for(TreeNode<Stmt> subpathNode : subpath){
						if(!lists.contains(subpathNode.data())){//current lists doesn't contain at least one subpath stmt
							replaceStmt = stmt;
							break;
						}
					}
					if(replaceStmt != null)
						break;
				}
			}
			if(replaceStmt != null){
				hasChanged = true;
				int index = lists.indexOf(replaceStmt);//first match
				Stack<Stmt> listNew = new Stack<Stmt>();
				listNew.addAll(lists.subList(0, index));
				Collection<? extends TreeNode<Stmt>> subpath = subPaths.get(replaceStmt);
				for(TreeNode<Stmt> node : subpath){
					listNew.add(node.data());
				}
				if(index+1 <= lists.size()-1) 
					listNew.addAll(lists.subList(index+1, lists.size()));
				allPaths.put(listNew, pathEntry.getValue());
			}
		}
		return hasChanged;
	}
	
	private boolean generateMorePaths2(){
		Iterator<Entry<Stack<Stmt>, Boolean>> pathIt = allPaths.entrySet().iterator();
		Entry<Stack<Stmt>, Boolean> pathEntry = null;
		boolean hasChanged = false;
		while(pathIt.hasNext()){
			pathEntry = pathIt.next();
			Stack<Stmt> lists = pathEntry.getKey();
			for(TreeNode<Stmt> replaceLeaf : needReplaceLeaves){
				Stmt replaceStmt = replaceLeaf.data();
				if(lists.contains(replaceStmt)){
					Collection<? extends TreeNode<Stmt>> prefixpath = this.root.path(replaceLeaf);
					if(!lists.containsAll(prefixpath)){
						hasChanged = true;
						int index = lists.indexOf(replaceStmt);//first match
						Stack<Stmt> listNew = new Stack<Stmt>();
						for(TreeNode<Stmt> node : prefixpath){
							listNew.add(node.data());
						}
						if(index+1 <= lists.size()-1) 
							listNew.addAll(lists.subList(index+1, lists.size()));
						allPaths.put(listNew, pathEntry.getValue());
					}
				}
			}
		}
		return hasChanged;
	}

	

	private void addStmtOutOfLoops(Stmt s) {
		if (stmts_OutOfLoops == null) {
			stmts_OutOfLoops = new HashMap<Integer,Stmt>();  
			//may be extend this into a TreeSet inherited class (need extends because  java.lang.ClassCastException: soot.jimple.internal.JAssignStmt cannot be cast to java.lang.Comparable)
		}
		
		int m_tag=((IntegerConstantValueTag) s.getTag("IntegerConstantValueTag")).getIntValue(); 
		if(!stmts_OutOfLoops.containsKey(m_tag)){
			stmts_OutOfLoops.put(m_tag, s);
		}
		

	}

	private void addStmtExceptions(Stmt s) {
		if (stmts_exceptions == null) {
			stmts_exceptions = new HashMap<Integer,ExceptionStmt>();  
			//may be extend this into a TreeSet inherited class (need extends because  java.lang.ClassCastException: soot.jimple.internal.JAssignStmt cannot be cast to java.lang.Comparable)
		}
		
		int m_tag=((IntegerConstantValueTag) s.getTag("IntegerConstantValueTag")).getIntValue(); 
		if(!stmts_exceptions.containsKey(m_tag)){
			ExceptionStmt stmt_exp=new ExceptionStmt((JIdentityStmt)s);
			stmts_exceptions.put(m_tag, stmt_exp);
		}
		

	}
	
	private void getAll(Stmt header, Stmt exit) {
		Map<Unit, Integer> states = new HashMap<Unit, Integer>();
		Stack<Stmt> stack = new Stack<Stmt>();

		Stmt s = header;
		stack.push(s);
		if (!states.containsKey(s)) {
			states.put(s, 1); // mark as visited
		}

		Stmt prev;
		while (!stack.isEmpty()) {
			prev = s;
			List<Unit> units = g.getSuccsOf(s);

			for (Unit u : units) {
				if (!states.containsKey(u) || states.get(u) != 1) {
					s = (Stmt) u;
				} else {
					continue;
				}
			}

			if (!s.equals(prev)) {
				stack.push(s);
				states.put(s, 1);
			}

			if (s == exit) {
				// find the path
				while (!stack.isEmpty()) {
					// out.println();
					stack.pop();
				}
				// out.println("**END OF LOOP TRAVERSE*****************");

			}

			if (s.equals(prev) && s != exit) {
				// out.println("Need more algorithm on deep search");
				break;
			}
		}
	}


	//If two stmts in the same loop path is opposite, and the variable is not changed between the two stmts, 
	//Then, this loop path is not valid.
	public void pathPrune() {
		List<Integer> pruneIDs = new ArrayList<Integer>();
		boolean pruned = false;
		for(int index = 0; index < paths.size(); index++){
			List<Condition> condlist = paths.get(index).getconditions();
			if(condlist == null || (condlist != null && condlist.size() < 2))
				return;
			pruned = false;
			for(int i = 0; i < condlist.size()-1; i++){
				if(pruned == true)
					break;
				for(int j = i + 1; j < condlist.size(); j++){
					if(oppositeCondition(condlist.get(i), condlist.get(j))){
						int startStmtId = LoopUtils.stmtTag(condlist.get(i).stmt);
						int endStmtId = LoopUtils.stmtTag(condlist.get(j).stmt);
						Value var = condlist.get(i).cond.getOp1();
						if(isUnchanged(var, startStmtId, endStmtId, paths.get(index).getpathStmt())){
							pruneIDs.add(index);
							pruned = true;
							break;
						}
					}
				}
			}
		}
		for(int i = 0; i < pruneIDs.size(); i++){
			paths.remove(pruneIDs.get(i)-i);
		}	
	}
	
	boolean isUnchanged(Value var, int startId, int endId, List<Stmt> stmts){
		//if(startId == -1 || endId == -1)
		//	return false;
		for(Stmt stmt : stmts){
			int stmtID = LoopUtils.stmtTag(stmt);
			if(stmtID <= startId)
				continue;
			else if(stmtID >= endId)
				break;
			else{
				List<ValueBox> defs = stmt.getDefBoxes();
				for(ValueBox def : defs){
					if(def.getValue().equals(var)){
						return false;
					}
				}
			}
		}
		return true;
	}
	
	boolean isUnchanged(Value var, List<LoopPath> paths){
		for(LoopPath path : paths){
			List<Stmt> pathStmts = path.getpathStmt();
			int varAssignedNum = 0;
			for(Stmt stmt: pathStmts){
				if(!(stmt instanceof JGotoStmt)){
					//out.println(stmt.toString());
					List<ValueBox> defs = stmt.getDefBoxes();
					for(ValueBox def : defs){
						if(def.getValue().equals(var)){
							varAssignedNum++;
						}
					}
				}
			}
			if(varAssignedNum > 2) //loop header and loop tail
				return false;
		}
		return true;
	}
	
	boolean oppositeCondition(Condition cond1, Condition cond2){
		if(cond1.equals(cond2))
			return false;
		else if(cond1.cond.getOp1().equals(cond2.cond.getOp1())
				&& cond1.cond.getOp2().equals(cond2.cond.getOp2())){
			String symbol1 = cond1.cond.getSymbol().trim();
			String symbol2 = cond2.cond.getSymbol().trim();
			/*if(symbol1.equals(">=") && symbol2.equals("<"))
				return true;
			if(symbol1.equals("<=") && symbol2.equals(">"))
				return true;
			if(symbol1.equals("=") && (symbol2.equals(">") || symbol2.equals("<")))
				return true;
			if(symbol1.equals(">") && (symbol2.equals("<") || symbol2.equals("<=") || symbol2.equals("=")))
				return true;
			if(symbol1.equals("<") && (symbol2.equals(">") || symbol2.equals(">=") || symbol2.equals("=")))
				return true;*/
			if(symbol1.equals("!=") && symbol2.equals("=="))
				return true;
		}
		return false;
	}
}

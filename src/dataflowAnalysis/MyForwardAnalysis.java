package dataflowAnalysis;


import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.G;
import soot.Local;
import soot.PrimType;
import soot.RefType;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.UnitBox;
import soot.Value;
import soot.ValueBox;
import soot.jimple.ConcreteRef;
import soot.jimple.Constant;
import soot.jimple.DefinitionStmt;
import soot.jimple.FieldRef;
import soot.jimple.IdentityRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.ParameterRef;
import soot.jimple.Ref;
import soot.jimple.Stmt;
import soot.jimple.internal.AbstractDefinitionStmt;
import soot.jimple.internal.JArrayRef;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JInstanceFieldRef;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JNewExpr;
import soot.jimple.internal.JSpecialInvokeExpr;
import soot.tagkit.LineNumberTag;
import soot.tagkit.SignatureTag;
import soot.tagkit.Tag;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import utils.Pair;


public class MyForwardAnalysis extends ForwardFlowAnalysis {

	public MyForwardAnalysis(UnitGraph graph) {
		super(graph);
		// TODO Auto-generated constructor stub
//		throw new RuntimeException("Don't use this Constructor!");
		 doAnalysis();
	}

	public MyForwardAnalysis(UnitGraph graph, Set<Unit> exits) {
		super(graph);
		doAnalysis();
	}
	

	@Override
	protected void flowThrough(Object in, Object d, Object out) {
		copy(in, out);

		addPair(in, d, out);

	}
	
	
	protected void addPair(Object src, Object n, Object dest) {
		Unit node = (Unit) n;
		//out.println("print unit n =" + n.toString());
		FlowSet srcSet = (FlowSet) src;
		FlowSet destSet = (FlowSet) dest;		
		Set<Object> defs = new HashSet<Object>();
		Set<Object> uses = new HashSet<Object>();
		
//		G.v().out.println("node = " + node);
		
		for (ValueBox box : node.getDefBoxes()) {
			Value value = box.getValue();
			if (value instanceof Local /*|| value instanceof Ref*/ || value instanceof Constant){
				defs.add(value);
			} else if(value instanceof FieldRef){
				defs.add(((FieldRef)value).getField());
			} else if (value instanceof InstanceFieldRef){
				defs.add(((InstanceFieldRef)value).getType());
			}
		}
		for (ValueBox box : node.getUseBoxes()) {
			Value value = box.getValue();
//			G.v().out.println("value = " + value + " type = " + value.getClass());
			if(value instanceof FieldRef){
//				G.v().out.println("value = " + value + " type = " + value.getClass());
				uses.add(((FieldRef)value).getField());
//				G.v().out.println("field = " + ((FieldRef)value).getField());
			} else if (value instanceof IdentityRef){
//				G.v().out.println("value " + value + " is IdentityRef");
				if(value instanceof ParameterRef){
//					G.v().out.println("value " + value + " type is " + ((ParameterRef)value).getType());
					uses.add(value);
				}
			} else if ( value instanceof Local
					/*|| (value instanceof Ref && !(value instanceof JArrayRef))*/ 
					|| value instanceof Constant 
					|| value instanceof JNewExpr ){ //JNewExpr for new object
				uses.add(value);
				//out.println("Use: local, ref, constant--"+value.toString()+"----type-"+value.getType().toString()+" class:--"+value.getClass().toString());	
			} else if(value instanceof JSpecialInvokeExpr){
				JSpecialInvokeExpr spExpr = (JSpecialInvokeExpr) value;
				//G.v().out.println("Use: not local, ref, constant--"+value.toString()+"----type-"+value.getType().toString()+" class:--"+value.getClass().toString());	
				Set<Value> args = new HashSet<Value>();
				for(Object arg : spExpr.getArgs()){
					uses.add((Value)arg);
					args.add((Value)arg);
				}
				for(Object vb : spExpr.getUseBoxes()){
					Value val = ((ValueBox)vb).getValue();
					if(!args.contains(val)){
						defs.add(val);
					}
				}
			} else if(value instanceof InstanceInvokeExpr){
				InstanceInvokeExpr expr = (InstanceInvokeExpr)value;
				Value base = expr.getBase();
				uses.add(base);
				for(Object arg : expr.getArgs()){
					uses.add((Value)arg);
				}
			}
		}

		
		for(Object v_def : defs){
			for(Object v_use : uses){
				destSet.add(new Pair(v_def, v_use)); //defined value(v_def) is impacted by used value(v_use)
			}
		}
	}
	
	

	@Override
	protected Object newInitialFlow() {
		// TODO Auto-generated method stub
		return new ArraySparseSet();
	}

	@Override
	protected Object entryInitialFlow() {
		// TODO Auto-generated method stub
		return new ArraySparseSet();
	}

	@Override
	protected void merge(Object in1, Object in2, Object out) {
		// TODO Auto-generated method stub
		FlowSet inSet1 = (FlowSet) in1;
		FlowSet inSet2 = (FlowSet) in2;
		FlowSet outSet = (FlowSet) out;
		inSet1.union(inSet2, outSet);
	}

	@Override
	protected void copy(Object source, Object dest) {
		FlowSet srcSet = (FlowSet) source;
		FlowSet destSet = (FlowSet) dest;
		srcSet.copy(destSet);
	}

	private void kill(Object src, Object n, Object dest) {
		Unit node=(Unit)n;
		FlowSet srcSet = (FlowSet)src;
		FlowSet destSet = (FlowSet) dest;
		
		for (ValueBox box : node.getDefBoxes()) {
			Value value = box.getValue();
			if (value instanceof Local)
				destSet.remove((Local) value);
		}
	}

	private void gen(Object dest, Object n) {
		Unit node=(Unit)n;
		FlowSet destSet = (FlowSet) dest;
		for (ValueBox box : node.getUseBoxes()) {
			Value value = box.getValue();
			if (value instanceof Local)
				destSet.add((Local) value);
		}
	}
}

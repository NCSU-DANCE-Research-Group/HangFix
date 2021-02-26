package utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.G;
import soot.Local;
import soot.SootClass;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.tagkit.IntegerConstantValueTag;
import soot.tagkit.LineNumberTag;
import soot.tagkit.Tag;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.util.Chain;
import soot.coffi.field_info;
import soot.coffi.element_value;
import soot.coffi.method_info;
import soot.coffi.Util;
import soot.jimple.FieldRef;
import soot.jimple.Ref;
import soot.jimple.Stmt;
import soot.jimple.toolkits.pointer.representations.ReferenceVariable;


public class MethodTagTransformer extends BodyTransformer {

	@Override
	protected void internalTransform(Body body, String phaseName, Map options) {
		// TODO Auto-generated method stub
		String classname = body.getMethod().getDeclaringClass().getName();
		// use signature instead to avoid same-name functions
		String methodname = body.getMethod().getSubSignature(); 
		

		G.v().out.println("MethodTagTransformer: "+classname + ": " + methodname);
		
		UnitGraph g = new ExceptionalUnitGraph(body); //control-flow graph inside this method
		G.v().out.println("MethodTagTransformer Size: "+g.size());
		
		
		Iterator<Unit> it=g.iterator(); //iterate all statements of the method body 
		int i=0;
		while(it.hasNext()){
			Unit u=it.next();
			u.addTag(new IntegerConstantValueTag(i++)); // add tag is necessary
		}
		
		G.v().out.println("MethodTagTransformer Tags: "+i);
		
		
		//print method
		if(isPrint){
			PrintStream out;
			try{
				String fname = ConfUtils.getFileName(classname, methodname,".txt");
				G.v().out.println(fname);
				out = new PrintStream(new FileOutputStream(fname));
				
				Iterator<Unit> it_p = g.iterator();
				while(it_p.hasNext()){
					Unit u = it_p.next();
					out.println(((IntegerConstantValueTag) (u.getTag("IntegerConstantValueTag"))).getIntValue()
							+": "+u.toString() + "	" + u.getClass().toString());
				}	
			} catch(FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public void setMethodPrint(boolean isPrint){
		this.isPrint=isPrint;
	}
	
	boolean isPrint=false;
}

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

public class ConfUtils {

	static public String getFileName(String classname, String methodname, String appendix) {
		String c_name = classname.replaceAll(prefix, ""); // remove prefix
		String m_name = methodname.substring(methodname.indexOf(" ") + 1); //start after return name
		m_name = m_name.replaceAll(prefix, ""); // remove prefix
		return outputDir + c_name + "_" + m_name + appendix;
	}
	
	static public String getFileName(String classname, String methodname, String predix,
			String appendix) {
		// String c_name=classname.substring(classname.lastIndexOf(".")+1);
		// String m_name=methodname.substring(methodname.indexOf(" ")+1);
		// m_name.replaceAll(prefix, "");

		String c_name = classname.replaceAll(prefix, ""); // remove prefix
		String m_name = methodname.substring(methodname.indexOf(" ") + 1); //start after return name
		m_name = m_name.replaceAll(prefix, ""); // remove prefix
		return outputDir + predix + c_name + "_" + m_name + appendix;
	}


	static String conf_name = "conf";
	static String outputDir;
	static String prefix;
	static {	
		File filter = new File(conf_name);
		try {
			BufferedReader br = new BufferedReader(new FileReader(filter));
			String s = br.readLine();
			String substr = s.substring(s.indexOf("=") + 1);
			outputDir = substr;
			s = br.readLine();
			if(s!=null){
				substr = s.substring(s.indexOf("=") + 1);
				prefix = substr;
				System.out.println("prefix:"+prefix);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

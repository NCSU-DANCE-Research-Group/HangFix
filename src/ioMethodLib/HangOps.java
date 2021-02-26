package ioMethodLib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HangOps {
	public static HashMap<String, List<String>> classMethodMap = new HashMap<>();
	
	static{
		String className = "lucene772.MockInflater";
		String methodName = "int inflate(byte[])";
		List<String> methods = new ArrayList<String>();
		methods.add(methodName);
		classMethodMap.put(className, methods);
	}
	static{
		String className = "hive5235.MockInflater";
		String methodName = "int inflate(byte[],int,int)";
		List<String> methods = new ArrayList<String>();
		methods.add(methodName);
		classMethodMap.put(className, methods);
	}
	static{
		String className = "java.util.zip.Inflater";
		String methodName = "int inflate(byte[])";
		String methodName2 = "int inflate(byte[],int,int)";
		List<String> methods = new ArrayList<String>();
		methods.add(methodName);
		methods.add(methodName2);
		classMethodMap.put(className, methods);
	}
	
	
}

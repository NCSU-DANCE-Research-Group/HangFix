package ioMethodLib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import soot.G;
import soot.SootClass;
import soot.SootMethod;
import soot.Value;
import soot.jimple.ConditionExpr;
import soot.jimple.EqExpr;
import soot.jimple.GeExpr;
import soot.jimple.GtExpr;
import soot.jimple.IntConstant;
import soot.jimple.LeExpr;
import soot.jimple.LongConstant;
import soot.jimple.LtExpr;
import soot.jimple.NeExpr;
import soot.jimple.NumericConstant;
import soot.jimple.internal.JEqExpr;
import soot.jimple.internal.JGeExpr;
import soot.jimple.internal.JGtExpr;
import soot.jimple.internal.JLeExpr;
import soot.jimple.internal.JLtExpr;
import soot.jimple.internal.JNeExpr;
import yarn2905.AggregatedLogFormat;

public class IOFuncRetRange {
//	public class FuncRetEntry{
//		public FuncRetEntry(String methodStr2, List<ConditionExpr> condList2) {
//			methodStr = methodStr2;
//			condList = condList2;
//		}
//		String methodStr;
//		List<ConditionExpr> condList;
//	}
	
	public static int BOUND_CHECKING = 1;
	public static int BOUND_UPDATE = 2;
	public static int INDEX_FORWARD = 3;
	public static int INDEX_BACKWARD = 4;
	public static int INDEX_RESET = 5;
	
	public class IndexBoundChecking {
		boolean hasBoundCheck;
		boolean hasBoundUpdate;
		boolean hasIndexForward;
		boolean hasIndexBackward;
		boolean hasIndexReset;
		public IndexBoundChecking(boolean hasBoundCheck,
				boolean hasBoundUpdate,
				boolean hasIndexForward,
				boolean hasIndexBackward,
				boolean hasIndexReset){
			this.hasBoundCheck = hasBoundCheck;
			this.hasBoundUpdate = hasBoundUpdate;
			this.hasIndexForward = hasIndexForward;
			this.hasIndexBackward = hasIndexBackward;
			this.hasIndexReset = hasIndexReset;
		}
	}
	
	public static class IndexBoundAPIs {
		List<String> boundCheckAPI;
		List<String> boundUpdateAPI;
		List<String> indexForwardAPI;
		List<String> indexBackwardAPI;
		List<String> indexResetAPI;
		public IndexBoundAPIs(List<String> boundCheckAPI,
				List<String> boundUpdateAPI,
				List<String> indexForwardAPI,
				List<String> indexBackwardAPI,
				List<String> indexResetAPI){
			this.boundCheckAPI = boundCheckAPI;
			this.boundUpdateAPI = boundUpdateAPI;
			this.indexForwardAPI = indexForwardAPI;
			this.indexBackwardAPI = indexBackwardAPI;
			this.indexResetAPI = indexResetAPI;
		}
	}
	
	public static Map<String, Map<String, List<ConditionExpr>>> ioFuncRetLib = new HashMap<String, Map<String, List<ConditionExpr>>>();
	public static Map<String, Map<String, Integer>> ioFuncArgLib = new HashMap<String, Map<String, Integer>>(); 
	public static Map<String, Map<String, String>> ioFuncErrMsgLib = new HashMap<String, Map<String, String>>();
	
	public static Map<String, IndexBoundAPIs> invokedAPIs = new HashMap<String, IndexBoundAPIs>();
	
	static
	{
		String className = "java.io.InputStream";
		Map<String, List<ConditionExpr>> funcList = new HashMap<String, List<ConditionExpr>>();
		Map<String, Integer> argList = new HashMap<String, Integer>();
		Map<String, String> errMsgList = new HashMap<String, String>();
		{
			{
				String methodStr = "long skip(long)";
				List<ConditionExpr> condList = new ArrayList<ConditionExpr>();
				ConditionExpr condexpr = new JEqExpr(LongConstant.v(0), LongConstant.v(0));//the first parameter is not used
				condList.add(condexpr);
				ConditionExpr condexpr2 = new JEqExpr(LongConstant.v(-1), LongConstant.v(-1));//the first parameter is not used
				condList.add(condexpr2);
//				ConditionExpr condexpr2 = new JEqExpr(LongConstant.v(-1), LongConstant.v(-1));//the first parameter is not used
//				condList.add(condexpr2);
				funcList.put(methodStr, condList);
				argList.put(methodStr, -1);//the corruption does not happen on the arg, thus, using -1
				errMsgList.put(methodStr, "InputStream is corrupted, causing loop stride always be 0/-1 when invoking skip(long) operation.");
			}
			{
				String methodStr = "int read(byte[],int,int)";
				List<ConditionExpr> condList = new ArrayList<ConditionExpr>();
				ConditionExpr condexpr = new JEqExpr(IntConstant.v(0), IntConstant.v(0));//the first parameter is not used
				condList.add(condexpr);
				funcList.put(methodStr, condList);
				argList.put(methodStr, 2);//the 2nd(3rd) arg can be potentially corrupted.
				errMsgList.put(methodStr, "The 3rd argument is zero, causing loop stride always be 0 when METHOD invokes read(byte[],int,int) operation.");
			}
			{
				String methodStr = "int read(byte[])";
				List<ConditionExpr> condList = new ArrayList<ConditionExpr>();
				ConditionExpr condexpr = new JEqExpr(IntConstant.v(0), IntConstant.v(0));//the first parameter is not used
				condList.add(condexpr);
				funcList.put(methodStr, condList);
				argList.put(methodStr, 0);//the 0th arg can be potentially corrupted.
				errMsgList.put(methodStr, "The parameter is corrupted, causing loop stride always be 0 when METHOD invokes read(byte[]) operation on a zero-size array.");
			}
			
		}
		ioFuncRetLib.put(className, funcList);
		ioFuncArgLib.put(className, argList);
		ioFuncErrMsgLib.put(className, errMsgList);
	}
	static 
	{
		String className = "java.io.RandomAccessFile";
		Map<String, String> errMsgList = new HashMap<String, String>();
		List<String> boundCheckAPI = new ArrayList<String>();
		List<String> boundUpdateAPI = new ArrayList<String>();
		List<String> indexForwardAPI = new ArrayList<String>();
		List<String> indexBackwardAPI = new ArrayList<String>();
		List<String> indexResetAPI = new ArrayList<String>();
		{
			boundCheckAPI.add("boolean isEOF()");
		}
		{
			indexForwardAPI.add("int read()");
			indexForwardAPI.add("int readBytes(byte[],int,int)");
			indexForwardAPI.add("int read(byte[],int,int)");
			indexForwardAPI.add("int read(byte[])");
			indexForwardAPI.add("void readFully(byte[])");
			indexForwardAPI.add("void readFully(byte[],int,int)");
			indexForwardAPI.add("boolean readBoolean()");
			indexForwardAPI.add("byte readByte()");
			indexForwardAPI.add("int readUnsignedByte()");
			indexForwardAPI.add("short readShort()");
			indexForwardAPI.add("int readUnsignedShort()");
			indexForwardAPI.add("char readChar()");
			indexForwardAPI.add("int readInt()");
			indexForwardAPI.add("long readLong()");
			indexForwardAPI.add("float readFloat()");
			indexForwardAPI.add("double readDouble()");
			indexForwardAPI.add("String readLine()");
			indexForwardAPI.add("String readUTF()");
		}
		{
			indexResetAPI.add("void seek(long)");
		}
		//currently, we only consider the forward api, other cases will be considered later...
		errMsgList.put("FORWARD", "Data corruption causes exception, which then causes LOOP_INDEX_FORWARD API skipped.");
		ioFuncErrMsgLib.put(className, errMsgList);
		invokedAPIs.put(className, new IndexBoundAPIs(boundCheckAPI,boundUpdateAPI,indexForwardAPI,indexBackwardAPI,indexResetAPI));
	}
	static 
	{
		String className = "java.io.DataInput";
		List<String> boundCheckAPI = new ArrayList<String>();
		List<String> boundUpdateAPI = new ArrayList<String>();
		List<String> indexForwardAPI = new ArrayList<String>();
		List<String> indexBackwardAPI = new ArrayList<String>();
		List<String> indexResetAPI = new ArrayList<String>();
		{
			indexForwardAPI.add("void readFully(byte[])");
			indexForwardAPI.add("void readFully(byte[],int,int)");
			indexForwardAPI.add("boolean readBoolean()");
			indexForwardAPI.add("byte readByte()");
			indexForwardAPI.add("int readUnsignedByte()");
			indexForwardAPI.add("short readShort()");
			indexForwardAPI.add("int readUnsignedShort()");
			indexForwardAPI.add("char readChar()");
			indexForwardAPI.add("int readInt()");
			indexForwardAPI.add("long readLong()");
			indexForwardAPI.add("float readFloat()");
			indexForwardAPI.add("double readDouble()");
			indexForwardAPI.add("String readLine()");
			indexForwardAPI.add("String readUTF()");
		}
		invokedAPIs.put(className, new IndexBoundAPIs(boundCheckAPI,boundUpdateAPI,indexForwardAPI,indexBackwardAPI,indexResetAPI));
	}
	static{
		String className = "java.io.File";
		List<String> boundCheckAPI = new ArrayList<String>();
		List<String> boundUpdateAPI = new ArrayList<String>();
		List<String> indexForwardAPI = new ArrayList<String>();
		List<String> indexBackwardAPI = new ArrayList<String>();
		List<String> indexResetAPI = new ArrayList<String>();
		{
			boundCheckAPI.add("boolean exists()");
		}
		invokedAPIs.put(className, new IndexBoundAPIs(boundCheckAPI,boundUpdateAPI,indexForwardAPI,indexBackwardAPI,indexResetAPI));
	}
	static{
		String className = "mapreduce6991.MockShellCommandExecutor";
		List<String> boundCheckAPI = new ArrayList<String>();
		List<String> boundUpdateAPI = new ArrayList<String>();
		List<String> indexForwardAPI = new ArrayList<String>();
		List<String> indexBackwardAPI = new ArrayList<String>();
		List<String> indexResetAPI = new ArrayList<String>();
		{
			indexForwardAPI.add("void execute()");
		}
		invokedAPIs.put(className, new IndexBoundAPIs(boundCheckAPI,boundUpdateAPI,indexForwardAPI,indexBackwardAPI,indexResetAPI));
	}
	static
	{
		String className = "hadoop15088.BufferedInputStream";
		Map<String, List<ConditionExpr>> funcList = new HashMap<String, List<ConditionExpr>>();
		Map<String, Integer> argList = new HashMap<String, Integer>();
		Map<String, String> errMsgList = new HashMap<String, String>();
		{
			{
				String methodStr = "long skip(long)";
				List<ConditionExpr> condList = new ArrayList<ConditionExpr>();
				ConditionExpr condexpr = new JEqExpr(LongConstant.v(0), LongConstant.v(0));//the first parameter is not used
				condList.add(condexpr);
//				ConditionExpr condexpr2 = new JEqExpr(LongConstant.v(-1), LongConstant.v(-1));//the first parameter is not used
//				condList.add(condexpr2);
				funcList.put(methodStr, condList);
				argList.put(methodStr, -1);//the corruption does not happen on the arg, thus, using -1
				errMsgList.put(methodStr, "BufferedInputStream is corrupted, causing loop stride always be 0 when invoking skip(long) operation.");
			}
		}
		ioFuncRetLib.put(className, funcList);
		ioFuncArgLib.put(className, argList);
		ioFuncErrMsgLib.put(className, errMsgList);
	}
	static
	{
		String className = "compress87.ZipArchiveInputStream";
		Map<String, List<ConditionExpr>> funcList = new HashMap<String, List<ConditionExpr>>();
		Map<String, Integer> argList = new HashMap<String, Integer>();
		Map<String, String> errMsgList = new HashMap<String, String>();
		{
			{
				String methodStr = "int read(byte[],int,int)";
				List<ConditionExpr> condList = new ArrayList<ConditionExpr>();
				ConditionExpr condexpr = new JEqExpr(IntConstant.v(0), IntConstant.v(0));//the first parameter is not used
				condList.add(condexpr);
				funcList.put(methodStr, condList);
				argList.put(methodStr, -1);//the corruption does not happen on the arg, thus, using -1
				errMsgList.put(methodStr, "ZipArchiveInputStream is corrupted, causing loop stride always be 0 when invoking read(byte[],int,int) operation.");
			}
		}
		ioFuncRetLib.put(className, funcList);
		ioFuncArgLib.put(className, argList);
		ioFuncErrMsgLib.put(className, errMsgList);
	}
	static
	{
		String className = "mapreduce6990.MockFileInputStream";
		Map<String, List<ConditionExpr>> funcList = new HashMap<String, List<ConditionExpr>>();
		Map<String, Integer> argList = new HashMap<String, Integer>();
		Map<String, String> errMsgList = new HashMap<String, String>();
		{
			{
				String methodStr = "long skip(long)";
				List<ConditionExpr> condList = new ArrayList<ConditionExpr>();
				ConditionExpr condexpr = new JEqExpr(LongConstant.v(0), LongConstant.v(0));//the first parameter is not used
				condList.add(condexpr);
//				ConditionExpr condexpr2 = new JEqExpr(LongConstant.v(-1), LongConstant.v(-1));//the first parameter is not used
//				condList.add(condexpr2);
				funcList.put(methodStr, condList);
				argList.put(methodStr, -1);//the corruption does not happen on the arg, thus, using -1
				errMsgList.put(methodStr, "InputStream is corrupted, causing loop stride always be 0 when invoking skip(long) operation.");
			}
		}
		ioFuncRetLib.put(className, funcList);
		ioFuncArgLib.put(className, argList);
		ioFuncErrMsgLib.put(className, errMsgList);
	}
	static
	{
		String className = "java.io.Reader";
		Map<String, List<ConditionExpr>> funcList = new HashMap<String, List<ConditionExpr>>();
		Map<String, Integer> argList = new HashMap<String, Integer>();
		Map<String, String> errMsgList = new HashMap<String, String>();
		{
			{
				String methodStr = "long skip(long)";
				List<ConditionExpr> condList = new ArrayList<ConditionExpr>();
				ConditionExpr condexpr = new JEqExpr(LongConstant.v(0), LongConstant.v(0));//the first parameter is not used
				condList.add(condexpr);
//				ConditionExpr condexpr2 = new JEqExpr(LongConstant.v(-1), LongConstant.v(-1));//the first parameter is not used
//				condList.add(condexpr2);
				funcList.put(methodStr, condList);
				argList.put(methodStr, -1);//the corruption does not happen on the arg, thus, using -1
				errMsgList.put(methodStr, "Reader is corrupted, causing loop stride always be 0 when invoking skip(long) operation.");
			}
		}
		ioFuncRetLib.put(className, funcList);
		ioFuncArgLib.put(className, argList);
		ioFuncErrMsgLib.put(className, errMsgList);
	}
	static
	{
		String className = "yarn2905.AggregatedLogFormat$ContainerLogsReader";
		Map<String, List<ConditionExpr>> funcList = new HashMap<String, List<ConditionExpr>>();
		Map<String, Integer> argList = new HashMap<String, Integer>();
		Map<String, String> errMsgList = new HashMap<String, String>();
		{
			{
				String methodStr = "long skip(long)";
				List<ConditionExpr> condList = new ArrayList<ConditionExpr>();
				ConditionExpr condexpr = new JEqExpr(LongConstant.v(0), LongConstant.v(0));//the first parameter is not used
				condList.add(condexpr);
//				ConditionExpr condexpr2 = new JEqExpr(LongConstant.v(-1), LongConstant.v(-1));//the first parameter is not used
//				condList.add(condexpr2);
				funcList.put(methodStr, condList);
				argList.put(methodStr, -1);//the corruption does not happen on the arg, thus, using -1
				errMsgList.put(methodStr, "BoundedInputStream in AggregatedLogFormat$ContainerLogsReader is corrupted, causing loop stride always be 0 when invoking skip(long) operation.");
			}
		}
		ioFuncRetLib.put(className, funcList);
		ioFuncArgLib.put(className, argList);
		ioFuncErrMsgLib.put(className, errMsgList);
	}
	static
	{
		String className = "kafka6271.MockFileInputStream";
		Map<String, List<ConditionExpr>> funcList = new HashMap<String, List<ConditionExpr>>();
		Map<String, Integer> argList = new HashMap<String, Integer>();
		Map<String, String> errMsgList = new HashMap<String, String>();
		{
			{
				String methodStr = "long skip(long)";
				List<ConditionExpr> condList = new ArrayList<ConditionExpr>();
				ConditionExpr condexpr = new JEqExpr(LongConstant.v(0), LongConstant.v(0));//the first parameter is not used
				condList.add(condexpr);
//				ConditionExpr condexpr2 = new JEqExpr(LongConstant.v(-1), LongConstant.v(-1));//the first parameter is not used
//				condList.add(condexpr2);
				funcList.put(methodStr, condList);
				argList.put(methodStr, -1);//the corruption does not happen on the arg, thus, using -1
				errMsgList.put(methodStr, "InputStream is corrupted, causing loop stride always be 0 when invoking skip(long) operation.");
			}
		}
		ioFuncRetLib.put(className, funcList);
		ioFuncArgLib.put(className, argList);
		ioFuncErrMsgLib.put(className, errMsgList);
	}
	static
	{
		String className = "java.nio.ByteBuffer";
		Map<String, List<ConditionExpr>> funcList = new HashMap<String, List<ConditionExpr>>();
		Map<String, Integer> argList = new HashMap<String, Integer>();
		Map<String, String> errMsgList = new HashMap<String, String>();
		{
			{
				String methodStr = "int capacity()";
				List<ConditionExpr> condList = new ArrayList<ConditionExpr>();
				ConditionExpr condexpr = new JEqExpr(LongConstant.v(0), LongConstant.v(0));//the first parameter is not used
				condList.add(condexpr);
//				ConditionExpr condexpr2 = new JEqExpr(LongConstant.v(-1), LongConstant.v(-1));//the first parameter is not used
//				condList.add(condexpr2);
				funcList.put(methodStr, condList);
				argList.put(methodStr, -1);//the corruption does not happen on the arg, thus, using -1
				errMsgList.put(methodStr, "ByteBuffer is corrupted, causing loop stride always be 0 when invoking capacity() operation.");
			}
		}
		ioFuncRetLib.put(className, funcList);
		ioFuncArgLib.put(className, argList);
		ioFuncErrMsgLib.put(className, errMsgList);
	}

	
	
	public static int checkAPIType(String methodName, IndexBoundAPIs apiList){
		if(apiList == null) return -1;
		if(apiList.boundCheckAPI.contains(methodName))
			return BOUND_CHECKING;
		else if(apiList.boundUpdateAPI.contains(methodName))
			return BOUND_UPDATE;
		else if(apiList.indexForwardAPI.contains(methodName))
			return INDEX_FORWARD;
		else if(apiList.indexBackwardAPI.contains(methodName))
			return INDEX_BACKWARD;
		else if(apiList.indexResetAPI.contains(methodName))
			return INDEX_RESET;
		return -1;
	}
	
	
	
	//check whether cond1 contains cond2
	public static boolean containCond(ConditionExpr cond1, ConditionExpr cond2){
		if(cond2 instanceof EqExpr){//currently, we consider in our dictionary, the cond2 is only an EqExpr
			Value op2Cond1 = cond1.getOp2();
			Value op2Cond2 = cond2.getOp2();
			NumericConstant numop2cond1 = null;
			NumericConstant numop2cond2 = null;
			if(op2Cond1 instanceof NumericConstant && op2Cond2 instanceof NumericConstant){
				numop2cond1 = (NumericConstant)op2Cond1;
				numop2cond2 = (NumericConstant)op2Cond2; 
			}
			if (cond1 instanceof EqExpr) {
				NumericConstant ret = numop2cond1.equalEqual(numop2cond2);
				if(ret instanceof IntConstant && ((IntConstant)ret).equals(IntConstant.v(1))
						|| ret instanceof LongConstant && ((LongConstant)ret).equals(LongConstant.v(1)))
					return true;
			} else if (cond1 instanceof NeExpr) {
				NumericConstant ret = numop2cond1.notEqual(numop2cond2);
				if(ret instanceof IntConstant && ((IntConstant)ret).equals(IntConstant.v(1))
						|| ret instanceof LongConstant && ((LongConstant)ret).equals(LongConstant.v(1)))
					return true;
			} else if (cond1 instanceof GeExpr) {
				NumericConstant ret = numop2cond1.lessThanOrEqual(numop2cond2);
				if(ret instanceof IntConstant && ((IntConstant)ret).equals(IntConstant.v(1))
						|| ret instanceof LongConstant && ((LongConstant)ret).equals(LongConstant.v(1)))
					return true;
			} else if (cond1 instanceof GtExpr) {
				NumericConstant ret = numop2cond1.lessThan(numop2cond2);
				if(ret instanceof IntConstant && ((IntConstant)ret).equals(IntConstant.v(1))
						|| ret instanceof LongConstant && ((LongConstant)ret).equals(LongConstant.v(1)))
					return true;
			} else if (cond1 instanceof LeExpr) {
				NumericConstant ret = numop2cond1.greaterThanOrEqual(numop2cond2);
				if(ret instanceof IntConstant && ((IntConstant)ret).equals(IntConstant.v(1))
						|| ret instanceof LongConstant && ((LongConstant)ret).equals(LongConstant.v(1)))
					return true;
			} else if (cond1 instanceof LtExpr) {
				NumericConstant ret = numop2cond1.greaterThan(numop2cond2);
				if(ret instanceof IntConstant && ((IntConstant)ret).equals(IntConstant.v(1))
						|| ret instanceof LongConstant && ((LongConstant)ret).equals(LongConstant.v(1)))
					return true;
			}
		}
		return false;
	}
	
	
	//merge two conditions
	public static ConditionExpr mergeConds(ConditionExpr cond1, ConditionExpr cond2){
		if(cond1 instanceof EqExpr && cond2 instanceof EqExpr){//currently, we consider in our dictionary, the cond2 is only an EqExpr
			Value op2Cond1 = cond1.getOp2();
			Value op2Cond2 = cond2.getOp2();
			NumericConstant numop2cond1 = null;
			NumericConstant numop2cond2 = null;
			if(op2Cond1 instanceof NumericConstant && op2Cond2 instanceof NumericConstant){
				numop2cond1 = (NumericConstant)op2Cond1;
				numop2cond2 = (NumericConstant)op2Cond2; 
			}
			if(numop2cond1 instanceof IntConstant && numop2cond2 instanceof IntConstant){
				IntConstant int1 = (IntConstant)numop2cond1;
				IntConstant int2 = (IntConstant)numop2cond2;
				if(int1.equals(IntConstant.v(0)) && int2.equals(IntConstant.v(-1))
						|| int1.equals(IntConstant.v(-1)) && int2.equals(IntConstant.v(0))){
					ConditionExpr condexpr = new JLeExpr(IntConstant.v(0), IntConstant.v(0));//the op1 is not used, just for formating.
					return condexpr;
				}
			} else if(numop2cond1 instanceof LongConstant && numop2cond2 instanceof LongConstant){
				LongConstant int1 = (LongConstant)numop2cond1;
				LongConstant int2 = (LongConstant)numop2cond2;
				if(int1.equals(LongConstant.v(0)) && int2.equals(LongConstant.v(-1))
						|| int1.equals(LongConstant.v(-1)) && int2.equals(LongConstant.v(0))){
					ConditionExpr condexpr = new JLeExpr(LongConstant.v(0), LongConstant.v(0));//the op1 is not used, just for formating.
					return condexpr;
				}
			}
		}
		return null;
	}
}

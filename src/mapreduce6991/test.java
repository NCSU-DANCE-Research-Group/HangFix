package mapreduce6991;

import java.io.IOException;
import java.util.Vector;

import org.apache.hadoop.util.Shell.ExitCodeException;

public class test {
//	public static void main(String[] args){
//		String str = "TestProcfsBasedProcessTree: String getRogueTaskPID()";
//		String[] strs = str.split(":");
//		System.out.println(strs[0]);
//	}
	
	public static void main(String[] args){
		
	}
	
	private class RogueTaskThread extends Thread {
	    public void run() throws IOException {
	      try {
	        throw new IOException("an IOException is thrown from run");
	      } catch (IOException ioe) {  
	        throw ioe;
	      } 
	    }
	  }
}

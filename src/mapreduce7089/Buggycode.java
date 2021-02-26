package mapreduce7089;

import hadoop2Conf.Configuration;
import hadoop2Conf.JobConf;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Buggycode {
	private static final int DEFAULT_BUFFER_SIZE = 1000000;  
	static int bufferSize = DEFAULT_BUFFER_SIZE;
	protected static byte[] buffer;
	protected static Closeable stream;
	
	  
	  public void configure(JobConf conf) {
//		   bufferSize = conf.getInt("test.io.file.buffer.size", 4096);
		   bufferSize = conf.mockGetInt("test.io.file.buffer.size", 4096);//inject faults here
		   buffer = new byte[bufferSize];
	  }
	  
	  
	  public static class ReadMapper {

		    public ReadMapper() { 
		    }
		    
		    public void getIOStream(String name) throws IOException {
		        // open file
		    	File f = new File(name);
		        FileInputStream in = new FileInputStream(f);
		        stream = in;
//		        return in;
		    }

		    public Long doIO(/*Reporter reporter, 
		                       String name,*/ 
		                       long totalSize // in bytes
		                     ) throws IOException {
		      InputStream in = (InputStream)stream;
		      long actualSize = 0;
		      while (actualSize < totalSize) {
		        int curSize = in.read(buffer, 0, bufferSize);
		        if(curSize < 0) break;
		        actualSize += curSize;
		        System.out.println("inside loop...curSize = " + curSize);
		      }
		      return Long.valueOf(actualSize);
		    }
		  }
}

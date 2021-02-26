package mapreduce7088;

import hadoop2FSDataIPS.FSDataInputStream;
import hadoop2FSDataIPS.RandomInputStream;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class testcode {
	  
	  
	public static void main(String[] args) throws Exception {
		args = new String[]{"-bufferSize", "0"};
		testcode ins = new testcode();
		ins.testDoIO(args);
	}
	 

	public void testDoIO(String[] args) throws IOException{

	    String usage = "Usage: DistributedFSCheck [-root name] [-clean] [-resFile resultFileName] [-bufferSize Bytes] [-stats] ";
	    int bufferSize = 0;
	    
	    if (args.length == 1 && args[0].startsWith("-h")) {
	      System.err.println(usage);
	      System.exit(-1);
	    }
	    for(int i = 0; i < args.length; i++) {       // parse command line
	      if (args[i].equals("-bufferSize")) {
	        bufferSize = Integer.parseInt(args[++i]);
	      }
	    }

//	    LOG.info("root = " + rootName);
//	    LOG.info("bufferSize = " + bufferSize);
	  
//	    Configuration conf = new Configuration();  
//	    conf.setInt("test.io.file.buffer.size", bufferSize);
	    Buggycode.DistributedFSCheckMapper inst = new Buggycode.DistributedFSCheckMapper(bufferSize);
	    DataInputStream in3 = new DataInputStream(new ByteArrayInputStream("aaabbbbbbb".getBytes()));
	    InputStream in = new RandomInputStream(in3);
	    
	    inst.doIO(new FSDataInputStream(in), "test", 1024);
	}
}

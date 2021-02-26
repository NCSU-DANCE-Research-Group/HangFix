package mapreduce7088;

import hadoop2FSDataIPS.FSDataInputStream;

import java.io.IOException;

public class Buggycode {
	
	private static int bufferSize = 0;
	private static byte[] buffer = new byte[1024];
	  /**
	   * DistributedFSCheck mapper class.
	   */
	  public static class DistributedFSCheckMapper {

	    public DistributedFSCheckMapper(int size) {
	    	bufferSize = size;
	    }

	    public Object doIO(FSDataInputStream in,/*Reporter reporter,*/ 
	                       String name, 
	                       long offset 
	                       ) throws IOException {
	      // open file
//	      FSDataInputStream in = null;
//	      Path p = new Path(name);
//	      try {
//	        in = fs.open(p);
//	      } catch(IOException e) {
//	        return name + "@(missing)";
//	      }
	      in.seek(offset);
	      long actualSize = 0;
	      try {
//	        long blockSize = fs.getDefaultBlockSize(p);
	        long blockSize = 10;
//	        reporter.setStatus("reading " + name + "@" + offset + "/" + blockSize);
	        for( int curSize = bufferSize; 
	             curSize == bufferSize && actualSize < blockSize;
	             actualSize += curSize) {
	          curSize = in.read(buffer, 0, bufferSize);
	          System.out.println("inside loop...curSize = " + curSize);
	        }
	      } catch(IOException e) {
//	        LOG.info("Corrupted block detected in \"" + name + "\" at " + offset);
	        return name + "@" + offset;
	      } finally {
	        in.close();
	      }
	      return new Long(actualSize);
	    }

	  }
}

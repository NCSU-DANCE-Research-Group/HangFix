package mapreduce6990;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


public class Buggycode {
		  
	  public static class Reader extends InputStream {
		    private long bytesRemaining;
		    private MockFileInputStream file;

		    /**
		     * Read a log file from start to end positions. The offsets may be negative,
		     * in which case they are relative to the end of the file. For example,
		     * Reader(taskid, kind, 0, -1) is the entire file and 
		     * Reader(taskid, kind, -4197, -1) is the last 4196 bytes. 
		     * @param taskid the id of the task to read the log file for
		     * @param kind the kind of log to read
		     * @param start the offset to read from (negative is relative to tail)
		     * @param end the offset to read upto (negative is relative to tail)
		     * @param isCleanup whether the attempt is cleanup attempt or not
		     * @throws IOException
		     */
		    public Reader(File f,/*TaskAttemptID taskid, LogName kind,*/ 
		                  long start, long end/*, boolean isCleanup*/) throws IOException {
		      // find the right log file
//		      LogFileDetail fileDetail = getLogFileDetail(taskid, kind, isCleanup);
		      // calculate the start and stop
//		      long size = fileDetail.length;
		      long size = 6;
		      if (start < 0) {
		        start += size + 1;
		      }
		      if (end < 0) {
		        end += size + 1;
		      }
		      start = Math.max(0, Math.min(start, size));
		      end = Math.max(0, Math.min(end, size));
//		      start += fileDetail.start;
//		      end += fileDetail.start;
		      start += 0;
		      end += 0;
		      bytesRemaining = end - start;
//		      String owner = obtainLogDirOwner(taskid);
//		      file = SecureIOUtils.openForRead(new File(fileDetail.location, kind.toString()), owner, null);
		      file = new MockFileInputStream(f);
		      // skip upto start
		      long pos = 0;
		      while (pos < start) {
		        long result = file.skip(start - pos);
		        System.out.println("in a loop...result = " + result);
		        if (result < 0) {
		          bytesRemaining = 0;
		          break;
		        }
		        pos += result;
		      }
		    }
		    
		    @Override
		    public int read() throws IOException {
		      int result = -1;
		      if (bytesRemaining > 0) {
		        bytesRemaining -= 1;
		        result = file.read();
		      }
		      return result;
		    }
		    
		    @Override
		    public int read(byte[] buffer, int offset, int length) throws IOException {
		      length = (int) Math.min(length, bytesRemaining);
		      int bytes = file.read(buffer, offset, length);
		      if (bytes > 0) {
		        bytesRemaining -= bytes;
		      }
		      return bytes;
		    }
		    
		    @Override
		    public int available() throws IOException {
		      return (int) Math.min(bytesRemaining, file.available());
		    }

		    @Override
		    public void close() throws IOException {
		      file.close();
		    }
		  }
	  
	  
}

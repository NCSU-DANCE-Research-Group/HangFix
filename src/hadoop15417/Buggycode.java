package hadoop15417;


import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.hadoop.fs.s3.S3FileSystemConfigKeys;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Buggycode {
	private int bufferSize;
	private Configuration conf;
	private static final Log LOG = LogFactory.getLog(Buggycode.class.getName());

	  public void initialize(Configuration conf) throws IOException {
		  this.conf = conf;
		  this.bufferSize = conf.getInt(
		                       S3FileSystemConfigKeys.S3_STREAM_BUFFER_SIZE_KEY,
		                       S3FileSystemConfigKeys.S3_STREAM_BUFFER_SIZE_DEFAULT);
		  System.out.println("bufferSize = " + bufferSize);
	  }
	  
	  public File retrieveBlock() throws IOException {
		  	File fileBlock = null;
		    InputStream in = null;
		    OutputStream out = null;
		    try {
		      fileBlock = newBackupFile();
		      in = get();
		      out = new BufferedOutputStream(new FileOutputStream(fileBlock));
		      byte[] buf = new byte[bufferSize];
		      int numRead;
		      while ((numRead = in.read(buf)) >= 0) {
		    	System.out.println("inside loop...numRead = " + numRead);
		        out.write(buf, 0, numRead);
		      }
		      return fileBlock;
		    } catch (IOException e) {
		      // close output stream to file then delete file
		      closeQuietly(out);
		      out = null; // to prevent a second close
		      if (fileBlock != null) {
		        boolean b = fileBlock.delete();
		        if (!b) {
		          LOG.warn("Ignoring failed delete");
		        }
		      }
		      throw e;
		    } finally {
		      closeQuietly(out);
		      closeQuietly(in);
		    }
	  }
	  
	  private InputStream get() throws IOException {
		  String filename = "/home/ting/DataLoopBugDetection/workspace_java/testInstrumentJavaClass/bin/hadoop15417/input.txt";
		  File f = new File(filename);
		  if(!f.exists()) f.createNewFile();
		  InputStream in = new FileInputStream(f);
		  return in;
	  }
	  
	  private File newBackupFile() throws IOException {
		    File dir = new File(conf.get("fs.s3.buffer.dir"));
		    if (!dir.exists() && !dir.mkdirs()) {
		      throw new IOException("Cannot create S3 buffer directory: " + dir);
		    }
		    File result = File.createTempFile("input-", ".tmp", dir);
		    result.deleteOnExit();
		    return result;
	  }
	  
	  private void closeQuietly(Closeable closeable) {
		    if (closeable != null) {
		      try {
		        closeable.close();
		      } catch (IOException e) {
		        // ignore
		      }
		    }
	  }
}

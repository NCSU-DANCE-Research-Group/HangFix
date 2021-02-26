package hive19406;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Buggycode {
	private static final Log LOG = LogFactory.getLog(Buggycode.class);
	private MockFile parentFile;
	private File tmpFile;
	
	  public void setupOutput() throws IOException {
		    if (parentFile == null) {
		      while (true) {
		        parentFile = MockFile.createTempFile("hive-resultcache", "");
		        if (parentFile.delete() && parentFile.mkdir()) {
		          parentFile.deleteOnExit();
		          break;
		        }
//		        if (LOG.isDebugEnabled()) {
		          LOG.debug("Retry creating tmp result-cache directory...");
//		        }
		      }
		    }

//		    if (tmpFile == null || input != null) {
//		      tmpFile = File.createTempFile("ResultCache", ".tmp", parentFile);
//		      LOG.info("ResultCache created temp file " + tmpFile.getAbsolutePath());
//		      tmpFile.deleteOnExit();
//		    }

//		    FileOutputStream fos = null;
//		    try {
//		      fos = new FileOutputStream(tmpFile);
//		      output = new Output(fos);
//		    } finally {
//		      if (output == null && fos != null) {
//		        fos.close();
//		      }
//		    }
		  }
}

package hive19391;

import java.io.File;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mockito.Mockito;

public class Buggycode {
	protected static Log LOG = LogFactory.getLog(Buggycode.class);
	private File tmpFile; // temporary file holding the spilled blocks
	public MockFile parentFile;
	private List<Object> keyObject;
	
	  protected void setupWriter() throws HiveException {
		    try {

		      if ( tmpFile != null ) {
		        return;
		      }

		      String suffix = ".tmp";
		      if (this.keyObject != null) {
		        suffix = "." + this.keyObject.toString() + suffix;
		      }

		      while (true) {
		        parentFile = MockFile.createTempFile("hive-rowcontainer", "");
		        boolean success = parentFile.delete() && parentFile.mkdir();
		        if (success) {
		          break;
		        }
		        LOG.debug("retry creating tmp row-container directory...");
		      }

		      tmpFile = File.createTempFile("RowContainer", suffix, parentFile);
		      LOG.info("RowContainer created temp file " + tmpFile.getAbsolutePath());
		      // Delete the temp file if the JVM terminate normally through Hadoop job
		      // kill command.
		      // Caveat: it won't be deleted if JVM is killed by 'kill -9'.
		      parentFile.deleteOnExit();
		      tmpFile.deleteOnExit();

		      // rFile = new RandomAccessFile(tmpFile, "rw");
//		      HiveOutputFormat<?, ?> hiveOutputFormat = tblDesc.getOutputFileFormatClass().newInstance();
//		      tempOutPath = new Path(tmpFile.toString());
//		      JobConf localJc = getLocalFSJobConfClone(jc);
//		      rw = HiveFileFormatUtils.getRecordWriter(this.jobCloneUsingLocalFs,
//		          hiveOutputFormat, serde.getSerializedClass(), false,
//		          tblDesc.getProperties(), tempOutPath, reporter);
		    } catch (Exception e) {
//		      clearRows();
		      LOG.error(e.toString(), e);
		      throw new HiveException(e);
		    }

		  }
	  
}

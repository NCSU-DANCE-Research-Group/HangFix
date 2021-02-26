package yarn163;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.hadoop.util.StringUtils;

public class Buggycode {
	public void printLogs(File logFile, String startS, String endS) {
		long start = startS.isEmpty() ? -4 * 1024 : Long.parseLong(startS);
	    start = start < 0 ? logFile.length() + start : start;
	    start = start < 0 ? 0 : start;
	    long end = endS.isEmpty() ? logFile.length() : Long.parseLong(endS);
	        end = end < 0 ? logFile.length() + end : end;
	        end = end < 0 ? logFile.length() : end;
	    InputStreamReader reader = null;
	    try {
	      long toRead = end - start;
	      // TODO: Use secure IO Utils to avoid symlink attacks.
	      // TODO Fix findBugs close warning along with IOUtils change
	      reader = new MockFileReader(logFile);
	      int bufferSize = 65536;
	      char[] cbuf = new char[bufferSize];

	      long skipped = 0;
	      long totalSkipped = 0;
	      while (totalSkipped < start) {
	        skipped = reader.skip(start - totalSkipped);
	        totalSkipped += skipped;
	        System.out.println("inside loop...skipped = " + skipped);
	      }
//
//	      int len = 0;
//	      int currentToRead = toRead > bufferSize ? bufferSize : (int) toRead;
//	      writer().write("<pre>");
//
//	      while ((len = reader.read(cbuf, 0, currentToRead)) > 0
//	          && toRead > 0) {
//	        writer().write(cbuf, 0, len); // TODO: HTMl Quoting?
//	        toRead = toRead - len;
//	        currentToRead = toRead > bufferSize ? bufferSize : (int) toRead;
//	      }
//
//	      reader.close();
//	      writer().write("</pre>");

	    } catch (IOException e) {
	      System.out.println("Exception reading log-file. Log file was likely aggregated. "
	          + StringUtils.stringifyException(e));
	    } finally {
	      if (reader != null) {
	        try {
	          reader.close();
	        } catch (IOException e) {
	          // Ignore
	        }
	      }
	    }
	}
	
}

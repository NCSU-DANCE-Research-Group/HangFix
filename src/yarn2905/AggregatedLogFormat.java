package yarn2905;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.io.input.BoundedInputStream;

public class AggregatedLogFormat {
	public static class ContainerLogsReader {
	    private DataInputStream valueStream;
	    private String currentLogType = null;
	    private long currentLogLength = 0;
	    private BoundedInputStream currentLogData = null;
	    private InputStreamReader currentLogISR;

	    public ContainerLogsReader(DataInputStream stream) {
	      valueStream = stream;
	    }

	    public String nextLog() throws IOException {
	      if (currentLogData != null && currentLogLength > 0) {
	        // seek to the end of the current log, relying on BoundedInputStream
	        // to prevent seeking past the end of the current log
	        do {
	          if (currentLogData.skip(currentLogLength) < 0) {
	            break;
	          }
	        } while (currentLogData.read() != -1);
	      }

	      currentLogType = null;
	      currentLogLength = 0;
	      currentLogData = null;
	      currentLogISR = null;

	      try {
	        String logType = valueStream.readUTF();
	        String logLengthStr = valueStream.readUTF();
	        currentLogLength = Long.parseLong(logLengthStr);
	        currentLogData =
	            new BoundedInputStream(valueStream, currentLogLength);
	        currentLogData.setPropagateClose(false);
	        currentLogISR = new InputStreamReader(currentLogData);
	        currentLogType = logType;
	      } catch (EOFException e) {
	      }

	      return currentLogType;
	    }

	    public String getCurrentLogType() {
	      return currentLogType;
	    }

	    public long getCurrentLogLength() {
	      return currentLogLength;
	    }

	    public long skip(long n) throws IOException {
	      return currentLogData.skip(n);
	    }

	    public int read(byte[] buf, int off, int len) throws IOException {
	      return currentLogData.read(buf, off, len);
	    }

	    public int read(char[] buf, int off, int len) throws IOException {
	      return currentLogISR.read(buf, off, len);
	    }
	  }
}

package yarn2905;

import java.io.IOException;



public class Buggycode {
	public boolean readContainerLogs(/*Block html,*/AggregatedLogFormat.ContainerLogsReader logReader, /*LogLimits logLimits,
		      String desiredLogType*/
			long start) throws IOException {
//		    int bufferSize = 65536;
//		    char[] cbuf = new char[bufferSize];

		    boolean foundLog = false;
		    String logType = logReader.nextLog();
		    while (logType != null) {
//		      if (desiredLogType == null || desiredLogType.isEmpty()
//		          || desiredLogType.equals(logType)) {
//		        long logLength = logReader.getCurrentLogLength();

//		        if (foundLog) {
//		          html.pre()._("\n\n")._();
//		        }
//
//		        html.p()._("Log Type: " + logType)._();
//		        html.p()._("Log Length: " + Long.toString(logLength))._();

//		        long start = logLimits.start < 0
//		            ? logLength + logLimits.start : logLimits.start;
//		        start = start < 0 ? 0 : start;
//		        start = start > logLength ? logLength : start;
//		        long end = logLimits.end < 0
//		            ? logLength + logLimits.end : logLimits.end;
//		        end = end < 0 ? 0 : end;
//		        end = end > logLength ? logLength : end;
//		        end = end < start ? start : end;

//		        long toRead = end - start;
//		        if (toRead < logLength) {
//		            html.p()._("Showing " + toRead + " bytes of " + logLength
//		                + " total. Click ")
//		                .a(url("logs", $(NM_NODENAME), $(CONTAINER_ID),
//		                    $(ENTITY_STRING), $(APP_OWNER),
//		                    logType, "?start=0"), "here").
//		                    _(" for the full log.")._();
//		        }

		        long totalSkipped = 0;
		        while (totalSkipped < start) {
		          long ret = logReader.skip(start - totalSkipped);
		          System.out.println("inside loop...ret = " + ret);
		          if (ret < 0) {
		            throw new IOException( "Premature EOF from container log");
		          }
		          totalSkipped += ret;
		        }

//		        int len = 0;
//		        int currentToRead = toRead > bufferSize ? bufferSize : (int) toRead;
//		        PRE<Hamlet> pre = html.pre();
//
//		        while (toRead > 0
//		            && (len = logReader.read(cbuf, 0, currentToRead)) > 0) {
//		          pre._(new String(cbuf, 0, len));
//		          toRead = toRead - len;
//		          currentToRead = toRead > bufferSize ? bufferSize : (int) toRead;
//		        }
//
//		        pre._();
//		        foundLog = true;
//		      }

		      logType = logReader.nextLog();
		    }

		    return foundLog;
		  }
}

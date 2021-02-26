package kafka6271;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Buggycode {
	private static final Logger log = LoggerFactory.getLogger(Buggycode.class);
	public static final String FILENAME_FIELD = "filename";
    public  static final String POSITION_FIELD = "position";

    private String filename;
    private InputStream stream;
    private BufferedReader reader = null;
//    private char[] buffer = new char[1024];
//    private int offset = 0;
//    private String topic = null;
//
    private Long streamOffset;
    
    Buggycode(String filename){
    	this.filename = filename;
    }
//	
	 public void poll(Object lastRecordedOffset) throws InterruptedException {
	        if (stream == null) {
	            try {
	                stream = new MockFileInputStream(filename);
//	                Map<String, Object> offset = context.offsetStorageReader().offset(Collections.singletonMap(FILENAME_FIELD, filename));
//	                if (offset != null) {
//	                    Object lastRecordedOffset = offset.get(POSITION_FIELD);
	                    if (lastRecordedOffset != null && !(lastRecordedOffset instanceof Long))
	                        throw new ConnectException("Offset position is the incorrect type");
	                    if (lastRecordedOffset != null) {
	                        log.debug("Found previous offset, trying to skip to file offset {}", lastRecordedOffset);
	                        long skipLeft = (Long) lastRecordedOffset;
	                        while (skipLeft > 0) {
	                            try {
	                                long skipped = stream.skip(skipLeft);
	                                skipLeft -= skipped;
	                                System.out.println("inside loop, skipped = " + skipped);
	                            } catch (IOException e) {
	                                log.error("Error while trying to seek to previous offset in file: ", e);
	                                throw new ConnectException(e);
	                            }
	                        }
//	                        log.debug("Skipped to offset {}", lastRecordedOffset);
	                    }
	                    streamOffset = (lastRecordedOffset != null) ? (Long) lastRecordedOffset : 0L;
//	                } else {
//	                    streamOffset = 0L;
//	                }
	                reader = new BufferedReader(new InputStreamReader(stream));
	                log.debug("Opened {} for reading", logFilename());
	            } catch (FileNotFoundException e) {
	                log.warn("Couldn't find file {} for FileStreamSourceTask, sleeping to wait for it to be created", logFilename());
	                synchronized (this) {
	                    this.wait(1000);
	                }
	            }
	        }
	 }
	 
	 private String logFilename() {
	     return filename == null ? "stdin" : filename;
	 }
}

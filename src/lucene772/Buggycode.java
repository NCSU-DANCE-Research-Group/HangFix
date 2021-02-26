package lucene772;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class Buggycode {
	  public final byte[] uncompress(final byte[] input) throws IOException {

	    Inflater decompressor = new MockInflater();
	    decompressor.setInput(input);

	    // Create an expandable byte array to hold the decompressed data
	    ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);

	    // Decompress the data
	    byte[] buf = new byte[1024];
	    while (!decompressor.finished()) {
	      try {
	        int count = decompressor.inflate(buf);
//	        int count = threadWTO(decompressor, buf);
	        bos.write(buf, 0, count);
	      } catch (DataFormatException e) {
	        // this will happen if the field is not compressed
	        IOException newException = new IOException("field data are in wrong format: " + e.toString());
	        newException.initCause(e);
	        throw newException;
	      }
	    }
	  
	    decompressor.end();
	    
	    // Get the decompressed data
	    return bos.toByteArray();
	  }
	  
//	  public int threadWTO(final Inflater decompressor, final byte[] buf) throws DataFormatException 
//	  {   
//	      ExecutorService executor = Executors.newSingleThreadExecutor();
//	      Callable<Integer> callable = new Callable<Integer>() {
//	          @Override
//	          public Integer call() throws DataFormatException {
//	              return decompressor.inflate(buf);
//	          }
//	      };
//	      Future<Integer> future = executor.submit(callable);
//	      int count = 0;
//	      try {
//			count = future.get(2, TimeUnit.SECONDS);
//	      } catch (InterruptedException | ExecutionException | TimeoutException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			future.cancel(true);
//			throw new DataFormatException("Inflater is corrupted");
//	      } finally {
//	    	  executor.shutdown();
//	      }
//	      return count;
//	  }
}

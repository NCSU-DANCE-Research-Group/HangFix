package hive5235;

import java.io.IOException;
import java.nio.ByteBuffer;
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
	public void decompress(ByteBuffer in, ByteBuffer out) throws IOException {

//	    if(in.isDirect() && out.isDirect()) {
//	      directDecompress(in, out);
//	      return;
//	    }

	    Inflater inflater = new MockInflater(true);
	    inflater.setInput(in.array(), in.arrayOffset() + in.position(),
	                      in.remaining());
	    while (!(inflater.finished() || inflater.needsDictionary() ||
	             inflater.needsInput())) {
	      try {
	        int count = inflater.inflate(out.array(),
	                                     out.arrayOffset() + out.position(),
	                                     out.remaining());
//	        int count = threadWTO(inflater, out);
	        out.position(count + out.position());
	      } catch (DataFormatException dfe) {
	        throw new IOException("Bad compression data", dfe);
	      }
	    }
	    out.flip();
	    inflater.end();
	    in.position(in.limit());
	  }
	
	public int threadWTO(final Inflater inflater, final ByteBuffer out) throws DataFormatException 
	  {   
	      ExecutorService executor = Executors.newSingleThreadExecutor();
	      Callable<Integer> callable = new Callable<Integer>() {
	          @Override
	          public Integer call() throws DataFormatException {
	              return inflater.inflate(out.array(),out.arrayOffset() + out.position(),out.remaining());
	          }
	      };
	      Future<Integer> future = executor.submit(callable);
	      int count = 0;
	      try {
			count = future.get(2, TimeUnit.SECONDS);
	      } catch (InterruptedException | ExecutionException | TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			future.cancel(true);
			throw new DataFormatException("Endlessly blocking");
	      } finally {
	    	  executor.shutdown();
	      }
	      return count;
	  }
}

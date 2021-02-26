package lucene772;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;


public class MyCallableThread {
//	  /* the counter, initialize to zero */
//	  private static int c = 0;
//	 
//	  /**
//	   * increases the counter by <pre>howmany</pre>
//	   * @param howmany, the increment of the counter.
//	   */
//	  public static synchronized void increase(int howmany) {
//	    c += howmany;
//	  }
//	 
//	  /**
//	   * reports the counter content.
//	   */
//	  public static synchronized int report() {
//	    return c;
//	  }
	  
	  public static int decompressWTO(final Inflater decompressor, final byte[] buf) throws DataFormatException 
	  {   
	      ExecutorService executor = Executors.newSingleThreadExecutor();
	      Callable<Integer> callable = new Callable<Integer>() {
	          @Override
	          public Integer call() throws DataFormatException {
	              return decompressor.inflate(buf);
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
			throw new DataFormatException("Inflater is corrupted");
	      } finally {
	    	  executor.shutdown();
	      }
	      return count;
	  }

}

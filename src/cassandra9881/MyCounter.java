package cassandra9881;


public class MyCounter {
	  /* the counter, initialize to zero */
	  private static int c = 0;
	 
	  /**
	   * increases the counter by <pre>howmany</pre>
	   * @param howmany, the increment of the counter.
	   */
	  public static synchronized void increase(int howmany) {
	    c += howmany;
	  }
	 
	  /**
	   * reports the counter content.
	   */
	  public static synchronized int report() {
	    return c;
	  }
}

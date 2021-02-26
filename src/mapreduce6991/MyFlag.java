package mapreduce6991;


public class MyFlag {
	
	  private static boolean b = false;
	 
	  /**
	   * set the boolean to be true.
	   */
	  public static synchronized void set() {
	    b = true;
	  }
	 
	  /**
	   * reports the boolean content.
	   */
	  public static synchronized boolean get() {
	    return b;
	  }
}

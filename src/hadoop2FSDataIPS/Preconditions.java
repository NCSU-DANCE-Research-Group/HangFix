package hadoop2FSDataIPS;

public class Preconditions {
	public static void checkArgument(boolean expression) {
	    if (!expression) {
	      throw new IllegalArgumentException();
	    }
	}
	
	public static void checkState(boolean expression) {
	    if (!expression) {
	      throw new IllegalStateException();
	    }
	}
	
	public static <T> T checkNotNull(T reference) {
	    if (reference == null) {
	      throw new NullPointerException();
	    }
	    return reference;
	  }
}

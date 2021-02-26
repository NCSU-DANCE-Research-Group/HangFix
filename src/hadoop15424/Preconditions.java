package hadoop15424;

public class Preconditions {
	  public static void checkState(boolean expression) {
		    if (!expression) {
		      throw new IllegalStateException();
		    }
		  }
}

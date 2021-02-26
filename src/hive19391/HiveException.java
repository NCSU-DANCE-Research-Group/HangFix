package hive19391;

public class HiveException extends Exception {
	  /**
	   * Standard predefined message with error code and possibly SQL State, etc.
	   */
//	  private ErrorMsg canonicalErrorMsg = ErrorMsg.GENERIC_ERROR;
	  public HiveException() {
	    super();
	  }

	  public HiveException(String message) {
	    super(message);
	  }

	  public HiveException(Throwable cause) {
	    super(cause);
	  }

	  public HiveException(String message, Throwable cause) {
	    super(message, cause);
	  }

//	  public HiveException(ErrorMsg message, String... msgArgs) {
//	    this(null, message, msgArgs);
//	  }

	  /**
	   * This is the recommended constructor to use since it helps use
	   * canonical messages throughout.  
	   * @param errorMsg Canonical error message
	   * @param msgArgs message arguments if message is parametrized; must be {@code null} is message takes no arguments
	   */
//	  public HiveException(Throwable cause, ErrorMsg errorMsg, String... msgArgs) {
//	    super(errorMsg.format(msgArgs), cause);
//	    canonicalErrorMsg = errorMsg;
//
//	  }
//	  /**
//	   * @return {@link ErrorMsg#GENERIC_ERROR} by default
//	   */
//	  public ErrorMsg getCanonicalErrorMsg() {
//	    return canonicalErrorMsg;
//	  }
	}

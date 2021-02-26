package hadoop15424;

public abstract class RpcAuthInfo {
	  /** Different types of authentication as defined in RFC 1831 */
	  public enum AuthFlavor {
	    AUTH_NONE(0),
	    AUTH_SYS(1),
	    AUTH_SHORT(2),
	    AUTH_DH(3),
	    RPCSEC_GSS(6);
	    
	    private int value;
	    
	    AuthFlavor(int value) {
	      this.value = value;
	    }
	    
	    public int getValue() {
	      return value;
	    }
	    
	    static AuthFlavor fromValue(int value) {
	      for (AuthFlavor v : values()) {
	        if (v.value == value) {
	          return v;
	        }
	      }
	      throw new IllegalArgumentException("Invalid AuthFlavor value " + value);
	    }
	  }
	  
	  private final AuthFlavor flavor;
	  
	  protected RpcAuthInfo(AuthFlavor flavor) {
	    this.flavor = flavor;
	  }
	  
	  /** Load auth info */
	  public abstract void read(XDR xdr);
	  
	  /** Write auth info */
	  public abstract void write(XDR xdr);
	  
	  public AuthFlavor getFlavor() {
	    return flavor;
	  }
	  
	  @Override
	  public String toString() {
	    return "(AuthFlavor:" + flavor + ")";
	  }
	}

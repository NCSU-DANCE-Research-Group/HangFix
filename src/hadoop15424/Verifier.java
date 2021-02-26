package hadoop15424;

public abstract class Verifier extends RpcAuthInfo {

	  public static final Verifier VERIFIER_NONE = new VerifierNone();

	  protected Verifier(AuthFlavor flavor) {
	    super(flavor);
	  }

	  /** Read both AuthFlavor and the verifier from the XDR */
	  public static Verifier readFlavorAndVerifier(XDR xdr) {
	    AuthFlavor flavor = AuthFlavor.fromValue(xdr.readInt());
	    final Verifier verifer;
	    if(flavor == AuthFlavor.AUTH_NONE) {
	      verifer = new VerifierNone();
	    } else if(flavor == AuthFlavor.RPCSEC_GSS) {
	      verifer = new VerifierGSS();
	    } else {
	      throw new UnsupportedOperationException("Unsupported verifier flavor"
	          + flavor);
	    }
	    verifer.read(xdr);
	    return verifer;
	  }
	  
	  /**
	   * Write AuthFlavor and the verifier to the XDR
	   */
	  public static void writeFlavorAndVerifier(Verifier verifier, XDR xdr) {
	    if (verifier instanceof VerifierNone) {
	      xdr.writeInt(AuthFlavor.AUTH_NONE.getValue());
	    } else if (verifier instanceof VerifierGSS) {
	      xdr.writeInt(AuthFlavor.RPCSEC_GSS.getValue());
	    } else {
	      throw new UnsupportedOperationException("Cannot recognize the verifier");
	    }
	    verifier.write(xdr);
	  }  
	}

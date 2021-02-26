package hadoop15424;

public class VerifierNone extends Verifier {

	  public VerifierNone() {
	    super(AuthFlavor.AUTH_NONE);
	  }

	  @Override
	  public void read(XDR xdr) {
	    int length = xdr.readInt();
	    Preconditions.checkState(length == 0);
	  }

	  @Override
	  public void write(XDR xdr) {
	    xdr.writeInt(0);
	  }
}

package hadoop15424;

public class CredentialsNone extends Credentials {

	  public CredentialsNone() {
	    super(AuthFlavor.AUTH_NONE);
	    mCredentialsLength = 0;
	  }

	  @Override
	  public void read(XDR xdr) {
	    mCredentialsLength = xdr.readInt();
	    Preconditions.checkState(mCredentialsLength == 0);
	  }

	  @Override
	  public void write(XDR xdr) {
	    Preconditions.checkState(mCredentialsLength == 0);
	    xdr.writeInt(mCredentialsLength);
	  }
	}

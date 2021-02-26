package hadoop15424;

public class CredentialsGSS extends Credentials {

	  public CredentialsGSS() {
	    super(AuthFlavor.RPCSEC_GSS);
	  }

	  @Override
	  public void read(XDR xdr) {
	    // TODO Auto-generated method stub
	    
	  }

	  @Override
	  public void write(XDR xdr) {
	    // TODO Auto-generated method stub
	    
	  }

	}

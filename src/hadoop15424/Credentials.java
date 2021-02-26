package hadoop15424;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Base class for all credentials. Currently we only support 3 different types
 * of auth flavors: AUTH_NONE, AUTH_SYS, and RPCSEC_GSS.
 */
public abstract class Credentials extends RpcAuthInfo {
  public static final Log LOG = LogFactory.getLog(Credentials.class);

  public static Credentials readFlavorAndCredentials(XDR xdr) {
    AuthFlavor flavor = AuthFlavor.fromValue(xdr.readInt());
    final Credentials credentials;
    if(flavor == AuthFlavor.AUTH_NONE) {
      credentials = new CredentialsNone();
    } else if(flavor == AuthFlavor.AUTH_SYS) {
      credentials = new CredentialsSys();
    } else if(flavor == AuthFlavor.RPCSEC_GSS) {
      credentials = new CredentialsGSS();
    } else {
      throw new UnsupportedOperationException("Unsupported Credentials Flavor "
          + flavor);
    }
    credentials.read(xdr);
    return credentials;
  }
  
  /**
   * Write AuthFlavor and the credentials to the XDR
   */
  public static void writeFlavorAndCredentials(Credentials cred, XDR xdr) {
    if (cred instanceof CredentialsNone) {
      xdr.writeInt(AuthFlavor.AUTH_NONE.getValue());
    } else if (cred instanceof CredentialsSys) {
      xdr.writeInt(AuthFlavor.AUTH_SYS.getValue());
    } else if (cred instanceof CredentialsGSS) {
      xdr.writeInt(AuthFlavor.RPCSEC_GSS.getValue());
    } else {
      throw new UnsupportedOperationException("Cannot recognize the verifier");
    }
    cred.write(xdr);
  }
  
  protected int mCredentialsLength;
  
  protected Credentials(AuthFlavor flavor) {
    super(flavor);
  }
}

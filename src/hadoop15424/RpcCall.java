package hadoop15424;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Represents an RPC message of type RPC call as defined in RFC 1831
 */
public class RpcCall extends RpcMessage {
  public static final int RPC_VERSION = 2;
  private static final Log LOG = LogFactory.getLog(RpcCall.class);
  
  public static RpcCall read(XDR xdr) {
    return new RpcCall(xdr.readInt(), RpcMessage.Type.fromValue(xdr.readInt()),
        xdr.readInt(), xdr.readInt(), xdr.readInt(), xdr.readInt(), 
        Credentials.readFlavorAndCredentials(xdr),
        Verifier.readFlavorAndVerifier(xdr));
  }
  
  public static RpcCall getInstance(int xid, int program, int version,
      int procedure, Credentials cred, Verifier verifier) {
    return new RpcCall(xid, RpcMessage.Type.RPC_CALL, 2, program, version,
        procedure, cred, verifier);
  }
  
  private final int rpcVersion;
  private final int program;
  private final int version;
  private final int procedure;
  private final Credentials credentials;
  private final Verifier verifier;

  protected RpcCall(int xid, RpcMessage.Type messageType, int rpcVersion,
      int program, int version, int procedure, Credentials credential,
      Verifier verifier) {
    super(xid, messageType);
    this.rpcVersion = rpcVersion;
    this.program = program;
    this.version = version;
    this.procedure = procedure;
    this.credentials = credential;
    this.verifier = verifier;
    if (LOG.isTraceEnabled()) {
      LOG.trace(this);
    }
    validate();
  }
  
  private void validateRpcVersion() {
    if (rpcVersion != RPC_VERSION) {
      throw new IllegalArgumentException("RPC version is expected to be "
          + RPC_VERSION + " but got " + rpcVersion);
    }
  }
  
  public void validate() {
    validateMessageType(RpcMessage.Type.RPC_CALL);
    validateRpcVersion();
    // Validate other members
    // Throw exception if validation fails
  }


  public int getRpcVersion() {
    return rpcVersion;
  }

  public int getProgram() {
    return program;
  }

  public int getVersion() {
    return version;
  }

  public int getProcedure() {
    return procedure;
  }
  
  public Credentials getCredential() {
    return credentials;
  }

  public Verifier getVerifier() {
    return verifier;
  }
  
  @Override
  public XDR write(XDR xdr) {
    xdr.writeInt(xid);
    xdr.writeInt(RpcMessage.Type.RPC_CALL.getValue());
    xdr.writeInt(2);
    xdr.writeInt(program);
    xdr.writeInt(version);
    xdr.writeInt(procedure);
    Credentials.writeFlavorAndCredentials(credentials, xdr);
    Verifier.writeFlavorAndVerifier(verifier, xdr);
    return xdr;
  }
  
  @Override
  public String toString() {
    return String.format("Xid:%d, messageType:%s, rpcVersion:%d, program:%d,"
        + " version:%d, procedure:%d, credential:%s, verifier:%s", xid,
        messageType, rpcVersion, program, version, procedure,
        credentials.toString(), verifier.toString());
  }
}


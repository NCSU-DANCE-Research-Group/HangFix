package hadoop15424;

public class RpcDeniedReply extends RpcReply {
	  public enum RejectState {
	    // the order of the values below are significant.
	    RPC_MISMATCH,
	    AUTH_ERROR;

	    int getValue() {
	      return ordinal();
	    }

	    static RejectState fromValue(int value) {
	      return values()[value];
	    }
	  }

	  private final RejectState rejectState;

	  public RpcDeniedReply(int xid, ReplyState replyState,
	      RejectState rejectState, Verifier verifier) {
	    super(xid, replyState, verifier);
	    this.rejectState = rejectState;
	  }

	  public static RpcDeniedReply read(int xid, ReplyState replyState, XDR xdr) {
	    Verifier verifier = Verifier.readFlavorAndVerifier(xdr);
	    RejectState rejectState = RejectState.fromValue(xdr.readInt());
	    return new RpcDeniedReply(xid, replyState, rejectState, verifier);
	  }

	  public RejectState getRejectState() {
	    return rejectState;
	  }
	  
	  @Override
	  public String toString() {
	    return new StringBuffer().append("xid:").append(xid)
	        .append(",messageType:").append(messageType).append("verifier_flavor:")
	        .append(verifier.getFlavor()).append("rejectState:")
	        .append(rejectState).toString();
	  }
	  
	  @Override
	  public XDR write(XDR xdr) {
	    xdr.writeInt(xid);
	    xdr.writeInt(messageType.getValue());
	    xdr.writeInt(replyState.getValue());
	    Verifier.writeFlavorAndVerifier(verifier, xdr);
	    xdr.writeInt(rejectState.getValue());
	    return xdr;
	  }
	}

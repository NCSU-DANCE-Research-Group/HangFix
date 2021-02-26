package hadoop15424;

public abstract class RpcReply extends RpcMessage {
	  /** RPC reply_stat as defined in RFC 1831 */
	  public enum ReplyState {
	    // the order of the values below are significant.
	    MSG_ACCEPTED,
	    MSG_DENIED;
	    
	    int getValue() {
	      return ordinal();
	    }
	    
	    public static ReplyState fromValue(int value) {
	      return values()[value];
	    }
	  }
	  
	  protected final ReplyState replyState;
	  protected final Verifier verifier;
	  
	  RpcReply(int xid, ReplyState state, Verifier verifier) {
	    super(xid, RpcMessage.Type.RPC_REPLY);
	    this.replyState = state;
	    this.verifier = verifier;
	  }
	  
	  public RpcAuthInfo getVerifier() {
	    return verifier;
	  }

	  public static RpcReply read(XDR xdr) {
	    int xid = xdr.readInt();
	    final Type messageType = Type.fromValue(xdr.readInt());
	    Preconditions.checkState(messageType == RpcMessage.Type.RPC_REPLY);
	    
	    ReplyState stat = ReplyState.fromValue(xdr.readInt());
	    switch (stat) {
	    case MSG_ACCEPTED:
	      return RpcAcceptedReply.read(xid, stat, xdr);
	    case MSG_DENIED:
	      return RpcDeniedReply.read(xid, stat, xdr);
	    }
	    return null;
	  }

	  public ReplyState getState() {
	    return replyState;
	  }
	}


package hadoop15424;

public class RpcAcceptedReply extends RpcReply {
	  public enum AcceptState {
	    // the order of the values below are significant.
	    SUCCESS, /* RPC executed successfully */
	    PROG_UNAVAIL, /* remote hasn't exported program */
	    PROG_MISMATCH, /* remote can't support version # */
	    PROC_UNAVAIL, /* program can't support procedure */
	    GARBAGE_ARGS, /* procedure can't decode params */
	    SYSTEM_ERR; /* e.g. memory allocation failure */
	    
	    public static AcceptState fromValue(int value) {
	      return values()[value];
	    }

	    public int getValue() {
	      return ordinal();
	    }
	  };
	  
	  public static RpcAcceptedReply getAcceptInstance(int xid, 
	      Verifier verifier) {
	    return getInstance(xid, AcceptState.SUCCESS, verifier);
	  }
	  
	  public static RpcAcceptedReply getInstance(int xid, AcceptState state,
	      Verifier verifier) {
	    return new RpcAcceptedReply(xid, ReplyState.MSG_ACCEPTED, verifier,
	        state);
	  }

	  private final AcceptState acceptState;

	  RpcAcceptedReply(int xid, ReplyState state, Verifier verifier,
	      AcceptState acceptState) {
	    super(xid, state, verifier);
	    this.acceptState = acceptState;
	  }

	  public static RpcAcceptedReply read(int xid, ReplyState replyState, XDR xdr) {
	    Verifier verifier = Verifier.readFlavorAndVerifier(xdr);
	    AcceptState acceptState = AcceptState.fromValue(xdr.readInt());
	    return new RpcAcceptedReply(xid, replyState, verifier, acceptState);
	  }

	  public AcceptState getAcceptState() {
	    return acceptState;
	  }
	  
	  @Override
	  public XDR write(XDR xdr) {
	    xdr.writeInt(xid);
	    xdr.writeInt(messageType.getValue());
	    xdr.writeInt(replyState.getValue());
	    Verifier.writeFlavorAndVerifier(verifier, xdr);
	    xdr.writeInt(acceptState.getValue());
	    return xdr;
	  }
	}

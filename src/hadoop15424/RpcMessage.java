package hadoop15424;

public abstract class RpcMessage {
	  /** Message type */
	  public static enum Type {
	    // the order of the values below are significant.
	    RPC_CALL,
	    RPC_REPLY;
	    
	    public int getValue() {
	      return ordinal();
	    }

	    public static Type fromValue(int value) {
	      if (value < 0 || value >= values().length) {
	        return null;
	      }
	      return values()[value];
	    }
	  }

	  protected final int xid;
	  protected final Type messageType;
	  
	  RpcMessage(int xid, Type messageType) {
	    if (messageType != Type.RPC_CALL && messageType != Type.RPC_REPLY) {
	      throw new IllegalArgumentException("Invalid message type " + messageType);
	    }
	    this.xid = xid;
	    this.messageType = messageType;
	  }
	  
	  public abstract XDR write(XDR xdr);
	  
	  public int getXid() {
	    return xid;
	  }

	  public Type getMessageType() {
	    return messageType;
	  }
	  
	  protected void validateMessageType(Type expected) {
	    if (expected != messageType) {
	      throw new IllegalArgumentException("Message type is expected to be "
	          + expected + " but got " + messageType);
	    }
	  }
	}
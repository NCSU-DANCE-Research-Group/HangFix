package hadoop15424;

public class PortmapResponse {
	  public static XDR voidReply(XDR xdr, int xid) {
	    RpcAcceptedReply.getAcceptInstance(xid, new VerifierNone()).write(xdr);
	    return xdr;
	  }

	  public static XDR intReply(XDR xdr, int xid, int value) {
	    RpcAcceptedReply.getAcceptInstance(xid, new VerifierNone()).write(xdr);
	    xdr.writeInt(value);
	    return xdr;
	  }

	  public static XDR booleanReply(XDR xdr, int xid, boolean value) {
	    RpcAcceptedReply.getAcceptInstance(xid, new VerifierNone()).write(xdr);
	    xdr.writeBoolean(value);
	    return xdr;
	  }

	  public static XDR pmapList(XDR xdr, int xid, PortmapMapping[] list) {
	    RpcAcceptedReply.getAcceptInstance(xid, new VerifierNone()).write(xdr);
	    for (PortmapMapping mapping : list) {
	      xdr.writeBoolean(true); // Value follows
	      mapping.serialize(xdr);
	    }
	    xdr.writeBoolean(false); // No value follows
	    return xdr;
	  }
}

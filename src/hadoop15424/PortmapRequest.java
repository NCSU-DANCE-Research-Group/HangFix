package hadoop15424;

public class PortmapRequest {
	  public static PortmapMapping mapping(XDR xdr) {
	    return PortmapMapping.deserialize(xdr);
	  }

	  public static XDR create(PortmapMapping mapping, boolean set) {
	    XDR request = new XDR();
	    int procedure = set ? RpcProgramPortmap.PMAPPROC_SET
	        : RpcProgramPortmap.PMAPPROC_UNSET;
	    RpcCall call = RpcCall.getInstance(
	        RpcUtil.getNewXid(String.valueOf(RpcProgramPortmap.PROGRAM)),
	        RpcProgramPortmap.PROGRAM, RpcProgramPortmap.VERSION, procedure,
	        new CredentialsNone(), new VerifierNone());
	    call.write(request);
	    return mapping.serialize(request);
	  }
	}

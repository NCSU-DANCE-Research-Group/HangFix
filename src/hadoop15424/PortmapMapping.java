package hadoop15424;

public class PortmapMapping {
	  public static final int TRANSPORT_TCP = 6;
	  public static final int TRANSPORT_UDP = 17;

	  private final int program;
	  private final int version;
	  private final int transport;
	  private final int port;

	  public PortmapMapping(int program, int version, int transport, int port) {
	    this.program = program;
	    this.version = version;
	    this.transport = transport;
	    this.port = port;
	  }

	  public XDR serialize(XDR xdr) {
	    xdr.writeInt(program);
	    xdr.writeInt(version);
	    xdr.writeInt(transport);
	    xdr.writeInt(port);
	    return xdr;
	  }

	  public static PortmapMapping deserialize(XDR xdr) {
	    return new PortmapMapping(xdr.readInt(), xdr.readInt(), xdr.readInt(),
	        xdr.readInt());
	  }

	  public int getPort() {
	    return port;
	  }

	  public static String key(PortmapMapping mapping) {
	    return mapping.program + " " + mapping.version + " " + mapping.transport;
	  }
	  
	  @Override
	  public String toString() {
	    return String.format("(PortmapMapping-%d:%d:%d:%d)", program, version,
	        transport, port);
	  }
	}

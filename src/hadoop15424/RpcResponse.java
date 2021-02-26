package hadoop15424;

import java.net.SocketAddress;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * RpcResponse encapsulates a response to a RPC request. It contains the data
 * that is going to cross the wire, as well as the information of the remote
 * peer.
 */
public class RpcResponse {
  private final ChannelBuffer data;
  private final SocketAddress remoteAddress;

  public RpcResponse(ChannelBuffer data, SocketAddress remoteAddress) {
    this.data = data;
    this.remoteAddress = remoteAddress;
  }

  public ChannelBuffer data() {
    return data;
  }

  public SocketAddress remoteAddress() {
    return remoteAddress;
  }
}

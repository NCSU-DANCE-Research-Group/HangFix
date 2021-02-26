package hadoop15424;

import java.net.SocketAddress;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;

/**
 * RpcInfo records all contextual information of an RPC message. It contains
 * the RPC header, the parameters, and the information of the remote peer.
 */
public final class RpcInfo {
  private final RpcMessage header;
  private final ChannelBuffer data;
  private final Channel channel;
  private final SocketAddress remoteAddress;

  public RpcInfo(RpcMessage header, ChannelBuffer data,
      ChannelHandlerContext channelContext, Channel channel,
      SocketAddress remoteAddress) {
    this.header = header;
    this.data = data;
    this.channel = channel;
    this.remoteAddress = remoteAddress;
  }

  public RpcMessage header() {
    return header;
  }

  public ChannelBuffer data() {
    return data;
  }

  public Channel channel() {
    return channel;
  }

  public SocketAddress remoteAddress() {
    return remoteAddress;
  }
}
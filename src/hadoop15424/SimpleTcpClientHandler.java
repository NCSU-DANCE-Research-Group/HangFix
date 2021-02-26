package hadoop15424;


import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

/**
 * A simple TCP based RPC client handler used by {@link SimpleTcpServer}.
 */
public class SimpleTcpClientHandler extends SimpleChannelHandler {
//  public static final Log LOG = LogFactory.getLog(SimpleTcpClient.class);
  protected final XDR request;

  public SimpleTcpClientHandler(XDR request) {
    this.request = request;
  }

  @Override
  public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
    // Send the request
//    if (LOG.isDebugEnabled()) {
//      LOG.debug("sending PRC request");
//    }
    ChannelBuffer outBuf = XDR.writeMessageTcp(request, true);
    e.getChannel().write(outBuf);
  }

  /**
   * Shutdown connection by default. Subclass can override this method to do
   * more interaction with the server.
   */
  @Override
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
    e.getChannel().close();
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
//    LOG.warn("Unexpected exception from downstream: ", e.getCause());
    e.getChannel().close();
  }
}


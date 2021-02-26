package hadoop15424;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

/**
 * Simple UDP server implemented using netty.
 */
public class SimpleTcpServer {
//  public static final Log LOG = LogFactory.getLog(SimpleTcpServer.class);
  protected final int port;
  protected int boundPort = -1; // Will be set after server starts
  protected final SimpleChannelUpstreamHandler rpcProgram;
  
  /** The maximum number of I/O worker threads */
  protected final int workerCount;

  /**
   * @param port TCP port where to start the server at
   * @param program RPC program corresponding to the server
   * @param workercount Number of worker threads
   */
  public SimpleTcpServer(int port, RpcProgram program, int workercount) {
    this.port = port;
    this.rpcProgram = program;
    this.workerCount = workercount;
  }
  
  public void run() {
    // Configure the Server.
    ChannelFactory factory;
    if (workerCount == 0) {
      // Use default workers: 2 * the number of available processors
      factory = new NioServerSocketChannelFactory(
          Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
    } else {
      factory = new NioServerSocketChannelFactory(
          Executors.newCachedThreadPool(), Executors.newCachedThreadPool(),
          workerCount);
    }
    
    ServerBootstrap bootstrap = new ServerBootstrap(factory);
    bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

      @Override
      public ChannelPipeline getPipeline() throws Exception {
        return Channels.pipeline(RpcUtil.constructRpcFrameDecoder(),
            RpcUtil.STAGE_RPC_MESSAGE_PARSER, rpcProgram,
            RpcUtil.STAGE_RPC_TCP_RESPONSE);
      }
    });
    bootstrap.setOption("child.tcpNoDelay", true);
    bootstrap.setOption("child.keepAlive", true);
    
    // Listen to TCP port
    Channel ch = bootstrap.bind(new InetSocketAddress(port));
    InetSocketAddress socketAddr = (InetSocketAddress) ch.getLocalAddress();
    boundPort = socketAddr.getPort();
    
//    LOG.info("Started listening to TCP requests at port " + boundPort + " for "
//        + rpcProgram + " with workerCount " + workerCount);
  }
  
  // boundPort will be set only after server starts
  public int getBoundPort() {
    return this.boundPort;
  }
}

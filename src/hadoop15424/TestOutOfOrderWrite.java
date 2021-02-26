package hadoop15424;

import java.util.Arrays;
import java.util.Random;






//import org.apache.hadoop.oncrpc.RpcAcceptedReply;
//import org.apache.hadoop.oncrpc.RpcCall;
//import org.apache.hadoop.oncrpc.RpcInfo;
//import org.apache.hadoop.oncrpc.RpcProgram;
//import org.apache.hadoop.oncrpc.RpcResponse;
//import org.apache.hadoop.oncrpc.RpcUtil;
//import org.apache.hadoop.oncrpc.SimpleTcpClient;
//import org.apache.hadoop.oncrpc.SimpleTcpServer;
//import org.apache.hadoop.oncrpc.XDR;
//import org.apache.hadoop.oncrpc.security.CredentialsNone;
//import org.apache.hadoop.oncrpc.security.VerifierNone;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelException;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;

public class TestOutOfOrderWrite {

  static class TestRpcProgram extends RpcProgram {

	    protected TestRpcProgram(String program, String host, int port,
	        int progNumber, int lowProgVersion, int highProgVersion,
	        boolean allowInsecurePorts) {
	      super(program, host, port, progNumber, lowProgVersion, highProgVersion,
	          null, allowInsecurePorts);
	    }

	    @Override
	    protected void handleInternal(ChannelHandlerContext ctx, RpcInfo info) {
	      // This is just like what's done in RpcProgramMountd#handleInternal and
	      // RpcProgramNfs3#handleInternal.
	      RpcCall rpcCall = (RpcCall) info.header();
	      final int procedure = rpcCall.getProcedure();
	      if (procedure != 0) {
	        boolean portMonitorSuccess = doPortMonitoring(info.remoteAddress());
	        if (!portMonitorSuccess) {
	          sendRejectedReply(rpcCall, info.remoteAddress(), ctx);
	          return;
	        }
	      }
	      
//	      resultSize = info.data().readableBytes();
	      RpcAcceptedReply reply = RpcAcceptedReply.getAcceptInstance(1234, new VerifierNone()); //response = id1234
	      XDR out = new XDR();
	      reply.write(out);
	      System.out.println("inside handleInternal, out = " + Arrays.toString(out.getBytes()));
	      ChannelBuffer b = ChannelBuffers.wrappedBuffer(out.asReadOnlyWrap().buffer());
	      
	      RpcResponse rsp = new RpcResponse(b, info.remoteAddress());
	      System.out.println(info.remoteAddress());
	      
//	      RpcMessage recv = info.header();//this is the received msg
//	      int xid = recv.getXid(); //get xid
//	      System.out.println("xid = " + xid);
//	      XDR in = new XDR(0);
//	      in.writeInt(xid);
	      
	      RpcUtil.sendRpcResponse(ctx, rsp);
	    }

	    @Override
	    protected boolean isIdempotent(RpcCall call) {
	      return false;
	    }
  }
 
  private static SimpleTcpServer tcpServer = null;
  private static int startRpcServer(boolean allowInsecurePorts) {
	    Random rand = new Random();
	    int serverPort = 30000 + rand.nextInt(10000);
	    System.out.println("serverPort = " + serverPort);
	    int retries = 10;    // A few retries in case initial choice is in use.

	    while (true) {
	      try {
	        RpcProgram program = new TestRpcProgram("TestRpcProgram",
	            "localhost", serverPort, 100000, 1, 2, allowInsecurePorts);
	        tcpServer = new SimpleTcpServer(serverPort, program, 1);
	        tcpServer.run();
	        break;          // Successfully bound a port, break out.
	      } catch (ChannelException ce) {
	        if (retries-- > 0) {
	          serverPort += rand.nextInt(20); // Port in use? Try another.
	        } else {
	          throw ce;     // Out of retries.
	        }
	      }
	    }
	    return serverPort;
  }
  
  static public void testFrames() {
	    int serverPort = startRpcServer(true);

	    XDR xdrOut = createGetportMount();
	    int headerSize = xdrOut.size();
	    int bufsize = 2 * 1024 * 1024;
	    byte[] buffer = new byte[bufsize];
	    xdrOut.writeFixedOpaque(buffer);
	    int requestSize = xdrOut.size() - headerSize;

	    // Send the request to the server
	    testRequest2(xdrOut, serverPort);
	    System.out.println("sent to the server.");

	    // Verify the server got the request with right size
//	    assertEquals(requestSize, resultSize);
	    
  }
  
  static void createPortmapXDRheader(XDR xdr_out, int procedure) {
	    // Make this a method
	    RpcCall.getInstance(0, 100000, 2, procedure, new CredentialsNone(), new VerifierNone()).write(xdr_out); //reply = id0
  }
  
  static XDR createGetportMount() {
	    XDR xdr_out = new XDR();
	    createPortmapXDRheader(xdr_out, 3);
	    System.out.println("xdr_out = " + Arrays.toString(xdr_out.getBytes()));
	    return xdr_out;
 }
  
  static void testRequest(XDR request, int serverPort) {
	    // Reset resultSize so as to avoid interference from other tests in this class.
//	    resultSize = 0;
	    SimpleTcpClient tcpClient = new SimpleTcpClient("localhost", serverPort, request,
	        true);
	    tcpClient.run();
  }

  static void testRequest2(XDR request, int serverPort) {
	    // Reset resultSize so as to avoid interference from other tests in this class.
	    WriteClient client = new WriteClient("localhost", serverPort, request, false);
	    client.run();
  }
  
  
  static class WriteClient extends SimpleTcpClient {

	    public WriteClient(String host, int port, XDR request, Boolean oneShot) {
	      super(host, port, request, oneShot);
	    }

	    @Override
	    protected ChannelPipelineFactory setPipelineFactory() {
	      this.pipelineFactory = new ChannelPipelineFactory() {
	        @Override
	        public ChannelPipeline getPipeline() {
	          return Channels.pipeline(
	              RpcUtil.constructRpcFrameDecoder(),
	              new WriteHandler(request));
	        }
	      };
	      return this.pipelineFactory;
	    }

 }
  
  
  static class WriteHandler extends SimpleTcpClientHandler {

	    public WriteHandler(XDR request) {
	      super(request);
	    }

	    @Override
	    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
	      // Get handle from create response
	      ChannelBuffer buf = (ChannelBuffer) e.getMessage();
	      byte[] serverResp = buf.array();
	      serverResp = new byte[0]; // inject faults here, making the received xdr be empty
	      XDR rsp = new XDR(serverResp);
	      System.out.println("get msg from server " + Arrays.toString(buf.array()));
	      System.out.println("serverResp.length = " + serverResp.length);
//	      if (rsp.getBytes().length == 0) {
////	        LOG.info("rsp length is zero, why?");
//	    	  System.out.println("rsp length is zero, why?");
//	        return;
//	      }
//	      LOG.info("rsp length=" + rsp.getBytes().length);

//	      RpcReply reply = RpcReply.read(rsp);
//	      int xid = reply.getXid();
//	      // Only process the create response
//	      if (xid != 0x8000004c) {
//	        return;
//	      }
	      XDR newxdr = new XDR(rsp.buffer().capacity());
	      newxdr.writeInt(1234);
	      int status = rsp.readInt();
//	      if (status != Nfs3Status.NFS3_OK) {
////	        LOG.error("Create failed, status =" + status);
//	        return;
//	      }
//	      LOG.info("Create succeeded");
//	      rsp.readBoolean(); // value follow
//	      handle = new FileHandle();
//	      handle.deserialize(rsp);
//	      channel = e.getChannel();
	    }
	  }

  
  
  
  public static void main(String[] args) throws InterruptedException {
	  
	  testFrames();
  }
}

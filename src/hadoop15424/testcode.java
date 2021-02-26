package hadoop15424;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.logging.Logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class testcode {
	public static void main(String[] args){
		testcode ins = new testcode();
		ins.testEnsureFreeSpace();
//		MessageEvent e = new MessageEvent();
	}
	
	public void testEnsureFreeSpace(){
		XDR xdr = new XDR(0);
		xdr.writeInt(1);
	}
	
	private static final Log LOG = LogFactory.getLog(testcode.class);
	
//	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
//	      ChannelBuffer buf = (ChannelBuffer) e.getMessage();
//	      ByteBuffer b = buf.toByteBuffer().asReadOnlyBuffer();
//	      XDR in = new XDR(b, XDR.State.READING);
//
//	      RpcInfo info = null;
//	      try {
//	        RpcCall callHeader = RpcCall.read(in);
//	        ChannelBuffer dataBuffer = ChannelBuffers.wrappedBuffer(in.buffer().slice());
//	        info = new RpcInfo(callHeader, dataBuffer, ctx, e.getChannel(),
//	            e.getRemoteAddress());
//	      } catch (Exception exc) {
//	        LOG.info("Malfromed RPC request from " + e.getRemoteAddress());
//	      }
//
////	      if (info != null) {
////	        Channels.fireMessageReceived(ctx, info);
////	      }
//	}
}

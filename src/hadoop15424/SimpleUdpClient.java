package hadoop15424;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

/**
 * A simple UDP based RPC client which just sends one request to a server.
 */
public class SimpleUdpClient {
  
  protected final String host;
  protected final int port;
  protected final XDR request;
  protected final boolean oneShot;
  protected final DatagramSocket clientSocket;

  public SimpleUdpClient(String host, int port, XDR request,
      DatagramSocket clientSocket) {
    this(host, port, request, true, clientSocket);
  }

  public SimpleUdpClient(String host, int port, XDR request, Boolean oneShot,
      DatagramSocket clientSocket) {
    this.host = host;
    this.port = port;
    this.request = request;
    this.oneShot = oneShot;
    this.clientSocket = clientSocket;
  }

  public void run() throws IOException {
    InetAddress IPAddress = InetAddress.getByName(host);
    byte[] sendData = request.getBytes();
    byte[] receiveData = new byte[65535];
    // Use the provided socket if there is one, else just make a new one.
    DatagramSocket socket = this.clientSocket == null ?
        new DatagramSocket() : this.clientSocket;

    try {
      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
          IPAddress, port);
      socket.send(sendPacket);
      socket.setSoTimeout(500);
      DatagramPacket receivePacket = new DatagramPacket(receiveData,
          receiveData.length);
      socket.receive(receivePacket);
  
      // Check reply status
      XDR xdr = new XDR(Arrays.copyOfRange(receiveData, 0,
          receivePacket.getLength()));
      RpcReply reply = RpcReply.read(xdr);
      if (reply.getState() != RpcReply.ReplyState.MSG_ACCEPTED) {
        throw new IOException("Request failed: " + reply.getState());
      }
    } finally {
      // If the client socket was passed in to this UDP client, it's on the
      // caller of this UDP client to close that socket.
      if (this.clientSocket == null) {
        socket.close();
      }
    }
  }
}


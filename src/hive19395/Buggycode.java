package hive19395;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Buggycode {
	
	private ByteBuffer current = null;
	private final int bufferSize;
	private long uncompressedBytes = 0;
	
	Buggycode(/*String name,*/
            int bufferSize /*,
            CompressionCodec codec,
            OutputReceiver receiver*/) throws IOException {
//    this.name = name;
    this.bufferSize = bufferSize;
//    this.codec = codec;
//    this.receiver = receiver;
//    this.suppress = false;
  }
	
	private void getNewInputBuffer() throws IOException {
//	    if (codec == null) {
	      current = ByteBuffer.allocate(bufferSize);
//	    } else {
//	      current = ByteBuffer.allocate(bufferSize + HEADER_SIZE);
//	      writeHeader(current, 0, bufferSize, true);
//	      current.position(HEADER_SIZE);
//	    }
	  }
	
	public void write(byte[] bytes, int offset, int length) throws IOException {
	    if (current == null) {
	      getNewInputBuffer();
	    }
	    int remaining = Math.min(current.remaining(), length);
	    current.put(bytes, offset, remaining);
	    uncompressedBytes += remaining;
	    length -= remaining;
	    while (length != 0) {
	      spill();
	      offset += remaining;
	      remaining = Math.min(current.remaining(), length);
	      System.out.println("inside loop, remaining = " + remaining);
	      current.put(bytes, offset, remaining);
	      uncompressedBytes += remaining;
	      length -= remaining;
	    }
	  }
	
	private void spill() throws java.io.IOException {
	    // if there isn't anything in the current buffer, don't spill
	    if (current == null ||
	        current.position() == 0) {
	      return;
	    }
	}
}

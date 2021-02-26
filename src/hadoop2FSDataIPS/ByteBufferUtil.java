package hadoop2FSDataIPS;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public final class ByteBufferUtil {

	  /**
	   * Determine if a stream can do a byte buffer read via read(ByteBuffer buf)
	   */
	  private static boolean streamHasByteBufferRead(InputStream stream) {
	    if (!(stream instanceof ByteBufferReadable)) {
	      return false;
	    }
	    if (!(stream instanceof FSDataInputStream)) {
	      return true;
	    }
	    return ((FSDataInputStream)stream).getWrappedStream() 
	        instanceof ByteBufferReadable;
	  }

	  /**
	   * Perform a fallback read.
	   */
	  public static ByteBuffer fallbackRead(
	      InputStream stream, ByteBufferPool bufferPool, int maxLength)
	          throws IOException {
	    if (bufferPool == null) {
	      throw new UnsupportedOperationException("zero-copy reads " +
	          "were not available, and you did not provide a fallback " +
	          "ByteBufferPool.");
	    }
	    boolean useDirect = streamHasByteBufferRead(stream);
	    ByteBuffer buffer = bufferPool.getBuffer(useDirect, maxLength);
	    if (buffer == null) {
	      throw new UnsupportedOperationException("zero-copy reads " +
	          "were not available, and the ByteBufferPool did not provide " +
	          "us with " + (useDirect ? "a direct" : "an indirect") +
	          "buffer.");
	    }
	    Preconditions.checkState(buffer.capacity() > 0);
	    Preconditions.checkState(buffer.isDirect() == useDirect);
	    maxLength = Math.min(maxLength, buffer.capacity());
	    boolean success = false;
	    try {
	      if (useDirect) {
	        buffer.clear();
	        buffer.limit(maxLength);
	        ByteBufferReadable readable = (ByteBufferReadable)stream;
	        int totalRead = 0;
	        while (true) {
	          if (totalRead >= maxLength) {
	            success = true;
	            break;
	          }
	          int nRead = readable.read(buffer);
	          if (nRead < 0) {
	            if (totalRead > 0) {
	              success = true;
	            }
	            break;
	          }
	          totalRead += nRead;
	        }
	        buffer.flip();
	      } else {
	        buffer.clear();
	        int nRead = stream.read(buffer.array(),
	            buffer.arrayOffset(), maxLength);
	        if (nRead >= 0) {
	          buffer.limit(nRead);
	          success = true;
	        }
	      }
	    } finally {
	      if (!success) {
	        // If we got an error while reading, or if we are at EOF, we 
	        // don't need the buffer any more.  We can give it back to the
	        // bufferPool.
	        bufferPool.putBuffer(buffer);
	        buffer = null;
	      }
	    }
	    return buffer;
	  }
	}

package hadoop2FSDataIPS;

import java.nio.ByteBuffer;

public interface ByteBufferPool {
	  /**
	   * Get a new direct ByteBuffer.  The pool can provide this from
	   * removing a buffer from its internal cache, or by allocating a 
	   * new buffer.
	   *
	   * @param direct     Whether the buffer should be direct.
	   * @param length     The minimum length the buffer will have.
	   * @return           A new ByteBuffer.  This ByteBuffer must be direct.
	   *                   Its capacity can be less than what was requested, but
	   *                   must be at least 1 byte.
	   */
	  ByteBuffer getBuffer(boolean direct, int length);

	  /**
	   * Release a buffer back to the pool.
	   * The pool may choose to put this buffer into its cache.
	   *
	   * @param buffer    a direct bytebuffer
	   */
	  void putBuffer(ByteBuffer buffer);
}

package hadoop2FSDataIPS;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.EnumSet;

/**
 * FSDataInputStreams implement this interface to provide enhanced
 * byte buffer access.  Usually this takes the form of mmap support.
 */
public interface HasEnhancedByteBufferAccess {
  /**
   * Get a ByteBuffer containing file data.
   *
   * This ByteBuffer may come from the stream itself, via a call like mmap,
   * or it may come from the ByteBufferFactory which is passed in as an
   * argument.
   *
   * @param factory
   *            If this is non-null, it will be used to create a fallback
   *            ByteBuffer when the stream itself cannot create one.
   * @param maxLength
   *            The maximum length of buffer to return.  We may return a buffer
   *            which is shorter than this.
   * @param opts
   *            Options to use when reading.
   *
   * @return
   *            We will always return an empty buffer if maxLength was 0,
   *            whether or not we are at EOF.
   *            If maxLength > 0, we will return null if the stream has
   *            reached EOF.
   *            Otherwise, we will return a ByteBuffer containing at least one 
   *            byte.  You must free this ByteBuffer when you are done with it 
   *            by calling releaseBuffer on it.  The buffer will continue to be
   *            readable until it is released in this manner.  However, the
   *            input stream's close method may warn about unclosed buffers.
   * @throws
   *            IOException: if there was an error reading.
   *            UnsupportedOperationException: if factory was null, and we
   *            needed an external byte buffer.  UnsupportedOperationException
   *            will never be thrown unless the factory argument is null.
   */
  public ByteBuffer read(ByteBufferPool factory, int maxLength,
      EnumSet<ReadOption> opts)
          throws IOException, UnsupportedOperationException;

  /**
   * Release a ByteBuffer which was created by the enhanced ByteBuffer read
   * function. You must not continue using the ByteBuffer after calling this 
   * function.
   *
   * @param buffer
   *            The ByteBuffer to release.
   */
  public void releaseBuffer(ByteBuffer buffer);
}

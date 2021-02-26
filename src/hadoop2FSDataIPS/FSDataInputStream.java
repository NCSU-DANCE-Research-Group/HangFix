package hadoop2FSDataIPS;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.EnumSet;


/** Utility that wraps a {@link FSInputStream} in a {@link DataInputStream}
 * and buffers input through a {@link BufferedInputStream}. */
//@InterfaceAudience.Public
//@InterfaceStability.Stable
public class FSDataInputStream extends DataInputStream
    implements Seekable, PositionedReadable, 
      ByteBufferReadable, HasFileDescriptor, CanSetDropBehind, CanSetReadahead,
      HasEnhancedByteBufferAccess {
  /**
   * Map ByteBuffers that we have handed out to readers to ByteBufferPool 
   * objects
   */
  private final IdentityHashStore<ByteBuffer, ByteBufferPool>
    extendedReadBuffers
      = new IdentityHashStore<ByteBuffer, ByteBufferPool>(0);

  public FSDataInputStream(InputStream in) {
    super(in);
    if( !(in instanceof Seekable) || !(in instanceof PositionedReadable) ) {
      throw new IllegalArgumentException(
          "In is not an instance of Seekable or PositionedReadable");
    }
  }
  
  /**
   * Seek to the given offset.
   *
   * @param desired offset to seek to
   */
  @Override
  public synchronized void seek(long desired) throws IOException {
    ((Seekable)in).seek(desired);
  }

  /**
   * Get the current position in the input stream.
   *
   * @return current position in the input stream
   */
  @Override
  public long getPos() throws IOException {
    return ((Seekable)in).getPos();
  }
  
  /**
   * Read bytes from the given position in the stream to the given buffer.
   *
   * @param position  position in the input stream to seek
   * @param buffer    buffer into which data is read
   * @param offset    offset into the buffer in which data is written
   * @param length    maximum number of bytes to read
   * @return total number of bytes read into the buffer, or <code>-1</code>
   *         if there is no more data because the end of the stream has been
   *         reached
   */
  @Override
  public int read(long position, byte[] buffer, int offset, int length)
    throws IOException {
    return ((PositionedReadable)in).read(position, buffer, offset, length);
  }

  /**
   * Read bytes from the given position in the stream to the given buffer.
   * Continues to read until <code>length</code> bytes have been read.
   *
   * @param position  position in the input stream to seek
   * @param buffer    buffer into which data is read
   * @param offset    offset into the buffer in which data is written
   * @param length    the number of bytes to read
   * @throws EOFException If the end of stream is reached while reading.
   *                      If an exception is thrown an undetermined number
   *                      of bytes in the buffer may have been written. 
   */
  @Override
  public void readFully(long position, byte[] buffer, int offset, int length)
    throws IOException {
    ((PositionedReadable)in).readFully(position, buffer, offset, length);
  }
  
  /**
   * See {@link #readFully(long, byte[], int, int)}.
   */
  @Override
  public void readFully(long position, byte[] buffer)
    throws IOException {
    ((PositionedReadable)in).readFully(position, buffer, 0, buffer.length);
  }
  
  /**
   * Seek to the given position on an alternate copy of the data.
   *
   * @param  targetPos  position to seek to
   * @return true if a new source is found, false otherwise
   */
  @Override
  public boolean seekToNewSource(long targetPos) throws IOException {
    return ((Seekable)in).seekToNewSource(targetPos); 
  }
  
  /**
   * Get a reference to the wrapped input stream. Used by unit tests.
   *
   * @return the underlying input stream
   */
//  @InterfaceAudience.LimitedPrivate({"HDFS"})
  public InputStream getWrappedStream() {
    return in;
  }

  @Override
  public int read(ByteBuffer buf) throws IOException {
    if (in instanceof ByteBufferReadable) {
      return ((ByteBufferReadable)in).read(buf);
    }

    throw new UnsupportedOperationException("Byte-buffer read unsupported by input stream");
  }

  @Override
  public FileDescriptor getFileDescriptor() throws IOException {
    if (in instanceof HasFileDescriptor) {
      return ((HasFileDescriptor) in).getFileDescriptor();
    } else if (in instanceof FileInputStream) {
      return ((FileInputStream) in).getFD();
    } else {
      return null;
    }
  }

  @Override
  public void setReadahead(Long readahead)
      throws IOException, UnsupportedOperationException {
    try {
      ((CanSetReadahead)in).setReadahead(readahead);
    } catch (ClassCastException e) {
      throw new UnsupportedOperationException(
          "this stream does not support setting the readahead " +
          "caching strategy.");
    }
  }

  @Override
  public void setDropBehind(Boolean dropBehind)
      throws IOException, UnsupportedOperationException {
    try {
      ((CanSetDropBehind)in).setDropBehind(dropBehind);
    } catch (ClassCastException e) {
      throw new UnsupportedOperationException("this stream does not " +
          "support setting the drop-behind caching setting.");
    }
  }

  @Override
  public ByteBuffer read(ByteBufferPool bufferPool, int maxLength,
      EnumSet<ReadOption> opts) 
          throws IOException, UnsupportedOperationException {
    try {
      return ((HasEnhancedByteBufferAccess)in).read(bufferPool,
          maxLength, opts);
    }
    catch (ClassCastException e) {
      ByteBuffer buffer = ByteBufferUtil.
          fallbackRead(this, bufferPool, maxLength);
      if (buffer != null) {
        extendedReadBuffers.put(buffer, bufferPool);
      }
      return buffer;
    }
  }

  private static final EnumSet<ReadOption> EMPTY_READ_OPTIONS_SET =
      EnumSet.noneOf(ReadOption.class);

  final public ByteBuffer read(ByteBufferPool bufferPool, int maxLength)
          throws IOException, UnsupportedOperationException {
    return read(bufferPool, maxLength, EMPTY_READ_OPTIONS_SET);
  }
  
  @Override
  public void releaseBuffer(ByteBuffer buffer) {
    try {
      ((HasEnhancedByteBufferAccess)in).releaseBuffer(buffer);
    }
    catch (ClassCastException e) {
      ByteBufferPool bufferPool = extendedReadBuffers.remove( buffer);
      if (bufferPool == null) {
        throw new IllegalArgumentException("tried to release a buffer " +
            "that was not created by this stream.");
      }
      bufferPool.putBuffer(buffer);
    }
  }
}

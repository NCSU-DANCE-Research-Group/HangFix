package hadoop2FSDataIPS;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.net.URI;

/**
 * Input Stream that generates specified number of random bytes.
 */
public class RandomInputStream extends InputStream
    implements Seekable, PositionedReadable {
	
//	private static final int DEFAULT_BUFFER_SIZE = 1024  * 1024; // 1MB

//  private final Random r = new Random();
//  private BytesWritable val = null;
  private InputStream in = null;
//  private int positionInVal = 0;// current position in the buffer 'val'
//
//  private long totalSize = 0;// total number of random bytes to be generated
  private long curPos = 0;// current position in this stream

  /**
   * @param size total number of random bytes to be generated in this stream
   * @param bufferSize the buffer size. An internal buffer array of length
   * <code>bufferSize</code> is created. If <code>bufferSize</code> is not a
   * positive number, then a default value of 1MB is used.
   */
//  RandomInputStream(long size, int bufferSize) {
//    totalSize = size;
//    if (bufferSize <= 0) {
//      bufferSize = DEFAULT_BUFFER_SIZE;
//    }
//    val = new BytesWritable(new byte[bufferSize]);
//  }
  
  public RandomInputStream(InputStream in){
	  this.in = in;
  }
  
  @Override
  public int read() throws IOException {
	  return in.read();
//    byte[] b = new byte[1];
//    if (curPos < totalSize) {
//      if (positionInVal < val.getLength()) {// use buffered byte
//        b[0] = val.getBytes()[positionInVal++];
//        ++curPos;
//      } else {// generate data
//        int num = read(b);
//        if (num < 0) {
//          return num;
//        }
//      }
//    } else {
//      return -1;
//    }
//    return b[0];
  }

  @Override
  public int read(byte[] bytes) throws IOException {
    return read(bytes, 0, bytes.length);
  }

  @Override
  public int read(byte[] bytes, int off, int len) throws IOException {
	  return in.read(bytes, off, len);
//    if (curPos == totalSize) {
//      return -1;// EOF
//    }
//    int numBytes = len;
//    if (numBytes > (totalSize - curPos)) {// position in file is close to EOF
//      numBytes = (int)(totalSize - curPos);
//    }
//    if (numBytes > (val.getLength() - positionInVal)) {
//      // need to generate data into val
//      r.nextBytes(val.getBytes());
//      positionInVal = 0;
//    }
//
//    System.arraycopy(val.getBytes(), positionInVal, bytes, off, numBytes);
//    curPos += numBytes;
//    positionInVal += numBytes;
//    return numBytes;
  }
  
//  @Override
//  public long skip(long len) throws IOException {
//	  long ret = in.skip(len);
//	  if(ret == 0)
//		  return -1;
//	  else
//		  return ret;
//  }

  @Override
  public int available() throws IOException {
	  return in.available();
//    return (int)(val.getLength() - positionInVal);
  }

  @Override
  public int read(long position, byte[] buffer, int offset, int length)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void readFully(long position, byte[] buffer) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void readFully(long position, byte[] buffer, int offset, int length)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  /**
   * Get the current position in this stream/pseudo-file
   * @return the position in this stream/pseudo-file
   * @throws IOException
   */
  @Override
  public long getPos() throws IOException {
    return curPos;
  }

  @Override
  public void seek(long pos) throws IOException {
//    throw new UnsupportedOperationException();
	  //do nothing
  }

  @Override
  public boolean seekToNewSource(long targetPos) throws IOException {
    throw new UnsupportedOperationException();
  }
}
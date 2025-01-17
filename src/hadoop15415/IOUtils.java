package hadoop15415;

import java.io.*;
import java.net.Socket;

import org.apache.commons.logging.Log;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
//import org.apache.hadoop.conf.Configuration;
import hadoop2Conf.Configuration;

/**
 * An utility class for I/O related functionality. 
 */
@InterfaceAudience.Public
@InterfaceStability.Evolving
public class IOUtils {

  /**
   * Copies from one stream to another.
   *
   * @param in InputStrem to read from
   * @param out OutputStream to write to
   * @param buffSize the size of the buffer 
   * @param close whether or not close the InputStream and 
   * OutputStream at the end. The streams are closed in the finally clause.  
   */
  public static void copyBytes(InputStream in, OutputStream out, int buffSize, boolean close) 
    throws IOException {
    try {
      copyBytes(in, out, buffSize);
      if(close) {
        out.close();
        out = null;
        in.close();
        in = null;
      }
    } finally {
      if(close) {
        closeStream(out);
        closeStream(in);
      }
    }
  }
  
  /**
   * Copies from one stream to another.
   * 
   * @param in InputStrem to read from
   * @param out OutputStream to write to
   * @param buffSize the size of the buffer 
   */
  public static void copyBytes(InputStream in, OutputStream out, int buffSize) 
    throws IOException {
    PrintStream ps = out instanceof PrintStream ? (PrintStream)out : null;
    byte buf[] = new byte[buffSize];
    int bytesRead = in.read(buf);
    while (bytesRead >= 0) {
    	System.out.println("inside loop...bytesRead = " + bytesRead);
      out.write(buf, 0, bytesRead);
      if ((ps != null) && ps.checkError()) {
        throw new IOException("Unable to write to output stream.");
      }
      bytesRead = in.read(buf);
    }
  }

  /**
   * Copies from one stream to another. <strong>closes the input and output streams 
   * at the end</strong>.
   *
   * @param in InputStrem to read from
   * @param out OutputStream to write to
   * @param conf the Configuration object 
   */
  public static void copyBytes(InputStream in, OutputStream out, Configuration conf)
    throws IOException {
    copyBytes(in, out, conf.mockGetInt("io.file.buffer.size", 4096), true);//inject faults here
  }
  
  /**
   * Copies from one stream to another.
   *
   * @param in InputStream to read from
   * @param out OutputStream to write to
   * @param conf the Configuration object
   * @param close whether or not close the InputStream and 
   * OutputStream at the end. The streams are closed in the finally clause.
   */
  public static void copyBytes(InputStream in, OutputStream out, Configuration conf, boolean close)
    throws IOException {
    copyBytes(in, out, conf.mockGetInt("io.file.buffer.size", 4096),  close);
  }

//  /**
//   * Copies count bytes from one stream to another.
//   *
//   * @param in InputStream to read from
//   * @param out OutputStream to write to
//   * @param count number of bytes to copy
//   * @param close whether to close the streams
//   * @throws IOException if bytes can not be read or written
//   */
//  public static void copyBytes(InputStream in, OutputStream out, long count,
//      boolean close) throws IOException {
//    byte buf[] = new byte[4096];
//    long bytesRemaining = count;
//    int bytesRead;
//
//    try {
//      while (bytesRemaining > 0) {
//        int bytesToRead = (int)
//          (bytesRemaining < buf.length ? bytesRemaining : buf.length);
//
//        bytesRead = in.read(buf, 0, bytesToRead);
//        if (bytesRead == -1)
//          break;
//
//        out.write(buf, 0, bytesRead);
//        bytesRemaining -= bytesRead;
//      }
//      if (close) {
//        out.close();
//        out = null;
//        in.close();
//        in = null;
//      }
//    } finally {
//      if (close) {
//        closeStream(out);
//        closeStream(in);
//      }
//    }
//  }
  
//  /**
//   * Reads len bytes in a loop.
//   *
//   * @param in InputStream to read from
//   * @param buf The buffer to fill
//   * @param off offset from the buffer
//   * @param len the length of bytes to read
//   * @throws IOException if it could not read requested number of bytes 
//   * for any reason (including EOF)
//   */
//  public static void readFully(InputStream in, byte buf[],
//      int off, int len) throws IOException {
//    int toRead = len;
//    while (toRead > 0) {
//      int ret = in.read(buf, off, toRead);
//      if (ret < 0) {
//        throw new IOException( "Premature EOF from inputStream");
//      }
//      toRead -= ret;
//      off += ret;
//    }
//  }
//  
//  /**
//   * Similar to readFully(). Skips bytes in a loop.
//   * @param in The InputStream to skip bytes from
//   * @param len number of bytes to skip.
//   * @throws IOException if it could not skip requested number of bytes 
//   * for any reason (including EOF)
//   */
//  public static void skipFully(InputStream in, long len) throws IOException {
//    while (len > 0) {
//      long ret = in.skip(len);
//      if (ret < 0) {
//        throw new IOException( "Premature EOF from inputStream");
//      }
//      len -= ret;
//    }
//  }
  
  /**
   * Close the Closeable objects and <b>ignore</b> any {@link IOException} or 
   * null pointers. Must only be used for cleanup in exception handlers.
   *
   * @param log the log to record problems to at debug level. Can be null.
   * @param closeables the objects to close
   */
  public static void cleanup(Log log, java.io.Closeable... closeables) {
    for (java.io.Closeable c : closeables) {
      if (c != null) {
        try {
          c.close();
        } catch(IOException e) {
          if (log != null && log.isDebugEnabled()) {
            log.debug("Exception in closing " + c, e);
          }
        }
      }
    }
  }

  /**
   * Closes the stream ignoring {@link IOException}.
   * Must only be called in cleaning up from exception handlers.
   *
   * @param stream the Stream to close
   */
  public static void closeStream(java.io.Closeable stream) {
    cleanup(null, stream);
  }
  
  /**
   * Closes the socket ignoring {@link IOException}
   *
   * @param sock the Socket to close
   */
  public static void closeSocket(Socket sock) {
    if (sock != null) {
      try {
        sock.close();
      } catch (IOException ignored) {
      }
    }
  }
  
  /**
   * The /dev/null of OutputStreams.
   */
  public static class NullOutputStream extends OutputStream {
    public void write(byte[] b, int off, int len) throws IOException {
    }

    public void write(int b) throws IOException {
    }
  }  
}

package compress87;

import java.io.IOException;
import java.io.InputStream;

public abstract class ArchiveInputStream extends InputStream {

    private byte[] SINGLE = new byte[1];
    private static final int BYTE_MASK = 0xFF;
    
    /** holds the number of bytes read in this stream */
    private int bytesRead = 0;

    /**
     * Returns the next Archive Entry in this Stream.
     *
     * @return the next entry,
     *         or <code>null</code> if there are no more entries
     * @throws IOException if the next entry could not be read
     */
    public abstract ArchiveEntry getNextEntry() throws IOException;

    /*
     * Note that subclasses also implement specific get() methods which
     * return the appropriate class without need for a cast.
     * See SVN revision r743259
     * @return
     * @throws IOException
     */
    // public abstract XXXArchiveEntry getNextXXXEntry() throws IOException;

    /**
     * Reads a byte of data. This method will block until enough input is
     * available.
     * 
     * Simply calls the {@link #read(byte[], int, int)} method.
     * 
     * MUST be overridden if the {@link #read(byte[], int, int)} method
     * is not overridden; may be overridden otherwise.
     * 
     * @return the byte read, or -1 if end of input is reached
     * @throws IOException
     *             if an I/O error has occurred or if a CPIO file error has
     *             occurred
     */
    public int read() throws IOException {
        int num = read(SINGLE, 0, 1);
        return num == -1 ? -1 : SINGLE[0] & BYTE_MASK;
    }
    
    /**
     * Increments the counter of already read bytes.
     * Doesn't increment if the EOF has been hit (read == -1)
     * 
     * @param read the number of bytes read
     */
    protected void count(int read) {
        if(read != -1) {
            bytesRead = bytesRead + read;
        }
    }
    
    /**
     * Returns the current number of bytes read from this stream.
     * @return the number of read bytes
     */
    public int getCount() {
        return bytesRead;
    }
}

package compress87;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public abstract class ArchiveOutputStream extends OutputStream {
    
    /** Temporary buffer used for the {@link #write(int)} method */
    private final byte[] oneByte = new byte[1];
    static final int BYTE_MASK = 0xFF;

    /** holds the number of bytes read in this stream */
    private int bytesRead = 0;
    // Methods specific to ArchiveOutputStream
    
    /**
     * Writes the headers for an archive entry to the output stream.
     * The caller must then write the content to the stream and call
     * {@link #closeArchiveEntry()} to complete the process.
     * 
     * @param entry describes the entry
     * @throws IOException
     */
    public abstract void putArchiveEntry(ArchiveEntry entry) throws IOException;

    /**
     * Closes the archive entry, writing any trailer information that may
     * be required.
     * @throws IOException
     */
    public abstract void closeArchiveEntry() throws IOException;
    
    /**
     * Finishes the addition of entries to this stream, without closing it.
     * Additional data can be written, if the format supports it.
     * 
     * The finish() method throws an Exception if the user forgets to close the entry
     * .
     * @throws IOException
     */
    public abstract void finish() throws IOException;

    /**
     * Create an archive entry using the inputFile and entryName provided.
     * 
     * @param inputFile
     * @param entryName 
     * @return the ArchiveEntry set up with details from the file
     * 
     * @throws IOException
     */
    public abstract ArchiveEntry createArchiveEntry(File inputFile, String entryName) throws IOException;
    
    // Generic implementations of OutputStream methods that may be useful to sub-classes
    
    /**
     * Writes a byte to the current archive entry.
     *
     * This method simply calls write( byte[], 0, 1 ).
     *
     * MUST be overridden if the {@link #write(byte[], int, int)} method
     * is not overridden; may be overridden otherwise.
     * 
     * @param b The byte to be written.
     * @throws IOException on error
     */
    public void write(int b) throws IOException {
        oneByte[0] = (byte) (b & BYTE_MASK);
        write(oneByte, 0, 1);
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

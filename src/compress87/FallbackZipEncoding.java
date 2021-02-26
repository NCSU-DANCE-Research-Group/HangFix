package compress87;

import java.io.IOException;
import java.nio.ByteBuffer;

class FallbackZipEncoding implements ZipEncoding {
    private final String charset;

    /**
     * Construct a fallback zip encoding, which uses the platform's
     * default charset.
     */
    public FallbackZipEncoding() {
        this.charset = null;
    }

    /**
     * Construct a fallback zip encoding, which uses the given charset.
     * 
     * @param charset The name of the charset or <code>null</code> for
     *                the platform's default character set.
     */
    public FallbackZipEncoding(String charset) {
        this.charset = charset;
    }

    /**
     * @see
     * org.apache.commons.compress.archivers.zip.ZipEncoding#canEncode(java.lang.String)
     */
    public boolean canEncode(String name) {
        return true;
    }

    /**
     * @see
     * org.apache.commons.compress.archivers.zip.ZipEncoding#encode(java.lang.String)
     */
    public ByteBuffer encode(String name) throws IOException {
        if (this.charset == null) { // i.e. use default charset, see no-args constructor
            return ByteBuffer.wrap(name.getBytes());
        } else {
            return ByteBuffer.wrap(name.getBytes(this.charset));
        }
    }

    /**
     * @see
     * org.apache.commons.compress.archivers.zip.ZipEncoding#decode(byte[])
     */
    public String decode(byte[] data) throws IOException {
        if (this.charset == null) { // i.e. use default charset, see no-args constructor
            return new String(data);
        } else {
            return new String(data,this.charset);
        }
    }
}
package compress87;

import java.io.IOException;
import java.nio.ByteBuffer;

interface ZipEncoding {
    /**
     * Check, whether the given string may be losslessly encoded using this
     * encoding.
     * 
     * @param name A filename or ZIP comment.
     * @return Whether the given name may be encoded with out any losses.
     */
    boolean canEncode(String name);

    /**
     * Encode a filename or a comment to a byte array suitable for
     * storing it to a serialized zip entry.
     * 
     * <p>Examples for CP 437 (in pseudo-notation, right hand side is
     * C-style notation):</p>
     * <pre>
     *  encode("\u20AC_for_Dollar.txt") = "%U20AC_for_Dollar.txt"
     *  encode("\u00D6lf\u00E4sser.txt") = "\231lf\204sser.txt"
     * </pre>
     * 
     * @param name A filename or ZIP comment. 
     * @return A byte buffer with a backing array containing the
     *         encoded name.  Unmappable characters or malformed
     *         character sequences are mapped to a sequence of utf-16
     *         words encoded in the format <code>%Uxxxx</code>.  It is
     *         assumed, that the byte buffer is positioned at the
     *         beginning of the encoded result, the byte buffer has a
     *         backing array and the limit of the byte buffer points
     *         to the end of the encoded result.
     * @throws IOException 
     */
    ByteBuffer encode(String name) throws IOException;

    /**
     * @param data The byte values to decode.
     * @return The decoded string.
     * @throws IOException 
     */
    String decode(byte [] data) throws IOException;
}
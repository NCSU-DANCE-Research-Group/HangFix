package compress87;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

class NioZipEncoding implements ZipEncoding {
    private final Charset charset;

    /**
     * Construct an NIO based zip encoding, which wraps the given
     * charset.
     * 
     * @param charset The NIO charset to wrap.
     */
    public NioZipEncoding(Charset charset) {
        this.charset = charset;
    }

    /**
     * @see
     * org.apache.commons.compress.archivers.zip.ZipEncoding#canEncode(java.lang.String)
     */
    public boolean canEncode(String name) {
        CharsetEncoder enc = this.charset.newEncoder();
        enc.onMalformedInput(CodingErrorAction.REPORT);
        enc.onUnmappableCharacter(CodingErrorAction.REPORT);

        return enc.canEncode(name);
    }

    /**
     * @see
     * org.apache.commons.compress.archivers.zip.ZipEncoding#encode(java.lang.String)
     */
    public ByteBuffer encode(String name) {
        CharsetEncoder enc = this.charset.newEncoder();

        enc.onMalformedInput(CodingErrorAction.REPORT);
        enc.onUnmappableCharacter(CodingErrorAction.REPORT);

        CharBuffer cb = CharBuffer.wrap(name);
        ByteBuffer out = ByteBuffer.allocate(name.length()
                                             + (name.length() + 1) / 2);

        while (cb.remaining() > 0) {
            CoderResult res = enc.encode(cb, out,true);

            if (res.isUnmappable() || res.isMalformed()) {

                // write the unmappable characters in utf-16
                // pseudo-URL encoding style to ByteBuffer.
                if (res.length() * 6 > out.remaining()) {
                    out = ZipEncodingHelper.growBuffer(out, out.position()
                                                       + res.length() * 6);
                }

                for (int i=0; i<res.length(); ++i) {
                    ZipEncodingHelper.appendSurrogate(out,cb.get());
                }

            } else if (res.isOverflow()) {

                out = ZipEncodingHelper.growBuffer(out, 0);

            } else if (res.isUnderflow()) {

                enc.flush(out);
                break;

            }
        }

        out.limit(out.position());
        out.rewind();
        return out;
    }

    /**
     * @see
     * org.apache.commons.compress.archivers.zip.ZipEncoding#decode(byte[])
     */
    public String decode(byte[] data) throws IOException {
        return this.charset.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
            .decode(ByteBuffer.wrap(data)).toString();
    }
}

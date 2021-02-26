package compress87;

public class UnicodePathExtraField extends AbstractUnicodeExtraField {

    public static final ZipShort UPATH_ID = new ZipShort(0x7075);

    public UnicodePathExtraField () { 
    }

    /**
     * Assemble as unicode path extension from the name given as
     * text as well as the encoded bytes actually written to the archive.
     * 
     * @param text The file name
     * @param bytes the bytes actually written to the archive
     * @param off The offset of the encoded filename in <code>bytes</code>.
     * @param len The length of the encoded filename or comment in
     * <code>bytes</code>.
     */
    public UnicodePathExtraField(String text, byte[] bytes, int off, int len) {
        super(text, bytes, off, len);
    }

    /**
     * Assemble as unicode path extension from the name given as
     * text as well as the encoded bytes actually written to the archive.
     * 
     * @param name The file name
     * @param bytes the bytes actually written to the archive
     */
    public UnicodePathExtraField(String name, byte[] bytes) {
        super(name, bytes);
    }

    public ZipShort getHeaderId() {
        return UPATH_ID;
    }
}
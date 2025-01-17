package compress87;

public final class ZipLong implements Cloneable {

    private static final int WORD = 4;
    //private static final int BYTE_BIT_SIZE = 8;
    private static final int BYTE_MASK = 0xFF;

    private static final int BYTE_1 = 1;
    private static final int BYTE_1_MASK = 0xFF00;
    private static final int BYTE_1_SHIFT = 8;

    private static final int BYTE_2 = 2;
    private static final int BYTE_2_MASK = 0xFF0000;
    private static final int BYTE_2_SHIFT = 16;

    private static final int BYTE_3 = 3;
    private static final long BYTE_3_MASK = 0xFF000000L;
    private static final int BYTE_3_SHIFT = 24;

    private final long value;

    /** Central File Header Signature */
    public static final ZipLong CFH_SIG = new ZipLong(0X02014B50L);

    /** Local File Header Signature */
    public static final ZipLong LFH_SIG = new ZipLong(0X04034B50L);

    /**
     * Create instance from a number.
     * @param value the long to store as a ZipLong
     */
    public ZipLong(long value) {
        this.value = value;
    }

    /**
     * Create instance from bytes.
     * @param bytes the bytes to store as a ZipLong
     */
    public ZipLong (byte[] bytes) {
        this(bytes, 0);
    }

    /**
     * Create instance from the four bytes starting at offset.
     * @param bytes the bytes to store as a ZipLong
     * @param offset the offset to start
     */
    public ZipLong (byte[] bytes, int offset) {
        value = ZipLong.getValue(bytes, offset);
    }

    /**
     * Get value as four bytes in big endian byte order.
     * @return value as four bytes in big endian order
     */
    public byte[] getBytes() {
        return ZipLong.getBytes(value);
    }

    /**
     * Get value as Java long.
     * @return value as a long
     */
    public long getValue() {
        return value;
    }

    /**
     * Get value as four bytes in big endian byte order.
     * @param value the value to convert
     * @return value as four bytes in big endian byte order
     */
    public static byte[] getBytes(long value) {
        byte[] result = new byte[WORD];
        result[0] = (byte) ((value & BYTE_MASK));
        result[BYTE_1] = (byte) ((value & BYTE_1_MASK) >> BYTE_1_SHIFT);
        result[BYTE_2] = (byte) ((value & BYTE_2_MASK) >> BYTE_2_SHIFT);
        result[BYTE_3] = (byte) ((value & BYTE_3_MASK) >> BYTE_3_SHIFT);
        return result;
    }

    /**
     * Helper method to get the value as a Java long from four bytes starting at given array offset
     * @param bytes the array of bytes
     * @param offset the offset to start
     * @return the correspondanding Java long value
     */
    public static long getValue(byte[] bytes, int offset) {
        long value = (bytes[offset + BYTE_3] << BYTE_3_SHIFT) & BYTE_3_MASK;
        value += (bytes[offset + BYTE_2] << BYTE_2_SHIFT) & BYTE_2_MASK;
        value += (bytes[offset + BYTE_1] << BYTE_1_SHIFT) & BYTE_1_MASK;
        value += (bytes[offset] & BYTE_MASK);
        return value;
    }

    /**
     * Helper method to get the value as a Java long from a four-byte array
     * @param bytes the array of bytes
     * @return the correspondanding Java long value
     */
    public static long getValue(byte[] bytes) {
        return getValue(bytes, 0);
    }

    /**
     * Override to make two instances with same value equal.
     * @param o an object to compare
     * @return true if the objects are equal
     */
    public boolean equals(Object o) {
        if (o == null || !(o instanceof ZipLong)) {
            return false;
        }
        return value == ((ZipLong) o).getValue();
    }

    /**
     * Override to make two instances with same value equal.
     * @return the value stored in the ZipLong
     */
    public int hashCode() {
        return (int) value;
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException cnfe) {
            // impossible
            throw new RuntimeException(cnfe);
        }
    }
}

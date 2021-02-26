package cassandraIO;


import java.nio.ByteBuffer;

public class ByteBufferUtil {
	public static String bytesToHex(ByteBuffer bytes)
    {
        final int offset = bytes.position();
        final int size = bytes.remaining();
        final char[] c = new char[size * 2];
        for (int i = 0; i < size; i++)
        {
            final int bint = bytes.get(i+offset);
            c[i * 2] = Hex.byteToChar[(bint & 0xf0) >> 4];
            c[1 + i * 2] = Hex.byteToChar[bint & 0x0f];
        }
        return Hex.wrapCharArray(c);
    }
	
	public static int compareUnsigned(ByteBuffer o1, ByteBuffer o2)
    {
        assert o1 != null;
        assert o2 != null;
        if (o1 == o2)
            return 0;

        if (o1.hasArray() && o2.hasArray())
        {
            return FBUtilities.compareUnsigned(o1.array(), o2.array(), o1.position() + o1.arrayOffset(),
                    o2.position() + o2.arrayOffset(), o1.remaining(), o2.remaining());
        }

        int end1 = o1.position() + o1.remaining();
        int end2 = o2.position() + o2.remaining();
        for (int i = o1.position(), j = o2.position(); i < end1 && j < end2; i++, j++)
        {
            int a = (o1.get(i) & 0xff);
            int b = (o2.get(j) & 0xff);
            if (a != b)
                return a - b;
        }
        return o1.remaining() - o2.remaining();
    }
}

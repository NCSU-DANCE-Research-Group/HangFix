package cassandraIO;

import java.lang.reflect.Constructor;

public class Hex
{
    private static final Constructor<String> stringConstructor = getProtectedConstructor(String.class, int.class, int.class, char[].class);
    private final static byte[] charToByte = new byte[256];

    // package protected for use by ByteBufferUtil. Do not modify this array !!
    static final char[] byteToChar = new char[16];
    static
    {
        for (char c = 0; c < charToByte.length; ++c)
        {
            if (c >= '0' && c <= '9')
                charToByte[c] = (byte)(c - '0');
            else if (c >= 'A' && c <= 'F')
                charToByte[c] = (byte)(c - 'A' + 10);
            else if (c >= 'a' && c <= 'f')
                charToByte[c] = (byte)(c - 'a' + 10);
            else
                charToByte[c] = (byte)-1;
        }

        for (int i = 0; i < 16; ++i)
        {
            byteToChar[i] = Integer.toHexString(i).charAt(0);
        }
    }

    public static byte[] hexToBytes(String str)
    {
        if (str.length() % 2 == 1)
            throw new NumberFormatException("An hex string representing bytes must have an even length");

        byte[] bytes = new byte[str.length() / 2];
        for (int i = 0; i < bytes.length; i++)
        {
            byte halfByte1 = charToByte[str.charAt(i * 2)];
            byte halfByte2 = charToByte[str.charAt(i * 2 + 1)];
            if (halfByte1 == -1 || halfByte2 == -1)
                throw new NumberFormatException("Non-hex characters in " + str);
            bytes[i] = (byte)((halfByte1 << 4) | halfByte2);
        }
        return bytes;
    }

    public static String bytesToHex(byte... bytes)
    {
        char[] c = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++)
        {
            int bint = bytes[i];
            c[i * 2] = byteToChar[(bint & 0xf0) >> 4];
            c[1 + i * 2] = byteToChar[bint & 0x0f];
        }

        return wrapCharArray(c);
    }

    /**
     * Create a String from a char array with zero-copy (if available), using reflection to access a package-protected constructor of String.
     * */
    public static String wrapCharArray(char[] c)
    {
        if (c == null)
            return null;

        String s = null;

        if (stringConstructor != null)
        {
            try
            {
                s = stringConstructor.newInstance(0, c.length, c);
            }
            catch (Exception e)
            {
                // Swallowing as we'll just use a copying constructor
            }
        }
        return s == null ? new String(c) : s;
    }

    /**
     * Used to get access to protected/private constructor of the specified class
     * @param klass - name of the class
     * @param paramTypes - types of the constructor parameters
     * @return Constructor if successful, null if the constructor cannot be
     * accessed
     */
    public static Constructor getProtectedConstructor(Class klass, Class... paramTypes)
    {
        Constructor c;
        try
        {
            c = klass.getDeclaredConstructor(paramTypes);
            c.setAccessible(true);
            return c;
        }
        catch (Exception e)
        {
            return null;
        }
    }
}


package cassandraIO;

public class FBUtilities {
	public static int compareUnsigned(byte[] bytes1, byte[] bytes2, int offset1, int offset2, int len1, int len2)
    {
        return FastByteComparisons.compareTo(bytes1, offset1, len1, bytes2, offset2, len2);
    }
}

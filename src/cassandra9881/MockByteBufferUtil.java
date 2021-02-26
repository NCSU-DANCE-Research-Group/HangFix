package cassandra9881;

import java.io.DataInput;
import java.io.IOException;
import java.nio.ByteBuffer;

import cassandraIO.FileDataInput;


public class MockByteBufferUtil {
	public static final ByteBuffer EMPTY_BYTE_BUFFER = ByteBuffer.wrap(new byte[0]);
	
	public static ByteBuffer readWithShortLength(DataInput in) throws IOException
    {
		throw new IOException("corrupted DataInput...");
//        return MockByteBufferUtil.read(in, readShortLength(in));
    }
	
	public static ByteBuffer read(DataInput in, int length) throws IOException
    {
        if (length == 0)
            return EMPTY_BYTE_BUFFER;

        if (in instanceof FileDataInput)
            return ((FileDataInput) in).readBytes(length);

        byte[] buff = new byte[length];
        in.readFully(buff);
        return ByteBuffer.wrap(buff);
    }

	public static int readShortLength(DataInput in) throws IOException
    {
        return in.readUnsignedShort();
    }
	
	
}

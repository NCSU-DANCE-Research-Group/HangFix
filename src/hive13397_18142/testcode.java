package hive13397_18142;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.Test;

public class testcode {
	
	@Test
	public void testReadLongBE() throws IOException{
		SerializationUtils su = new SerializationUtils();
		long[] buffer = new long[2];
		int offset = 1;
		int len = 9;
		int bitSize = 8;
		String name = "1234";
		ByteBuffer[] input = new ByteBuffer[2];
        long[] offsets = new long[2];
        long length = 0;
        CompressionCodec codec = null;
        int bufferSize = 5;
        InStream in = new InStream.CompressedStream(name, input, offsets, length, codec, bufferSize);
		su.readInts(buffer, offset, len, bitSize, in);
	
	}
	
	@Test
	public void testReadRemainingLongs() throws IOException {
		SerializationUtils su = new SerializationUtils();
		long[] buffer = new long[2];
		int offset = 1;
		int len = 7;
		int bitSize = 8;
		String name = "1234";
		ByteBuffer[] input = new ByteBuffer[2];
        long[] offsets = new long[2];
        long length = 0;
        CompressionCodec codec = null;
        int bufferSize = 5;
        InStream in = new InStream.CompressedStream(name, input, offsets, length, codec, bufferSize);
		su.readInts(buffer, offset, len, bitSize, in);
	}
}

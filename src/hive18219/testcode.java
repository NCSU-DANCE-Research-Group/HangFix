package hive18219;

import java.io.IOException;

public class testcode {
	
	public static void main(String[] args){
		testcode inst = new testcode();
		try {
			inst.testSkipBytes();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void testSkipBytes() throws IOException{
		NonSyncDataInputBuffer inputBuff = new NonSyncDataInputBuffer();
		byte[] input = new byte[]{0x1, 0x2, 0x3, 0x4};
		inputBuff.reset(input, 4);
		inputBuff.skipBytes(1);
	}
}

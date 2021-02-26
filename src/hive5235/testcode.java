package hive5235;

import java.io.IOException;
import java.nio.ByteBuffer;

public class testcode {
	public static void main(String[] args){
		testcode inst = new testcode();
		inst.testDecompress();
	}
	
	
	public void testDecompress(){
		Buggycode bc = new Buggycode();
		ByteBuffer bb = ByteBuffer.allocate(4); 
        bb.put((byte)20); 
        bb.put((byte)30); 
        bb.put((byte)40); 
        bb.put((byte)50); 
        bb.rewind(); 
        
        ByteBuffer bb2 = ByteBuffer.allocate(4); 
        try {
			bc.decompress(bb, bb2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

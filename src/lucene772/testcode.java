package lucene772;

import java.io.IOException;

public class testcode {
	public static void main(String[] args){
		testcode inst = new testcode();
		inst.testUncompress();
	}
	
	public void testUncompress(){
		Buggycode bc = new Buggycode();
		byte[] input = new byte[]{0x1, 0x2, 0x3, 0x4};
		try {
			bc.uncompress(input);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

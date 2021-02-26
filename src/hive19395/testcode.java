package hive19395;

import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class testcode {
	public static void main(String[] args){
		testcode inst = new testcode();
		try {
			inst.testwrite();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void testwrite() throws IOException{
		Buggycode bc = new Buggycode(0);
		byte[] bytes = new byte[]{0x0, 0x1};
		bc.write(bytes, 0, 2);
	}
}

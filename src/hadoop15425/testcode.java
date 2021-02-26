package hadoop15425;


import hadoop2FSDataIPS.FSDataInputStream;
import hadoop2FSDataIPS.RandomInputStream;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;



public class testcode {
	public static void main(String[] args) throws IOException{
		testcode ins = new testcode();
		ins.testslowReadUntilMatch();
	}
	
	public void testslowReadUntilMatch() throws IOException{
//		long fileSize = 100;
//		int bufferSize = 0;
//		InputStream in = new RandomInputStream(fileSize, bufferSize);
		
		
		DataInputStream in3 = new DataInputStream(new ByteArrayInputStream("aaabbbbbbb".getBytes()));
//		while(true)
//			System.out.print((char)in3.readByte());
		InputStream in = new RandomInputStream(in3);
//		FSDataInputStream in = new FSDataInputStream(in3);    
		
		Buggycode inst = new Buggycode();
		inst.configure(0); //inject faults
		inst.doCopyFile(new FSDataInputStream(in));
		
	}
}

package kafka6271;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class testcode {
	public static void main(String[] args){
		testcode inst = new testcode();
		try {
			inst.testPoll();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void testPoll() throws IOException{
		byte inArray[] = new byte[] {0, 1, 2, 3, 4};
		String filename = "/home/ting/DataLoopBugDetection/workspace_java/testInstrumentJavaClass/bin/kafka6271/test.txt";
		File f = new File(filename);
		FileOutputStream fileout = new FileOutputStream(f);
		try{
			fileout.write(inArray);
		} finally {
			fileout.close();
		}
		
		Buggycode bc = new Buggycode(filename);
		Object lastRecordedOffset = (long) 5;
		try {
			bc.poll(lastRecordedOffset);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

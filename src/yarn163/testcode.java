package yarn163;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class testcode {
	
	
	public static void main(String[] args){
		testcode inst = new testcode();
		try {
			inst.testContainerLogsPage();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void testContainerLogsPage() throws IOException{
		
		byte inArray[] = new byte[] {0, 1, 2, 3, 4};
		File f = new File("/home/ting/DataLoopBugDetection/workspace_java/testInstrumentJavaClass/bin/yarn163/test.txt");
		FileOutputStream fileout = new FileOutputStream(f);
		try{
			fileout.write(inArray);
		} finally {
			fileout.close();
		}
		String startS = "1";
		String endS = "2";
		File logFile = f;
		
		
		Buggycode bc = new Buggycode();
		
		
		
		bc.printLogs(f, startS, endS);
	}
}

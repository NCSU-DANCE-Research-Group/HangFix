package mapreduce6990;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class testcode {
	 public static void main(String args[]) throws IOException{
	    	testReader();
	 }
	 
		// FileInputStream can skip as more than available().
		public static void testReader() throws IOException {
			byte inArray[] = new byte[] {0, 1, 2, 3, 4};
			File f = new File("/home/ting/DataLoopBugDetection/workspace_java/testInstrumentJavaClass/dataset/test.txt");
			FileOutputStream fileout = new FileOutputStream(f);
			try{
				fileout.write(inArray);
			} finally {
				fileout.close();
			}
			FileInputStream file = new FileInputStream(f);
			System.out.println("file length = " + file.available());
			
//			byte[] buffer1 = new byte[5];
//			BufferedInputStream inputStream = new BufferedInputStream(file); 
//			inputStream.read(buffer1, 0, buffer1.length); 
			
			
			long start = 6;
	    	long end = 6;
		    InputStream ins = new Buggycode.Reader(f,start,end);
		}
}

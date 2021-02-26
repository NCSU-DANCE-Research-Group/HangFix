package compress451;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class testcode {
	public static void main(String[] args){
		testcode inst = new testcode();
		try {
			inst.testCopy();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void testCopy() throws IOException{
    	String fileInput = "/home/ting/DataLoopBugDetection/workspace_java/testInstrumentJavaClass/bin/compress87/testinput.txt";
    	File fin = new File(fileInput);
    	if(!fin.exists()) fin.createNewFile();
		FileInputStream filein = new FileInputStream(fin);
    	
		String fileOutput = "/home/ting/DataLoopBugDetection/workspace_java/testInstrumentJavaClass/bin/compress87/testout.txt";
		File fout = new File(fileOutput);
		if(!fout.exists()) fout.createNewFile();
		FileOutputStream fileout = new FileOutputStream(fout);
		
		IOUtils.copy(filein, fileout, 0);

    	
	}
}

package lucene8294;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class testcode {
	public static void main(String[] args){
		testcode inst = new testcode();
		try {
			inst.testNext();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void testNext() throws IOException{
		File f = new File("/home/ting/DataLoopBugDetection/workspace_java/testInstrumentJavaClass/bin/lucene8294/input.txt");
		if(!f.exists()) f.createNewFile();
		InputStream in = new FileInputStream(f);
		Reader read = new InputStreamReader(in, "UTF-8");
		int bufferSize = 0;
		KeywordTokenizer kt = new KeywordTokenizer(read, bufferSize);
		kt.next();
	}
}

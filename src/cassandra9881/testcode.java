package cassandra9881;

import java.io.File;
import java.io.IOException;

public class testcode {
	
	public static void main(String[] args) throws IOException{
		testcode inst = new testcode();
		inst.testScrub();
	}
	
	public void testScrub() throws IOException{
		
		LocalPartitioner partitioner = new LocalPartitioner();
		
		SSTableReader sstable = new SSTableReader(partitioner);
		File f = new File("/home/ting/DataLoopBugDetection/workspace_java/testInstrumentJavaClass/dataset/test.txt");
		Scrubber scrubber = new Scrubber(sstable, f);
		scrubber.scrub();
	}

}

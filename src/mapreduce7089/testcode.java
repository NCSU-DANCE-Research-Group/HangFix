package mapreduce7089;

import hadoop2Conf.Configuration;
import hadoop2Conf.JobConf;

import java.io.IOException;

public class testcode {
	public static void main(String[] args) throws IOException{
		testcode inst = new testcode();
		inst.testDoIO();
	}
	
	public void testDoIO() throws IOException{
		
//		Configuration conf = new Configuration();
		JobConf conf = new JobConf();
		Buggycode bc = new Buggycode();
		bc.configure(conf);
		
		Buggycode.ReadMapper rmap = new Buggycode.ReadMapper();
		rmap.getIOStream("/home/ting/DataLoopBugDetection/workspace_java/testInstrumentJavaClass/dataset/test.txt");
		rmap.doIO(2);
		
	}
}

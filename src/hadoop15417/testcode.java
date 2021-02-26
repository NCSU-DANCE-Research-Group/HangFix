package hadoop15417;




import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.hadoop.fs.s3.Block;

public class testcode {
	public static void main(String[] args){
		testcode inst = new testcode();
		try {
			inst.testRetrieveBlock();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void testRetrieveBlock() throws IOException, URISyntaxException{
		Buggycode bc = new Buggycode();
		Configuration conf = new Configuration();
		bc.initialize(conf);
		bc.retrieveBlock();
//		Jets3tFileSystemStore jsfs = new Jets3tFileSystemStore();
////		URI uri = new URI("localhost");
//		Configuration conf = new Configuration();
//		jsfs.initialize(conf);
//		Block block = new Block(1234, 5678);
//		jsfs.retrieveBlock(block, 5555);
	}
}

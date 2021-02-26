package hadoop8614;


import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;


public class testcode {
	
	public static void main(String[] args) throws IOException{
		testcode inst = new testcode();
		inst.testSkipFully();
	}
	
	public void testSkipFully() throws IOException {
		  byte inArray[] = new byte[] {0, 1, 2, 3, 4};
		  ByteArrayInputStream in = new ByteArrayInputStream(inArray);
		  try {
		    in.mark(inArray.length);
		    Buggycode.skipFully(in, 2);
		    Buggycode.skipFully(in, 2);
		    try {
		    	Buggycode.skipFully(in, 2);
		    } catch (EOFException e) {
		    	e.printStackTrace();
		    }
		  } finally {
		    in.close();
		  }
		}
}

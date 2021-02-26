package typeMissingTimeout;

import java.util.zip.Inflater;

public class MockInflater extends Inflater {
	@Override
	public int inflate(byte[] buf){
		try {
			Thread.sleep(Integer.MAX_VALUE);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
		return 0;
	}
}

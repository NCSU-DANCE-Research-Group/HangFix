package hive5235;

import java.util.zip.Inflater;

public class MockInflater extends Inflater {
	public MockInflater(boolean b) {
		// TODO Auto-generated constructor stub
		super(b);
	}
	

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
	
	@Override
	public int inflate(byte[] buf, int off, int len){
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

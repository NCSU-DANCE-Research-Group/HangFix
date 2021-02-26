package kafka6271;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MockFileInputStream extends FileInputStream{

	public MockFileInputStream(File file) throws FileNotFoundException {
		super(file);
		// TODO Auto-generated constructor stub
	}
	
	public MockFileInputStream(String filename) throws FileNotFoundException {
		// TODO Auto-generated constructor stub
		super(filename);
	}

	@Override
	public long skip(long len) throws IOException{
		return 0;
	}
}

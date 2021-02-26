package hive19391;

import java.io.File;
import java.io.IOException;
import java.security.AccessController;
import java.security.SecureRandom;

public class MockFile extends File {
	public MockFile(String prefix, String child) {
		super(prefix, child);
		// TODO Auto-generated constructor stub
	}

	public static MockFile createTempFile(String prefix, String suffix)
	        throws IOException
	{
	    return new MockFile(prefix, suffix);
	}
	
	public boolean delete() {
		return false;
	}
	
	public boolean mkdir() {
		return false;
	}
    
}

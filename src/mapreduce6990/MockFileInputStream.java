package mapreduce6990;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;

public class MockFileInputStream extends FileInputStream {
	
	
	public MockFileInputStream(File file) throws FileNotFoundException {
		super(file);
		curr = 0;
	}

	//http://developer.classpath.org/doc/java/io/FileInputStream-source.html
    /**
    * This method skips the specified number of bytes in the stream.  It
    * returns the actual number of bytes skipped, which may be less than the
    * requested amount.
    * <p>
    * @param numBytes The requested number of bytes to skip
    *
    * @return The actual number of bytes skipped.
    *
    * @exception IOException If an error occurs
    */
	@Override
    public synchronized long skip (long numBytes) throws IOException
    {
     if (numBytes < 0)
       throw new IllegalArgumentException ("Can't skip negative bytes: " + numBytes);

     if (numBytes == 0)
       return 0;

     long oldPos = position();
     position(oldPos + numBytes); //this op doesn't work
     return position() - oldPos;
    }
	
	
	//http://www.docjar.org/html/api/gnu/java/nio/channels/FileChannelImpl.java.html
	public long position() throws IOException
	{
		return implPosition();
	}

	public void position(long newPosition) throws IOException
	{
		if (newPosition < 0)
			throw new IllegalArgumentException ("newPostition: " + newPosition);

		// FIXME note semantics if seeking beyond eof.
		// We should seek lazily - only on a write.
		seek (newPosition);
	}
	
	long curr;
	
	//it is native code
	private long implPosition() throws IOException {
		return curr;
	}
	
	//it is native code, here we inject faults
	//since it the driver code, we should not trust the results read from the HW directly
	private void seek(long newPosition) throws IOException{
		//do nothing, inject faults
	}
}

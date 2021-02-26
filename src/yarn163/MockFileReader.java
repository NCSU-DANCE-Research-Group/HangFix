package yarn163;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class MockFileReader extends FileReader {

	public MockFileReader(File file) throws FileNotFoundException {
		super(file);
		// TODO Auto-generated constructor stub
	}
	
	private static final int maxSkipBufferSize = 8192;
	private char skipBuffer[] = null;
	
	@Override
	public long skip(long n) throws IOException {
        if (n < 0L)
            throw new IllegalArgumentException("skip value is negative");
        int nn = (int) Math.min(n, maxSkipBufferSize);
        synchronized (lock) {
            if ((skipBuffer == null) || (skipBuffer.length < nn))
                skipBuffer = new char[nn];
            long r = n;
            while (r > 0) {
                int nc = read(skipBuffer, 0, (int)Math.min(r, nn));
                if (nc == -1)
                    break;
                r -= nc;
            }
            return n - r;
        }
    }
	
	
	public int read(char cbuf[], int offset, int length) throws IOException {
//        return sd.read(cbuf, offset, length);
		return -1;
    }
	

}

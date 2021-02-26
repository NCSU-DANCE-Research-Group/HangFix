package cassandra7330;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;


public class Buggycode {
	
	protected void drain(InputStream dis, long bytesRead) throws IOException
	{
	    long toSkip = totalSize() - bytesRead;
	    toSkip = toSkip - dis.skip(toSkip);
	    System.out.println("before loop...toskip = " + toSkip);
	    while (toSkip > 0){
	        toSkip = toSkip - dis.skip(toSkip);
	        System.out.println("in loop...toskip = " + toSkip);
	    }
	}
	
	protected long totalSize()
    {
        return size;
    }
	
	protected final int size;
	
	public Buggycode(int size)
    {
        this.size = size;
    }
	
}

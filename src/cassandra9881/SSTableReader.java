package cassandra9881;

import java.io.Closeable;
import java.io.IOException;

public class SSTableReader extends SSTable implements Closeable{
	
	public SSTableReader(IPartitioner partitioner){
		super(partitioner);
    }
	
	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
				
	}
            
     
}

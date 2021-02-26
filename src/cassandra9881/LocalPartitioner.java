package cassandra9881;

import java.nio.ByteBuffer;

import cassandraIO.DecoratedKey;
import cassandraIO.Token;

public class LocalPartitioner extends AbstractPartitioner
{
    public LocalPartitioner()
    {
    }

	@Override
	public DecoratedKey decorateKey(ByteBuffer key) {
		return new DecoratedKey(getToken(key), key);
	}
	
	public Token getToken(ByteBuffer key){
		return null;
	}
}

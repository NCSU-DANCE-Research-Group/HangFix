package cassandraIO;

public interface RingPosition<T> extends Comparable<T>
{
    public Token getToken();
//    public boolean isMinimum(IPartitioner partitioner);
}
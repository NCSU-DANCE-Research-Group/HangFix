package cassandra9881;

public abstract class SSTable
{
	public final IPartitioner partitioner;
	
	 protected SSTable(IPartitioner partitioner){
		 this.partitioner = partitioner;
	 }
}

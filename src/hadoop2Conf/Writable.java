package hadoop2Conf;

import java.io.DataOutput;
import java.io.DataInput;
import java.io.IOException;

//import org.apache.hadoop.classification.InterfaceAudience;
//import org.apache.hadoop.classification.InterfaceStability;

/**
 * A serializable object which implements a simple, efficient, serialization 
 * protocol, based on {@link DataInput} and {@link DataOutput}.
 *
 * <p>Any <code>key</code> or <code>value</code> type in the Hadoop Map-Reduce
 * framework implements this interface.</p>
 * 
 * <p>Implementations typically implement a static <code>read(DataInput)</code>
 * method which constructs a new instance, calls {@link #readFields(DataInput)} 
 * and returns the instance.</p>
 * 
 * <p>Example:</p>
 * <p><blockquote><pre>
 *     public class MyWritable implements Writable {
 *       // Some data     
 *       private int counter;
 *       private long timestamp;
 *       
 *       public void write(DataOutput out) throws IOException {
 *         out.writeInt(counter);
 *         out.writeLong(timestamp);
 *       }
 *       
 *       public void readFields(DataInput in) throws IOException {
 *         counter = in.readInt();
 *         timestamp = in.readLong();
 *       }
 *       
 *       public static MyWritable read(DataInput in) throws IOException {
 *         MyWritable w = new MyWritable();
 *         w.readFields(in);
 *         return w;
 *       }
 *     }
 * </pre></blockquote></p>
 */
//@InterfaceAudience.Public
//@InterfaceStability.Stable
public interface Writable {
  /** 
   * Serialize the fields of this object to <code>out</code>.
   * 
   * @param out <code>DataOuput</code> to serialize this object into.
   * @throws IOException
   */
  void write(DataOutput out) throws IOException;

  /** 
   * Deserialize the fields of this object from <code>in</code>.  
   * 
   * <p>For efficiency, implementations should attempt to re-use storage in the 
   * existing object where possible.</p>
   * 
   * @param in <code>DataInput</code> to deseriablize this object from.
   * @throws IOException
   */
  void readFields(DataInput in) throws IOException;
}

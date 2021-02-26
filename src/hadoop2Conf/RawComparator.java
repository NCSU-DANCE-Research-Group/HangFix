package hadoop2Conf;

import java.util.Comparator;

//import org.apache.hadoop.classification.InterfaceAudience;
//import org.apache.hadoop.classification.InterfaceStability;
//import org.apache.hadoop.io.serializer.DeserializerComparator;

/**
 * <p>
 * A {@link Comparator} that operates directly on byte representations of
 * objects.
 * </p>
 * @param <T>
 * @see DeserializerComparator
 */
//@InterfaceAudience.Public
//@InterfaceStability.Stable
public interface RawComparator<T> extends Comparator<T> {

  /**
   * Compare two objects in binary.
   * b1[s1:l1] is the first object, and b2[s2:l2] is the second object.
   * 
   * @param b1 The first byte array.
   * @param s1 The position index in b1. The object under comparison's starting index.
   * @param l1 The length of the object in b1.
   * @param b2 The second byte array.
   * @param s2 The position index in b2. The object under comparison's starting index.
   * @param l2 The length of the object under comparison in b2.
   * @return An integer result of the comparison.
   */
  public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2);

}
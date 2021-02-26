package hadoop2Conf;

//import org.apache.hadoop.classification.InterfaceAudience;
//import org.apache.hadoop.classification.InterfaceStability;
//
///** Something that may be configured with a {@link Configuration}. */
//@InterfaceAudience.Public
//@InterfaceStability.Stable
public interface Configurable {

  /** Set the configuration to be used by this object. */
  void setConf(Configuration conf);

  /** Return the configuration used by this object. */
  Configuration getConf();
}
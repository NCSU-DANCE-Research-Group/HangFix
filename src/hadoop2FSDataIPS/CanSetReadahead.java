package hadoop2FSDataIPS;

import java.io.IOException;

public interface CanSetReadahead {
  /**
   * Set the readahead on this stream.
   *
   * @param readahead     The readahead to use.  null means to use the default.
   * @throws IOException  If there was an error changing the dropBehind
   *                      setting.
   *         UnsupportedOperationException  If this stream doesn't support
   *                                        setting readahead. 
   */
  public void setReadahead(Long readahead)
    throws IOException, UnsupportedOperationException;
}


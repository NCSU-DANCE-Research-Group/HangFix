package hadoop2FSDataIPS;

import java.io.FileDescriptor;
import java.io.IOException;

/**
 * Having a FileDescriptor
 */
public interface HasFileDescriptor {

  /**
   * @return the FileDescriptor
   * @throws IOException
   */
  public FileDescriptor getFileDescriptor() throws IOException;

}

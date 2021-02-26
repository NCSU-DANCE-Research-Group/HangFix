package hadoop15425;


import java.io.IOException;
import hadoop2FSDataIPS.FSDataInputStream;


public class Buggycode {
    
	
	private byte[] buffer = null;
	private int sizeBuf = 128 * 1024;
	
	public void configure(int size)
    {
//      sizeBuf = job.getInt("copy.buf.size", 128 * 1024);
	  sizeBuf = size;
      buffer = new byte[sizeBuf];
    }
	
    public long doCopyFile(FSDataInputStream in/*FileStatus srcstat, Path tmpfile, Path absdst,
                            Reporter reporter*/) throws IOException {
//      FSDataInputStream in = null;
//      FSDataOutputStream out = null;
      long bytesCopied = 0L;
      try {
//        Path srcPath = srcstat.getPath();
        // open src file
//        in = srcPath.getFileSystem(job).open(srcPath);
        // open tmp file
//        out = create(tmpfile, reporter, srcstat);
        
        // copy file
        for(int bytesRead; (bytesRead = in.read(buffer)) >= 0; ) {
        	System.out.println("inside loop...bytesRead = " + bytesRead);
//          out.write(buffer, 0, bytesRead);
//          bytesCopied += bytesRead;
        }
      } finally {
//        checkAndClose(in);
//        checkAndClose(out);
      }
      return bytesCopied;
    }

}

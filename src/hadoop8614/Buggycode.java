package hadoop8614;
import java.io.IOException;
import java.io.InputStream;


public class Buggycode {
	  public static void skipFully(InputStream in, long len) throws IOException {
		  while (len > 0) {
		      long ret = in.skip(len);
		      System.out.println("Inside loop..., ret = " + ret);
		      if (ret < 0) {
		        throw new IOException( "Premature EOF from inputStream");
		      }
		      len -= ret;
		    }
	  }
}

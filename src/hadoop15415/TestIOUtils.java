package hadoop15415;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import hadoop2Conf.JobConf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test cases for IOUtils.java
 */
public class TestIOUtils {
	
	@Test
    public void testCopyBytesWithInvalidBufferSize() throws IOException {
      InputStream inputStream = Mockito.mock(InputStream.class);
      OutputStream outputStream = Mockito.mock(OutputStream.class);
      IOUtils.copyBytes(inputStream, outputStream, 0, false);
    }
	
	@Test
	public void testCopyBytesWithCorruptedConfig() throws IOException {
		JobConf conf = new JobConf();
		InputStream inputStream = Mockito.mock(InputStream.class);
	    OutputStream outputStream = Mockito.mock(OutputStream.class);
	    IOUtils.copyBytes(inputStream, outputStream, conf, false);
	}

//  @Test
//  public void testCopyBytesShouldCloseStreamsWhenCloseIsTrue() throws Exception {
//    InputStream inputStream = Mockito.mock(InputStream.class);
//    OutputStream outputStream = Mockito.mock(OutputStream.class);
//    Mockito.doReturn(-1).when(inputStream).read(new byte[1]);
//    IOUtils.copyBytes(inputStream, outputStream, 1, true);
//    Mockito.verify(inputStream, Mockito.atLeastOnce()).close();
//    Mockito.verify(outputStream, Mockito.atLeastOnce()).close();
//  }
//
//  @Test
//  public void testCopyBytesShouldCloseInputSteamWhenOutputStreamCloseThrowsException()
//      throws Exception {
//    InputStream inputStream = Mockito.mock(InputStream.class);
//    OutputStream outputStream = Mockito.mock(OutputStream.class);
//    Mockito.doReturn(-1).when(inputStream).read(new byte[1]);
//    Mockito.doThrow(new IOException()).when(outputStream).close();
//    try{
//      IOUtils.copyBytes(inputStream, outputStream, 1, true);
//    } catch (IOException e) {
//    }
//    Mockito.verify(inputStream, Mockito.atLeastOnce()).close();
//    Mockito.verify(outputStream, Mockito.atLeastOnce()).close();
//  }
//
//  @Test
//  public void testCopyBytesShouldNotCloseStreamsWhenCloseIsFalse()
//      throws Exception {
//    InputStream inputStream = Mockito.mock(InputStream.class);
//    OutputStream outputStream = Mockito.mock(OutputStream.class);
//    Mockito.doReturn(-1).when(inputStream).read(new byte[1]);
//    IOUtils.copyBytes(inputStream, outputStream, 1, false);
//    Mockito.verify(inputStream, Mockito.atMost(0)).close();
//    Mockito.verify(outputStream, Mockito.atMost(0)).close();
//  }
//  
//  @Test
//  public void testCopyBytesWithCountShouldCloseStreamsWhenCloseIsTrue()
//      throws Exception {
//    InputStream inputStream = Mockito.mock(InputStream.class);
//    OutputStream outputStream = Mockito.mock(OutputStream.class);
//    Mockito.doReturn(-1).when(inputStream).read(new byte[4096], 0, 1);
//    IOUtils.copyBytes(inputStream, outputStream, (long) 1, true);
//    Mockito.verify(inputStream, Mockito.atLeastOnce()).close();
//    Mockito.verify(outputStream, Mockito.atLeastOnce()).close();
//  }
//
//  @Test
//  public void testCopyBytesWithCountShouldNotCloseStreamsWhenCloseIsFalse()
//      throws Exception {
//    InputStream inputStream = Mockito.mock(InputStream.class);
//    OutputStream outputStream = Mockito.mock(OutputStream.class);
//    Mockito.doReturn(-1).when(inputStream).read(new byte[4096], 0, 1);
//    IOUtils.copyBytes(inputStream, outputStream, (long) 1, false);
//    Mockito.verify(inputStream, Mockito.atMost(0)).close();
//    Mockito.verify(outputStream, Mockito.atMost(0)).close();
//  }
//
//  @Test
//  public void testCopyBytesWithCountShouldThrowOutTheStreamClosureExceptions()
//      throws Exception {
//    InputStream inputStream = Mockito.mock(InputStream.class);
//    OutputStream outputStream = Mockito.mock(OutputStream.class);
//    Mockito.doReturn(-1).when(inputStream).read(new byte[4096], 0, 1);
//    Mockito.doThrow(new IOException("Exception in closing the stream")).when(
//        outputStream).close();
//    try {
//      IOUtils.copyBytes(inputStream, outputStream, (long) 1, true);
//      fail("Should throw out the exception");
//    } catch (IOException e) {
//      assertEquals("Not throwing the expected exception.",
//          "Exception in closing the stream", e.getMessage());
//    }
//    Mockito.verify(inputStream, Mockito.atLeastOnce()).close();
//    Mockito.verify(outputStream, Mockito.atLeastOnce()).close();
//  }
  
}

package cassandra7330;


import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.cassandra.utils.Pair;
import org.apache.cassandra.streaming.compress.CompressionInfo;
import org.apache.cassandra.streaming.messages.FileMessageHeader;

import com.ning.compress.lzf.LZFInputStream;


public class testcode {
	
	public static void main(String[] args) throws IOException{
		testcode inst = new testcode();
//		inst.testDrainSR();
		inst.testDrainWithLZF();
	}
	
	
	
	public FileMessageHeader deserialize(int count) throws IOException
    {
        UUID cfId = new UUID(0, 0);
        int sequenceNumber = 1;
        String sstableVersion = "2";
        long estimatedKeys = 3;
        List<Pair<Long, Long>> sections = new ArrayList<>(count);
        for (int k = 0; k < count; k++)
            sections.add(Pair.create((long)k, (long)k+1));
        CompressionInfo compressionInfo = null;
        return new FileMessageHeader(cfId, sequenceNumber, sstableVersion, estimatedKeys, sections, compressionInfo);
    }
	
	
	//ByteArrayInputStream returns 0 at EOF
	public void testDrainSR() throws IOException {
		  byte inArray[] = new byte[] {0, 1, 2, 3, 4};
		  ByteArrayInputStream in = new ByteArrayInputStream(inArray);
		  FileMessageHeader header = deserialize(5);
		  StreamReader streamreader = new StreamReader(header, null);
		  try {
		    streamreader.drain(in, 2);
		    try {
		    	System.out.println("invoke second drain func");
		    	streamreader.drain(in, 2);
		    } catch (EOFException e) {
		    	e.printStackTrace();
		    }
		  } finally {
		    in.close();
		  }
	}
	
	public void testDrainWithLZF() throws IOException{
		File f = new File("/home/ting/DataLoopBugDetection/workspace_java/testInstrumentJavaClass/bin/cassandra7330/input.txt");
		if(!f.exists()) f.createNewFile();
		InputStream in = new FileInputStream(f);
		ReadableByteChannel channel = Channels.newChannel(in);
		DataInputStream dis = new DataInputStream(new LZFInputStream(Channels.newInputStream(channel)));
		FileMessageHeader header = deserialize(5);
		StreamReader streamreader = new StreamReader(header, null);
		streamreader.drain(dis, 2);  
	}
	
	
	//ByteArrayInputStream returns 0 at EOF
	public void testDrain() throws IOException {
		  byte inArray[] = new byte[] {0, 1, 2, 3, 4};
		  ByteArrayInputStream in = new ByteArrayInputStream(inArray);
		  Buggycode streamreader = new Buggycode(5);
		  try {
		    streamreader.drain(in, 2);
		    try {
		    	System.out.println("invoke second drain func");
		    	streamreader.drain(in, 2);
		    } catch (EOFException e) {
		    	e.printStackTrace();
		    }
		  } finally {
		    in.close();
		  }
	}
	
	
	// FileInputStream can skip as more than available().
	public void testDrain2() throws IOException {
		byte inArray[] = new byte[] {0, 1, 2, 3, 4};
		File f = new File("/home/ting/DataLoopBugDetection/workspace_java/testInstrumentJavaClass/bin/cassandra7330/test.txt");
		FileOutputStream fileout = new FileOutputStream(f);
		try{
			fileout.write(inArray);
		} finally {
			fileout.close();
		}
		FileInputStream file = new FileInputStream(f);
		System.out.println("file length = " + file.available());
		long ret = file.skip(10000);
		System.out.println("skipped " + ret + " bytes.");
//		  Buggycode streamreader = new Buggycode(2);
//		  try {
//		    streamreader.drain(file, 2);
//		    try {
//		    	System.out.println("invoke second drain func");
//		    	streamreader.drain(file, 2);
//		    } catch (EOFException e) {
//		    	e.printStackTrace();
//		    }
//		  } finally {
//		    file.close();
//		  }
	}
	
	
}

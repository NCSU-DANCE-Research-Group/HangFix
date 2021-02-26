package yarn2905;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public class testcode {
	public static void main(String[] args) throws IOException{
		testcode inst = new testcode();
		inst.testreadContainerLogs();
	}
	
	
	private DataInputStream getDIS() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StringBuffer testBuffer = new StringBuffer();
        testBuffer.append('t');//type
        testBuffer.append('e');
        testBuffer.append('x');
        testBuffer.append('t');
        String testString = testBuffer.toString();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeUTF(testString);
        
        testBuffer.delete(0, testBuffer.length());
        testBuffer.append('5');//length
        testString = testBuffer.toString();
        dos.writeUTF(testString);

        byte[] testBytes = baos.toByteArray();

        // Attempt to decode the bytes back into a String
        ByteArrayInputStream bais = new ByteArrayInputStream(testBytes);
        DataInputStream dis = new DataInputStream(bais);
//        String s = dis.readUTF();
//        System.out.println("s = " + s);
//        String s2 = dis.readUTF();
//        System.out.println("s2 = " + s2);
        return dis;
    }
	
	
	public void testreadContainerLogs() throws IOException{
		DataInputStream din = getDIS(); //This DIS only contains type and length, the main content are empty
		AggregatedLogFormat.ContainerLogsReader logReader = new AggregatedLogFormat.ContainerLogsReader(din);
		Buggycode bc = new Buggycode();
		long start = 1;
		try {
			bc.readContainerLogs(logReader, start);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}

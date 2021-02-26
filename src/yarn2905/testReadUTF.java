package yarn2905;

import java.io.*;
import java.util.*;

public class testReadUTF {

    private static Random generator = new Random();

    private static final int TEST_ITERATIONS = 1000;

    private static final int A_NUMBER_NEAR_65535 = 60000;

    private static final int MAX_CORRUPTIONS_PER_CYCLE = 3;

    public static final void main(String[] args) throws Exception {
        for (int i=0; i<TEST_ITERATIONS; i++) {
            try {
//                writeAndReadAString();
                writeAString();
            } catch (UTFDataFormatException utfdfe) {
                if (utfdfe.getMessage() == null)
                    throw new RuntimeException("vague exception thrown");
            } catch (EOFException eofe) {
                // These are rare and beyond the scope of the test
            }
        }
    }

    private static void writeAndReadAString() throws Exception {
        // Write out a string whose UTF-8 encoding is quite possibly
        // longer than 65535 bytes
        int length = generator.nextInt(A_NUMBER_NEAR_65535) + 1;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StringBuffer testBuffer = new StringBuffer();
        for (int i=0; i<length; i++)
            testBuffer.append('a' + i);
        String testString = testBuffer.toString();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeUTF(testString);

        // Corrupt the data to produce malformed characters
        byte[] testBytes = baos.toByteArray();
//        int dataLength = testBytes.length;
//        int corruptions = generator.nextInt(MAX_CORRUPTIONS_PER_CYCLE);
//        for (int i=0; i<corruptions; i++) {
//            int index = generator.nextInt(dataLength);
//            testBytes[index] = (byte)generator.nextInt();
//        }
//
//        // Pay special attention to mangling the end to produce
//        // partial characters at end
//        testBytes[dataLength-1] = (byte)generator.nextInt();
//        testBytes[dataLength-2] = (byte)generator.nextInt();

        // Attempt to decode the bytes back into a String
        ByteArrayInputStream bais = new ByteArrayInputStream(testBytes);
        DataInputStream dis = new DataInputStream(bais);
        String s = dis.readUTF();
        System.out.println("s = " + s);
    }
    
    
    private static void writeAString() throws Exception {
        // Write out a string whose UTF-8 encoding is quite possibly
        // longer than 65535 bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StringBuffer testBuffer = new StringBuffer();
        testBuffer.append('a');
        String testString = testBuffer.toString();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeUTF(testString);
        
        testBuffer.delete(0, testBuffer.length());
        testBuffer.append('5');
        testString = testBuffer.toString();
        dos.writeUTF(testString);

        // Corrupt the data to produce malformed characters
        byte[] testBytes = baos.toByteArray();
//        int dataLength = testBytes.length;
//        int corruptions = generator.nextInt(MAX_CORRUPTIONS_PER_CYCLE);
//        for (int i=0; i<corruptions; i++) {
//            int index = generator.nextInt(dataLength);
//            testBytes[index] = (byte)generator.nextInt();
//        }
//
//        // Pay special attention to mangling the end to produce
//        // partial characters at end
//        testBytes[dataLength-1] = (byte)generator.nextInt();
//        testBytes[dataLength-2] = (byte)generator.nextInt();

        // Attempt to decode the bytes back into a String
        ByteArrayInputStream bais = new ByteArrayInputStream(testBytes);
        DataInputStream dis = new DataInputStream(bais);
        String s = dis.readUTF();
        System.out.println("s = " + s);
        String s2 = dis.readUTF();
        System.out.println("s2 = " + s2);
    }
}

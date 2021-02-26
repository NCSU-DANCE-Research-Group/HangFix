package compress87;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.zip.ZipFile;

import junit.framework.TestCase;

/**
 * JUnit 3 testcase for a multi-volume zip file.
 * 
 * Some tools (like 7-zip) allow users to split a large archives into 'volumes'
 * with a given size to fit them into multiple cds, usb drives, or emails with
 * an attachment size limit. It's basically the same file split into chunks of
 * exactly 65536 bytes length. Concatenating volumes yields exactly the original
 * file. There is no mechanism in the ZIP algorithm to accommodate for this.
 * Before commons-compress used to enter an infinite loop on the last entry for
 * such a file. This test is intended to prove that this error doesn't occur
 * anymore. All entries but the last one are returned correctly, the last entry
 * yields an exception.
 * 
 */
public class testcode {
	public static void main(String[] args) throws IOException, URISyntaxException{
		testcode inst = new testcode();
		inst.testRead7ZipMultiVolumeArchiveForStream();
	}
    
	private static final String [] ENTRIES = new String [] {
		"apache-maven-2.2.1/",
		"apache-maven-2.2.1/LICENSE.txt",
		"apache-maven-2.2.1/NOTICE.txt",
		"apache-maven-2.2.1/README.txt",
		"apache-maven-2.2.1/bin/",
		"apache-maven-2.2.1/bin/m2.conf",
		"apache-maven-2.2.1/bin/mvn",
		"apache-maven-2.2.1/bin/mvn.bat",
		"apache-maven-2.2.1/bin/mvnDebug",
		"apache-maven-2.2.1/bin/mvnDebug.bat",
		"apache-maven-2.2.1/boot/",
		"apache-maven-2.2.1/boot/classworlds-1.1.jar",
		"apache-maven-2.2.1/conf/",
		"apache-maven-2.2.1/conf/settings.xml",
		"apache-maven-2.2.1/lib/"
	    };
	
	private static final String LAST_ENTRY_NAME = 
			"apache-maven-2.2.1/lib/maven-2.2.1-uber.jar";
	
	
    public void testRead7ZipMultiVolumeArchiveForStream() throws IOException, URISyntaxException {
    	String zipString = "/home/ting/DataLoopBugDetection/workspace_java/testInstrumentJavaClass/dataset/apache-maven-2.2.1.zip.001";
    	FileInputStream archive = new FileInputStream(new File(zipString));
    	ZipArchiveInputStream zi = null;
    	try {
    		zi = new ZipArchiveInputStream(archive,null,false);
    	    
    		//the below getNextEntry funcs are used to move the index of the truncated zip file
    	    for (int i = 0; i < ENTRIES.length; i++) {
    	    	zi.getNextEntry();
//    			System.out.println(ENTRIES[i] + "\t" + zi.getNextEntry().getName());
    		}
    	    
    	    // this is the last entry that is truncated
		    ArchiveEntry lastEntry = zi.getNextEntry();
//		    System.out.println(LAST_ENTRY_NAME + "\t"+ lastEntry.getName());
		    byte [] buffer = new byte [4096];
	
		    // before the fix, we'd get 0 bytes on this read and all
		    // subsequent reads thus a client application might enter
		    // an infinite loop after the fix, we should get an
		    // exception
//		    try {
//	           int read = 0;
//	           while ((read = zi.read(buffer)) > 0) {  }
////	           TestCase.fail("shouldn't be able to read from truncated entry");
//		    } catch (IOException e) {
//	            //assertEquals("Truncated ZIP file", e.getMessage());
//		    	e.printStackTrace();
//		    }
    	    
    	    zi.skip(Long.MAX_VALUE); //skip is the buggy function
    	    
    	} finally {
    		archive.close();
    	}
    }
    
    
}

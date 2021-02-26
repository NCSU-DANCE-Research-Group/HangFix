package hive19391;


import java.io.File;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.mockito.Mockito;

public class testcode {
	public static void main(String[] args){
		testcode inst = new testcode();
		inst.testSetupWriter();
	}
	
	public void testSetupWriter(){
//		File mockB = Mockito.mock(File.class);
//	    Mockito.doReturn(false).when(mockB).mkdir();
//		File file = Mockito.mock(java.io.File.class);
//		Mockito.when(file.mkdir()).thenReturn(false);
		Buggycode bc = new Buggycode();
		
		Logger.getRootLogger().setLevel(Level.DEBUG);

//		bc.parentFile = mockB;
		try {
			bc.setupWriter();
		} catch (HiveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

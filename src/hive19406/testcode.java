package hive19406;

import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class testcode {
	public static void main(String[] args){
		testcode inst = new testcode();
		inst.testSetupOutput();
	}
	
	public void testSetupOutput() {
		Buggycode bc = new Buggycode();
		Logger.getRootLogger().setLevel(Level.ALL);
		try {
			bc.setupOutput();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

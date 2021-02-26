package hive18216;

import hadoop2Conf.Text;

public class testcode {
	public static void main(String[] args){
		testcode inst = new testcode();
		inst.testprocessInput();
	}
	
	
	public void testprocessInput(){
		Buggycode bc = new Buggycode();
		byte[] byteArr = new byte[]{(byte) 0x81, (byte) 0x82}; //as long as the first one is between 0x80 and 0xc1
		int length; 
		Text text = new Text(byteArr);
		bc.processInput(text);
	}
}

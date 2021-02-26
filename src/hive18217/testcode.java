package hive18217;

import hadoop2Conf.Text;

public class testcode {
	public static void main(String[] args){
		testcode inst = new testcode();
		inst.testprocessInput();
	}
	
	
	public void testprocessInput(){
		Buggycode bc = new Buggycode();
		byte[] byteArr = new byte[]{(byte) 0x81, (byte) 0x82}; //as long as the first one is between 0x80 and 0xc1
		Text from = new Text(byteArr);
		byte[] byteArr2 = new byte[]{(byte) 0x78, (byte) 0x79};
		Text to = new Text(byteArr);
		bc.populateMappings(from, to);
	}
}

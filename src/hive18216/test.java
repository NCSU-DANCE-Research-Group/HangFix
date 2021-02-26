package hive18216;

import java.nio.ByteBuffer;

public class test {
	public static void main(String[] args){
		ByteBuffer bb = ByteBuffer.allocate(10);
		System.out.println(bb.position());
		bb.get();
		System.out.println(bb.position());
		testText.testGet(bb);
		System.out.println(bb.position());
		testText.testGet(bb);
		System.out.println(bb.position());
		test inst = new test();
		inst.testGet2(bb);
		System.out.println(bb.position());
		
	}
	
	
	static class testText{
		public static void testGet(ByteBuffer bb){
			System.out.println("Inside testGet, " + bb.position());
			bb.get();
		}
	}
	
	public void testGet2(ByteBuffer bb){
		System.out.println("Inside testGet2, " + bb.position());
		bb.get();
	}
}


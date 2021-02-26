package hive19391;

import org.mockito.Mockito;

public class test {
//	public static void main(String[] args){
//		String path = "/home/ting/";
//		System.out.println(path.indexOf('\u0000'));
//		System.out.println('\u0000');
//	}
	public static void main(String[] args){
		test inst = new test();
		inst.testA();
	}
	
	class B{
		public String getBName(String id){ 
			return "id";
		}
	}
	
	class A{
		  B b;

		  public String getAName(String id){

		    // do something
		    return b.getBName(id);
		  }
	}
	
	public void testA(){
		String id = "id";
	    B mockB = Mockito.mock(B.class);
	    Mockito.doReturn("Bar").when(mockB).getBName(id);
	    A a = new A();
	    a.b = mockB; //add this line to use mock in A
	    String testStr = a.getAName(id); //this still calls "b.getBName(id)" in class implementation
	    System.out.println(testStr);

	}
}

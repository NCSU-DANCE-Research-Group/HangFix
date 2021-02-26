package cassandra7330;

import java.io.IOException;

public class test {
	public static void main(String[] args){
		try {
			read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static void read() throws IOException{
		try{
			throwException(1);
			int i = 0;
			i++;
		} catch (Throwable e){
			throwException(2);
		}
	}
	
	
	public static void throwException(int random) throws IOException{
		 throw new IOException("random exception " + random);
	}
	
	
}

package typeDataContent;

public class test {
	public static void main(String[] args){
		String str = "The 3rd argument is zero, causing loop stride always be 0 when METHOD invokes read(byte[],int,int) operation.";
		str = str.replace("METHOD", "NOTHING");
		System.out.println(str);
	}
}

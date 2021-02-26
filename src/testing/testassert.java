package testing;

public class testassert {
	public static void main(String[] args){
		int i = 1;
		if(i == 0)
			assert false : "in if branch";
		else 
			assert true : "in else branch";
	}
}

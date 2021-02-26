package utils;


public class Pair<T> {
	public T impacted;
	public T impacting;

	public Pair(T h1, T h2) {
		impacted = h1;
		impacting = h2;
	}
	
	public String toString(){
		return impacting + "->" + impacted;
	}
	
	public boolean equals(Object obj){
		Pair<T> p = (Pair<T>) obj;
		if(this.impacted.equals(p.impacted) && this.impacting.equals(p.impacting)){
			return true;
		}
		return false;
	}
	
	public int hashCode(){
		return toString().hashCode();
	}
}

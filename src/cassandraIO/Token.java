package cassandraIO;

public abstract class Token<T> {
	public final T token;
	abstract public int compareTo(Token<T> o);
	
	protected Token(T token)
    {
        this.token = token;
    }
}

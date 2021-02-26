package hive5235;

import java.util.concurrent.TimeUnit;

public class testRunnable {
	public abstract class RunnableWithTimeout<V> implements Runnable {
		 
	    protected long startTimeMillis = System.currentTimeMillis();
	    protected long timeOutMillis = 0;
	 
	    protected V result = null;
	    protected boolean done = false;
	    protected Exception exception = null;
	 
	    public RunnableWithTimeout(long timeout, TimeUnit unit) {
	        timeOutMillis = unit.toMillis(timeout);
	    }
	    
	    public Exception getException() {
	        return exception;
	    }
	 
	    public void waitFor() throws InterruptedException {
	        synchronized (this) {
	            while (!done && exception == null) {
	                long timeLeft = startTimeMillis + timeOutMillis - System.currentTimeMillis(); 
	                if (timeLeft > 0) {
	                    wait(timeLeft);
	                }
	            }
	        }
	    }
	 
	    public abstract V runNotingCompletion();
	 
	    public abstract void executeIfNotDoneAfterTimeout();
	 
	    @Override
	    public void run() {
	        try {
	            result = runNotingCompletion();
	            done = true;
	        } catch (Exception e) {
	            exception = e;
	        }
	        synchronized (this) {
	            notifyAll();
	        }
	    }
	 
	    public V getResultIfPresent() {
	        try {
	            waitFor();
	        } catch (InterruptedException e) {
	            //Wait
	        }
	        if (! done) {
	            executeIfNotDoneAfterTimeout();
	        }
	        return result;
	    }
	}
}

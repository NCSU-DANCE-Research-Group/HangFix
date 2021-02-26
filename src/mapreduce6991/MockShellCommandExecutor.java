package mapreduce6991;

import java.io.IOException;

import org.apache.hadoop.util.Shell.ShellCommandExecutor;

public class MockShellCommandExecutor extends ShellCommandExecutor{

	public MockShellCommandExecutor(String[] execString) {
		super(execString);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void execute() throws ExitCodeException, IOException{
		throw new IOException("disk full, creating pidFile failed.");
	}

}

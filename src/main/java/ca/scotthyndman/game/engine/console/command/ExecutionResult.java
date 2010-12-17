package ca.scotthyndman.game.engine.console.command;

public class ExecutionResult {

	public boolean isError;
	public String resultString;
	
	public ExecutionResult(String resultString) {
		this(false, resultString);
	}
	
	public ExecutionResult(Throwable t) {
		this(true, t.getMessage());
	}
	
	public ExecutionResult(boolean isError, String resultString) {
		this.isError = isError;
		this.resultString = resultString;
	}
}

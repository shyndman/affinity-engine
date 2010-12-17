package ca.scotthyndman.game.engine.console.command;

import org.jruby.Ruby;

import ca.scotthyndman.game.engine.console.EditorController;

public class ClearOutputCommand extends Command {

	@Override
	public ExecutionResult execute(Ruby runtime, String command, EditorController controller) {
		controller.clearOuput();
		return null;
	}

}

package ca.scotthyndman.game.engine.console.command;

import org.jruby.Ruby;

import ca.scotthyndman.game.engine.console.EditorController;

public class ReloadCommand extends Command {

	@Override
	public ExecutionResult execute(Ruby runtime, String command, EditorController controller) {
		controller.printLine("Reloading...");
		controller.getManager().reset();
		return null;
	}

}

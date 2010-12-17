package ca.scotthyndman.game.engine.console.command;

import org.jruby.Ruby;

import ca.scotthyndman.game.engine.console.EditorController;

/**
 * Represents an operation that is associated with a Ruby command.
 * 
 * @author scottyhyndman
 */
public abstract class Command {

	public abstract ExecutionResult execute(Ruby runtime, String command, EditorController controller);
}

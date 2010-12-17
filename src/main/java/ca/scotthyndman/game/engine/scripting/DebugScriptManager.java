package ca.scotthyndman.game.engine.scripting;

import org.jruby.RubyInstanceConfig;

import ca.scotthyndman.game.engine.console.Console;
import ca.scotthyndman.game.engine.state.InGameState;

/**
 * ScriptManager for debugging (with console)
 * 
 * @author scottyhyndman
 */
public class DebugScriptManager extends ScriptManager {

	Console console;

	public DebugScriptManager() {
		console = new Console();
	}

	@Override
	public ScriptEngine start() throws Exception {
		ScriptEngine engine = super.start();
		console.initialize(this);
		return engine;
	}
	
	@Override
	public void runGame(InGameState state, String gameScript) {
		// At this point the env has a root node.
		console.runGame(state);
		super.runGame(state, gameScript);
	}
	
	@Override
	public void reset() {
		super.reset();
		console.reset();
	}
	
	@Override
	public void shutdown() {
		super.shutdown();
		console.shutdown();
	}

	@Override
	protected RubyInstanceConfig getConfig() {
		return console.getRubyConfig();
	}

}

package ca.scotthyndman.game.engine.console;

import java.util.Collections;

import javax.swing.WindowConstants;

import org.jruby.RubyInstanceConfig;

import ca.scotthyndman.game.engine.Engine;
import ca.scotthyndman.game.engine.scripting.ScriptManager;
import ca.scotthyndman.game.engine.state.InGameState;

public class Console {

	private RubyInstanceConfig config;
	private ConsoleFrame frame;
	private SGTreeModel treeModel;

	public Console() {
		treeModel = new SGTreeModel();
		frame = new ConsoleFrame(treeModel);
		config = new RubyInstanceConfig() {
			{
				setObjectSpaceEnabled(true); // useful for code completion inside the IRB
				setCompileMode(CompileMode.OFF);
			}
		};
	}

	/**
	 * Called by the outside to configure the ruby runtime.
	 * 
	 * @return the ruby configuration for a debugging session
	 */
	public RubyInstanceConfig getRubyConfig() {
		return config;
	}

	/**
	 * Initializes the console with the ruby runtime.
	 * 
	 * @param runtime
	 */
	public void initialize(ScriptManager scriptManager) {
		frame.initialize(scriptManager);
	}

	/**
	 * Sets the root node.
	 */
	public void runGame(InGameState state) {
		
	}

	/**
	 * Resets the game.
	 */
	public void reset() {
	}

	/**
	 * Shuts down the console.
	 */
	public void shutdown() {
		frame.dispose();
	}

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		Console console = new Console();
		ScriptManager manager = ScriptManager.newScriptManager(Engine.GameMode.DEBUG, Collections.EMPTY_LIST);
		manager.start();
		console.initialize(manager);
		console.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}
}

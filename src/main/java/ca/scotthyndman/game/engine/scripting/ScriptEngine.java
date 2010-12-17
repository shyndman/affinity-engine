package ca.scotthyndman.game.engine.scripting;

import ca.scotthyndman.game.engine.entity.Env;
import ca.scotthyndman.game.engine.event.EventManager;
import ca.scotthyndman.game.engine.input.InputSystem;

/**
 * The {@link ScriptEngine} class represents the link between the scripting world, and the Java game engine. The
 * ScriptManagerOld provides all the gaming logic to the engine.
 * 
 * @author scottyhyndman
 */
public interface ScriptEngine {

	/**
	 * Called when the game is ready, and scripts can begin doing their thing.
	 * 
	 * @param gameScript
	 *            A script containing the real game logic.
	 */
	void ready(String gameScript);

	/**
	 * Sets the environment.
	 * 
	 * @param env
	 *            the environment
	 */
	void env(Env env);

	/**
	 * Sets the event manager.
	 * 
	 * @param manager
	 *            the manager
	 */
	void event_manager(EventManager manager);

	/**
	 * Sets the input system.
	 * 
	 * @param manager
	 *            the manager
	 */
	void input_system(InputSystem system);

	/**
	 * Gives ruby access to some special functions.
	 * 
	 * @param functions
	 *            the functions
	 */
	void specials(ScriptSpecialFunctions functions);

	/**
	 * Reloads the engine.
	 */
	void reset();
}

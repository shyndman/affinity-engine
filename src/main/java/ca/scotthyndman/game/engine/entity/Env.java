package ca.scotthyndman.game.engine.entity;

import ca.scotthyndman.game.engine.Engine.GameMode;
import ca.scotthyndman.game.engine.input.InputState;
import ca.scotthyndman.game.engine.scene.RootNode;
import ca.scotthyndman.game.engine.scene.UpdateManager;

/**
 * Represents the current state of the game.
 * 
 * @author scottyhyndman
 */
public class Env {

	/**
	 * The singleton
	 */
	private static Env instance;

	/**
	 * The root spatial.
	 */
	private RootNode rootNode;

	/**
	 * The input state.
	 */
	private InputState inputState;

	/**
	 * The update manager.
	 */
	private UpdateManager updateManager;

	/**
	 * <code>true</code> if we're in debug mode.
	 */
	private GameMode gameMode;

	/**
	 * Private constructor to prevent creation.
	 */
	private Env() {
	}

	/**
	 * Gets the root spatial in the scene graph.
	 */
	public RootNode getRootNode() {
		return rootNode;
	}

	/**
	 * Sets the root spatial in the scene graph.
	 */
	public void setRootNode(RootNode rootNode) {
		this.rootNode = rootNode;
	}

	/**
	 * Returns whether we're in debug mode.
	 */
	public GameMode getGameMode() {
		return gameMode;
	}

	/**
	 * Whether we're in debug mode.
	 */
	public void setGameMode(GameMode mode) {
		this.gameMode = mode;
	}

	/**
	 * Gets the update manager.
	 */
	public UpdateManager getUpdateManager() {
		return updateManager;
	}

	/**
	 * Sets the update manager.
	 */
	public void setUpdateManager(UpdateManager updateManager) {
		this.updateManager = updateManager;
	}

	/**
	 * Returns the object that keeps track of the states of the user input devices.
	 */
	public InputState getInputState() {
		return inputState;
	}

	/**
	 * Sets the object that keeps track of the states of the user input devices.
	 */
	public void setInputState(InputState inputState) {
		this.inputState = inputState;
	}

	/**
	 * Returns the {@link Env} instance.
	 */
	public static Env getInstance() {
		if (instance == null) {
			instance = new Env();
		}
		return instance;
	}
}

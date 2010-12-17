package ca.scotthyndman.game.engine;

import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.scotthyndman.game.engine.config.EngineConfig;
import ca.scotthyndman.game.engine.entity.Env;
import ca.scotthyndman.game.engine.event.EventManager;
import ca.scotthyndman.game.engine.scripting.ScriptManager;
import ca.scotthyndman.game.engine.state.InGameState;
import ca.scotthyndman.game.engine.state.LoadingGameState;
import ca.scotthyndman.game.engine.state.ShutdownGameState;

import com.jme.system.DisplaySystem;
import com.jme.util.resource.ResourceLocatorTool;
import com.jme.util.resource.SimpleResourceLocator;
import com.jmex.game.StandardGame;
import com.jmex.game.StandardGame.GameType;
import com.jmex.game.state.GameStateManager;

public class Engine {

	//
	// ======== THE GAME MODE
	//

	/**
	 * The game mode.
	 * 
	 * @author scottyhyndman
	 */
	public static enum GameMode {
		RELEASE, DEBUG;
	}

	//
	// ======== SETTING ENGINE STATE
	//

	/**
	 * Resumes the game.
	 */
	public static void resume() {
		instance.paused = false;
	}

	/**
	 * Pauses the game.
	 */
	public static void pause() {
		instance.paused = true;
	}

	/**
	 * Returns <code>true</code> if the game is paused.
	 */
	public static boolean isPaused() {
		return instance.paused;
	}

	/**
	 * Shuts down the game.
	 */
	public static void shutDown() {
		System.out.println("Shutting down");
		instance.game.shutdown();
		instance.scriptManager.shutdown();
	}

	//
	// ======== MEMBERS
	//

	private StandardGame game;

	private EngineConfig config;

	private ScriptManager scriptManager;

	private EventManager eventManager;

	private boolean paused = false;

	public Engine(EngineConfig config) throws Exception {
		// VARIABLE SETUP
		Engine.instance = this;
		this.config = config;

		// SET LOGGING LEVELS
		initializeLogging();

		// RESOURCE SETUP
		initializeResourcePaths();

		// CREATE AND START THE GAME
		this.game = createGame();

		// START THE DISPLAY SYSTEM
		startDisplaySystem();

		// CREATE THE ENV
		createEnvironment();
	}

	/**
	 * Starts the game engine.
	 * 
	 * @throws Exception
	 */
	public void start(LoadingGameState state) throws Exception {
		// START EVENT MANAGER
		state.increment("Starting Event Subsystem");
		this.eventManager = createEventManager();
		
		// START SCRIPTING
		state.increment("Loading Scripts");
		this.scriptManager = createScriptManager();
	}

	/**
	 * Runs the game.
	 */
	public void runGame(LoadingGameState state) throws Exception {
		String gameScript = config.getGameScript();
		InGameState inGameState = new InGameState(InGameState.class.getName(), gameScript, state);
		GameStateManager.getInstance().attachChild(inGameState);
		state.setProgress(1f);
		inGameState.setActive(true);
	}

	/**
	 * Sets up the loggers.
	 */
	private void initializeLogging() {
		Logger.getLogger("com.jme").setLevel(Level.WARNING);
		Logger.getLogger("com.jmex").setLevel(Level.WARNING);
	}

	/**
	 * Creates an returns the game.
	 */
	private StandardGame createGame() {
		//
		// Build and start the game.
		//
		StandardGame game = new StandardGame(config.getWindowTitle(), GameType.GRAPHICAL, config,
				Thread.getDefaultUncaughtExceptionHandler());
		game.start();

		//
		// Register the shutdown state.
		//
		GameStateManager.getInstance().attachChild(new ShutdownGameState());

		return game;
	}

	/**
	 * Initializes the game's resource paths.
	 */
	private void initializeResourcePaths() throws Exception {
		for (Entry<String, String> entry : config.getResourcePaths().entrySet()) {
			ResourceLocatorTool.addResourceLocator(entry.getKey(), new SimpleResourceLocator(
					Engine.class.getClassLoader().getResource(entry.getValue())));
		}
	}

	/**
	 * Starts the display system.
	 */
	private void startDisplaySystem() {
		DisplaySystem disp = DisplaySystem.getDisplaySystem();
		System.out.println(disp);
		System.out.println(disp.getRenderer());
		disp.getRenderer().getCamera().setFrustumPerspective(45.0f, (float) disp.getWidth() / (float) disp.getHeight(),
				1f, 100000);
		disp.getRenderer().getCamera().update();
	}

	/**
	 * Creates the environment.
	 */
	private void createEnvironment() {
		Env env = Env.getInstance();
		env.setGameMode(config.getGameMode());
	}

	/**
	 * Creates and starts the script manager.
	 */
	private ScriptManager createScriptManager() throws Exception {
		ScriptManager scriptManager = ScriptManager.newScriptManager(config.getGameMode(), config.getScriptLoadPaths());
		scriptManager.start();
		return scriptManager;
	}

	/**
	 * Creates an event manager.
	 */
	private EventManager createEventManager() throws Exception {
		return new EventManager();
	}

	//
	// ======== PROPERTIES
	//

	public StandardGame getGame() {
		return game;
	}

	public ScriptManager getScriptManager() {
		return scriptManager;
	}

	public EventManager getEventManager() {
		return eventManager;
	}

	public EngineConfig getConfig() {
		return config;
	}

	//
	// ======== SINGLETON
	//

	private static Engine instance;

	/**
	 * Gets the engine singleton.
	 */
	public static Engine getInstance() {
		return instance;
	}
}

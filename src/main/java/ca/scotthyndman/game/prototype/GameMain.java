package ca.scotthyndman.game.prototype;

import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.scotthyndman.game.prototype.script.ScriptEngine;
import ca.scotthyndman.game.prototype.script.ScriptManager;
import ca.scotthyndman.game.prototype.state.InGameState;

import com.jme.system.DisplaySystem;
import com.jme.util.resource.ResourceLocatorTool;
import com.jme.util.resource.SimpleResourceLocator;
import com.jmex.game.StandardGame;
import com.jmex.game.StandardGame.GameType;
import com.jmex.game.state.GameStateManager;
import com.jmex.game.state.load.TransitionGameState;

public class GameMain {

	private static final String GAME_NAME = "Prototype";

	private static StandardGame game;

	private static boolean paused = false;

	public static void resume() {
		paused = false;
	}

	public static void pause() {
		paused = true;
	}

	public static boolean isPaused() {
		return paused;
	}

	public static void shutDown() {
		game.shutdown();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Build and start the game.
		game = new StandardGame(GAME_NAME, GameType.GRAPHICAL, null, Thread.getDefaultUncaughtExceptionHandler());
		game.start();

		// Set locations to find resources
		try {
			ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, new SimpleResourceLocator(
					GameMain.class.getClassLoader().getResource("textures/")));
			ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_AUDIO, new SimpleResourceLocator(
					GameMain.class.getClassLoader().getResource("sounds/")));
			ResourceLocatorTool.addResourceLocator("script", new SimpleResourceLocator(
					GameMain.class.getClassLoader().getResource("scripts/")));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		// Set logging levels
		Logger.getLogger("com.jme").setLevel(Level.WARNING);
		Logger.getLogger("com.jmex").setLevel(Level.WARNING);

		// Initialize the display system
		DisplaySystem disp = DisplaySystem.getDisplaySystem();
		disp.getRenderer().getCamera().setFrustumPerspective(45.0f, (float) disp.getWidth() / (float) disp.getHeight(),
				1f, 100000);
		disp.getRenderer().getCamera().update();

		// Create a transition game state
		TransitionGameState trans = new TransitionGameState(10, ResourceLocatorTool.locateResource(
				ResourceLocatorTool.TYPE_TEXTURE, "loading_black.png"));
		GameStateManager.getInstance().attachChild(trans);
		trans.setActive(true);

		// Write out a few things so the user can see what's happening
		trans.setProgress(0, "Initializing Game ...");

		// Load scripts
		trans.increment("Loading Scripts ...");
		ScriptEngine engine;
		try {
			engine = ScriptManager.loadScriptEngine();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		// Create the intro state
		// trans.increment("Initializing GameState: Intro ...");
		// GameStateManager.getInstance().attachChild(new IntroState("Intro"));

		// Create the menu state
		// trans.increment("Initializing GameState: Menu ...");
		// GameStateManager.getInstance().attachChild(new MenuState("Menu", trans));

		// Create the game state
		trans.increment("Initializing GameState: InGame ...");
		GameStateManager.getInstance().attachChild(new InGameState("InGame", trans));

		// Set the progress
		trans.setProgress(1.0f, "Finished Loading");

		// Activate the menu
		GameStateManager.getInstance().activateChildNamed("InGame");
	}

}

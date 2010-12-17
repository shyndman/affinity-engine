package ca.scotthyndman.game.prototype;

import ca.scotthyndman.game.engine.Engine;
import ca.scotthyndman.game.engine.Engine.GameMode;
import ca.scotthyndman.game.engine.config.EngineConfig;
import ca.scotthyndman.game.engine.state.LoadingGameState;

import com.jme.util.resource.ResourceLocatorTool;
import com.jmex.game.state.GameStateManager;

public class NewPrototype {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		//
		// Parse arguments
		//
		GameMode gameMode = GameMode.RELEASE;
		for (String arg : args) {
			if (arg.trim().equals("-debug")) {
				gameMode = GameMode.DEBUG;
				break;
			}
		}

		//
		// Load config
		//
		final GameMode gm = gameMode;
		EngineConfig config = new EngineConfig() {
			{
				setGameMode(gm);
				setGameScript("lotsgoingon.rb");
			}
		};

		//
		// Create the engine
		//
		Engine engine = new Engine(config);

		//
		// Activate the in-game state
		//
		LoadingGameState trans = new LoadingGameState(10, ResourceLocatorTool.locateResource(
				ResourceLocatorTool.TYPE_TEXTURE, "loading_black.png"));
		GameStateManager.getInstance().attachChild(trans);
		trans.setActive(true);
		trans.increment("Initializing Game...");

		//
		// Start the engine
		//
		engine.start(trans);
		
		//
		// Start the game
		//
		engine.runGame(trans);
	}
}

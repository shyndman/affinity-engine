package ca.scotthyndman.game.engine.state;

import ca.scotthyndman.game.engine.Engine;

import com.jmex.game.state.GameState;

/**
 * Automatically registered with the engine to handle shutdown.
 * 
 * @author scottyhyndman
 */
public class ShutdownGameState extends GameState {

	public ShutdownGameState() {
		setName(getClass().getName());
	}
	
	@Override
	public void cleanup() {
		Engine.shutDown();
	}

	@Override
	public void render(float tpf) {
	}

	@Override
	public void update(float tpf) {
	}
}

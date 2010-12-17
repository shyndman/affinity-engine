package ca.scotthyndman.game.engine.state;

import java.net.URL;

import com.jme.input.MouseInput;
import com.jmex.game.state.GameState;
import com.jmex.game.state.load.TransitionGameState;

public class LoadingGameState extends TransitionGameState {

	public LoadingGameState(URL imagePath) {
		super(imagePath);
	}

	public LoadingGameState(int steps, URL imagePath) {
		super(steps, imagePath);
	}

	public LoadingGameState(GameState leadIn, URL imagePath) {
		super(leadIn, imagePath);
	}

	public LoadingGameState(GameState leadIn, int steps, URL imagePath) {
		super(leadIn, steps, imagePath);
	}

	@Override
	public void update(float tpf) {
		if (!MouseInput.get().isCursorVisible()) {
			MouseInput.get().setCursorVisible(true);
		}
		super.update(tpf);
	}
}

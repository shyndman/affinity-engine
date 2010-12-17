package ca.scotthyndman.game.engine.scene;

import ca.scotthyndman.game.engine.entity.Env;

public interface Updatable {

	public void update(Env env, float tpf);

}
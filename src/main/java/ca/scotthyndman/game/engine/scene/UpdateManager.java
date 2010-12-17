package ca.scotthyndman.game.engine.scene;

public interface UpdateManager {

	void groupWasAdded(Group updatable);

	void groupWasRemoved(Group updatable);

	void updatableWasAdded(Updatable updatable);

	void updatableWasRemoved(Updatable updatable);
}

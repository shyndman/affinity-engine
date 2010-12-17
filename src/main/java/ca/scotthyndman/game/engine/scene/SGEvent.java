package ca.scotthyndman.game.engine.scene;

import ca.scotthyndman.game.engine.event.Event;

public class SGEvent extends Event {

	public Positioned node;
	public String path;
	public boolean rooted;
	
	public SGEvent(Positioned node) {
		this.node = node;
		this.rooted = isRooted(node);
	}

	public static boolean isRooted(Positioned node) {
		return node.rooted;
	}
}

package ca.scotthyndman.game.engine.event;

/**
 * Holds tick information.
 * 
 * @author scottyhyndman
 */
public class TickEvent extends Event {
	float timePerFrame;
	
	public TickEvent(float tpf) {
		this.timePerFrame = tpf;
	}
	
	public float getTimePerFrame() {
		return timePerFrame;
	}
}

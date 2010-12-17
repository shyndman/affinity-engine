package ca.scotthyndman.game.engine.input;

import ca.scotthyndman.game.engine.event.InputEvent;

/**
 * Responds to user input events.
 * 
 * @author scottyhyndman
 */
public abstract class InputActionHandler extends ActionHandler<InputEvent> {

	/**
	 * The minimum period (in seconds) at which this event reoccurs. The default is 0.
	 */
	private float speed;

	/**
	 * Gets the minimum period between the reoccurrences of this event.
	 * 
	 * @return the time in seconds
	 */
	public float getSpeed() {
		return speed;
	}

	/**
	 * Sets the minimum period between reoccurrences of this event (in seconds).
	 * 
	 * @param speed
	 *            the time in seconds
	 */
	public void setSpeed(float speed) {
		this.speed = speed;
	}
}

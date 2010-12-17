package ca.scotthyndman.game.engine.event;

import net.java.games.input.Component;

/**
 * Represents a user's interaction with an input device.
 * 
 * @author scottyhyndman
 */
public class InputEvent extends Event {

	private Component component;
	private float value;
	private long nanos;
	private float time;

	/**
	 * Builds a new {@link InputEvent}
	 * 
	 * @param srcEvent
	 *            the event source
	 */
	public InputEvent(net.java.games.input.Event srcEvent, float time) {
		set(srcEvent.getComponent(), srcEvent.getValue(), srcEvent.getNanos());
		this.time = time;
	}

	/**
	 * Gets the component.
	 * 
	 * @return the component, like the button, key, or axis affected by the user input
	 */
	public final Component getComponent() {
		return component;
	}

	/**
	 * Gets the component value.
	 * 
	 * @return the value
	 */
	public final float getValue() {
		return value;
	}

	/**
	 * Return the time the event happened, in nanoseconds. The time is relative and therefore can only be used to
	 * compare with other event times.
	 */
	public final long getNanos() {
		return nanos;
	}

	public float getTime() {
		return time;
	}

	protected final void set(Component component, float value, long nanos) {
		this.component = component;
		this.value = value;
		this.nanos = nanos;
	}
}

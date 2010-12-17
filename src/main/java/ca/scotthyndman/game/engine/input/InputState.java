package ca.scotthyndman.game.engine.input;

import net.java.games.input.Component;
import net.java.games.input.Keyboard;
import net.java.games.input.Mouse;
import net.java.games.input.Component.Identifier;

/**
 * The {@link InputState} object provides ways of conveniently checking mouse and keyboard state.
 * 
 * @author scottyhyndman
 */
public class InputState {

	Keyboard keyboard;
	Mouse mouse;

	private float mouseX;
	private float mouseY;

	/**
	 * Constructs a new {@link InputState}
	 * 
	 * @param keyboard
	 *            the keyboard component
	 * @param mouse
	 *            the mouse component
	 */
	InputState(Keyboard keyboard, Mouse mouse) {
		this.keyboard = keyboard;
		this.mouse = mouse;

		setMousePosition(400, 300);
	}

	/**
	 * Returns the mouse's x-coordinate.
	 */
	public float getMouseX() {
		return mouseX;
	}

	/**
	 * Return's the mouse's y-coordinate.
	 */
	public float getMouseY() {
		return mouseY;
	}

	/**
	 * Returns true if the specified mouse button is down.
	 * 
	 * @param button
	 *            the mouse button identifier
	 * @return <code>true</code> if the mouse button is down
	 */
	public boolean isMouseButtonDown(Identifier.Button button) {
		Component component = mouse.getComponent(button);
		return component == null ? false : component.getPollData() == 1f;
	}

	/**
	 * Returns <code>true</code> if the specified key is down.
	 */
	public boolean isKeyDown(Identifier.Key key) {
		return keyboard.isKeyDown(key);
	}

	/**
	 * Sets the mouse position.
	 * 
	 * @param x
	 *            the x position
	 * @param y
	 *            the y position
	 */
	void setMousePosition(float x, float y) {
		this.mouseX = x;
		this.mouseY = y;
	}

	/**
	 * Translates the mouse position.
	 * 
	 * @param x
	 *            the x position
	 * @param y
	 *            the y position
	 */
	void translateMousePosition(float dx, float dy) {
		this.mouseX += dx;
		this.mouseY -= dy;
	}

	/**
	 * Returns a string representation of the input state.
	 */
	@Override
	public String toString() {
		return "x: " + mouseX + ", y: " + mouseY;
	}
}

package ca.scotthyndman.game.prototype.input;

import com.jme.input.InputHandler;
import com.jme.input.KeyBindingManager;
import com.jme.input.KeyInput;

/**
 * Handles the player's movement.
 * 
 * @author scottyhyndman
 */
public class PlayerInputHandler extends InputHandler {

	public PlayerInputHandler() {
		setKeyBindings();
	}
	
	private void setKeyBindings() {
		KeyBindingManager keyboard = KeyBindingManager.getKeyBindingManager();
		
		// Make this configurable
		keyboard.add("moveLeft", KeyInput.KEY_A);
		keyboard.add("moveRight", KeyInput.KEY_D);
		keyboard.add("crouch", KeyInput.KEY_S);
		keyboard.add("jump", KeyInput.KEY_W);
	}
}

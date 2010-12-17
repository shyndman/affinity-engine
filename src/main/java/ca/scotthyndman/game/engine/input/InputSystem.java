package ca.scotthyndman.game.engine.input;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.java.games.input.AbstractController;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;
import net.java.games.input.Keyboard;
import net.java.games.input.Mouse;
import ca.scotthyndman.game.engine.event.InputEvent;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * The {@link InputSystem} is responsible for managing input device setup, polling of the devices and mapping between
 * logical "action" names and input components.
 * 
 * @author scottyhyndman
 */
@SuppressWarnings("unchecked")
public class InputSystem implements ActionHandlerManager {

	/**
	 * The class' logger.
	 */
	private static final Logger logger = Logger.getLogger(InputSystem.class.getName());

	/**
	 * The state of the input.
	 */
	InputState inputState;

	/**
	 * The mouse. TODO this should be refactored to avoid dependencies on specific input systems
	 */
	Mouse mouse;

	/**
	 * The keyboard. TODO this should be refactored to avoid dependencies on specific input systems
	 */
	Keyboard keyboard;

	/**
	 * Controllers used by this input system.
	 */
	List<AbstractController> controllers = new ArrayList<AbstractController>();

	/**
	 * A map between component identifiers and game "actions".
	 */
	Multimap<Component.Identifier, String> actionBindings = new ArrayListMultimap<Component.Identifier, String>();

	/**
	 * A map between game "actions" and handlers for those actions.
	 */
	Multimap<String, ActionHandler> actionHandlers = new HashMultimap<String, ActionHandler>();

	/**
	 * Constructs a new {@link InputSystem} instance.
	 */
	public InputSystem() {
		findControllers();
	}

	/**
	 * Finds controllers.
	 */
	private void findControllers() {
		for (Controller ctrl : ControllerEnvironment.getDefaultEnvironment().getControllers()) {
			if (ctrl instanceof Mouse && ctrl.getName().contains("Trackpad")) {
				mouse = (Mouse) ctrl;
			}

			if (ctrl instanceof Keyboard) {
				keyboard = (Keyboard) ctrl;
			}
		}

		controllers.add(mouse);
		controllers.add(keyboard);

		System.out.println(mouse);

		inputState = new InputState(keyboard, mouse);
	}

	/**
	 * Returns the input state.
	 */
	public InputState getInputState() {
		return inputState;
	}

	/**
	 * Returns <code>true</code> if the input system has input components associated with the specified action name.
	 * 
	 * @param action
	 *            the action name
	 */
	public boolean hasAction(String action) {
		return actionBindings.containsValue(action);
	}

	/**
	 * Adds an action binding to the input system.
	 * 
	 * @param identifier
	 * @param action
	 */
	public void addActionBinding(Component.Identifier identifier, String action) {
		logger.log(Level.INFO, "Adding action binding... " + identifier.toString() + " => " + action);
		actionBindings.put(identifier, action);
	}

	/**
	 * @see ca.scotthyndman.game.engine.input.ActionHandlerManager#addActionHandler(java.lang.String,
	 *      ca.scotthyndman.game.engine.input.InputActionHandler)
	 */
	public void addActionHandler(String action, ActionHandler handler) {
		handler.setManager((ActionHandlerManager) this);
		actionHandlers.put(action, handler);
	}

	/**
	 * @see ca.scotthyndman.game.engine.input.ActionHandlerManager#removeActionHandler(java.lang.String,
	 *      ca.scotthyndman.game.engine.input.InputActionHandler)
	 */
	public void removeActionHandler(String action, ActionHandler handler) {
		actionHandlers.remove(action, handler);
	}

	/**
	 * @see ca.scotthyndman.game.engine.input.ActionHandlerManager#removeActionHandler(ca.scotthyndman.game.engine.input.InputActionHandler)
	 */
	public void removeActionHandler(ActionHandler handler) {
		actionHandlers.removeAll(handler);
	}

	/**
	 * Clears the action handlers.
	 */
	public void clearActionHandlers() {
		actionHandlers.clear();
	}

	/**
	 * Clears the action bindings.
	 */
	public void clearActionBindings() {
		actionBindings.clear();
	}

	/**
	 * Polls input devices and dispatches events to any relevant action handlers.
	 * 
	 * @param tpf
	 */
	public void update(float tpf) {
		//
		// First poll the controllers
		//
		for (AbstractController ctrl : controllers) {
			ctrl.poll();
		}

		//
		// Update the mouse state
		//
		float x = mouse.getX().getPollData();
		float y = mouse.getY().getPollData();
		if (mouse.getX().isRelative())
			inputState.translateMousePosition(x, y);
		else
			inputState.setMousePosition(x, y);

		//
		// Go through each of the controllers, submitting events.
		//
		for (AbstractController ctrl : controllers) {
			EventQueue queue = ctrl.getEventQueue();
			Event event = new Event();

			while (queue.getNextEvent(event)) {
				//
				// Check if we have actions associated with this
				//
				Component.Identifier identifier = event.getComponent().getIdentifier();
				if (!actionBindings.containsKey(identifier)) {
					continue;
				}

				//
				// Notify any related ActionHandlers
				//
				InputEvent inputEvent = new InputEvent(event, tpf);

				for (String action : actionBindings.get(identifier)) {
					for (ActionHandler handler : actionHandlers.get(action)) {
						handler.performAction(inputEvent);
					}
				}
			}
		}
	}
}

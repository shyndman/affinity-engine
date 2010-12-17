package ca.scotthyndman.game.engine.input;

import ca.scotthyndman.game.engine.event.Event;

public interface ActionHandlerManager<T extends ActionHandler<? extends Event>> {

	/**
	 * Associates an action handler with an action.
	 * 
	 * @param action
	 * @param handler
	 */
	public void addActionHandler(String action, T handler);

	/**
	 * Dissociates an action handler with an action.
	 * 
	 * @param action
	 * @param handler
	 */
	public void removeActionHandler(String action, T handler);

	/**
	 * Dissociates an action handler with all actions.
	 * 
	 * @param action
	 * @param handler
	 */
	public void removeActionHandler(T handler);

	/**
	 * Clears the action handlers.
	 */
	public void clearActionHandlers();
}
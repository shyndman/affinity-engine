package ca.scotthyndman.game.engine.input;

import ca.scotthyndman.game.engine.event.Event;

/**
 * Event handler base class.
 * 
 * @author scottyhyndman
 * @param <T>
 *            The event class.
 */
public abstract class ActionHandler<T extends Event> {

	ActionHandlerManager<ActionHandler<Event>> manager;
	
	/**
	 * Called by the system when this handler's action is triggered.
	 * 
	 * @param event
	 *            the event
	 */
	public abstract void performAction(T event);
	
	public ActionHandlerManager<ActionHandler<Event>> getManager() {
		return manager;
	}
	public void setManager(ActionHandlerManager<ActionHandler<Event>> manager) {
		this.manager = manager;
	}
}
package ca.scotthyndman.game.engine.event;

import java.util.logging.Logger;

import ca.scotthyndman.game.engine.input.ActionHandler;
import ca.scotthyndman.game.engine.input.ActionHandlerManager;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * Manages the eventing system.
 * 
 * @author scottyhyndman
 */
public class EventManager implements ActionHandlerManager<ActionHandler<Event>> {

	/**
	 * The class' logger.
	 */
	private static final Logger logger = Logger.getLogger(EventManager.class.getName());

	/**
	 * Holds event handlers.
	 */
	private Multimap<String, ActionHandler<Event>> handlers = new HashMultimap<String, ActionHandler<Event>>();

	/**
	 * Holds event handlers.
	 */
	private Multimap<String, ActionHandler<Event>> persistentHandlers = new HashMultimap<String, ActionHandler<Event>>();
	
	/**
	 * Tick event. Reused.
	 */
	private TickEvent tickEvent = new TickEvent(0);

	/**
	 * Constructs a new {@link EventManager}
	 */
	public EventManager() {
	}

	/**
	 * Adds an {@link ActionHandler} for the specified event.
	 * 
	 * @param event
	 * @param handler
	 */
	public void addActionHandler(String event, ActionHandler<Event> handler) {
		// logger.log(Level.INFO, "Adding action handler for " + event);
		handlers.put(event, handler);
		handler.setManager(this);
	}
	
	/**
	 * Adds an {@link ActionHandler} for the specified event.
	 * 
	 * @param event
	 * @param handler
	 */
	public void addActionHandler(String event, ActionHandler<Event> handler, boolean persistent) {
		// logger.log(Level.INFO, "Adding action handler for " + event);
		persistentHandlers.put(event, handler);
		handler.setManager(this);
	}

	/**
	 * @see ActionHandlerManager#removeActionHandler(String, ActionHandler)
	 */
	public void removeActionHandler(String action, ActionHandler<Event> handler) {
		handlers.remove(action, handler);
	}

	/**
	 * @see ActionHandlerManager#removeActionHandler(ActionHandler)
	 */
	public void removeActionHandler(ActionHandler<Event> handler) {
		handlers.removeAll(handler);
	}

	/**
	 * Clears the action handlers.
	 */
	public void clearActionHandlers() {
		handlers.clear();
	}

	/**
	 * @param eventName
	 * @param eventInfo
	 */
	public void dispatchEvent(String eventName, Event eventInfo) {
		for (ActionHandler<Event> handler : handlers.get(eventName)) {
			handler.performAction(eventInfo);
		}
		for (ActionHandler<Event> handler : persistentHandlers.get(eventName)) {
			handler.performAction(eventInfo);
		}
	}

	/**
	 * Updates the EventManager, and sends out a tick event.
	 * 
	 * @param tpf
	 */
	public void update(float tpf) {
		tickEvent.timePerFrame = tpf;
		dispatchEvent("tick", tickEvent);
	}
}

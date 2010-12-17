package ca.scotthyndman.game.engine.scripting;

import static ca.scotthyndman.game.engine.scripting.ScriptingUtil.getValue;
import static java.lang.String.format;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.jruby.Ruby;
import org.jruby.RubyHash;
import org.jruby.RubyObject;
import org.jruby.RubyProc;
import org.jruby.java.proxies.JavaProxy;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;

import ca.scotthyndman.game.engine.animation.Finishable;
import ca.scotthyndman.game.engine.event.Event;
import ca.scotthyndman.game.engine.input.ActionHandler;
import ca.scotthyndman.game.engine.input.InputActionHandler;

import com.jme.input.action.InputActionEvent;
import com.jme.util.Timer;

/**
 * An {@link InputActionHandler} who's callback is a {@link RubyProc}.
 * 
 * @author scottyhyndman
 */
public class ScriptActionHandler<T extends Event> extends ActionHandler<T> {

	private static Logger logger = Logger.getLogger(ScriptActionHandler.class.getName());

	Finishable waitingFor = null;
	Ruby runtime;
	RubyHash info;
	RubyObject object;
	String actionName;
	String handlerName;
	RubyHash options;

	/**
	 * Constructs a new {@link ScriptActionHandler}.
	 * 
	 * @param info
	 */
	public ScriptActionHandler(RubyHash info) {
		this.info = info;
		this.runtime = info.getMetaClass().getRuntime();
		this.object = (RubyObject) getValue(runtime, info, "object");
		this.actionName = getValue(runtime, info, "action").toString();
		this.handlerName = getValue(runtime, info, "handler_name").toString();
		this.options = (RubyHash) getValue(runtime, info, "options");
	}

	public String getActionName() {
		return actionName;
	}
	
	/**
	 * Returns <code>true</code> if the action handler can perform its action on the specified event.
	 */
	protected boolean canPerformAction(T evt) {
		return waitingFor == null || waitingFor.isFinished();
	}

	/**
	 * Attempts to perform the handler's action. This method calls {@link #canPerformAction(InputActionEvent)} and
	 * ensures true is returned before any work is done.
	 */
	public void performAction(T evt) {
		//
		// Check to see if we can perform the action
		//
		if (!canPerformAction(evt)) {
			return;
		}
		waitingFor = null;

		//
		// Perform it!
		//
		IRubyObject returnValue = this.object.callMethod(runtime.getCurrentContext(), handlerName,
				JavaEmbedUtils.javaToRuby(runtime, evt));

		//
		// Did we get a return we can handle?
		//
		if (returnValue.getJavaClass().equals(void.class) || !(returnValue instanceof JavaProxy)) {
			return;
		}

		//
		// Do the switch baby
		//
		HandlerResult res = (HandlerResult) JavaEmbedUtils.rubyToJava(runtime, returnValue, HandlerResult.class);
		switch (res.resultCode) {
		case 0:
			// return. we're cool.
			return;

		case 1:
			waitingFor = res.finishable;
			return;

		case 2:
			logger.log(Level.FINE, format("Removing %s as an action handler", this.toString()));
			getManager().removeActionHandler((ActionHandler) this);
			return;

		default:
			logger.log(Level.SEVERE, format("An unknown handler result code of %d was encountered", res.resultCode));
			return;
		}
	}

	/**
	 * A handler result instructs the script action handler on how to proceed when calling the handler method in the
	 * future.
	 * 
	 * @author scottyhyndman
	 */
	public static class HandlerResult {
		public static final int RC_CONTINUE = 0;
		public static final int RC_WAIT = 1;
		public static final int RC_TERMINATE = 2;

		public int resultCode = 0;
		public Finishable finishable;

		private HandlerResult(int resultCode) {
			this.resultCode = resultCode;
		}

		private HandlerResult(int resultCode, Finishable finishable) {
			this.resultCode = resultCode;
			this.finishable = finishable;
		}

		@Override
		public String toString() {
			switch (resultCode) {
			case RC_CONTINUE:
				return "Handler => Continue";
			case RC_WAIT:
				return "Handler => Waiting for..." + finishable;
			case RC_TERMINATE:
				return "Handler => Terminating";
			}

			return "Handler => Unknown??";
		}

		public static HandlerResult continueOn() {
			return new HandlerResult(RC_CONTINUE);
		}

		public static HandlerResult waitFor(Finishable finishable) {
			return new HandlerResult(RC_WAIT, finishable);
		}

		public static HandlerResult waitDelay(float delayInSeconds) {
			return new HandlerResult(RC_WAIT, new DelayFinishable(delayInSeconds));
		}

		public static HandlerResult terminate() {
			return new HandlerResult(RC_TERMINATE);
		}
	}

	private static class DelayFinishable implements Finishable {
		private float delay;
		private float start;

		public DelayFinishable(float delayInSeconds) {
			this.delay = delayInSeconds;
			this.start = Timer.getTimer().getTimeInSeconds();
		}

		public boolean isFinished() {
			return (Timer.getTimer().getTimeInSeconds() - start) > delay;
		}
	}
}

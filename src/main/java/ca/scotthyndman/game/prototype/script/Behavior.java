package ca.scotthyndman.game.prototype.script;

import org.jruby.Ruby;
import org.jruby.RubyHash;
import org.jruby.RubyObject;

public class Behavior {
	private Ruby runtime;
	private RubyObject rubyObject;
	private RubyHash eventBlocks;

	/**
	 * Constructs a new {@link RubyBehavior}.
	 * 
	 * @param rubyObject
	 */
	public Behavior(RubyObject rubyObject) {
		this.rubyObject = rubyObject;
		this.runtime = rubyObject.getMetaClass().getRuntime();
		this.eventBlocks = (RubyHash) rubyObject.getInstanceVariable("@event_blocks");
	}

	public void handleEvent(String event, Object node) {

	}
}
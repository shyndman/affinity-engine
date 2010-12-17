package ca.scotthyndman.game.engine.scripting;

import org.jruby.RubyHash;

import ca.scotthyndman.game.engine.event.InputEvent;

public class ScriptInputActionHandler extends ScriptActionHandler<InputEvent> {

	public ScriptInputActionHandler(RubyHash info) {
		super(info);
	}

}

package ca.scotthyndman.game.engine.scripting;

import org.jruby.RubyInstanceConfig;

public class ReleaseScriptManager extends ScriptManager {

	@Override
	protected RubyInstanceConfig getConfig() {
		return new RubyInstanceConfig() {
			{
				setCompileMode(CompileMode.JIT);
				
			}
		};
	}

}

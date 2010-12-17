package ca.scotthyndman.game.engine.scripting;

import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyMethod;
import org.jruby.RubyProc;
import org.jruby.runtime.Block;
import org.jruby.runtime.builtin.IRubyObject;

public class ScriptSpecialFunctions {

	Ruby runtime;
	
	public ScriptSpecialFunctions(Ruby runtime) {
		this.runtime = runtime;
	}
	
	public IRubyObject special_call(RubyMethod method, RubyArray args) {
		boolean hasBlock = args.last() instanceof RubyProc;
		if (hasBlock) {
			RubyProc proc = (RubyProc) args.pop(runtime.getCurrentContext());
			return method.call(runtime.getCurrentContext(), args.toJavaArray(), proc.getBlock());
		}
		return method.call(runtime.getCurrentContext(), args.toJavaArray(), Block.NULL_BLOCK);
	}
}

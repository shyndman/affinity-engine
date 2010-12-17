package ca.scotthyndman.game.engine.console.command;

import org.jruby.Ruby;
import org.jruby.RubyBinding;
import org.jruby.RubyKernel;
import org.jruby.RubyObject;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.Block;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

import ca.scotthyndman.game.engine.console.EditorController;

public class RunRubyCommand extends Command {

	@Override
	public ExecutionResult execute(Ruby runtime, String command, EditorController controller) {
		try {
			ThreadContext currentContext = runtime.getCurrentContext();
			RubyBinding binding = RubyKernel.binding(currentContext, runtime.getTopSelf(), Block.NULL_BLOCK);
			IRubyObject ret = (RubyObject) RubyKernel.eval(currentContext, runtime.getTopSelf(), new IRubyObject[] {
					JavaEmbedUtils.javaToRuby(runtime, command), binding }, Block.NULL_BLOCK);

			return new ExecutionResult(ret.inspect().toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ExecutionResult(e);
		}
	}

}

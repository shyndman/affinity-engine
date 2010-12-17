package ca.scotthyndman.game.engine.console;

import static java.util.regex.Pattern.quote;
import static org.jruby.javasupport.JavaEmbedUtils.javaToRuby;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyBinding;
import org.jruby.RubyInstanceConfig;
import org.jruby.RubyKernel;
import org.jruby.RubyModule;
import org.jruby.RubyObject;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.Block;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class InputCompleter {

	private static final String[] RESERVED_WORDS = new String[] { "BEGIN", "END", "alias", "and", "begin", "break",
			"case", "class", "def", "defined", "do", "else", "elsif", "end", "ensure", "false", "for", "if", "in",
			"module", "next", "nil", "not", "or", "redo", "rescue", "retry", "return", "self", "super", "then", "true",
			"undef", "unless", "until", "when", "while", "yield", };

	private static final List<Object> RESERVED_WORDS_LIST = new ArrayList<Object>();
	{
		RESERVED_WORDS_LIST.addAll(Arrays.asList(RESERVED_WORDS));
	}

	public Iterable<Object> doComplete(Ruby runtime, String input) {
		RubyBinding binding = RubyKernel.binding(runtime.getCurrentContext(), runtime.getTopSelf(), Block.NULL_BLOCK);
		return doComplete(runtime, binding, input);
	}

	@SuppressWarnings("unchecked")
	public Iterable<Object> doComplete(Ruby runtime, RubyBinding binding, String input) {
		ThreadContext context = runtime.getCurrentContext();
		MatchWrapper m = new MatchWrapper();
		if (doesMatch(REGEXP, input, m)) {
			return selectMessage(m.receiver, m.message, getCandidates(runtime, "Regexp.instance_methods(true)"));
		} else if (doesMatch(ARRAY, input, m)) {
			return selectMessage(m.receiver, m.message, getCandidates(runtime, "Array.instance_methods(true)"));
		} else if (doesMatch(PROC_OR_HASH, input, m)) {
			return selectMessage(m.receiver, m.message, getCandidates(runtime,
					"Array.instance_methods(true) | Hash.instance_methods(true)"));
		} else if (doesMatch(SYMBOL, input, m)) {
			IRubyObject symbolCls = runtime.evalScriptlet("Symbol");
			if (symbolCls.respondsTo("all_symbols")) {
				return selectMessage("", m.receiver, prepend(getCandidates(runtime, "Symbol.all_symbols"), ":"));
			} else {
				return new ArrayList<Object>();
			}
		} else if (doesMatch(ABS_CONSTANT_OR_CLASS_METHOD, input, m)) {
			System.out.println("ABS_CONSTANT_OR_CLASS_METHOD");
			return prepend(selectMessage(m.receiver, m.message, getCandidates(runtime, "Object.constants")), "::");
		} else if (doesMatch(CONSTANT_OR_CLASS_METHOD, input, m)) {
			System.out.println("CONSTANT_OR_CLASS_METHOD");
			try {
				return prepend(selectMessage(m.receiver, m.message, getCandidates(runtime, m.receiver + ".constants | "
						+ m.receiver + ".methods")), m.receiver + "::");
			} catch (Exception e) {
				e.printStackTrace();
				return new ArrayList<Object>();
			}
		} else if (doesMatch(SYMBOL_METHOD, input, m)) {
			return selectMessage(m.receiver, m.message, getCandidates(runtime, "Symbol.instance_methods(true)"));
		} else if (doesMatch(NUMERIC, input, m) || doesMatch(HEX_NUMERIC, input, m)) {
			try {
				return selectMessage(m.receiver, m.message, toJavaList(eval(runtime, context, m.receiver, "methods",
						binding)));
			} catch (Exception e) {
				e.printStackTrace();
				return new ArrayList<Object>();
			}
		} else if (doesMatch(GLOBAL_VARIABLE, input, m)) {
			return selectMessage(m.receiver, quote(m.receiver), getCandidates(runtime, "global_variables"));
		} else if (doesMatch(VARIABLE, input, m)) {
			List candidates;
			RubyArray globals = eval(runtime, context, "global_variables", null, binding);
			RubyArray locals = eval(runtime, context, "local_variables", null, binding);
			RubyArray classConstants = eval(runtime, context, "self.class.constants", null, binding);
			RubyArray all = (RubyArray) ((RubyArray) globals.op_or(locals)).op_or(classConstants);
			if (all.includes(runtime.getCurrentContext(), javaToRuby(runtime, m.receiver))
					|| (doesMatch(CAPITALIZED, input) && doesMatch(HAS_DOT, input))) {
				try {
					candidates = toJavaList(eval(runtime, context, m.receiver + ".methods", null, binding));
				} catch (Exception e) {
					e.printStackTrace();
					return new ArrayList<Object>();
				}
			} else {
				candidates = new ArrayList<Object>();
				Iterator<RubyModule> it = runtime.getObjectSpace().iterator(runtime.fastGetModule("Module"));
				RubyModule module;
				while (null != (module = it.next())) {
					if (!module.respondsTo("instance_methods")) {
						continue;
					}

					candidates.addAll(toJavaList((RubyArray) module.callMethod(context, "instance_methods",
							JavaEmbedUtils.javaToRuby(runtime, false))));
				}

				candidates = new ArrayList<Object>(new HashSet<Object>(candidates));
				Collections.sort((List<String>) candidates);
			}

			return selectMessage(m.receiver, m.message, candidates);
		} else if (doesMatch(MAYBE_STRING, input, m)) {
			m.message = Pattern.quote(m.message);
			return selectMessage(m.receiver, m.message, getCandidates(runtime, "String.instance_methods(true)"));
		} else {
			List<Object> candidates = new ArrayList<Object>(toJavaList(eval(runtime, context,
					"methods | private_methods | local_variables | self.class.constants", null, binding)));
			candidates.addAll(RESERVED_WORDS_LIST);
			return selectMessage(null, input, candidates);
		}
	}

	private boolean doesMatch(Pattern pattern, String input, MatchWrapper match) {
		match.clear();
		Matcher m = pattern.matcher(input);
		match.matcher = m;
		if (m.find()) {
			if (m.groupCount() > 0) {
				match.receiver = m.group(1);
			}
			if (m.groupCount() > 1) {
				match.message = m.group(m.groupCount());
			}
			return true;
		}

		return false;
	}

	private boolean doesMatch(Pattern pattern, String input) {
		Matcher m = pattern.matcher(input);
		return m.find();
	}

	private List<Object> getCandidates(Ruby runtime, String script) {
		return toJavaList((RubyArray) runtime.evalScriptlet(script));
	}

	private RubyArray eval(Ruby runtime, ThreadContext context, String receiver, String message, RubyBinding binding) {
		RubyObject object = (RubyObject) RubyKernel.eval(context, runtime.getTopSelf(), new IRubyObject[] {
				JavaEmbedUtils.javaToRuby(runtime, receiver), binding }, Block.NULL_BLOCK);
		if (message == null) {
			return (RubyArray) object;
		}

		return (RubyArray) object.callMethod(runtime.getCurrentContext(), message);
	}

	private Iterable<Object> selectMessage(Object receiver, String message, Iterable<Object> candidates) {
		final Pattern pattern = Pattern.compile("^" + message + ".*$");
		return Iterables.filter(candidates, new Predicate<Object>() {
			public boolean apply(Object t) {
				return pattern.matcher(t.toString()).matches();
			}
		});
	}

	public static final Pattern REGEXP = Pattern.compile("^(\\/[^\\/]*\\/)\\.([^.]*)$");
	public static final Pattern ARRAY = Pattern.compile("^([^\\]]*\\])\\.([^.]*)$");
	public static final Pattern PROC_OR_HASH = Pattern.compile("^([^\\}]*\\})\\.([^.]*)$");
	public static final Pattern SYMBOL = Pattern.compile("^(:[^:.]*)$");
	public static final Pattern ABS_CONSTANT_OR_CLASS_METHOD = Pattern.compile("^::([A-Z][^:\\.\\(]*)$");
	public static final Pattern CONSTANT_OR_CLASS_METHOD = Pattern.compile("^(((::)?[A-Z][^:.\\(]*)+)::?([^:.]*)$");
	public static final Pattern SYMBOL_METHOD = Pattern.compile("^(:[^:.]+)\\.([^.]*)$");
	public static final Pattern NUMERIC = Pattern.compile("^(-?(0[dbo])?[0-9_]+(\\.[0-9_]+)?([eE]-?[0-9]+)?)\\.([^.]*)$");
	public static final Pattern HEX_NUMERIC = Pattern.compile("^(-?0x[0-9a-fA-F_]+)\\.([^.]*)$");
	public static final Pattern GLOBAL_VARIABLE = Pattern.compile("^(\\$[^.]*)$");
	public static final Pattern VARIABLE = Pattern.compile("^((\\.?[^.]+)+)\\.([^.]*)$");
	public static final Pattern MAYBE_STRING = Pattern.compile("^\\.([^.]*)$");
	public static final Pattern CAPITALIZED = Pattern.compile("^[A-Z]");
	public static final Pattern HAS_DOT = Pattern.compile("\\.");

	private static class MatchWrapper {
		public Matcher matcher;
		public String receiver;
		public String message;

		public void clear() {
			matcher = null;
			receiver = null;
			message = null;
		}
	}

	private static Iterable<Object> prepend(Iterable<Object> src, final String prefix) {
		return Iterables.transform(src, new Function<Object, Object>() {
			public Object apply(Object from) {

				return prefix + from.toString();
			}
		});
	}

	private static List<Object> toJavaList(RubyArray array) {
		return Arrays.asList(array.toArray());
	}

	public static void main(String[] args) {
		InputCompleter completer = new InputCompleter();
		Ruby runtime = Ruby.newInstance(new RubyInstanceConfig() {
			{
				setCompileMode(CompileMode.OFF);
				setObjectSpaceEnabled(true);
			}
		});
		System.out.println(completer.doComplete(runtime, "3."));
		RubyKernel.eval(runtime.getCurrentContext(), runtime.getTopSelf(), new IRubyObject[] {
				JavaEmbedUtils.javaToRuby(runtime, "foo = 3"),
				RubyKernel.binding(runtime.getCurrentContext(), runtime.getTopSelf(), Block.NULL_BLOCK) },
				Block.NULL_BLOCK);
		System.out.println(completer.doComplete(runtime, "f"));
	}
}

package ca.scotthyndman.game.engine.scripting;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;

import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyException;
import org.jruby.RubyHash;
import org.jruby.RubyInstanceConfig;
import org.jruby.RubyNil;
import org.jruby.RubySymbol;
import org.jruby.ast.ClassNode;
import org.jruby.ast.Colon2Node;
import org.jruby.ast.NewlineNode;
import org.jruby.ast.Node;
import org.jruby.exceptions.JumpException;
import org.jruby.exceptions.RaiseException;
import org.jruby.internal.runtime.ValueAccessor;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.parser.EvalStaticScope;
import org.jruby.runtime.DynamicScope;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.runtime.scope.ManyVarsDynamicScope;

import ca.scotthyndman.game.engine.util.Tuple2;

/**
 * Utility methods for handling JRuby-scripted objects.
 * <p>
 * As of Spring 3.0, this class requires JRuby 1.1 or higher.
 * 
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Rick Evans
 * @since 2.0
 */
public abstract class ScriptingUtil {

	/**
	 * Gets a value out of a ruby hash.
	 * 
	 * @param runtime
	 * @param hash
	 * @param key
	 * @return
	 */
	public static Object getValue(Ruby runtime, RubyHash hash, String key) {
		return hash.get(RubySymbol.newSymbol(runtime, key));
	}

	public static int getIntValue(Object rubyNumber) {
		return ((Number) rubyNumber).intValue();
	}

	public static long getLongValue(Object rubyNumber) {
		return ((Number) rubyNumber).longValue();
	}

	public static float getFloatValue(Object rubyNumber) {
		return ((Number) rubyNumber).floatValue();
	}

	/**
	 * Create a new JRuby-scripted object from the given script source, using the default {@link ClassLoader}.
	 * 
	 * @param scriptSource
	 *            the script source text
	 * @param interfaces
	 *            the interfaces that the scripted Java object is to implement
	 * @return the scripted Java object
	 * @throws JumpException
	 *             in case of JRuby parsing failure
	 * @see ClassUtils#getDefaultClassLoader()
	 */
	@SuppressWarnings("unchecked")
	public static Tuple2<Object, Ruby> createJRubyObject(String scriptSource, Class[] interfaces,
			RubyInstanceConfig config) throws JumpException {
		return createJRubyObject(scriptSource, interfaces, ScriptingUtil.class.getClassLoader(),
				Collections.EMPTY_LIST, config, null);
	}

	/**
	 * Create a new JRuby-scripted object from the given script source, using the default {@link ClassLoader}.
	 * 
	 * @param scriptSource
	 *            the script source text
	 * @param interfaces
	 *            the interfaces that the scripted Java object is to implement
	 * @return the scripted Java object
	 * @throws JumpException
	 *             in case of JRuby parsing failure
	 * @see ClassUtils#getDefaultClassLoader()
	 */
	@SuppressWarnings("unchecked")
	public static Tuple2<Object, Ruby> createJRubyObject(String scriptSource, Class[] interfaces,
			List<String> loadPaths, RubyInstanceConfig config, ExecutorService service) throws JumpException {
		return createJRubyObject(scriptSource, interfaces, ScriptingUtil.class.getClassLoader(), loadPaths, config,
				service);
	}

	/**
	 * Create a new JRuby-scripted object from the given script source.
	 * 
	 * @param scriptSource
	 *            the script source text
	 * @param interfaces
	 *            the interfaces that the scripted Java object is to implement
	 * @param classLoader
	 *            the {@link ClassLoader} to create the script proxy with
	 * @return the scripted Java object
	 * @throws JumpException
	 *             in case of JRuby parsing failure
	 */
	@SuppressWarnings("unchecked")
	public static Tuple2<Object, Ruby> createJRubyObject(String scriptSource, Class[] interfaces,
			ClassLoader classLoader, List<String> loadPaths, RubyInstanceConfig config, ExecutorService service) {
		Ruby ruby = initializeRuntime(loadPaths, config, service);
		config = ruby.getInstanceConfig();
		ruby.getGlobalVariables().defineReadonly("$$", new ValueAccessor(ruby.newFixnum(System.identityHashCode(ruby))));

		Node scriptRootNode = ruby.parseEval(scriptSource, "affinity.rb", null, 0);
		IRubyObject rubyObject = ruby.runNormally(scriptRootNode, false);

		if (rubyObject instanceof RubyNil) {
			String className = findClassName(scriptRootNode);
			rubyObject = ruby.evalScriptlet("\n" + className + ".new");
		}
		// still null?
		if (rubyObject instanceof RubyNil) {
			throw new IllegalStateException("Compilation of JRuby script returned RubyNil: " + rubyObject);
		}

		return new Tuple2<Object, Ruby>(Proxy.newProxyInstance(classLoader, interfaces,
				new RubyObjectInvocationHandler(rubyObject, ruby)), ruby);
	}

	/**
	 * Initializes an instance of the {@link org.jruby.Ruby} runtime.
	 */
	public static Ruby initializeRuntime(List<String> loadPaths, RubyInstanceConfig config, ExecutorService service) {
		if (service == null) {
			Ruby r;
			if (config == null) {
				r = JavaEmbedUtils.initialize(loadPaths);
			} else {
				r = JavaEmbedUtils.initialize(loadPaths, config);
			}

			pushScope(r);

			return r;
		}

		FutureTask<Ruby> futureRuby = new FutureTask<Ruby>(new CreateRubyCallable(config, loadPaths));
		service.execute(futureRuby);
		try {
			return futureRuby.get();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static void pushScope(Ruby r) {
		DynamicScope currentScope = r.getCurrentContext().getCurrentScope();
		ManyVarsDynamicScope newScope = new ManyVarsDynamicScope(new EvalStaticScope(currentScope.getStaticScope()),
				currentScope);
		r.getCurrentContext().preEvalScriptlet(newScope);
	}

	public static void resetRuntime(Ruby r) {
		r.getCurrentContext().popScope();
		pushScope(r);
	}

	/**
	 * Given the root {@link Node} in a JRuby AST will locate the name of the class defined by that AST.
	 * 
	 * @throws IllegalArgumentException
	 *             if no class is defined by the supplied AST
	 */
	private static String findClassName(Node rootNode) {
		ClassNode classNode = findClassNode(rootNode);
		if (classNode == null) {
			throw new IllegalArgumentException("Unable to determine class name for root spatial '" + rootNode + "'");
		}
		Colon2Node node = (Colon2Node) classNode.getCPath();
		return node.getName();
	}

	/**
	 * Find the first {@link ClassNode} under the supplied {@link Node}.
	 * 
	 * @return the found <code>ClassNode</code>, or <code>null</code> if no {@link ClassNode} is found
	 */
	private static ClassNode findClassNode(Node node) {
		if (node instanceof ClassNode) {
			return (ClassNode) node;
		}
		List<Node> children = node.childNodes();
		for (Node child : children) {
			if (child instanceof ClassNode) {
				return (ClassNode) child;
			} else if (child instanceof NewlineNode) {
				NewlineNode nn = (NewlineNode) child;
				Node found = findClassNode(nn.getNextNode());
				if (found instanceof ClassNode) {
					return (ClassNode) found;
				}
			}
		}
		for (Node child : children) {
			Node found = findClassNode(child);
			if (found instanceof ClassNode) {
				return (ClassNode) found;
			}
		}
		return null;
	}

	/**
	 * InvocationHandler that invokes a JRuby script method.
	 */
	private static class RubyObjectInvocationHandler implements InvocationHandler {

		private final IRubyObject rubyObject;

		private final Ruby ruby;

		public RubyObjectInvocationHandler(IRubyObject rubyObject, Ruby ruby) {
			this.rubyObject = rubyObject;
			this.ruby = ruby;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (isEqualsMethod(method)) {
				return (isProxyForSameRubyObject(args[0]));
			} else if (isHashCodeMethod(method)) {
				return this.rubyObject.hashCode();
			} else if (isToStringMethod(method)) {
				String toStringResult = this.rubyObject.toString();
				if (toStringResult.trim().length() == 0) {
					toStringResult = identityToString(this.rubyObject);
				}
				return "JRuby object [" + toStringResult + "]";
			}
			try {
				IRubyObject[] rubyArgs = convertToRuby(args);
				IRubyObject rubyResult = this.rubyObject.callMethod(this.ruby.getCurrentContext(), method.getName(),
						rubyArgs);
				return convertFromRuby(rubyResult, method.getReturnType());
			} catch (RaiseException ex) {
				throw new JRubyExecutionException(ex);
			}
		}

		private boolean isProxyForSameRubyObject(Object other) {
			if (!Proxy.isProxyClass(other.getClass())) {
				return false;
			}
			InvocationHandler ih = Proxy.getInvocationHandler(other);
			return (ih instanceof RubyObjectInvocationHandler && this.rubyObject.equals(((RubyObjectInvocationHandler) ih).rubyObject));
		}

		private IRubyObject[] convertToRuby(Object[] javaArgs) {
			if (javaArgs == null || javaArgs.length == 0) {
				return new IRubyObject[0];
			}
			IRubyObject[] rubyArgs = new IRubyObject[javaArgs.length];
			for (int i = 0; i < javaArgs.length; ++i) {
				rubyArgs[i] = JavaEmbedUtils.javaToRuby(this.ruby, javaArgs[i]);
			}
			return rubyArgs;
		}

		private Object convertFromRuby(IRubyObject rubyResult, Class<?> returnType) {
			Object result = JavaEmbedUtils.rubyToJava(this.ruby, rubyResult, returnType);
			if (result instanceof RubyArray && returnType.isArray()) {
				result = convertFromRubyArray(((RubyArray) result).toJavaArray(), returnType);
			}
			return result;
		}

		private Object convertFromRubyArray(IRubyObject[] rubyArray, Class<?> returnType) {
			Class<?> targetType = returnType.getComponentType();
			Object javaArray = Array.newInstance(targetType, rubyArray.length);
			for (int i = 0; i < rubyArray.length; i++) {
				IRubyObject rubyObject = rubyArray[i];
				Array.set(javaArray, i, convertFromRuby(rubyObject, targetType));
			}
			return javaArray;
		}
	}

	/**
	 * Exception thrown in response to a JRuby {@link RaiseException} being thrown from a JRuby method invocation.
	 * <p>
	 * Introduced because the <code>RaiseException</code> class does not have useful {@link Object#toString()},
	 * {@link Throwable#getMessage()}, and {@link Throwable#printStackTrace} implementations.
	 */
	public static class JRubyExecutionException extends RuntimeException {

		/**
		 * Create a new <code>JRubyException</code>, wrapping the given JRuby <code>RaiseException</code>.
		 * 
		 * @param ex
		 *            the cause (must not be <code>null</code>)
		 */
		public JRubyExecutionException(RaiseException ex) {
			super(buildMessage(ex), ex);
		}

		private static String buildMessage(RaiseException ex) {
			RubyException rubyEx = ex.getException();
			return (rubyEx != null && rubyEx.message != null) ? rubyEx.message.toString() : "Unexpected JRuby error";
		}
	}

	/**
	 * Determine whether the given method is an "equals" method.
	 * 
	 * @see java.lang.Object#equals
	 */
	@SuppressWarnings("unchecked")
	public static boolean isEqualsMethod(Method method) {
		if (method == null || !method.getName().equals("equals")) {
			return false;
		}
		Class[] paramTypes = method.getParameterTypes();
		return (paramTypes.length == 1 && paramTypes[0] == Object.class);
	}

	/**
	 * Determine whether the given method is a "hashCode" method.
	 * 
	 * @see java.lang.Object#hashCode
	 */
	public static boolean isHashCodeMethod(Method method) {
		return (method != null && method.getName().equals("hashCode") && method.getParameterTypes().length == 0);
	}

	/**
	 * Determine whether the given method is a "toString" method.
	 * 
	 * @see java.lang.Object#toString()
	 */
	public static boolean isToStringMethod(Method method) {
		return (method != null && method.getName().equals("toString") && method.getParameterTypes().length == 0);
	}

	/**
	 * Return a String representation of an object's overall identity.
	 * 
	 * @param obj
	 *            the object (may be <code>null</code>)
	 * @return the object's identity as String representation, or an empty String if the object was <code>null</code>
	 */
	public static String identityToString(Object obj) {
		if (obj == null) {
			return "";
		}
		return obj.getClass().getName() + "@" + getIdentityHexString(obj);
	}

	/**
	 * Return a hex String form of an object's identity hash code.
	 * 
	 * @param obj
	 *            the object
	 * @return the object's identity code in hex notation
	 */
	public static String getIdentityHexString(Object obj) {
		return Integer.toHexString(System.identityHashCode(obj));
	}

	public static class CreateRubyCallable implements Callable<Ruby> {
		RubyInstanceConfig config;
		List<String> loadPaths;

		public CreateRubyCallable(RubyInstanceConfig config, List<String> loadPaths) {
			this.config = config;
			this.loadPaths = loadPaths;
		}

		public Ruby call() throws Exception {
			Ruby r;
			if (config == null) {
				r = JavaEmbedUtils.initialize(loadPaths);
			} else {
				r = JavaEmbedUtils.initialize(loadPaths, config);
			}

			pushScope(r);

			return r;
		}
	}
}

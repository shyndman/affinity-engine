package ca.scotthyndman.game.engine.scripting;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.jruby.CompatVersion;
import org.jruby.Ruby;
import org.jruby.RubyInstanceConfig;

import ca.scotthyndman.game.engine.Engine;
import ca.scotthyndman.game.engine.entity.Env;
import ca.scotthyndman.game.engine.state.InGameState;
import ca.scotthyndman.game.engine.util.FileUtil;
import ca.scotthyndman.game.engine.util.Tuple2;

/**
 * Manages game scripting.
 * 
 * @author scottyhyndman
 */
public abstract class ScriptManager {

	Logger logger = Logger.getLogger(getClass().getName());

	protected ScriptEngine engine;
	protected ExecutorService scriptExecutorService;
	protected Ruby runtime;
	protected List<String> loadPaths;
	protected String gameScript;
	protected boolean started = false;

	/**
	 * Builds a new script manager.
	 */
	protected ScriptManager() {
		scriptExecutorService = Executors.newSingleThreadExecutor();
	}

	/**
	 * Sets the load paths.
	 * 
	 * @param loadPaths
	 */
	public void setLoadPaths(List<String> loadPaths) {
		this.loadPaths = loadPaths;
	}

	/**
	 * Gets the load paths.
	 * 
	 * @return
	 */
	public List<String> getLoadPaths() {
		return loadPaths;
	}

	/**
	 * Returns the script engine.
	 * 
	 * @return
	 */
	public ScriptEngine getEngine() {
		return engine;
	}

	/**
	 * Returns the runtime.
	 * 
	 * @return
	 */
	public Ruby getRuntime() {
		return runtime;
	}

	/**
	 * Gets the executor service for scripts.
	 * 
	 * @return
	 */
	public ExecutorService getScriptExecutorService() {
		return scriptExecutorService;
	}

	/**
	 * Returns true if the manager is started.
	 */
	public boolean isStarted() {
		return started;
	}

	/**
	 * Resets the script manager.
	 */
	public void reset() {
		engine.reset();
	}

	/**
	 * Returns the configuration for the script manager.
	 */
	protected abstract RubyInstanceConfig getConfig();

	/**
	 * Loads in a ScriptEngine that can be asked to load game-specific scripts.
	 * 
	 * @param loadPaths
	 * @return
	 * @throws Exception
	 */
	public ScriptEngine start() throws Exception {
		if (started) {
			logger.warning("ScriptEngine is already started");
			return engine;
		}

		// Load in the main script
		InputStream is = new FileInputStream(new File("src/main/ruby/lib/affinity.rb"));
		String scriptContents = FileUtil.readStringFromStream(is);

		// Modify the loadPath to include the lib directory
		List<String> loadPaths = getLoadPaths();
		loadPaths = loadPaths == null || loadPaths.equals(Collections.EMPTY_LIST) ? new ArrayList<String>() : loadPaths;
		loadPaths.add("lib");

		//
		// Create the script engine and runtime
		//
		RubyInstanceConfig config = getConfig();
		config.setCompatVersion(CompatVersion.RUBY1_9);
		Tuple2<Object, Ruby> ret = ScriptingUtil.createJRubyObject(scriptContents, new Class[] { ScriptEngine.class },
				loadPaths, config, getScriptExecutorService());
		engine = (ScriptEngine) ret.first;
		runtime = ret.second;

		engine.specials(new ScriptSpecialFunctions(runtime));
		engine.env(Env.getInstance());

		started = true;
		return engine;
	}

	/**
	 * Shuts it down.
	 */
	public void shutdown() {
		getScriptExecutorService().shutdown();
	}

	/**
	 * Runs the game specified.
	 * 
	 * @param gameScript
	 */
	public void runGame(InGameState state, String gameScript) {
		this.gameScript = gameScript;
		engine.ready(gameScript);
	}

	/**
	 * Gets a new script manager for the specified scripting mode.
	 * 
	 * @param mode
	 *            the mode
	 * @return a new scripting manager
	 */
	public static ScriptManager newScriptManager(Engine.GameMode mode, List<String> loadPaths) {
		ScriptManager mgr;
		switch (mode) {
		case RELEASE:
			mgr = new ReleaseScriptManager();
		case DEBUG:
		default:
			mgr = new DebugScriptManager();
		}

		mgr.setLoadPaths(loadPaths);
		return mgr;
	}
}

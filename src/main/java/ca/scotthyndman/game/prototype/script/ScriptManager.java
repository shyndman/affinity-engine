package ca.scotthyndman.game.prototype.script;

import java.io.InputStream;
import java.net.URL;

import com.jme.util.resource.ResourceLocatorTool;

/**
 * Loads scripts into the system.
 * 
 * @author scottyhyndman
 */
public class ScriptManager {

	public static ScriptEngine loadScriptEngine() throws Exception {
		// Load in behavior repo
		URL url = ResourceLocatorTool.locateResource("script", "main.rb");
		StringBuilder sb = new StringBuilder();
		InputStream is = url.openStream();
		int c;
		while (-1 != (c = is.read())) {
			sb.append((char) c);
		}
		is.close();

		ScriptEngine engine = (ScriptEngine) ScriptingUtil.createJRubyObject(sb.toString(),
				new Class[] { ScriptEngine.class });
		return engine;
	}
}

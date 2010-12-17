package ca.scotthyndman.game.engine.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.scotthyndman.game.engine.Engine.GameMode;
import ca.scotthyndman.game.engine.util.CollectionUtil;

import com.jme.system.PropertiesGameSettings;
import com.jme.util.resource.ResourceLocatorTool;

/**
 * Configures a game engine.
 * 
 * @author scottyhyndman
 */
public class EngineConfig extends PropertiesGameSettings {

	private String windowTitle;
	private GameMode gameMode;
	private String gameScript;
	private List<String> scriptLoadPaths;
	private Map<String, String> resourcePaths = new HashMap<String, String>();

	public EngineConfig() {
		this("pgs.properties");
	}

	public EngineConfig(String userFile) {
		super(userFile);

		setWindowTitle("Affinity Prototype Game");
		setGameMode(GameMode.RELEASE);
		setFramerate(60);
		setWidth(800);
		setHeight(600);
		setScriptLoadPaths(CollectionUtil.list("src/main/ruby"));
		addScriptLoadPaths("src/main/ruby/prototype");
		addScriptLoadPaths("src/main/ruby/lib");
		putResourcePath(ResourceLocatorTool.TYPE_TEXTURE, "textures/");
		putResourcePath(ResourceLocatorTool.TYPE_AUDIO, "sounds/");
	}

	public String getWindowTitle() {
		return windowTitle;
	}

	public void setWindowTitle(String windowTitle) {
		this.windowTitle = windowTitle;
	}

	public GameMode getGameMode() {
		return gameMode;
	}

	public void setGameMode(GameMode isDebugMode) {
		this.gameMode = isDebugMode;
	}

	public String getGameScript() {
		return gameScript;
	}

	public void setGameScript(String gameScript) {
		this.gameScript = gameScript;
	}

	public List<String> getScriptLoadPaths() {
		return scriptLoadPaths;
	}

	public void setScriptLoadPaths(List<String> scriptLoadPaths) {
		this.scriptLoadPaths = scriptLoadPaths;
	}

	public void addScriptLoadPaths(String... loadPaths) {
		for (String path : loadPaths) {
			getScriptLoadPaths().add(path);
		}
	}

	public Map<String, String> getResourcePaths() {
		return resourcePaths;
	}

	public void setResourcePaths(Map<String, String> resourcePaths) {
		this.resourcePaths = resourcePaths;
	}

	public void putResourcePath(String type, String path) {
		getResourcePaths().put(type, path);
	}

	public String getResourcePath(String type) {
		return getResourcePaths().get(type);
	}
}

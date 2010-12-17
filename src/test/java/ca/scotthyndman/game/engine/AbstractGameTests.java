package ca.scotthyndman.game.engine;

import java.net.URISyntaxException;

import ca.scotthyndman.game.engine.Engine;

import com.jme.util.resource.ResourceLocatorTool;
import com.jme.util.resource.SimpleResourceLocator;

public class AbstractGameTests {

	static {
		// Set locations to find resources
		try {
			ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, new SimpleResourceLocator(
					Engine.class.getClassLoader().getResource("textures/")));
			ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_AUDIO, new SimpleResourceLocator(
					Engine.class.getClassLoader().getResource("sounds/")));
			ResourceLocatorTool.addResourceLocator("script", new SimpleResourceLocator(
					Engine.class.getClassLoader().getResource("lib/")));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

}

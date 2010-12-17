package ca.scotthyndman.game.engine.scene;

import java.net.URL;

import ca.scotthyndman.game.engine.animation.Real;

import com.jme.image.Texture;
import com.jme.scene.Spatial;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.TextureState;
import com.jme.util.TextureManager;
import com.jme.util.resource.ResourceLocatorTool;

public class Graphic extends Positioned {

	/**
	 * Graphic texture state.
	 */
	private Texture texture;

	/**
	 * Graphic texture state.
	 */
	private TextureState textureState;

	/**
	 * Creates a new graphic.
	 * 
	 * @param textureName
	 *            the name of the texture
	 */
	public Graphic(String textureName) {
		this(textureName, 0f, 0f);
	}

	/**
	 * Creates a new graphic.
	 * 
	 * @param textureName
	 *            the name of the texture
	 * @param x
	 *            the x coordinate
	 * @param y
	 *            the y coordinate
	 */
	public Graphic(String textureName, float x, float y) {
		this(TextureManager.loadTexture(urlForPath(textureName), Texture.MinificationFilter.BilinearNoMipMaps,
				Texture.MagnificationFilter.Bilinear, 1.0f, true), x, y);
	}

	/**
	 * Creates a new graphic.
	 * 
	 * @param texture
	 *            the texture
	 * @param x
	 *            the x coordinate
	 * @param y
	 *            the y coordinate
	 */
	public Graphic(Texture texture, float x, float y) {
		super("", x, y);

		name = texture.getTextureKey().getLocation().getFile();
		name = name.substring(name.lastIndexOf('/') + 1);
		
		
		this.texture = texture;
		textureState = renderer.createTextureState();
		textureState.setTexture(texture);

		// Get the width and height
		float width = texture.getImage().getWidth();
		float height = texture.getImage().getHeight();

		// Create the quad that displays the texture
		((Quad) spatial).resize(width, height);
		spatial.setRenderState(textureState);

		// Update geometry
		spatial.updateGeometricState(0, true);
		spatial.updateRenderState();
	}

	/**
	 * Constructs the spatial.
	 */
	@Override
	protected Spatial constructSpatial(Real x, Real y) {
		Quad spatial = new Quad(this.name);
		spatial.setLocalTranslation(x.get(), y.get(), 0f);
		spatial.updateGeometricState(0f, true);

		enableAlpha(spatial);
		return spatial;
	}
	
	/**
	 * Returns the texture.
	 */
	public Texture getTexture() {
		return texture;
	}

	/**
	 * Returns the URL for a texture path.
	 * 
	 * @param textureName
	 * @return
	 */
	private static URL urlForPath(String textureName) {
		return ResourceLocatorTool.locateResource(ResourceLocatorTool.TYPE_TEXTURE, textureName);
	}
}

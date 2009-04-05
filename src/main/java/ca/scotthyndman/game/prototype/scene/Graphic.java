package ca.scotthyndman.game.prototype.scene;

import java.net.URL;

import com.jme.image.Texture;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.TextureState;
import com.jme.util.TextureManager;
import com.jme.util.resource.ResourceLocatorTool;

public class Graphic extends Positioned {

	/**
	 * The geometry on which the graphic is drawn.
	 */
	private Quad geometry;

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
		super(texture.getTextureKey().getLocation().toString(), x, y);

		this.texture = texture;
		textureState = renderer.createTextureState();
		textureState.setTexture(texture);

		// Get the width and height
		float width = texture.getImage().getWidth();
		float height = texture.getImage().getHeight();

		// Create the quad that displays the texture
		geometry = new Quad("Graphic(" + this.texture.getTextureKey().getLocation().toString() + ")", width, height);
		geometry.setRenderState(textureState);

		// Add the geometry to the content node
		getContentNode().attachChild(geometry);
		
		// Update geometry
		geometry.updateGeometricState(0, true);
		geometry.updateRenderState();
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

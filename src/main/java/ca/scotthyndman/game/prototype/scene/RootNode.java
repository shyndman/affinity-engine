package ca.scotthyndman.game.prototype.scene;

import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.state.LightState;
import com.jme.system.DisplaySystem;

/**
 * The root node in the game scene graph.
 * 	
 * @author scottyhyndman
 */
public class RootNode extends Node {
	/**
	 * The serial version id.
	 */
	static final long serialVersionUID = 1L;

	/**
	 * the Display.
	 */
	private DisplaySystem display = DisplaySystem.getDisplaySystem();

	public RootNode() {
		setRenderQueueMode(Renderer.QUEUE_ORTHO);
		setCullHint(Spatial.CullHint.Never);

		LightState ls = display.getRenderer().createLightState();
		ls.setEnabled(false);
		setLightCombineMode(LightCombineMode.Replace);
		setRenderState(ls);
		updateRenderState();
	}
}

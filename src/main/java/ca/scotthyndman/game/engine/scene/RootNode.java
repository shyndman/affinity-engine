package ca.scotthyndman.game.engine.scene;

import java.util.ArrayList;
import java.util.List;

import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.state.LightState;
import com.jme.system.DisplaySystem;

/**
 * The root spatial in the game scene graph.
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

	private UpdateManager updateManager;

	private List<Positioned> children = new ArrayList<Positioned>();

	public RootNode(UpdateManager updateManager) {
		this.updateManager = updateManager;
		setRenderQueueMode(Renderer.QUEUE_ORTHO);
		setCullHint(Spatial.CullHint.Never);

		LightState ls = display.getRenderer().createLightState();
		ls.setEnabled(true);
		ls.setGlobalAmbient(new ColorRGBA(1f, 0.3f, 0.2f, 1f));

		// setLightCombineMode(LightCombineMode.Replace);
		setRenderState(ls);
		updateRenderState();
	}

	public int attachChild(Positioned child) {
		try {
			children.add(child);
			return super.attachChild(child.getTopNode());
		} finally {
			child.rooted = true;
			if (child instanceof Group) {
				updateManager.groupWasAdded((Group) child);
			} else {
				updateManager.updatableWasAdded(child);
			}
		}
	}

	@Override
	public void detachAllChildren() {
		super.detachAllChildren();
		for (Positioned child : children) {
			System.out.println("removing " + child);
			updateManager.updatableWasRemoved(child);
		}
	}
}

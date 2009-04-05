package ca.scotthyndman.game.prototype.scene;

import ca.scotthyndman.game.prototype.animation.Bool;
import ca.scotthyndman.game.prototype.animation.Property;
import ca.scotthyndman.game.prototype.animation.PropertyListener;
import ca.scotthyndman.game.prototype.animation.Real;
import ca.scotthyndman.game.prototype.entity.Env;

import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.MaterialState;
import com.jme.system.DisplaySystem;

/**
 * A positioned wraps around a {@link Node} in order to provide a simpler, more game-appropriate API. Each of the
 * {@link Positioned}'s properties are animatable directly. It also supports a rotational center other than the origin.
 * 
 * @author scottyhyndman
 */
public class Positioned implements PropertyListener, Updatable {

	//
	// ======== ANIMATABLE PROPERTIES
	//

	/**
	 * The x location of this Sprite.
	 */
	public final Real x = new Real(this);

	/**
	 * The y location of this Sprite.
	 */
	public final Real y = new Real(this);

	/**
	 * The x-scale of this Sprite.
	 */
	public final Real scaleX = new Real(this, 1f);

	/**
	 * The y-scale of this Sprite.
	 */
	public final Real scaleY = new Real(this, 1f);

	/**
	 * The x center of this Sprite.
	 */
	public final Real centerX = new Real(this);

	/**
	 * The y center of this Sprite.
	 */
	public final Real centerY = new Real(this);

	/**
	 * The angle of this Sprite, typically in range from 0 to 2*PI, although the angle can have any value. The Sprite is
	 * rotated around its anchor.
	 */
	public final Real angle = new Real(this);

	/**
	 * The alpha of this Sprite, in range from 0 to 1. A value of 0 is fully transparent and a value of 1 is fully
	 * opaque. The default is 255.
	 */
	public final Real alpha = new Real(this, 1f);

	/**
	 * The flag indicating whether or not this Sprite is visible.
	 */
	public final Bool visible = new Bool(this, true);

	//
	// ======== MEMBERS
	//

	protected final Renderer renderer = DisplaySystem.getDisplaySystem().getRenderer();
	protected String name;
	protected Group parent;
	protected Node pivotNode;
	protected Node node;
	protected boolean dirtyCenter = false;
	protected boolean dirtyGeometry = false;
	protected boolean dirtyAlpha = false;
	protected Quaternion quaternion;
	protected MaterialState materialState;

	//
	// ======== CONSTRUCTION
	//

	public Positioned() {
		this(null);
	}

	public Positioned(String name) {
		this(name, 0f, 0f);
	}

	public Positioned(String name, float x, float y) {
		this.name = name;
		this.x.set(x);
		this.y.set(y);
		this.quaternion = new Quaternion();

		buildNode(this.x, this.y);
	}

	/**
	 * Builds the node for this object.
	 */
	private void buildNode(Real x, Real y) {
		node = new Node(this.name);
		node.setLocalTranslation(x.get(), y.get(), 0f);
		node.updateGeometricState(0f, true);

		enableAlpha();
	}

	//
	// ======== NAVIGATING THE SCENEGRAPH
	//

	/**
	 * Gets this Sprite's parent node, or <code>null</code> if this node does not have a parent.
	 */
	public final Group getParent() {
		return parent;
	}

	/* package-private */ final void setParent(Group parent) {
		if (this.parent != parent) {
			this.parent = parent;
		}
	}

	/**
	 * Removes this node from its parent Group. If this node does not have a parent, this method does nothing.
	 */
	public void removeFromParent() {
		Group p = parent;
		if (p != null) {
			p.remove(this);
		}
	}

	/**
	 * Gets this node's oldest ancestor Group, or null if this Sprite does not have a parent.
	 */
	public final Group getRoot() {
		Group currRoot = null;
		Group nextRoot = parent;
		while (true) {
			if (nextRoot == null) {
				return currRoot;
			} else {
				currRoot = nextRoot;
				nextRoot = nextRoot.getParent();
			}
		}
	}

	//
	// ======== GETTING THE UNDERLYING NODE(S)
	//

	/**
	 * Gets the node that should be used if you want to reposition the subtree elsewhere.
	 */
	public Node getTopNode() {
		if (dirtyCenter && pivotNode == null) {
			buildPivotNode(0);
			dirtyCenter = false;
		}

		return pivotNode == null ? node : pivotNode;
	}

	/**
	 * Returns the node that should be used if you want to add things as children of this tree.
	 */
	public Node getContentNode() {
		return node;
	}

	//
	// ======== LISTENING TO CHANGES
	//

	/**
	 * Called when a property's value changes.
	 */
	public void onPropertyChange(Property property) {
		if (!dirtyGeometry
				&& (property == x || property == y || property == scaleX || property == scaleY || property == angle)) {
			dirtyGeometry = true;
		}

		if (!dirtyCenter && (property == centerX || property == centerY)) {
			dirtyCenter = true;
		}

		if (!dirtyAlpha && property == alpha) {
			dirtyAlpha = true;
		}
	}

	//
	// ======== UPDATING THE ENTITY
	//

	/**
	 * Updates the entity.
	 */
	public void update(Env env, float tpf) {
		int tpfms = (int) (tpf * 1000);

		x.update(tpfms);
		y.update(tpfms);
		scaleX.update(tpfms);
		scaleY.update(tpfms);
		angle.update(tpfms);
		centerX.update(tpfms);
		centerY.update(tpfms);
		alpha.update(tpfms);

		//
		// First, build a pivot node if required.
		//
		if (dirtyCenter) {
			if (pivotNode == null) {
				buildPivotNode(tpf);
			} else {
				updatePivot(tpf);
			}
		}

		//
		// Update differently depending on whether we have a pivot or not.
		//
		if (dirtyGeometry) {
			quaternion.fromAngleAxis(FastMath.DEG_TO_RAD * angle.get(), Vector3f.UNIT_Z);

			if (pivotNode != null) {
				updateGeometryWithPivot(tpf);
			} else {
				updateGeometryWithoutPivot(tpf);
			}
		}

		//
		// Update alpha
		//
		if (dirtyAlpha) {
			if (materialState == null) {
				enableAlpha();
			}

			materialState.getDiffuse().a = alpha.get();
			node.updateRenderState();
		}

		//
		// Force an update on the node
		//
		node.updateGeometricState(tpf, true); // bubbles up

		//
		// Set dirty states back to normal
		//
		dirtyCenter = false;
		dirtyGeometry = false;
		dirtyAlpha = false;
	}

	//
	// ======== UPDATING GEOMETRY
	//

	/**
	 * Pivot is responsible for rotation. The node is responsible for translation.
	 */
	private void updateGeometryWithPivot(float tpf) {
		// System.out.println("update w/ pivot");
		// System.out.println("top: " + getTopNode().getWorldTranslation());
		// System.out.println("content: " + getContentNode().getLocalTranslation());
		// System.out.println("graphic: " + getContentNode().getChild(0).getWorldTranslation());

		pivotNode.setLocalTranslation(x.get() + centerX.get(), y.get() + centerY.get(), 0);
		pivotNode.setLocalRotation(quaternion);
		node.setLocalScale(new Vector3f(scaleX.get(), scaleY.get(), 0f));
	}

	/**
	 * The node is responsible for both translation and rotation.
	 */
	private void updateGeometryWithoutPivot(float tpf) {
		node.setLocalTranslation(x.get(), y.get(), 0);
		node.setLocalRotation(quaternion);
		node.setLocalScale(new Vector3f(scaleX.get(), scaleY.get(), 0f));
	}

	//
	// ======== PIVOT NODES
	//

	/**
	 * Updates the pivot point.
	 */
	private void updatePivot(float tpf) {
		// 1. Determine the center point's location relative to the node's parent
		Vector3f rel = getCenterRelativeToParent(node.getParent());
		System.out.println("centerRelativeToParent: " + rel);

		// 2. Set the pivot's location and apply rotation
		pivotNode.setLocalTranslation(rel);
		pivotNode.setLocalRotation(quaternion);

		// 3. Translate the node by negative center point.
		node.setLocalTranslation(-centerX.get(), -centerY.get(), 0);

		// 4. Update the bounds
		node.updateGeometricState(tpf, true);
	}

	/**
	 * Builds a pivot node and attaches the node correctly.
	 */
	private void buildPivotNode(float tpf) {
		// Do we have a parent?
		Node parent = node.getParent();
		boolean hasParent = parent != null;

		// 1. Create a new pivot node
		pivotNode = new Node("pivot");

		// 2. Determine the center point's location relative to the node's parent
		Vector3f rel = getCenterRelativeToParent(parent);

		// 3. Set the pivot's location and apply rotation
		pivotNode.setLocalTranslation(rel);
		pivotNode.setLocalRotation(quaternion);

		// 4. If the node has a parent, overwrite its position with the pivot
		if (hasParent) {
			int idx = parent.getChildIndex(node);
			node.removeFromParent();
			parent.attachChildAt(pivotNode, idx);
		}

		// 5. Remove the node's rotation and translate it by negative center point.
		node.setLocalRotation(new Quaternion());
		node.setLocalTranslation(-centerX.get(), -centerY.get(), 0);

		// 6. Add the node to the pivot node.
		pivotNode.attachChild(node);

		// 7. Update the bounds
		node.updateGeometricState(tpf, true);
	}

	private Vector3f getCenterRelativeToParent(Node parent) {
		// 2. Determine the pivot's world location (center point transformed)
		Vector3f in = new Vector3f(centerX.get(), centerY.get(), 0);
		Vector3f out = new Vector3f();
		node.localToWorld(in, out);

		// 3. If we have a parent, figure out where the pivot is relative to it.
		Vector3f rel = new Vector3f(out);
		if (parent != null) {
			rel = new Vector3f();
			parent.worldToLocal(out, rel);
		}
		return rel;
	}

	//
	// ======== ALPHA
	//

	/**
	 * Enabled alpha support on the node.
	 */
	private void enableAlpha() {
		// the sphere material taht will be modified to make the sphere look opaque then transparent then opaque and so
		// on
		float opacityAmount = alpha.get();
		materialState = renderer.createMaterialState();
		materialState.setAmbient(new ColorRGBA(0.0f, 0.0f, 0.0f, opacityAmount));
		materialState.setDiffuse(new ColorRGBA(0.1f, 0.5f, 0.8f, opacityAmount));
		materialState.setSpecular(new ColorRGBA(1.0f, 1.0f, 1.0f, opacityAmount));
		materialState.setShininess(128.0f);
		materialState.setEmissive(new ColorRGBA(0.0f, 0.0f, 0.0f, opacityAmount));
		materialState.setEnabled(true);

		// IMPORTANT: this is used to handle the internal sphere faces when setting them to transparent, try commenting
		// this line to see what happens
		materialState.setMaterialFace(MaterialState.MaterialFace.FrontAndBack);

		node.setRenderState(materialState);
		node.updateRenderState();

		// To handle transparency: a BlendState
		final BlendState alphaState = renderer.createBlendState();
		alphaState.setBlendEnabled(true);
		alphaState.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
		alphaState.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
		alphaState.setTestEnabled(true);
		alphaState.setTestFunction(BlendState.TestFunction.GreaterThan);
		alphaState.setEnabled(true);

		node.setRenderState(alphaState);
	}
}

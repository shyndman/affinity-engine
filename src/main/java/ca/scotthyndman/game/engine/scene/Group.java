package ca.scotthyndman.game.engine.scene;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import ca.scotthyndman.game.engine.animation.Real;
import ca.scotthyndman.game.engine.entity.Env;

import com.jme.scene.Node;
import com.jme.scene.Spatial;

/**
 * A group represents a grouping of one or more {@link Positioned} nodes.
 * 
 * @author scottyhyndman
 */
public class Group extends Positioned implements Iterable<Positioned> {

	/**
	 * This group's children.
	 */
	protected List<Positioned> children;

	/**
	 * Creates a new group.
	 */
	public Group() {
		this(null);
	}

	/**
	 * Creates a new named group.
	 * 
	 * @param name
	 *            the name of the group
	 */
	public Group(String name) {
		this(name, 0, 0);
	}

	/**
	 * Creates a new group with an initial position.
	 * 
	 * @param name
	 *            the name of the group
	 * @param x
	 *            the initial x position
	 * @param y
	 *            the initial y position
	 */
	public Group(String name, float x, float y) {
		super(name, x, y);
	}

	/**
	 * Constructs the spatial.
	 */
	@Override
	protected Spatial constructSpatial(Real x, Real y) {
		Node spatial = new Node(this.name);
		spatial.setLocalTranslation(x.get(), y.get(), 0f);
		spatial.updateGeometricState(0f, true);

		enableAlpha(spatial);

		return spatial;
	}

	//
	// ======== POSITIONED LIST
	//

	/**
	 * Returns an unmodifiable iterator over this group's children.
	 */
	public Iterator<Positioned> iterator() {
		List<Positioned> children = this.children;
		if (children == null) {
			children = Collections.emptyList();
		}
		return children.iterator();
	}

	/**
	 * @return the number of children belonging to this group
	 */
	public int size() {
		return children == null ? 0 : children.size();
	}

	/**
	 * Returns the spatial at the specified position in this group. Returns {@code null} if the index is out of range (
	 * {@code index < 0 || index >= size()}).
	 */
	public Positioned get(int index) {
		return children == null ? null : (index < 0 || index >= children.size() ? null : children.get(index));
	}

	/**
	 * Returns {@code true} if this Group contains the specified spatial.
	 */
	public boolean contains(Positioned positioned) {
		return children.contains(positioned);
	}

	/**
	 * Returns {@code true} if this Group is an ancestor of the specified spatial.
	 */
	public boolean isAncestorOf(Positioned sprite) {
		Group parent = (sprite == null) ? null : sprite.getParent();
		while (parent != null) {
			if (parent == this) {
				return true;
			} else {
				parent = parent.getParent();
			}
		}
		return false;
	}

	/**
	 * Adds a Sprite to this Group. The Sprite is added so it appears above all other sprites in this Group. If this
	 * Sprite already belongs to a Group, it is first removed from that Group before added to this one.
	 */
	public void add(Positioned sprite) {
		System.out.println(this + ", " + sprite);
		if (children == null) {
			children = new ArrayList<Positioned>(4);
		}

		Group parent = sprite.getParent();
		if (parent != null) {
			parent.remove(sprite, false);
		}

		children.add(sprite);
		sprite.setParent(this);
		sprite.rooted = rooted;
		((Node) getContent()).attachChild(sprite.getTopNode());

		UpdateManager mgr = Env.getInstance().getUpdateManager();
		if (parent == null) {
			mgr.updatableWasAdded(sprite);
		}
	}

	/**
	 * Inserts a Sprite to this Group at the specified position. The Sprite at the current position (if any) and any
	 * subsequent Sprites are moved up in the z-order (adds one to their indices).
	 * <p>
	 * If the index is less than zero, the sprite is inserted at position zero (the bottom in the z-order). If the index
	 * is greater than or equal to {@link #size()}, the sprite is inserted at position {@link #size()} (the top in the
	 * z-order).
	 */
	public void add(int index, Positioned sprite) {
		if (children == null) {
			children = new ArrayList<Positioned>(4);
		}

		Group parent = sprite.getParent();
		if (parent != null) {
			parent.remove(sprite, false);
		}

		children.add(index, sprite);
		sprite.rooted = rooted;
		((Node) getContent()).attachChildAt(sprite.getTopNode(), index);

		UpdateManager mgr = Env.getInstance().getUpdateManager();
		if (parent == null) {
			mgr.updatableWasAdded(sprite);
		}
	}

	/**
	 * Removes a spatial from this Group.
	 */
	public void remove(Positioned sprite) {
		remove(sprite, true);
	}

	/**
	 * Removes a spatial from this Group.
	 */
	public void remove(Positioned sprite, boolean updateChange) {
		if (children == null) {
			return;
		}

		int index = children.indexOf(sprite);
		if (index != -1) {
			children.remove(index);
			sprite.setParent(null);
			sprite.rooted = false;
			((Node) getContent()).detachChildAt(index);
			
			if (updateChange) {
				UpdateManager mgr = Env.getInstance().getUpdateManager();
				mgr.updatableWasRemoved(sprite);
			}
		}
	}

	/**
	 * Removes all nodes from this Group.
	 */
	public void removeAll() {
		if (children == null) {
			return;
		}

		for (Positioned p : children) {
			p.setParent(null);

			p.rooted = false;
			UpdateManager mgr = Env.getInstance().getUpdateManager();
			mgr.updatableWasRemoved(p);
		}

		children.clear();
		((Node) getContent()).detachAllChildren();
	}

	/**
	 * Moves a sprites position in the child list.
	 * 
	 * @param sprite
	 * @param position
	 * @param relative
	 */
	private void move(Positioned sprite, int position, boolean relative) {
		int oldPosition = children.indexOf(sprite);
		if (oldPosition != -1) {
			if (relative) {
				position += oldPosition;
			}
			if (position < 0) {
				position = 0;
			} else if (position >= children.size()) {
				position = children.size() - 1;
			}
			if (oldPosition != position) {
				Positioned c2 = children.get(oldPosition);
				Positioned c1 = children.remove(position);
				children.add(position, c2);
				children.remove(oldPosition);
				children.add(oldPosition, c1);

				((Node) getContent()).swapChildren(position, oldPosition);
			}
		}
	}

	/**
	 * Moves the specified Sprite to the top of the z-order, so that all the other Sprites currently in this Group
	 * appear underneath it. If the specified Sprite is not in this Group, or the Sprite is already at the top, this
	 * method does nothing.
	 */
	public void moveToTop(Positioned sprite) {
		move(sprite, Integer.MAX_VALUE, false);
	}

	/**
	 * Moves the specified Sprite to the bottom of the z-order, so that all the other Sprites currently in this Group
	 * appear above it. If the specified Sprite is not in this Group, or the Sprite is already at the bottom, this
	 * method does nothing.
	 */
	public void moveToBottom(Positioned sprite) {
		move(sprite, 0, false);
	}

	/**
	 * Moves the specified Sprite up in z-order, swapping places with the first Sprite that appears above it. If the
	 * specified Sprite is not in this Group, or the Sprite is already at the top, this method does nothing.
	 */
	public void moveUp(Positioned sprite) {
		move(sprite, +1, true);
	}

	/**
	 * Moves the specified Sprite down in z-order, swapping places with the first Sprite that appears below it. If the
	 * specified Sprite is not in this Group, or the Sprite is already at the bottom, this method does nothing.
	 */
	public void moveDown(Positioned sprite) {
		move(sprite, -1, true);
	}
}

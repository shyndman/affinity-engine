package ca.scotthyndman.game.engine.console;

import java.util.Enumeration;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import ca.scotthyndman.game.engine.scene.Group;
import ca.scotthyndman.game.engine.scene.Positioned;

/**
 * Wraps around a sgNode so that it can be displayed in a tree.
 * 
 * @author scottyhyndman
 */
public class SGTreeNode implements MutableTreeNode {

	private SGTreeNode parent;
	private Vector<SGTreeNode> children;
	private Positioned sgNode;

	public SGTreeNode() {
		this(null);
	}

	public SGTreeNode(Positioned spatial) {
		this(null, spatial);
	}

	public SGTreeNode(SGTreeNode parent, Positioned spatial) {
		this.parent = parent;
		this.sgNode = spatial;
		if (spatial instanceof Group) {
			children = new Vector<SGTreeNode>();
			for (Positioned child : ((Group) spatial)) {
				children.add(new SGTreeNode(this, child));
			}
		}
	}
	
	public Positioned getSGNode() {
		return sgNode;
	}

	public Enumeration<? extends TreeNode> children() {
		return children.elements();
	}

	Vector<SGTreeNode> getChildren() {
		return children;
	}

	void setChildren(Vector<SGTreeNode> children) {
		this.children = children;
	}

	public boolean getAllowsChildren() {
		return sgNode instanceof Group;
	}

	public TreeNode getChildAt(int childIndex) {
		return children.get(childIndex);
	}

	public int getChildCount() {
		return (sgNode instanceof Group) ? ((Group) sgNode).size() : 0;
	}

	public int getIndex(TreeNode node) {
		return children.indexOf(node);
	}

	public TreeNode getParent() {
		return parent;
	}

	public boolean isLeaf() {
		return !(sgNode instanceof Group);
	}

	@Override
	public String toString() {
		if (sgNode == null) {
			return "Nodeless";
		} else {
			return sgNode.getClass().getSimpleName() + "::" + sgNode.name;
		}
	}

	//
	// ======== MUTABLE
	//

	public void insert(MutableTreeNode child, int index) {
		if (!getAllowsChildren()) {
			throw new IllegalStateException("node does not allow children");
		} else if (child == null) {
			throw new IllegalArgumentException("new child is null");
		} else if (isNodeAncestor(child)) {
			throw new IllegalArgumentException("new child is an ancestor");
		}

		MutableTreeNode oldParent = (MutableTreeNode) child.getParent();

		if (oldParent != null) {
			oldParent.remove(child);
		}
		child.setParent(this);
		if (children == null) {
			children = new Vector<SGTreeNode>();
		}
		children.insertElementAt((SGTreeNode) child, index);
	}

	public void remove(int index) {
		MutableTreeNode child = (MutableTreeNode) getChildAt(index);
		children.removeElementAt(index);
		child.setParent(null);
	}

	public void remove(MutableTreeNode node) {
		if (node == null) {
			throw new IllegalArgumentException("argument is null");
		}

		if (!isNodeChild(node)) {
			throw new IllegalArgumentException("argument is not a child");
		}
		remove(getIndex(node)); // linear search
	}

	public void removeFromParent() {
		MutableTreeNode parent = (MutableTreeNode) getParent();
		if (parent != null) {
			parent.remove(this);
		}
	}

	public void setParent(MutableTreeNode newParent) {
		parent = (SGTreeNode) newParent;
	}

	public void setUserObject(Object object) {
		// ignored
	}

	//
	// ======== QUERIES
	//

	/**
	 * Returns true if <code>anotherNode</code> is an ancestor of this node -- if it is this node, this node's parent,
	 * or an ancestor of this node's parent. (Note that a node is considered an ancestor of itself.) If
	 * <code>anotherNode</code> is null, this method returns false. This operation is at worst O(h) where h is the
	 * distance from the root to this node.
	 * 
	 * @see #isNodeDescendant
	 * @see #getSharedAncestor
	 * @param anotherNode
	 *            node to test as an ancestor of this node
	 * @return true if this node is a descendant of <code>anotherNode</code>
	 */
	public boolean isNodeAncestor(TreeNode anotherNode) {
		if (anotherNode == null) {
			return false;
		}

		TreeNode ancestor = this;

		do {
			if (ancestor == anotherNode) {
				return true;
			}
		} while ((ancestor = ancestor.getParent()) != null);

		return false;
	}

	/**
	 * Returns true if <code>anotherNode</code> is a descendant of this node -- if it is this node, one of this node's
	 * children, or a descendant of one of this node's children. Note that a node is considered a descendant of itself.
	 * If <code>anotherNode</code> is null, returns false. This operation is at worst O(h) where h is the distance from
	 * the root to <code>anotherNode</code>.
	 * 
	 * @see #isNodeAncestor
	 * @see #getSharedAncestor
	 * @param anotherNode
	 *            node to test as descendant of this node
	 * @return true if this node is an ancestor of <code>anotherNode</code>
	 */
	public boolean isNodeDescendant(DefaultMutableTreeNode anotherNode) {
		if (anotherNode == null)
			return false;

		return anotherNode.isNodeAncestor(this);
	}

	/**
	 * Returns true if <code>aNode</code> is a child of this node. If <code>aNode</code> is null, this method returns
	 * false.
	 * 
	 * @return true if <code>aNode</code> is a child of this node; false if <code>aNode</code> is null
	 */
	public boolean isNodeChild(TreeNode aNode) {
		boolean retval;

		if (aNode == null) {
			retval = false;
		} else {
			if (getChildCount() == 0) {
				retval = false;
			} else {
				retval = (aNode.getParent() == this);
			}
		}

		return retval;
	}

	//
	// ======== ROOT
	//

	/**
	 * Returns a new root node.
	 * 
	 * @return
	 */
	public static SGTreeNode newRootNode() {
		return new SGTreeNode() {
			{
				setChildren(new Vector<SGTreeNode>());
			}

			@Override
			public boolean getAllowsChildren() {
				return true;
			}

			@Override
			public int getChildCount() {
				return getChildren().size();
			}

			@Override
			public boolean isLeaf() {
				return false;
			}

			@Override
			public String toString() {
				return "ROOT";
			}
		};
	}
}

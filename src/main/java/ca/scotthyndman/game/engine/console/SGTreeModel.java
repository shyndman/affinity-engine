package ca.scotthyndman.game.engine.console;

import java.util.HashMap;
import java.util.Map;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import ca.scotthyndman.game.engine.Engine;
import ca.scotthyndman.game.engine.event.Event;
import ca.scotthyndman.game.engine.event.EventManager;
import ca.scotthyndman.game.engine.input.ActionHandler;
import ca.scotthyndman.game.engine.scene.Positioned;
import ca.scotthyndman.game.engine.scene.SGEvent;

public class SGTreeModel extends DefaultTreeModel implements TreeModel {

	private EventManager eventManager;
	private Map<Positioned, SGTreeNode> mappingTable = new HashMap<Positioned, SGTreeNode>();

	public SGTreeModel() {
		this(false);

		this.eventManager = Engine.getInstance().getEventManager();

		addActionHandlers();
	}

	public SGTreeModel(boolean asksAllowsChildren) {
		super(SGTreeNode.newRootNode(), asksAllowsChildren);

		mappingTable.put(null, (SGTreeNode) getRoot());
	}

	@SuppressWarnings("unchecked")
	public void addActionHandlers() {
		eventManager.addActionHandler("nodeAdded", new ActionHandler() {
			@Override
			public void performAction(Event event) {
				SGEvent e = (SGEvent) event;

				if (!mappingTable.containsKey(e.node)) {
					mappingTable.put(e.node, new SGTreeNode(e.node));
				}
				SGTreeNode n = mappingTable.get(e.node);
				insertNodeInto(n, mappingTable.get(e.node.getParent()), 0);

				System.out.println("Node added: " + e.node);
			}
		}, true);
		eventManager.addActionHandler("nodeRemoved", new ActionHandler() {
			@Override
			public void performAction(Event event) {
				SGEvent e = (SGEvent) event;
				SGTreeNode n = mappingTable.get(e.node);
				removeNodeFromParent(n);

				System.out.println("Node removed: " + e.node);
			}
		}, true);
	}
}

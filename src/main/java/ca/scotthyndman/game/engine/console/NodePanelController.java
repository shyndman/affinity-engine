package ca.scotthyndman.game.engine.console;

import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

public class NodePanelController implements TreeSelectionListener {

	JTree tree;
	JTable properties;
	SGTableModel propertiesModel;

	public NodePanelController(JTree tree, JTable properties, SGTableModel propertiesModel) {
		this.tree = tree;
		this.properties = properties;
		this.propertiesModel = propertiesModel;
	}

	public void valueChanged(TreeSelectionEvent e) {
		TreePath currentPath = e.getNewLeadSelectionPath();
		SGTreeNode sel = (SGTreeNode) (currentPath == null ? null : currentPath.getLastPathComponent());
		clearPropertiesTable();

		if (sel != null) {
			setProperties(sel);
		}
	}

	private void clearPropertiesTable() {
		propertiesModel.clear();
	}

	private void setProperties(SGTreeNode node) {
		propertiesModel.setNode(node.getSGNode());
	}
}

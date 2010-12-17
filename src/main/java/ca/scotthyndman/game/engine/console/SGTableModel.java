package ca.scotthyndman.game.engine.console;

import java.util.Collections;
import java.util.List;

import javax.swing.table.DefaultTableModel;

import ca.scotthyndman.game.engine.console.bean.BeanInfo;
import ca.scotthyndman.game.engine.console.bean.FieldPropertyEditor;
import ca.scotthyndman.game.engine.console.bean.Introspector;
import ca.scotthyndman.game.engine.scene.Positioned;

public class SGTableModel extends DefaultTableModel {

	public SGTableModel() {
		super(new Object[] { "Name", "Value" }, 0);
	}

	public void clear() {
		setRowCount(0);
	}

	public void setNode(Positioned node) {
		if (node == null) {
			return;
		}

		BeanInfo info = Introspector.introspect(node.getClass());
		setRowCount(info.getPropertyCount());
		
		int cnt = 0;
		
		List<String> names = info.getPropertyNames();
		Collections.sort(names);
		for (String name : names) {
			FieldPropertyEditor f = (FieldPropertyEditor) info.getPropertyEditor(name);
			f.setTarget(node);
			setValueAt(name, cnt, 0);
			setValueAt(f.getAsText(), cnt++, 1);
			
		}
	}
	
	@Override
	public boolean isCellEditable(int row, int column) {
		return column != 0;
	}
}

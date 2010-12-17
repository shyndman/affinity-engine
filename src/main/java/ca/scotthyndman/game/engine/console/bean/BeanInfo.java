package ca.scotthyndman.game.engine.console.bean;

import java.beans.PropertyEditor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeanInfo {

	private Class beanClass;
	private Map<String, PropertyEditor> properties = new HashMap<String, PropertyEditor>();

	public BeanInfo(Class beanClass) {
		this.beanClass = beanClass;
	}

	public Class getBeanClass() {
		return beanClass;
	}

	public int getPropertyCount() {
		return properties.size();
	}

	public void addProperty(String name, PropertyEditor editor) {
		properties.put(name, editor);
	}

	public boolean hasProperty(String name) {
		return properties.containsKey(name);
	}

	public List<String> getPropertyNames() {
		return new ArrayList<String>(properties.keySet());
	}

	public PropertyEditor getPropertyEditor(String name) {
		return properties.get(name);
	}
}

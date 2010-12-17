package ca.scotthyndman.game.engine.console.bean;

import java.beans.PropertyEditor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Introspector {

	private static Map<Class, BeanInfo> infoCache = new HashMap<Class, BeanInfo>();

	public static BeanInfo introspect(Class beanClass) {
		if (infoCache.containsKey(beanClass)) {
			return infoCache.get(beanClass);
		}

		BeanInfo info = new BeanInfo(beanClass);
		Class current = beanClass;
		do {
			System.out.println(current.getName());
			Field[] fields = current.getDeclaredFields();
			for (Field f : fields) {
				if (!f.isAnnotationPresent(Editable.class)) {
					continue;
				}

				if (info.hasProperty(f.getName())) {
					continue;
				}

				info.addProperty(f.getName(), getPropertyEditor(f));
			}
		} while (null != (current = current.getSuperclass()));

		infoCache.put(beanClass, info);
		return info;
	}

	private static PropertyEditor getPropertyEditor(Field f) {
		return new FieldPropertyEditor(f);
	}
}

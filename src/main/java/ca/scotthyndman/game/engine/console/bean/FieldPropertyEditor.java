package ca.scotthyndman.game.engine.console.bean;

import java.beans.PropertyEditorSupport;
import java.lang.reflect.Field;

public class FieldPropertyEditor extends PropertyEditorSupport {

	private Object target;
	private Field field;

	public FieldPropertyEditor(Field source) {
		super(source);
		this.field = source;
	}

	public void setTarget(Object target) {
		this.target = target;
	}

	public Object getTarget() {
		return target;
	}

	@Override
	public String getAsText() {
		try {
			return "" + field.get(target);
		} catch (Exception e) {
			e.printStackTrace();
			return "Error";
		}
	}
}

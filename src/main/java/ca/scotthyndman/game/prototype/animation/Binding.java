package ca.scotthyndman.game.prototype.animation;

/**
 * Helper class to facilitate property.bindTo() methods.
 */
/* package-private */final class Binding<T> implements PropertyBehavior, PropertyListener {

	private static final int FUNCTION_NONE = 0;
	private static final int FUNCTION_CUSTOM = 3;

	private final Property<T> target;
	private final Property<T> source;
	private final boolean bidirectional;
	private final BindFunction<T> customFunction;
	private final int function;

	/* package-private */Binding(Property<T> target, BindFunction function) {
		this.target = target;
		this.source = null;
		this.bidirectional = false;
		this.customFunction = function;
		this.function = FUNCTION_CUSTOM;
	}

	/* package-private */Binding(Property<T> target, Property<T> source, boolean bidirectional) {
		this.target = target;
		this.source = source;
		this.bidirectional = bidirectional;
		this.customFunction = null;
		this.function = FUNCTION_NONE;

		if (source != target) {
			source.addListener(this);
		}
	}

	public boolean isBidirectional() {
		return bidirectional;
	}

	public Property getSource() {
		return source;
	}

	public Property getTarget() {
		return target;
	}

	public void onPropertyChange(Property property) {
		if (target.getBehavior() != this) {
			source.removeListener(this);
		} else {
			target.setValue(getValue());
		}
	}

	public boolean updateAnimation(int elapsedTime) {
		return true;
	}

	public void fastForward() {
		// Do nothing
	}

	public boolean isFinished() {
		return false;
	}

	public T getValue() {
		switch (function) {
		default:
		case FUNCTION_NONE:
			return source.getValue();
		case FUNCTION_CUSTOM:
			target.setValue(customFunction.f());
			return target.getValue();
		}
	}
}

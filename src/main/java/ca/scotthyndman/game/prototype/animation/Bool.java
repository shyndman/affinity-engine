package ca.scotthyndman.game.prototype.animation;

/**
 * An Bool is an boolean value that can be animated over time.
 */
public final class Bool extends Property<Boolean> {

	public Bool() {
		this(null, false);
	}

	public Bool(PropertyListener listener) {
		this(listener, false);
	}

	public Bool(boolean value) {
		this(null, value);
	}

	public Bool(PropertyListener listener, boolean value) {
		super(listener, value);
	}

	public boolean get() {
		return getValue();
	}

	/**
	 * Sets the value of this Bool. Any previous animations are stopped.
	 */
	public void set(boolean value) {
		setValue(value);
		setBehavior(null);
	}

	/**
	 * Toggles the value of this Bool (same as the {@code ! } logical complement operator, inverting the value). Any
	 * previous animations are stopped.
	 */
	public void toggle() {
		set(!get());
	}

	/**
	 * Sets the value of this Bool after a specific delay. Any previous animations are stopped.
	 */
	public void set(boolean value, int delay) {
		setBehavior(new Tween<Boolean>(get(), value, delay, null, delay));
	}

	/**
	 * Binds this property to the specified property. If this property is given a new behavior, the binding is broken.
	 */
	public void bindTo(Bool property) {
		setBehavior(new Binding(this, property, false));
	}

	/**
	 * Bi-directionally binds this property to the specified property. If this property is given a new behavior, the
	 * specified property is then bi-directionally bound to this property. The binding is permanent, until a new
	 * bi-directional binding is specified.
	 */
	public void bindWithInverse(Bool property) {
		setBehavior(new Binding(this, property, true));
	}

	/**
	 * Binds this property to the specified function.
	 */
	public void bindTo(BindFunction function) {
		setBehavior(new Binding(this, function));
	}

	public String toString() {
		return "" + get();
	}

	/**
	 * Returns true if the specified object is a {@code Bool} or {@link java.lang.Boolean} and its value is equal to
	 * this value.
	 */
	public boolean equals(Object obj) {
		if (obj instanceof Bool) {
			return get() == ((Bool) obj).get();
		} else if (obj instanceof Boolean) {
			return get() == ((Boolean) obj).booleanValue();
		} else {
			return false;
		}
	}

	public int hashCode() {
		// Same as java.lang.Boolean
		return get() ? 1231 : 1237;
	}
}
package ca.scotthyndman.game.prototype.animation;

import java.text.DecimalFormat;
import java.text.ParseException;

import javax.swing.text.NumberFormatter;

/**
 * A Real is an fixed-point value (16 bits integer, 16 bits fraction) that can be animated over time. See
 * {@link pulpcore.math.CoreMath} for methods to convert between integers and fixed-point numbers.
 */
public final class Real extends Property<Float> {

	private static final NumberFormatter FORMATTER;

	public Real() {
		this(null, 0);
	}

	public Real(PropertyListener listener) {
		this(listener, 0);
	}

	//
	// Constructors with setters - 2 methods
	//

	public Real(int value) {
		this(null, value);
	}

	public Real(float value) {
		this(null, value);
	}

	//
	// Constructors with setters and listeners - 2 methods
	//

	public Real(PropertyListener listener, int value) {
		this(listener, (float) value);
	}

	public Real(PropertyListener listener, float value) {
		super(listener, value);
	}

	//
	// Getters
	//

	public int getAsInt() {
		return super.getValue().intValue();
	}

	public int getAsIntFloor() {
		return (int) Math.floor(super.getValue());
	}

	public int getAsIntCeil() {
		return (int) Math.ceil(super.getValue());
	}

	public int getAsIntRound() {
		return (int) Math.round(super.getValue());
	}

	public float get() {
		return super.getValue();
	}

	public String toString() {
		Float f = super.getValue();
		try {
			return FORMATTER.valueToString(f);
		} catch (ParseException e) {
			return f.toString();
		}
	}

	/**
	 * Returns true if the specified object is an {@code Int}, {@link Real}, {@link java.lang.Byte} ,
	 * {@link java.lang.Short}, {@link java.lang.Integer}, {@link java.lang.Long}, {@link java.lang.Float}, or
	 * {@link java.lang.Double}, and its value is equal to this value.
	 */
	public boolean equals(Object obj) {
		if (obj instanceof Real) {
			return get() == ((Real) obj).get();
		} else if (obj instanceof Int) {
			long objValue = ((Int) obj).get();
			return getAsInt() == objValue;
		} else if (obj instanceof Double) {
			return get() == ((Double) obj).doubleValue();
		} else if (obj instanceof Float) {
			return get() == ((Float) obj).floatValue();
		} else if (obj instanceof Byte || obj instanceof Short || obj instanceof Integer || obj instanceof Long) {
			long objValue = ((Number) obj).longValue();
			return get() == objValue;
		} else {
			return false;
		}
	}

	public int hashCode() {
		// Same as java.lang.Float
		return Float.floatToIntBits(get());
	}

	//
	// Setters - 3 methods
	//

	protected void setValue(Number value) {
		setValue(value.floatValue());
	}

	/**
	 * Sets the value of this property. Any previous animations are stopped.
	 */
	public void set(int value) {
		setValue(value);
		setBehavior(null);
	}

	/**
	 * Sets the value of this property. Any previous animations are stopped.
	 */
	public void set(float value) {
		setValue(value);
		setBehavior(null);
	}

	//
	// Setters (with a delay) - 3 methods
	//

	/**
	 * Sets the value of this property after a specific delay. Any previous animations are stopped.
	 */
	public void set(int value, int delay) {
		animateTo(value, 0, null, delay);
	}

	/**
	 * Sets the value of this property after a specific delay. Any previous animations are stopped.
	 */
	public void set(float value, int delay) {
		animateTo(value, 0, null, delay);
	}

	/**
	 * Binds this property to the specified property. If this property is given a new behavior, the binding is broken.
	 */
	public void bindTo(Int property) {
		setBehavior(new Binding(this, property, false));
	}

	/**
	 * Bi-directionally binds this property to the specified property. If this property is given a new behavior, the
	 * specified property is then bi-directionally bound to this property. The binding is permanent, until a new
	 * bi-directional binding is specified.
	 */
	public void bindWithInverse(Int property) {
		setBehavior(new Binding(this, property, true));
	}

	/**
	 * Binds this property to the specified property. If this property is given a new behavior, the binding is broken.
	 */
	public void bindTo(Real property) {
		setBehavior(new Binding(this, property, false));
	}

	/**
	 * Bi-directionally binds this property to the specified property. If this property is given a new behavior, the
	 * specified property is then bi-directionally bound to this property.
	 */
	public void bindWithInverse(Real property) {
		setBehavior(new Binding(this, property, true));
	}

	/**
	 * Binds this property to the specified function.
	 */
	public void bindTo(BindFunction function) {
		setBehavior(new Binding(this, function));
	}

	//
	// Animation convenience methods - float
	//

	/**
	 * Animates this property from the one double (fromValue) to another (toValue). Any previous animations are stopped.
	 */
	public void animate(float fromValue, float toValue, int duration) {
		setBehavior(new Tween<Float>(fromValue, toValue, duration));
	}

	/**
	 * Animates this property from the one double (fromValue) to another (toValue). Any previous animations are stopped.
	 */
	public void animate(float fromValue, float toValue, int duration, Easing easing) {
		setBehavior(new Tween<Float>(fromValue, toValue, duration, easing));
	}

	/**
	 * Animates this property from the one double (fromValue) to another (toValue). Any previous animations are stopped.
	 */
	public void animate(float fromValue, float toValue, int duration, Easing easing, int startDelay) {
		setBehavior(new Tween(fromValue, toValue, duration, easing, startDelay));
	}

	/**
	 * Animates this property from the current value to the specified double. Any previous animations are stopped.
	 */
	public void animateTo(float toValue, int duration) {
		setBehavior(new Tween<Float>(get(), toValue, duration));
	}

	/**
	 * Animates this property from the current value to the specified double. Any previous animations are stopped.
	 */
	public void animateTo(float toValue, int duration, Easing easing) {
		setBehavior(new Tween(get(), toValue, duration, easing));
	}

	/**
	 * Animates this property from the current value to the specified double. Any previous animations are stopped.
	 */
	public void animateTo(float toValue, int duration, Easing easing, int startDelay) {
		setBehavior(new Tween(get(), toValue, duration, easing, startDelay));
	}

	static {
		FORMATTER = new NumberFormatter(new DecimalFormat("###.#######"));
	}
}

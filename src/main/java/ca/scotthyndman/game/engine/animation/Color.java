package ca.scotthyndman.game.engine.animation;

import com.jme.renderer.ColorRGBA;

/**
 * An Color is a 32-bit ARGB value that can be animated over time.
 */
public class Color extends Property<ColorRGBA> {

	public Color() {
		this(null, new ColorRGBA(0f, 0f, 0f, 1f));
	}

	public Color(PropertyListener listener) {
		this(listener, new ColorRGBA(0, 0, 0, 1));
	}

	public Color(ColorRGBA argbColor) {
		this(null, argbColor);
	}

	public Color(PropertyListener listener, ColorRGBA argbColor) {
		super(listener, argbColor);
	}

	//
	// Set
	//

	public void set(ColorRGBA argbColor) {
		setValue(argbColor);
		setBehavior(null);
	}

	public void set(ColorRGBA argbColor, int delay) {
		animateTo(argbColor, 0, null, delay);
	}

	/**
	 * Gets the packed, 32-bit ARGB value of this color.
	 */
	public ColorRGBA get() {
		return super.getValue();
	}

	public String toString() {
		return get().toString();
	}

	/**
	 * Returns true if the specified object is a {@code Color} or {@link java.lang.Integer} and its value is equal to
	 * this value.
	 */
	public boolean equals(Object obj) {
		if (obj instanceof Color) {
			return get().equals(((Color) obj).get());
		} else if (obj instanceof Integer) {
			return get().asIntARGB() == ((Integer) obj).intValue();
		} else {
			return false;
		}
	}

	public int hashCode() {
		return get().hashCode();
	}

	//
	// Convenience methods
	//

	/**
	 * Binds this property to the specified property. If this property is given a new behavior, the binding is broken.
	 */
	public void bindTo(Color property) {
		setBehavior(new Binding(this, property, false));
	}

	/**
	 * Bi-directionally binds this property to the specified property. If this property is given a new behavior, the
	 * specified property is then bi-directionally bound to this property. The binding is permanent, until a new
	 * bi-directional binding is specified.
	 */
	public void bindWithInverse(Color property) {
		setBehavior(new Binding(this, property, true));
	}

	/**
	 * Binds this property to the specified function.
	 */
	public void bindTo(BindFunction function) {
		setBehavior(new Binding(this, function));
	}

	/**
	 * Animates this Int from the one value (fromValue) to another (toValue). Any previous animations are stopped.
	 */
	public void animate(ColorRGBA fromARGB, ColorRGBA toARGB, int duration) {
		setBehavior(new Tween<ColorRGBA>(fromARGB, toARGB, duration));
	}

	/**
	 * Animates this Int from the one value (fromValue) to another (toValue). Any previous animations are stopped.
	 */
	public void animate(ColorRGBA fromARGB, ColorRGBA toARGB, int duration, Easing easing) {
		setBehavior(new Tween<ColorRGBA>(fromARGB, toARGB, duration, easing));
	}

	/**
	 * Animates this Int from the one value (fromValue) to another (toValue). Any previous animations are stopped.
	 */
	public void animate(ColorRGBA fromARGB, ColorRGBA toARGB, int duration, Easing easing, int startDelay) {
		setBehavior(new Tween<ColorRGBA>(fromARGB, toARGB, duration, easing, startDelay));
	}

	/**
	 * Animates this Int from the current value to the specified value. Any previous animations are stopped.
	 */
	public void animateTo(ColorRGBA toARGB, int duration) {
		setBehavior(new Tween<ColorRGBA>(get(), toARGB, duration));
	}

	/**
	 * Animates this Int from the current value to the specified value. Any previous animations are stopped.
	 */
	public void animateTo(ColorRGBA toARGB, int duration, Easing easing) {
		setBehavior(new Tween<ColorRGBA>(get(), toARGB, duration, easing));
	}

	/**
	 * Animates this Int from the current value to the specified value. Any previous animations are stopped.
	 */
	public void animateTo(ColorRGBA toARGB, int duration, Easing easing, int startDelay) {
		setBehavior(new Tween<ColorRGBA>(get(), toARGB, duration, easing, startDelay));
	}
}

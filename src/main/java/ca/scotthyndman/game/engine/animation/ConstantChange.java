package ca.scotthyndman.game.engine.animation;

public class ConstantChange<T> extends Animation implements PropertyBehavior<T> {

	private final T fromValue;
	private final T step;
	private final Interpolator<T> interpolator;
	private T value;

	int lastTime;

	public ConstantChange(T fromValue, T step) {
		this(fromValue, step, 0);
	}

	public ConstantChange(T fromValue, T step, int startDelay) {
		super(Integer.MAX_VALUE / 2, null, startDelay);

		this.interpolator = Interpolator.typeInterpolator((Class<T>) fromValue.getClass());
		this.fromValue = fromValue;
		if (value == null)
			value = fromValue;
		this.step = step;
		this.lastTime = 0;
	}

	protected void updateState(int animTime) {
		int elapsed = animTime - lastTime;
		value = interpolator.interpolate(value, interpolator.add(value, step), (float) elapsed / 1000);
		lastTime = animTime;
	}

	public final T getFromValue() {
		return fromValue;
	}

	protected final void setValue(T value) {
		this.value = value;
	}

	public final T getValue() {
		return value;
	}
}

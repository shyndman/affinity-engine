package ca.scotthyndman.game.prototype.animation;


public class Tween<T> extends Animation implements PropertyBehavior {

	private final T fromValue;
	private final T toValue;
	private final Interpolator<T> interpolator;
	private T value;

	public Tween(T fromValue, T toValue, int duration) {
		this(fromValue, toValue, duration, null, 0);
	}

	public Tween(T fromValue, T toValue, int duration, Easing easing) {
		this(fromValue, toValue, duration, easing, 0);
	}

	public Tween(T fromValue, T toValue, int duration, Easing easing, int startDelay) {
		super(duration, easing, startDelay);

		this.interpolator = Interpolator.typeInterpolator((Class<T>) fromValue.getClass());
		this.fromValue = fromValue;
		this.toValue = toValue;
	}

	protected void updateState(int animTime) {
		if (getDuration() == 0) {
			if (animTime < 0) {
				value = fromValue;
			} else {
				value = toValue;
			}
		} else {
			value = interpolator.interpolate(fromValue, toValue, (float) animTime / getDuration());
		}
	}

	public final T getFromValue() {
		return fromValue;
	}

	public final T getToValue() {
		return toValue;
	}

	protected final void setValue(T value) {
		this.value = value;
	}

	public final T getValue() {
		return value;
	}
}

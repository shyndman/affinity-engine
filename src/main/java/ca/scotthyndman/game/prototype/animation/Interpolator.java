package ca.scotthyndman.game.prototype.animation;

import java.util.HashMap;
import java.util.Map;

import com.jme.renderer.ColorRGBA;

public abstract class Interpolator<T> {

	private static Map<Class, Interpolator> interpolatorMap = new HashMap<Class, Interpolator>();
	static {
		interpolatorMap.put(Float.class, new FloatInterpolator());
		interpolatorMap.put(Integer.class, new IntInterpolator());
		interpolatorMap.put(Long.class, new LongInterpolator());
		interpolatorMap.put(Boolean.class, new BoolInterpolator());
		interpolatorMap.put(ColorRGBA.class, new ColorRGBAInterpolator());
	}

	/**
	 * Gets the interpolator for the specified type.
	 * 
	 * @param <T>
	 * @param valueClass
	 * @return
	 */
	public static <T> Interpolator<T> typeInterpolator(Class<T> valueClass) {
		return interpolatorMap.get(valueClass);
	}

	/**
	 * This function takes two endpoints and an input value between 0 and 1 and returns a value representing the state
	 * inbetween the endpoints. The purpose of the function is to define how time (represented as a (0-1) fraction of
	 * the duration of an animation) is altered to derive different value calculations during an animation.
	 * 
	 * @param start
	 *            the start end point
	 * @param end
	 *            the end end point
	 * @param fraction
	 *            a value between 0 and 1, representing the elapsed fraction of a time interval (either an entire
	 *            animation cycle or an interval between two KeyTimes, depending on where this Interpolator has been
	 *            set)
	 * @return a fractional value
	 */
	public abstract T interpolate(T start, T end, float fraction);

	/**
	 * Interpolates floats.
	 * 
	 * @author scottyhyndman
	 */
	private static class FloatInterpolator extends Interpolator<Float> {
		@Override
		public Float interpolate(Float start, Float end, float fraction) {
			return start + (end - start) * fraction;
		}
	}

	/**
	 * Interpolates ints.
	 * 
	 * @author scottyhyndman
	 */
	private static class IntInterpolator extends Interpolator<Integer> {
		@Override
		public Integer interpolate(Integer start, Integer end, float fraction) {
			return start + (int) ((end - start) * fraction);
		}
	}

	/**
	 * Interpolates longs.
	 * 
	 * @author scottyhyndman
	 */
	private static class LongInterpolator extends Interpolator<Long> {
		@Override
		public Long interpolate(Long start, Long end, float fraction) {
			return start + (long) ((end - start) * fraction);
		}
	}

	/**
	 * Interpolates ints.
	 * 
	 * @author scottyhyndman
	 */
	private static class BoolInterpolator extends Interpolator<Boolean> {
		@Override
		public Boolean interpolate(Boolean start, Boolean end, float fraction) {
			return fraction < 0.5 ? start : end;
		}
	}

	/**
	 * Interpolates ints.
	 * 
	 * @author scottyhyndman
	 */
	private static class ColorRGBAInterpolator extends Interpolator<ColorRGBA> {
		IntInterpolator intInterp = new IntInterpolator();

		@Override
		public ColorRGBA interpolate(ColorRGBA start, ColorRGBA end, float fraction) {
			ColorRGBA ret = new ColorRGBA();
			ret.interpolate(start, end, fraction);
			return ret;
		}
	}
}

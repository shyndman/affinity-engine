package ca.scotthyndman.game.prototype.animation;

import java.util.ArrayList;

import ca.scotthyndman.game.prototype.animation.event.TimelineEvent;
import ca.scotthyndman.game.prototype.entity.Env;
import ca.scotthyndman.game.prototype.scene.Positioned;
import ca.scotthyndman.game.prototype.scene.Updatable;

import com.jme.renderer.ColorRGBA;

/**
 * A Timeline is a list of Animations.
 */
public final class Timeline extends Animation implements Updatable {

	private Timeline parent;

	// Parallel arrays - behaviors have a property
	private ArrayList<Animation> animationList; // Animations. Must implement PropertyBehavior if property is non-null.
	private ArrayList<Property> propertyList; // Property if PropertyBehavior; false otherwise

	private boolean playing;
	private double playSpeed = 1;

	// Remainder, in microseconds, of the play time. Used when playSpeed != 1.
	private int remainderMicros;

	private int lastAnimTime = 0;
	private int lastTime;
	private boolean lastParentLooped;

	public Timeline() {
		this(null, 0);
	}

	public Timeline(int startDelay) {
		this(null, startDelay);
	}

	public Timeline(Easing easing) {
		this(easing, 0);
	}

	public Timeline(Easing easing, int startDelay) {
		super(0, easing, startDelay);
		animationList = new ArrayList();
		propertyList = new ArrayList();
		playing = true;
	}

	private void setParent(Timeline parent) {
		this.parent = parent;
	}

	private void calcDuration() {
		// TODO: sort children by their getTotalDuration() ?
		int duration = 0;
		for (int i = 0; i < animationList.size(); i++) {
			Animation anim = (Animation) animationList.get(i);
			int childDuration = anim.getTotalDuration();
			if (childDuration == LOOP_FOREVER) {
				duration = LOOP_FOREVER;
				break;
			} else if (childDuration > duration) {
				duration = childDuration;
			}
		}
		super.setDuration(duration);
		if (parent != null) {
			parent.calcDuration();
		}
	}

	//
	// Movie controls
	//

	/**
	 * Sets the play speed. A speed of '1' is normal, '.5' is half speed, '2' is twice normal speed, and '-1' is reverse
	 * speed. Note: play speed only affects top-level Timelines - child Timelines play at their parent's speed.
	 */
	public void setPlaySpeed(double speed) {
		playSpeed = speed;
	}

	public double getPlaySpeed() {
		return playSpeed;
	}

	public void pause() {
		playing = false;
	}

	public void play() {
		playing = true;
	}

	public void stop() {
		playing = false;
		rewind();
	}

	public boolean isPlaying() {
		return playing;
	}

	public void update(Env env, float tpf) {
		updateAnimation((int) (tpf * 1000), false);
	}

	// @Override
	boolean updateAnimation(int elapsedTime, boolean parentLooped) {
		if (!playing || playSpeed == 0) {
			elapsedTime = 0;
		} else if (playSpeed == -1) {
			elapsedTime = -elapsedTime;
		} else if (playSpeed != 1) {
			long timeMicros = Math.round(elapsedTime * 1000L * playSpeed) + remainderMicros;
			elapsedTime = (int) (timeMicros / 1000);
			remainderMicros = (int) (timeMicros % 1000);
		}
		lastParentLooped = parentLooped;
		return super.updateAnimation(elapsedTime, parentLooped);
	}

	protected void updateState(int animTime) {
		int oldLoop = getAnimLoop(lastTime);
		int newLoop = getAnimLoop(getTime());
		boolean looped = lastParentLooped || (newLoop != oldLoop);
		if (looped) {
			for (int i = 0; i < animationList.size(); i++) {
				Animation anim = (Animation) animationList.get(i);
				boolean active = anim.updateAnimation(animTime - anim.getTime(), true);
				if (active && anim instanceof PropertyBehavior) {
					((Property) propertyList.get(i)).setValue(((PropertyBehavior) anim).getValue());
				}
			}
		} else {
			// First, update those animations that were previously in SECTION_ANIMATION
			for (int i = 0; i < animationList.size(); i++) {
				Animation anim = (Animation) animationList.get(i);
				if (anim.getSection(lastAnimTime) == SECTION_ANIMATION) {
					boolean active = anim.updateAnimation(animTime - anim.getTime(), false);
					if (active && anim instanceof PropertyBehavior) {
						((Property) propertyList.get(i)).setValue(((PropertyBehavior) anim).getValue());
					}
				}
			}

			// Next, update all other animations
			for (int i = 0; i < animationList.size(); i++) {
				Animation anim = (Animation) animationList.get(i);
				if (anim.getSection(lastAnimTime) != SECTION_ANIMATION) {
					boolean active = anim.updateAnimation(animTime - anim.getTime(), false);
					if (active && anim instanceof PropertyBehavior) {
						((Property) propertyList.get(i)).setValue(((PropertyBehavior) anim).getValue());
					}
				}
			}
		}

		lastAnimTime = animTime;
		lastTime = getTime();
	}

	//
	// Children
	//

	/**
	 * Creates a child timeline that starts at the specified time relative to the start of this timeline.
	 * <p>
	 * This method provides an alternative syntax for delayed animations:
	 * 
	 * <pre>
	 * timeline.at(500).animate(root.alpha, 0, 255, 500);
	 * </pre>
	 * 
	 * @param time
	 *            Time in milliseconds.
	 * @return the child timeline.
	 */
	public Timeline at(int time) {
		Timeline child = new Timeline(Easing.NONE, time);
		add(child);
		return child;
	}

	/**
	 * Creates a child timeline that starts at end of this timeline.
	 * <p>
	 * This method provides an alternative syntax for delayed animations:
	 * 
	 * <pre>
	 * timeline.animate(root.alpha, 0, 255, 500);
	 * timeline.after().set(root.enabled, true);
	 * </pre>
	 * 
	 * @return the child timeline.
	 */
	public Timeline after() {
		return after(0);
	}

	/**
	 * Creates a child timeline that starts at the specified time relative to the end of this timeline.
	 * <p>
	 * This method provides an alternative syntax for delayed animations. This code animates a root from x1 to x2, waits
	 * one second, then animates back from x2 to x1.
	 * 
	 * <pre>
	 * timeline.animate(root.x, x1, x2, 500);
	 * timeline.after(1000).animate(root.x, x2, x1, 500);
	 * </pre>
	 * 
	 * @param time
	 *            Time in milliseconds (after the current end of this Timeline).
	 * @return the child timeline.
	 */
	public Timeline after(int time) {
		int t = getDuration();
		if (t == LOOP_FOREVER) {
			return at(time);
		} else {
			return at(time + t);
		}
	}

	/**
	 * Adds an event to the timeline.
	 */
	public void addEvent(TimelineEvent event) {
		add(event);
	}

	/**
	 * @deprecated Replaced by {@link #add(Property, Animation) }
	 */
	public void animate(Property property, Animation animation) {
		add(property, animation);
	}

	public void add(Animation animation) {
		if (animation instanceof Timeline) {
			((Timeline) animation).setParent(this);
		}
		animationList.add(animation);
		propertyList.add(null);
		calcDuration();
	}

	public void add(Property property, Animation animation) {
		animationList.add(animation);
		propertyList.add(property);
		calcDuration();
	}

	/**
	 * Calls notifyAll() on all child TimelineEvents, waking any threads that are waiting for them to execute.
	 */
	public void notifyChildren() {
		for (int i = 0; i < animationList.size(); i++) {
			Object anim = animationList.get(i);
			if (anim instanceof Timeline) {
				((Timeline) anim).notifyChildren();
			} else if (anim instanceof TimelineEvent) {
				synchronized (anim) {
					((TimelineEvent) anim).notifyAll();
				}
			}
		}
	}

	//
	// Set convenience methods (immediate)
	//

	public void set(Bool property, boolean value) {
		set(property, value, 0);
	}

	public void set(Int property, int value) {
		set(property, value, 0);
	}

	public void set(Color property, ColorRGBA value) {
		set(property, value, 0);
	}

	public void setAsFixed(Real property, int value) {
		set(property, value, 0);
	}

	public void set(Real property, int value) {
		set(property, value, 0);
	}

	public void set(Real property, double value) {
		set(property, value, 0);
	}

	public void setLocation(Positioned sprite, int x, int y) {
		set(sprite.x, x, 0);
		set(sprite.y, y, 0);
	}

	public void setLocation(Positioned sprite, double x, double y) {
		set(sprite.x, x, 0);
		set(sprite.y, y, 0);
	}

	//
	// Set convenience methods (delayed)
	//

	public void set(Bool property, boolean value, int delay) {
		add(property, new Tween<Boolean>(property.get(), value, 0, null, delay));
	}

	public void set(Int property, int value, int delay) {
		add(property, new Tween<Integer>(property.get(), value, 0, null, delay));
	}

	public void set(Color property, ColorRGBA value, int delay) {
		add(property, new Tween<ColorRGBA>(property.get(), value, 0, null, delay));
	}

	public void set(Real property, float value, int delay) {
		add(property, new Tween<Float>(property.get(), value, 0, null, delay));
	}

	public void set(Real property, double value, int delay) {
		add(property, new Tween<Float>(property.get(), (float) value, 0, null, delay));
	}

	public void setLocation(Positioned sprite, int x, int y, int delay) {
		set(sprite.x, x, delay);
		set(sprite.y, y, delay);
	}

	public void setLocation(Positioned sprite, float x, float y, int delay) {
		set(sprite.x, x, delay);
		set(sprite.y, y, delay);
	}

	//
	// Int convenience methods
	// 

	public void animate(Int property, int fromValue, int toValue, int duration) {
		add(property, new Tween(fromValue, toValue, duration));
	}

	public void animate(Int property, int fromValue, int toValue, int duration, Easing easing) {
		add(property, new Tween(fromValue, toValue, duration, easing));
	}

	public void animate(Int property, int fromValue, int toValue, int duration, Easing easing, int startDelay) {
		add(property, new Tween(fromValue, toValue, duration, easing, startDelay));
	}

	public void animateTo(Int property, int toValue, int duration) {
		add(property, new Tween(property.get(), toValue, duration));
	}

	public void animateTo(Int property, int toValue, int duration, Easing easing) {
		add(property, new Tween(property.get(), toValue, duration, easing));
	}

	public void animateTo(Int property, int toValue, int duration, Easing easing, int startDelay) {
		add(property, new Tween(property.get(), toValue, duration, easing, startDelay));
	}

	//
	// Color convenience methods
	//

	public void animate(Color property, ColorRGBA fromARGB, ColorRGBA toARGB, int duration) {
		add(property, new Tween<ColorRGBA>(fromARGB, toARGB, duration));
	}

	public void animate(Color property, ColorRGBA fromARGB, ColorRGBA toARGB, int duration, Easing easing) {
		add(property, new Tween<ColorRGBA>(fromARGB, toARGB, duration, easing));
	}

	public void animate(Color property, ColorRGBA fromARGB, ColorRGBA toARGB, int duration, Easing easing,
			int startDelay) {
		add(property, new Tween<ColorRGBA>(fromARGB, toARGB, duration, easing, startDelay));
	}

	public void animateTo(Color property, ColorRGBA toARGB, int duration) {
		add(property, new Tween<ColorRGBA>(property.get(), toARGB, duration));
	}

	public void animateTo(Color property, ColorRGBA toARGB, int duration, Easing easing) {
		add(property, new Tween<ColorRGBA>(property.get(), toARGB, duration, easing));
	}

	public void animateTo(Color property, ColorRGBA toARGB, int duration, Easing easing, int startDelay) {
		add(property, new Tween<ColorRGBA>(property.get(), toARGB, duration, easing, startDelay));
	}

	//
	// Real as int convenience methods
	//

	public void animate(Real property, float fromValue, float toValue, int duration) {
		add(property, new Tween(fromValue, toValue, duration));
	}

	public void animate(Real property, float fromValue, float toValue, int duration, Easing easing) {
		add(property, new Tween(fromValue, toValue, duration, easing));
	}

	public void animate(Real property, float fromValue, float toValue, int duration, Easing easing, int startDelay) {
		add(property, new Tween(fromValue, toValue, duration, easing, startDelay));
	}

	public void animateTo(Real property, float toValue, int duration) {
		add(property, new Tween(property.get(), toValue, duration));
	}

	public void animateTo(Real property, float toValue, int duration, Easing easing) {
		add(property, new Tween(property.get(), toValue, duration, easing));
	}

	public void animateTo(Real property, float toValue, int duration, Easing easing, int startDelay) {
		add(property, new Tween(property.get(), toValue, duration, easing, startDelay));
	}

	//
	// Move as int convenience methods
	//

	public void move(Positioned sprite, int x1, int y1, int x2, int y2, int duration) {
		animate(sprite.x, x1, x2, duration);
		animate(sprite.y, y1, y2, duration);
	}

	public void move(Positioned sprite, int x1, int y1, int x2, int y2, int duration, Easing easing) {
		animate(sprite.x, x1, x2, duration, easing);
		animate(sprite.y, y1, y2, duration, easing);
	}

	public void move(Positioned sprite, int x1, int y1, int x2, int y2, int duration, Easing easing, int startDelay) {
		animate(sprite.x, x1, x2, duration, easing, startDelay);
		animate(sprite.y, y1, y2, duration, easing, startDelay);
	}

	public void moveTo(Positioned sprite, int x, int y, int duration) {
		animateTo(sprite.x, x, duration);
		animateTo(sprite.y, y, duration);
	}

	public void moveTo(Positioned sprite, int x, int y, int duration, Easing easing) {
		animateTo(sprite.x, x, duration, easing);
		animateTo(sprite.y, y, duration, easing);
	}

	public void moveTo(Positioned sprite, int x, int y, int duration, Easing easing, int startDelay) {
		animateTo(sprite.x, x, duration, easing, startDelay);
		animateTo(sprite.y, y, duration, easing, startDelay);
	}

	//
	// Move as double convenience methods
	//

	public void move(Positioned sprite, float x1, float y1, float x2, float y2, int duration) {
		animate(sprite.x, x1, x2, duration);
		animate(sprite.y, y1, y2, duration);
	}

	public void move(Positioned sprite, float x1, float y1, float x2, float y2, int duration, Easing easing) {
		animate(sprite.x, x1, x2, duration, easing);
		animate(sprite.y, y1, y2, duration, easing);
	}

	public void move(Positioned sprite, float x1, float y1, float x2, float y2, int duration, Easing easing,
			int startDelay) {
		animate(sprite.x, x1, x2, duration, easing, startDelay);
		animate(sprite.y, y1, y2, duration, easing, startDelay);
	}

	public void moveTo(Positioned sprite, float x, float y, int duration) {
		animateTo(sprite.x, x, duration);
		animateTo(sprite.y, y, duration);
	}

	public void moveTo(Positioned sprite, float x, float y, int duration, Easing easing) {
		animateTo(sprite.x, x, duration, easing);
		animateTo(sprite.y, y, duration, easing);
	}

	public void moveTo(Positioned sprite, float x, float y, int duration, Easing easing, int startDelay) {
		animateTo(sprite.x, x, duration, easing, startDelay);
		animateTo(sprite.y, y, duration, easing, startDelay);
	}
}

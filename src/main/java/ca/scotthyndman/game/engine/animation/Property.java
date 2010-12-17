package ca.scotthyndman.game.engine.animation;

/**
 * The Editable class is the base class for animating values. Properties have a value, a behavior to control how the
 * value changes, and listeners to alert when the value changes.
 * <p>
 * Properties have an abstract 32-bit value, and it's up to subclasses to interpret that value with get and set methods.
 */
public abstract class Property<T> implements Finishable {

	private PropertyBehavior<T> behavior;
	private PropertyListener listener;
	private String name;
	private T value;

	/**
	 * Creates a property with the specified listener and initial value. The listener may be {@code null}. The behavior
	 * is {@code null}.
	 */
	public Property(PropertyListener listener, T value) {
		this.listener = listener;
		this.value = value;
	}

	/**
	 * Creates a property with the specified listener and initial value. The listener may be {@code null}. The behavior
	 * is {@code null}.
	 */
	public Property(PropertyListener listener, T value, String name) {
		this.listener = listener;
		this.value = value;
		this.name = name;
	}

	/**
	 * Gets this property's name, or <code>null</code> if none.
	 * 
	 * @return the property's name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the value for this property. If the new value is different from the old value, any listeners are alerted.
	 * The behavior, if any, is not changed.
	 * 
	 * @param value
	 *            the new value.
	 */
	protected final void setValue(T value) {
		if (this.value != value) {
			this.value = value;
			if (listener != null) {
				listener.onPropertyChange(this);
			}
		}
	}

	/**
	 * Gets the value for this property.
	 * 
	 * @return the value.
	 */
	protected final T getValue() {
		return value;
	}

	/**
	 * Sets the behavior for this property, which may be null. The value of this property is immediately set if {@code
	 * behavior.update(0)} returns {@code true}.
	 * 
	 * @param behavior
	 *            The new behavior.
	 */
	public final void setBehavior(PropertyBehavior<T> behavior) {
		Binding bidirectionalBinding = null;
		if (isBehaviorBidirectionalBinding()) {
			bidirectionalBinding = (Binding) this.behavior;
		}

		// Set behavior and update value immediately
		this.behavior = behavior;
		update(0);

		// Inverse the bi-directional binding, if any.
		if (bidirectionalBinding != null) {
			Property source = bidirectionalBinding.getSource();
			Property target;
			if (isBehaviorBidirectionalBinding()) {
				target = ((Binding) this.behavior).getTarget();
			} else {
				target = bidirectionalBinding.getTarget();
			}
			source.setBehavior(new Binding(source, target, true));
		}
	}

	private boolean isBehaviorBidirectionalBinding() {
		return this.behavior != null && this.behavior instanceof Binding && ((Binding) this.behavior).isBidirectional();
	}

	/**
	 * Gets the behavior for this property, or null if this property currently does not have a behavior.
	 * 
	 * @return the behavior.
	 */
	public final PropertyBehavior<T> getBehavior() {
		return behavior;
	}

	/**
	 * Returns a newly allocated array of all the listeners registered on this Editable.
	 * 
	 * @return all of this Editable's {@link PropertyListener}s or an empty array if no listeners are registered.
	 */
	public final PropertyListener[] getListeners() {
		if (listener == null) {
			return new PropertyListener[0];
		} else if (listener instanceof MultiListener) {
			return ((MultiListener) listener).getListeners();
		} else {
			return new PropertyListener[] { listener };
		}
	}

	/**
	 * Adds the specified listener to receive events from this Editable. If the listener is {@code null}, no exception
	 * is thrown and no action is performed.
	 * 
	 * @param listener
	 *            The listener to add.
	 */
	public final void addListener(PropertyListener listener) {
		if (listener == null || this.listener == listener) {
			// Do nothing
		} else if (this.listener == null) {
			this.listener = listener;
		} else if (this.listener instanceof MultiListener) {
			((MultiListener) this.listener).addListener(listener);
		} else {
			this.listener = new MultiListener(this.listener, listener);
		}
	}

	/**
	 * Removes the specified listener so that it no longer receives events from this Editable. This method performs no
	 * function, nor does it throw an exception, if the listener specified by the argument was not previously added to
	 * this Editable. If the listener is {@code null}, no exception is thrown and no action is performed.
	 * 
	 * @param listener
	 *            The listener to remove.
	 */
	public final void removeListener(PropertyListener listener) {
		if (this.listener == listener) {
			this.listener = null;
		} else if (this.listener instanceof MultiListener) {
			MultiListener ml = ((MultiListener) this.listener);
			ml.removeListener(listener);
			if (ml.size() == 1) {
				this.listener = ml.get(0);
			}
		}
	}

	/**
	 * Updates this Editable, possibly modifying its value if it has a {@link PropertyBehavior}. This method should be
	 * called once per frame, and a {@link pulpcore.sprite.Sprite} typically handles property updating.
	 * 
	 * @param elapsedTime
	 *            Elapsed time since the last update, in milliseconds.
	 */
	public final void update(int elapsedTime) {
		if (behavior != null) {
			// Make a copy in case the behavior reference is changed in update() or setValue()
			PropertyBehavior<T> b = behavior;
			boolean isActive = b.updateAnimation(elapsedTime);
			if (isActive) {
				setValue(b.getValue());
			}
			if (behavior == b && b.isFinished()) {
				behavior = null;
			}
		}
	}

	/**
	 * Checks if this property has a behavior and it is not finished animating.
	 * 
	 * @return true if this property has a behavior and it is not finished animating.
	 */
	public final boolean isAnimating() {
		return (behavior != null && !behavior.isFinished());
	}

	/**
	 * Checks if this property has a behavior and if it is finished animating.
	 * 
	 * @return true if this property has a behavior and it is finished animating or it has no behavior.
	 */
	public final boolean isFinished() {
		return (behavior == null || behavior.isFinished());
	}

	/**
	 * Stops the behavior, if any.
	 * 
	 * @param gracefully
	 *            if true, the behavior is fast-forwarded to it's end and the property's value is immediately set.
	 */
	public final void stopAnimation(boolean gracefully) {
		if (!isBehaviorBidirectionalBinding()) {
			if (behavior != null && gracefully) {
				// Make a copy in case the behavior reference is changed in fastForward()
				PropertyBehavior<T> b = behavior;
				b.fastForward();
				setValue(b.getValue());
			}
			behavior = null;
		}
	}

	public abstract boolean equals(Object obj);

	public abstract int hashCode();
}
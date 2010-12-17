package ca.scotthyndman.game.engine.animation;

/**
 * A PropertyBehavior controls how a {@link Editable}'s value changes over time.
 * 
 * @see Editable#setBehavior(PropertyBehavior)
 */
public interface PropertyBehavior<T>
{

    /**
     * Updates this PropertyBehavior and returns true if the update causes the value to change.
     * 
     * @param elapsedTime
     *            The amount of time to increment, in milliseconds.
     * @return true if the update causes the value to change.
     */
    public boolean updateAnimation(int elapsedTime);

    /**
     * Fast-forwards to the end of the PropertyBehavior if possible.
     */
    public void fastForward();

    /**
     * Checks if the PropertyBehavior will no longer update its value. In other words, {@link #updateAnimation(int)}
     * will no longer return {@code true}.
     * 
     * @return true if this PropertyBehavior will no longer update it's value.
     */
    public boolean isFinished();

    /**
     * Returns this PropertyBehavior's current value. The value will be interpreted differently depending on
     * the Editable it's attached to.
     * 
     * @return The current value.
     */
    public T getValue();
}

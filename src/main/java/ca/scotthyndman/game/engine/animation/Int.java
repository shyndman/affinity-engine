package ca.scotthyndman.game.engine.animation;


/**
 * An Int is an integer value that can be animated over time.
 */
public class Int extends Property<Integer>
{

    /**
     * Constructs a new Int object with no listener and the value of zero.
     */
    public Int()
    {
        this(null, 0);
    }

    /**
     * Constructs a new Int object with the specified listener and the value of zero. The listener
     * is notified when the value is modified.
     */
    public Int(PropertyListener listener)
    {
        this(listener, 0);
    }

    /**
     * Constructs a new Int object with the specified value and no listener.
     */
    public Int(int value)
    {
        this(null, value);
    }

    /**
     * Constructs a new Int object with the specified listener and value. The listener is notified
     * when the value is modified.
     */
    public Int(PropertyListener listener, int value)
    {
        super(listener, value);
    }

    public int get()
    {
        return super.getValue();
    }

    public String toString()
    {
        return Integer.toString(get());
    }

    /**
     * Returns true if the specified object is an {@code Int}, {@link Real}, {@link java.lang.Byte}
     * , {@link java.lang.Short}, {@link java.lang.Integer}, {@link java.lang.Long},
     * {@link java.lang.Float}, or {@link java.lang.Double}, and its value is equal to this value.
     */
    public boolean equals(Object obj)
    {
        if (obj instanceof Int)
        {
            return get() == ((Int) obj).get();
        }
        else if (obj instanceof Real)
        {
            return get() == ((Real) obj).getAsInt();
        }
        else if (obj instanceof Double)
        {
            return get() == ((Double) obj).doubleValue();
        }
        else if (obj instanceof Float)
        {
            return get() == ((Float) obj).floatValue();
        }
        else if (obj instanceof Byte || obj instanceof Short || obj instanceof Integer
                || obj instanceof Long)
        {
            return get() == ((Number) obj).longValue();
        }
        else
        {
            return false;
        }
    }

    public int hashCode()
    {
        // Same as java.lang.Integer
        return get();
    }
    
    /**
     * Sets the value of this Int. Any previous animations are stopped.
     */
    public void set(int value)
    {
        setValue(value);
        setBehavior(null);
    }

    /**
     * Sets the value of this Int after a specific delay. Any previous animations are stopped.
     */
    public void set(int value, int delay)
    {
        animateTo(value, 0, null, delay);
    }

    //
    // Convenience methods
    //

    /**
     * Binds this property to the specified property. If this property is given a new behavior, the
     * binding is broken.
     */
    public void bindTo(Int property)
    {
        setBehavior(new Binding(this, property, false));
    }

    /**
     * Bi-directionally binds this property to the specified property. If this property is given a
     * new behavior, the specified property is then bi-directionally bound to this property. The
     * binding is permanent, until a new bi-directional binding is specified.
     */
    public void bindWithInverse(Int property)
    {
        setBehavior(new Binding(this, property, true));
    }

    /**
     * Binds this property to the specified property. If this property is given a new behavior, the
     * binding is broken.
     */
    public void bindTo(Real property)
    {
        setBehavior(new Binding(this, property, false));
    }

    /**
     * Bi-directionally binds this property to the specified property. If this property is given a
     * new behavior, the specified property is then bi-directionally bound to this property.
     */
    public void bindWithInverse(Real property)
    {
        setBehavior(new Binding(this, property, true));
    }

    /**
     * Binds this property to the specified function.
     */
    public void bindTo(BindFunction function)
    {
        setBehavior(new Binding(this, function));
    }

    /**
     * Animates this Int from the one value (fromValue) to another (toValue). Any previous
     * animations are stopped.
     */
    public void animate(int fromValue, int toValue, int duration)
    {
        setBehavior(new Tween(fromValue, toValue, duration));
    }

    /**
     * Animates this Int from the one value (fromValue) to another (toValue). Any previous
     * animations are stopped.
     */
    public void animate(int fromValue, int toValue, int duration, Easing easing)
    {
        setBehavior(new Tween(fromValue, toValue, duration, easing));
    }

    /**
     * Animates this Int from the one value (fromValue) to another (toValue). Any previous
     * animations are stopped.
     */
    public void animate(int fromValue, int toValue, int duration, Easing easing, int startDelay)
    {
        setBehavior(new Tween(fromValue, toValue, duration, easing, startDelay));
    }

    /**
     * Animates this Int from the current value to the specified value. Any previous animations are
     * stopped.
     */
    public void animateTo(int toValue, int duration)
    {
        setBehavior(new Tween(get(), toValue, duration));
    }

    /**
     * Animates this Int from the current value to the specified value. Any previous animations are
     * stopped.
     */
    public void animateTo(int toValue, int duration, Easing easing)
    {
        setBehavior(new Tween(get(), toValue, duration, easing));
    }

    /**
     * Animates this Int from the current value to the specified value. Any previous animations are
     * stopped.
     */
    public void animateTo(int toValue, int duration, Easing easing, int startDelay)
    {
        setBehavior(new Tween(get(), toValue, duration, easing, startDelay));
    }
}
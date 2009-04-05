package ca.scotthyndman.game.prototype.animation.event;

import ca.scotthyndman.game.prototype.animation.Animation;

/**
 * A TimelineEvent is an abstract class that can perform a certain action after a specific delay.
 * TimelineEvents are added to and executed by a {@link pulpcore.animation.Timeline}. Subclasses
 * implement the {@link #run } method.
 * <p>
 * An anonymous inner class can be used in a Scene2D to create a code block that is executed after a
 * delay:
 * 
 * <pre>
 * int delay = 1000; // milliseconds
 * addEvent(new TimelineEvent(delay)
 * {
 *     public void run()
 *     {
 *         // Code to execute after the delay
 *     }
 * });
 * </pre>
 */
public abstract class TimelineEvent extends Animation implements Runnable
{

    private boolean hasExecuted;

    public TimelineEvent(int delay)
    {
        super(0, null, Math.max(1, delay));
        hasExecuted = false;
    }

    protected void updateState(int animTime)
    {
        run();
        if (!hasExecuted)
        {
            synchronized (this)
            {
                hasExecuted = true;
                this.notify();
            }
        }
    }

    /**
     * Returns true if the event has executed at least once.
     */
    public final boolean hasExecuted()
    {
        return hasExecuted;
    }

    public abstract void run();
}
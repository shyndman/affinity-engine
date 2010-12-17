package ca.scotthyndman.game.engine.animation;

/**
 * The PropertyListener is the listener interface for receiving notification when a {@link Editable}
 * 's value has changed.
 */
public interface PropertyListener
{

    /**
     * Notifies that a property's value has changed.
     * 
     * @param property
     *            the property whose value has changed.
     */
    public void onPropertyChange(Property property);

}
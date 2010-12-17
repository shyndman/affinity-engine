package ca.scotthyndman.game.engine.animation;

import java.util.ArrayList;
import java.util.List;

/**
 * A PropertyListener for Properties with multiple listeners
 */
class MultiListener implements PropertyListener
{

    private List<PropertyListener> listeners;

    MultiListener(PropertyListener a, PropertyListener b)
    {
        listeners = new ArrayList<PropertyListener>(2);
        listeners.add(a);
        listeners.add(b);
    }

    PropertyListener[] getListeners()
    {
        PropertyListener[] array = new PropertyListener[listeners.size()];
        listeners.toArray(array);
        return array;
    }

    int size()
    {
        return listeners.size();
    }

    PropertyListener get(int index)
    {
        return (PropertyListener) listeners.get(index);
    }

    void addListener(PropertyListener listener)
    {
        if (!listeners.contains(listener))
        {
            listeners.add(listener);
        }
    }

    void removeListener(PropertyListener listener)
    {
        listeners.remove(listener);
    }

    public void onPropertyChange(Property property)
    {
        // Make a copy in case any of the listeners remove themselves
        PropertyListener[] list = getListeners();
        for (int i = 0; i < list.length; i++)
        {
            list[i].onPropertyChange(property);
        }
    }

}

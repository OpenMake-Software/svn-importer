/*****************************************************************************
 * Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is available at http://www.sun.com/

 * The Original Code is the CVS Client Library.
 * The Initial Developer of the Original Code is Robert Greig.
 * Portions created by Robert Greig are Copyright (C) 2000.
 * All Rights Reserved.

 * Contributor(s): Robert Greig.
 *****************************************************************************/
package org.netbeans.lib.cvsclient.event;

/**
 * This class is responsible for firing CVS events to registered listeners.
 * It can either fire events as they are generated or wait until a suitable
 * checkpoint and fire many events at once. This can prevent event storms
 * from degrading system performance.
 * @author  Robert Greig
 */
public class EventManager {
    /**
     * Registered listeners for events. This is an array for performance when
     * firing events. We take the hit when adding or removing listeners - that
     * should be a relatively rare occurrence.
     */
    private CVSListener[] listeners;

    /**
     * Holds value of property fireEnhancedEventSet.
     * If true, the library fires the EnhancedMessageEvents.
     * Default is true. Some builders might work badly, if set to false.
     */
    private boolean fireEnhancedEventSet = true;

    /**
     * Construct a new EventManager
     */
    public EventManager() {
    }

    /**
     * Add a listener to the list.
     * @param listener the listener to add
     */
    public synchronized void addCVSListener(CVSListener listener) {
        if (listeners == null || listeners.length == 0) {
            listeners = new CVSListener[1];
        }
        else {
            // allocate a new array and copy existing listeners
            CVSListener[] l = new CVSListener[listeners.length + 1];
            for (int i = 0; i < listeners.length; i++) {
                l[i] = listeners[i];
            }
            listeners = l;
        }
        listeners[listeners.length - 1] = listener;
    }

    /**
     * Remove a listeners from the list
     * @param l the listener to remove
     */
    public synchronized void removeCVSListener(CVSListener listener) {
        // TODO: test this method!!
        if (listeners.length == 1) {
            listeners = null;
        }
        else {
            CVSListener[] l = new CVSListener[listeners.length - 1];
            int i = 0;
            while (i < l.length) {
                if (listeners[i] == listener) {
                    for (int j = i + 1; j < listeners.length; j++) {
                        l[j - 1] = listeners[j];
                    }
                    break;
                }
                else {
                    l[i] = listeners[i];
                }
                i++;
            }
            listeners = l;
        }
    }

    /**
     * Fire a CVSEvent to all the listeners
     * @param e the event to send
     */
    public void fireCVSEvent(CVSEvent e) {
        // if we have no listeners, then there is nothing to do
        if (listeners == null || listeners.length == 0)
            return;
        CVSListener[] l = null;
        synchronized (listeners) {
            l = new CVSListener[listeners.length];
            System.arraycopy(listeners, 0, l, 0, l.length);
        }

        for (int i = 0; i < l.length; i++) {
            e.fireEvent(l[i]);
        }
    }

    /** Getter for property fireEnhancedEventSet.
     * @return Value of property fireEnhancedEventSet.
     */
    public boolean isFireEnhancedEventSet() {
        return fireEnhancedEventSet;
    }

    /** Setter for property fireEnhancedEventSet.
     * @param fireEnhancedEventSet New value of property fireEnhancedEventSet.
     */
    public void setFireEnhancedEventSet(boolean fireEnhancedEventSet) {
        this.fireEnhancedEventSet = fireEnhancedEventSet;
    }

}
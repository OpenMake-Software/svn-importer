/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.cvsclient.event;

/**
 * An event sent from the server to indicate that a binary message should be
 * displayed to the user
 * @author  Martin Entlicher
 */
public class BinaryMessageEvent extends CVSEvent {
    /**
     * Holds value of property message.
     */
    private byte[] message;

    /**
     * Construct a MessageEvent
     * @param source the source of the event
     * @param message the message text
     */
    public BinaryMessageEvent(Object source, byte[] message) {
        super(source);
        this.message = message;
    }

    /**
     * Getter for property message.
     * @return Value of property message.
     */
    public byte[] getMessage() {
        return message;
    }

    /**
     * Fire the event to the event listener. Subclasses should call the
     * appropriate method on the listener to dispatch this event.
     * @param listener the event listener
     */
    protected void fireEvent(CVSListener listener) {
        listener.messageSent(this);
    }

}
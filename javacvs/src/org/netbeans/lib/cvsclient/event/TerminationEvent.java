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
 * An event sent from the server to indicate that a the output from the server
 * has ended for the current command
 * @author  Milos Kleint
 */
public class TerminationEvent extends CVSEvent {

    /**
     * Whether the termination is an error or not
     */
    private boolean error;

    /**
     * Construct a MessageEvent
     * @param source the source of the event
     * @param message the message text
     * @param isError true if the message is an error message (i.e. intended
     * for stderr rather than stdout), false otherwise
     */
    public TerminationEvent(Object source, boolean isError) {
        super(source);
        setError(isError);
    }

    /**
     * Construct a MessageEvent with no message text
     * @param source the source of the event
     */
    public TerminationEvent(Object source) {
        this(source, false);
    }

    /**
     * Get whether the command ended successfully or not
     * @return true if the successfull
     */
    public boolean isError() {
        return error;
    }

    /**
     * Get whether the command ended successfully or not
     * @param error true if successfull
     */
    public void setError(boolean error) {
        this.error = error;
    }

    /**
     * Fire the event to the event listener. Subclasses should call the
     * appropriate method on the listener to dispatch this event.
     * @param listener the event listener
     */
    protected void fireEvent(CVSListener listener) {
        listener.commandTerminated(this);
    }
}
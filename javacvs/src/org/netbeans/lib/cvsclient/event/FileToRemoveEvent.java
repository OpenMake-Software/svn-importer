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
 * Indicates that a file is to be removed.
 * @author Martin Entlicher
 */
public class FileToRemoveEvent extends CVSEvent {

    /**
     * The path of the file that is going to be removed.
     */
    protected String path;

    /**
     * Construct a FileRemovedEvent
     * @param source the source of the event
     * @param path The path of the file that is going to be removed
     */
    public FileToRemoveEvent(Object source, String path) {
        super(source);
        this.path = path;
    }

    /**
     * Get the path of the file that is going to be removed.
     */
    public String getFilePath() {
        return path;
    }

    /**
     * Fire the event to the event listener. Subclasses should call the
     * appropriate method on the listener to dispatch this event.
     * @param listener the event listener
     */
    protected void fireEvent(CVSListener listener) {
        listener.fileToRemove(this);
    }
}

/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.cvsclient.event;

import org.netbeans.lib.cvsclient.command.*;

/**
 * This event is created when file information is received from the
 * server.
 * @author  Milos Kleint
 */
public class FileInfoEvent extends CVSEvent {
    /**
     * The information about the file.
     */
    private final FileInfoContainer infoContainer;

    /**
     * Construct a FileInfoEvent
     * @param source the source of the event
     * @param message the message text
     * @param isError true if the message is an error message (i.e. intended
     * for stderr rather than stdout), false otherwise
     */
    public FileInfoEvent(Object source, FileInfoContainer infoContainer) {
        super(source);
        this.infoContainer = infoContainer;
    }

    /**
     * Get the information in this event
     * @return the information object describing a file's info received from the server
     */
    public FileInfoContainer getInfoContainer() {
        return infoContainer;
    }

    /**
     * Fire the event to the event listener. Subclasses should call the
     * appropriate method on the listener to dispatch this event.
     * @param listener the event listener
     */
    protected void fireEvent(CVSListener listener) {
        listener.fileInfoGenerated(this);
    }
}
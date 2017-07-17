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
 * A convenience class for implementing the CVSListener. This class provides
 * empty implementations of the CVSListener interface. Subclasses should
 * override the methods for the event in which they are interested.
 * @author  Robert Greig
 */
public class CVSAdapter implements CVSListener {

    /**
     * Called when the server wants to send a message to be displayed to
     * the user. The message is only for information purposes and clients
     * can choose to ignore these messages if they wish.
     * @param e the event
     */
    public void messageSent(MessageEvent e) {
    }

    /**
     * Called when the server wants to send a binary message to be displayed to
     * the user. The message is only for information purposes and clients
     * can choose to ignore these messages if they wish.
     * @param e the event
     */
    public void messageSent(BinaryMessageEvent e) {
    }

    /**
     * Called when a file has been added.
     * @param e the event
     */
    public void fileAdded(FileAddedEvent e) {
    }

    /**
     * Called when a file is going to be removed.
     * @param e the event
     */
    public void fileToRemove(FileToRemoveEvent e) {
    }

    /**
     * Called when a file is removed.
     * @param e the event
     */
    public void fileRemoved(FileRemovedEvent e) {
    }

    /**
     * Called when a file has been updated
     * @param e the event
     */
    public void fileUpdated(FileUpdatedEvent e) {
    }

    /**
     * Called when file status information has been received
     */
    public void fileInfoGenerated(FileInfoEvent e) {
    }

    /**
     * called when server responses with "ok" or "error", (when the command finishes)
     */
    public void commandTerminated(TerminationEvent e) {
    }

    /**
     * Fire a module expansion event. This is called when the servers
     * has responded to an expand-modules request.
     */
    public void moduleExpanded(ModuleExpansionEvent e) {
    }

}
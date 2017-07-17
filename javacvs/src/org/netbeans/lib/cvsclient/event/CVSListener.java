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
 * This interface must be implemented by clients wishing to receive events
 * describing the results of commands.
 * @author  Robert Greig
 */
public interface CVSListener {
    /**
     * Called when the server wants to send a message to be displayed to
     * the user. The message is only for information purposes and clients
     * can choose to ignore these messages if they wish.
     * @param e the event
     */
    void messageSent(MessageEvent e);

    /**
     * Called when the server wants to send a binary message to be displayed to
     * the user. The message is only for information purposes and clients
     * can choose to ignore these messages if they wish.
     * @param e the event
     */
    void messageSent(BinaryMessageEvent e);
    
    /**
     * Called when a file has been added.
     * @param e the event
     */
    void fileAdded(FileAddedEvent e);

    /**
     * Called when a file is going to be removed.
     * @param e the event
     */
    void fileToRemove(FileToRemoveEvent e);

    /**
     * Called when a file is removed.
     * @param e the event
     */
    void fileRemoved(FileRemovedEvent e);

    /**
     * Called when a file has been updated
     * @param e the event
     */
    void fileUpdated(FileUpdatedEvent e);

    /**
     * Called when file information has been received
     */
    void fileInfoGenerated(FileInfoEvent e);

    /**
     * called when server responses with "ok" or "error", (when the command
     * finishes)
     */
    void commandTerminated(TerminationEvent e);

    /**
     * Fire a module expansion event. This is called when the servers
     * has responded to an expand-modules request.
     */
    void moduleExpanded(ModuleExpansionEvent e);
}
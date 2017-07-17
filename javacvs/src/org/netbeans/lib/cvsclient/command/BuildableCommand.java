/*****************************************************************************
 * Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is available at http://www.sun.com/
 *
 * The Original Code is the CVS Client Library.
 * The Initial Developer of the Original Code is Robert Greig.
 * Portions created by Robert Greig are Copyright (C) 2000.
 * All Rights Reserved.
 *
 * Contributor(s): Robert Greig.
 *****************************************************************************/
package org.netbeans.lib.cvsclient.command;

import org.netbeans.lib.cvsclient.*;
import org.netbeans.lib.cvsclient.connection.*;
import org.netbeans.lib.cvsclient.event.*;

/**
 * A class that provides common functionality for many of the CVS command
 * that send similar sequences of requests.
 * @author  Robert Greig
 */
public abstract class BuildableCommand extends Command {

    /**
     * An implementation of Builder interface that constructs a FileContainerInfo object from
     * the server's output..
     */
    protected Builder builder;

    private final StringBuffer taggedLineBuffer = new StringBuffer();

    /**
     * A boolean value indicating if the user has used the setBuilder() method.
     */
    private boolean builderSet;

    /**
     * Execute a command. This implementation sends a Root request, followed
     * by as many Directory and Entry requests as is required by the recurse
     * setting and the file arguments that have been set. Subclasses should
     * call this first, and tag on the end of the requests list any further
     * requests and, finally, the actually request that does the command (e.g.
     * <pre>update</pre>, <pre>status</pre> etc.)
     * @param client the client services object that provides any necessary
     * services to this command, including the ability to actually process
     * all the requests
     * @throws CommandException if an error occurs executing the command
     */
    public void execute(ClientServices client, EventManager eventManager)
            throws CommandException, AuthenticationException {
        super.execute(client, eventManager);

        if (builder == null && !isBuilderSet()) {
            builder = createBuilder(eventManager);
        }
    }

    /**
     * Method that is called while the command is being executed.
     * Descendants can override this method to return a Builder instance
     * that will parse the server's output and create data structures.
     */
    public Builder createBuilder(EventManager eventManager) {
        return null;
    }

    public void messageSent(MessageEvent e) {
        super.messageSent(e);
        if (builder == null) {
            return;
        }

        if (e instanceof EnhancedMessageEvent) {
            EnhancedMessageEvent eEvent = (EnhancedMessageEvent)e;
            builder.parseEnhancedMessage(eEvent.getKey(), eEvent.getValue());
            return;
        }

        if (e.isTagged()) {
            String message = MessageEvent.parseTaggedMessage(taggedLineBuffer, e.getMessage());
            if (message != null) {
                builder.parseLine(message, false);
                taggedLineBuffer.setLength(0);
            }
        }
        else {
            if (taggedLineBuffer.length() > 0) {
                builder.parseLine(taggedLineBuffer.toString(), false);
                taggedLineBuffer.setLength(0);
            }
            builder.parseLine(e.getMessage(), e.isError());
        }
    }

    /**
     * Returns whether the builder is set.
     */
    protected boolean isBuilderSet() {
        return builderSet;
    }

    /**
     * Used for setting user-defined builder.
     * Can be also set null, in that case the builder mechanism is not used at
     * all.
     */
    public void setBuilder(Builder builder) {
        this.builder = builder;
        builderSet = true;
    }

    /**
     * Called when server responses with "ok" or "error", (when the command finishes).
     */
    public void commandTerminated(TerminationEvent e) {
        if (builder == null) {
            return;
        }

        if (taggedLineBuffer.length() > 0) {
            builder.parseLine(taggedLineBuffer.toString(), false);
            taggedLineBuffer.setLength(0);
        }
        builder.outputDone();
    }
}

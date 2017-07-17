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
package org.netbeans.lib.cvsclient.command.status;

import java.io.*;

import org.netbeans.lib.cvsclient.*;
import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.connection.*;
import org.netbeans.lib.cvsclient.event.*;
import org.netbeans.lib.cvsclient.request.*;

/**
 * The status command looks up the status of files in the repository
 * @author  Robert Greig
 */
public class StatusCommand extends BasicCommand {
    /**
     * The event manager to use
     */
    private EventManager eventManager;

    /**
     * Holds value of property includeTags.
     */
    private boolean includeTags;

    /**
     * Construct a new status command
     */
    public StatusCommand() {
    }

    /**
     * Create a builder for this command.
     * @param eventMan the event manager used to receive events.
     */
    public Builder createBuilder(EventManager eventManager) {
        return new StatusBuilder(eventManager, this);
    }

    /**
     * Execute a command
     * @param client the client services object that provides any necessary
     * services to this command, including the ability to actually process
     * all the requests.
     */
    public void execute(ClientServices client, EventManager em)
            throws CommandException, AuthenticationException {
        client.ensureConnection();

        eventManager = em;

        super.execute(client, em);

        try {
            // parameters come now..
            if (includeTags) {
                requests.add(1, new ArgumentRequest("-v")); //NOI18N
            }

            addRequestForWorkingDirectory(client);
            addArgumentRequests();
            addRequest(CommandRequest.STATUS);

            client.processRequests(requests);
        }
        catch (CommandException ex) {
            throw ex;
        }
        catch (Exception e) {
            throw new CommandException(e, e.getLocalizedMessage());
        }
        finally {
            requests.clear();
        }
    }

    /**
     * Getter for property includeTags.
     * @return Value of property includeTags.
     */
    public boolean isIncludeTags() {
        return includeTags;
    }

    /**
     * Setter for property includeTags.
     * @param includeTags New value of property includeTags.
     */
    public void setIncludeTags(boolean inclTags) {
        includeTags = inclTags;
    }

    /**
     * called when server responses with "ok" or "error", (when the command finishes)
     */
    public void commandTerminated(TerminationEvent e) {
        if (builder != null) {
            builder.outputDone();
        }
    }

    /**
     * This method returns how the command would looklike when typed on the command line.
     * Each command is responsible for constructing this information.
     * @returns <command's name> [<parameters>] files/dirs. Example: checkout -p CvsCommand.java
     */
    public String getCVSCommand() {
        StringBuffer toReturn = new StringBuffer("status "); //NOI18N
        toReturn.append(getCVSArguments());
        File[] files = getFiles();
        if (files != null) {
            for (int index = 0; index < files.length; index++) {
                toReturn.append(files[index].getName());
                toReturn.append(' ');
            }
        }
        return toReturn.toString();
    }

    /**
     * takes the arguments and sets the command. To be mainly
     * used for automatic settings (like parsing the .cvsrc file)
     * @return true if the option (switch) was recognized and set
     */
    public boolean setCVSCommand(char opt, String optArg) {
        if (opt == 'R') {
            setRecursive(true);
        }
        else if (opt == 'l') {
            setRecursive(false);
        }
        else if (opt == 'v') {
            setIncludeTags(true);
        }
        else {
            return false;
        }
        return true;
    }

    /**
     * String returned by this method defines which options are available for this particular command
     */
    public String getOptString() {
        return "Rlv"; //NOI18N
    }

    /**
     * resets all switches in the command. After calling this method,
     * the command should have no switches defined and should behave defaultly.
     */
    public void resetCVSCommand() {
        setRecursive(true);
        setIncludeTags(false);
    }

    /**
     * Returns the arguments of the command in the command-line style.
     * Similar to getCVSCommand() however without the files and command's name
     */
    public String getCVSArguments() {
        StringBuffer toReturn = new StringBuffer(""); //NOI18N
        if (isIncludeTags()) {
            toReturn.append("-v "); //NOI18N
        }
        if (!isRecursive()) {
            toReturn.append("-l "); //NOI18N
        }
        return toReturn.toString();
    }

}

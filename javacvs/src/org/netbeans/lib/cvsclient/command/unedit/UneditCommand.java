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
package org.netbeans.lib.cvsclient.command.unedit;

import java.io.*;

import org.netbeans.lib.cvsclient.*;
import org.netbeans.lib.cvsclient.admin.*;
import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.command.edit.*;
import org.netbeans.lib.cvsclient.connection.*;
import org.netbeans.lib.cvsclient.event.*;
import org.netbeans.lib.cvsclient.file.*;
import org.netbeans.lib.cvsclient.request.*;

/**
 * @author  Thomas Singer
 */
public class UneditCommand extends BasicCommand {

    private Watch temporaryWatch;

    /**
     * Construct a new editors command.
     */
    public UneditCommand() {
        resetCVSCommand();
    }

    /**
     * Execute the command.
     *
     * @param client the client services object that provides any necessary
     *               services to this command, including the ability to actually
     *               process all the requests.
     */
    public void execute(ClientServices clientServices, EventManager eventManager)
            throws CommandException, AuthenticationException {
        clientServices.ensureConnection();

        try {
            super.execute(clientServices, eventManager);

            addRequestForWorkingDirectory(clientServices);
            addRequest(CommandRequest.NOOP);

            clientServices.processRequests(requests);
        }
        catch (CommandException ex) {
            throw ex;
        }
        catch (EOFException ex) {
            throw new CommandException(ex, CommandException.getLocalMessage("CommandException.EndOfFile", null)); //NOI18N
        }
        catch (Exception ex) {
            throw new CommandException(ex, ex.getLocalizedMessage());
        }
        finally {
            requests.clear();
        }
    }

    protected void addRequestForFile(File file, Entry entry) {
        String temporaryWatch = Watch.getWatchString(getTemporaryWatch());
        requests.add(new NotifyRequest(file, "U", temporaryWatch)); // NOI18N

        try {
            uneditFile(file);
        }
        catch (IOException ex) {
            // ignore
        }
    }

    /**
     * Called when server responses with "ok" or "error", (when the command
     * finishes).
     */
    public void commandTerminated(TerminationEvent e) {
        if (builder != null) {
            builder.outputDone();
        }
    }

    /**
     * This method returns how the tag command would looklike when typed on the
     * command line.
     */
    public String getCVSCommand() {
        StringBuffer cvsCommandLine = new StringBuffer("unedit "); //NOI18N
        cvsCommandLine.append(getCVSArguments());
        appendFileArguments(cvsCommandLine);
        return cvsCommandLine.toString();
    }

    /**
     * Takes the arguments and sets the command.
     * To be mainly used for automatic settings (like parsing the .cvsrc file)
     * @return true if the option (switch) was recognized and set
     */
    public boolean setCVSCommand(char opt, String optArg) {
        if (opt == 'R') {
            setRecursive(true);
        }
        else if (opt == 'l') {
            setRecursive(false);
        }
        else {
            return false;
        }
        return true;
    }

    /**
     * String returned by this method defines which options are available for
     * this command.
     */
    public String getOptString() {
        return "Rl"; //NOI18N
    }

    /**
     * Resets all switches in the command.
     * After calling this method, the command should have no switches defined
     * and should behave defaultly.
     */
    public void resetCVSCommand() {
        setRecursive(true);
    }

    /**
     * Returns the arguments of the command in the command-line style.
     * Similar to getCVSCommand() however without the files and command's name
     */
    public String getCVSArguments() {
        StringBuffer cvsArguments = new StringBuffer();
        if (!isRecursive()) {
            cvsArguments.append("-l "); //NOI18N
        }
        return cvsArguments.toString();
    }

    /**
     * Returns the temporary watch.
     */
    public Watch getTemporaryWatch() {
        return temporaryWatch;
    }

    /**
     * Sets the temporary watch.
     */
    public void setTemporaryWatch(Watch temporaryWatch) {
        this.temporaryWatch = temporaryWatch;
    }

    private void uneditFile(File file) throws IOException {
        removeBaserevEntry(file);
        EditCommand.getEditBackupFile(file).delete();
        FileUtils.setFileReadOnly(file, true);
    }

    private void removeBaserevEntry(File file) throws IOException {
        File baserevFile = new File(file.getParentFile(), "CVS/Baserev"); // NOI18N
        File backupFile = new File(baserevFile.getAbsolutePath() + '~');

        BufferedReader reader = null;
        BufferedWriter writer = null;
        final String entryStart = 'B' + file.getName() + '/';
        try {
            writer = new BufferedWriter(new FileWriter(backupFile));
            reader = new BufferedReader(new FileReader(baserevFile));

            for (String line = reader.readLine();
                 line != null;
                 line = reader.readLine()) {

                if (line.startsWith(entryStart)) {
                    continue;
                }

                writer.write(line);
                writer.newLine();
            }
        }
        catch (FileNotFoundException ex) {
            // ignore
        }
        finally {
            if (writer != null) {
                try {
                    writer.close();
                }
                catch (IOException ex) {
                    // ignore
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException ex) {
                    // ignore
                }
            }
        }
        baserevFile.delete();
        if (backupFile.length() > 0) {
            backupFile.renameTo(baserevFile);
        }
        else {
            backupFile.delete();
        }
    }

}

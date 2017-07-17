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

package org.netbeans.lib.cvsclient.command.remove;

import java.io.*;

import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.event.*;

/**
 * Handles the building of remove information object and the firing of
 * events when complete objects are built.
 *
 * @author  Milos Kleint
 */
public class RemoveBuilder implements Builder {
    private static final String UNKNOWN = ": nothing known about"; //NOI18N
    private static final String WARNING = ": warning: "; //NOI18N
    private static final String SCHEDULING = ": scheduling `"; //NOI18N
    private static final String USE_COMMIT = ": use 'cvs commit' "; //NOI18N
    private static final String DIRECTORY = ": Removing "; //NOI18N
    private static final String STILL_IN_WORKING = ": file `"; //NOI18N
    private static final String REMOVE_FIRST = "first"; //NOI18N
    private static final String UNKNOWN_FILE = "?"; //NOI18N

    /**
     * The status object that is currently being built
     */
    private RemoveInformation removeInformation;

    /**
     * The directory in which the file being processed lives. This is
     * relative to the local directory
     */
    private String fileDirectory;

    /**
     * The event manager to use
     */
    private final EventManager eventManager;

    private final RemoveCommand removeCommand;

    public RemoveBuilder(EventManager eventManager, RemoveCommand removeCommand) {
        this.eventManager = eventManager;
        this.removeCommand = removeCommand;
    }

    public void outputDone() {
        if (removeInformation != null) {
            eventManager.fireCVSEvent(new FileInfoEvent(this, removeInformation));
            removeInformation = null;
        }
    }

    public void parseLine(String line, boolean isErrorMessage) {
        if (line.indexOf(SCHEDULING) >= 0) {
            int endingIndex = line.indexOf('\'');
            String fn = line.substring(line.indexOf(SCHEDULING) + SCHEDULING.length(), endingIndex).trim();
            addFile(fn);
            removeInformation.setRemoved(true);
            outputDone();
        }
        if (line.startsWith(UNKNOWN_FILE)) {
            addFile(line.substring(UNKNOWN_FILE.length()));
            removeInformation.setRemoved(false);
            outputDone();
        }
        if (line.indexOf(STILL_IN_WORKING) >= 0) {
            int endingIndex = line.indexOf('\'');
            String fn = line.substring(line.indexOf(STILL_IN_WORKING) + STILL_IN_WORKING.length(), endingIndex).trim();
            addFile(fn);
            removeInformation.setRemoved(false);
            outputDone();
        }
        // ignore the rest..
    }

    protected File createFile(String fileName) {
        StringBuffer path = new StringBuffer();
        path.append(removeCommand.getLocalDirectory());
        path.append(File.separator);
        if (fileDirectory == null) {
            // happens for single files only
            // (for directories, the dir name is always sent before the actual files)
            File locFile = removeCommand.getFileEndingWith(fileName);
            if (locFile == null) {
                path.append(fileName);
            }
            else {
                path = new StringBuffer(locFile.getAbsolutePath());
            }
        }
        else {
//            path.append(fileDirectory);
//            path.append(File.separator);
            path.append(fileName);
        }
        String toReturn = path.toString();
        toReturn = toReturn.replace('/', File.separatorChar);
        return new File(path.toString());
    }

    protected void addFile(String name) {
        removeInformation = new RemoveInformation();
        removeInformation.setFile(createFile(name));
    }

    public void parseEnhancedMessage(String key, Object value) {
    }
}

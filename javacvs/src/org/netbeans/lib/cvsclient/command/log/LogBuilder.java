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
package org.netbeans.lib.cvsclient.command.log;

import java.io.*;
import java.util.*;

import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.event.*;
import org.netbeans.lib.cvsclient.util.*;

/**
 * Handles the building of a log information object and the firing of
 * events when complete objects are built.
 * @author Milos Kleint
 */
public class LogBuilder implements Builder {
    private static final String LOGGING_DIR = ": Logging "; //NOI18N
    private static final String RCS_FILE = "RCS file: "; //NOI18N
    private static final String WORK_FILE = "Working file: "; //NOI18N
    private static final String REV_HEAD = "head: "; //NOI18N
    private static final String BRANCH = "branch: "; //NOI18N
    private static final String LOCKS = "locks: "; //NOI18N
    private static final String ACCESS_LIST = "access list: "; //NOI18N
    private static final String SYM_NAME = "symbolic names:"; //NOI18N
    private static final String KEYWORD_SUBST = "keyword substitution: "; //NOI18N
    private static final String TOTAL_REV = "total revisions: "; //NOI18N
    private static final String SEL_REV = ";\tselected revisions: "; //NOI18N
    private static final String DESCRIPTION = "description:"; //NOI18N
    private static final String REVISION = "revision "; //NOI18N
    private static final String DATE = "date: "; //NOI18N
    private static final String BRANCHES = "branches: "; //NOI18N
    private static final String AUTHOR = "  author: "; //NOI18N
    private static final String STATE = "  state: "; //NOI18N
    private static final String LINES = "  lines: "; //NOI18N
    private static final String SPLITTER = "----------------------------"; //NOI18N
    private static final String FINAL_SPLIT = "============================================================================="; //NOI18N
    private static final String ERROR = ": nothing known about "; //NOI18N
    private static final String NO_FILE = "no file"; //NOI18N
    /**
     * The event manager to use
     */
    protected EventManager eventManager;
    protected BasicCommand logCommand;
    /**
     * The log object that is currently being built
     */
    protected LogInformation logInfo;
    protected LogInformation.Revision revision;
    /**
     * The directory in which the file being processed lives. This is
     * relative to the local directory
     */
    protected String fileDirectory;
    private boolean addingSymNames;
    private boolean addingDescription;
    private boolean addingLogMessage;
    private StringBuffer tempBuffer = null;
    
    private List messageList;

    public LogBuilder(EventManager eventMan, BasicCommand command) {
        logCommand = command;
        eventManager = eventMan;
        addingSymNames = false;
        addingDescription = false;
        addingLogMessage = false;
        logInfo = null;
        revision = null;
        messageList = new ArrayList(500);
    }

    public void outputDone() {
        if (logInfo != null) {
            eventManager.fireCVSEvent(new FileInfoEvent(this, logInfo));
            logInfo = null;
            messageList = null;
        }
    }

    public void parseLine(String line, boolean isErrorMessage) {
        if (line.equals(FINAL_SPLIT)) {
            if (addingDescription) {
                addingDescription = false;
                logInfo.setDescription(tempBuffer.toString());
            }
            if (addingLogMessage) {
                addingLogMessage = false;
                revision.setMessage(CommandUtils.findUniqueString(tempBuffer.toString(), messageList));
            }
            if (revision != null) {
                logInfo.addRevision(revision);
                revision = null;
            }
            // fire the event and exit
            if (logInfo != null) {
                eventManager.fireCVSEvent(new FileInfoEvent(this, logInfo));
                logInfo = null;
                tempBuffer = null;
            }
            return;
        }
        if (addingLogMessage) {
            // first check for the branches tag
            if (line.startsWith(BRANCHES)) {
                processBranches(line.substring(BRANCHES.length()));
            }
            else {
                processLogMessage(line);
                return;
            }
        }
        if (addingSymNames) {
            processSymbolicNames(line);
        }
        if (addingDescription) {
            processDescription(line);
        }
        // revision stuff first -> will be  the most common to parse
        if (line.startsWith(REVISION)) {
            processRevisionStart(line);
        }
        if (line.startsWith(DATE)) {
            processRevisionDate(line);
        }

        if (line.startsWith(KEYWORD_SUBST)) {
            logInfo.setKeywordSubstitution(line.substring(KEYWORD_SUBST.length()).trim().intern());
            addingSymNames = false;
            return;
        }

        if (line.startsWith(DESCRIPTION)) {
            tempBuffer = new StringBuffer(line.substring(DESCRIPTION.length()));
            addingDescription = true;
        }

        if (line.indexOf(LOGGING_DIR) >= 0) {
            fileDirectory = line.substring(line.indexOf(LOGGING_DIR) + LOGGING_DIR.length()).trim();
            return;
        }
        if (line.startsWith(RCS_FILE)) {
            processRcsFile(line.substring(RCS_FILE.length()));
            return;
        }
        if (line.startsWith(WORK_FILE)) {
            processWorkingFile(line.substring(WORK_FILE.length()));
            return;
        }
        if (line.startsWith(REV_HEAD)) {
            logInfo.setHeadRevision(line.substring(REV_HEAD.length()).trim().intern());
            return;
        }
        if (line.startsWith(BRANCH)) {
            logInfo.setBranch(line.substring(BRANCH.length()).trim().intern());
        }
        if (line.startsWith(LOCKS)) {
            logInfo.setLocks(line.substring(LOCKS.length()).trim().intern());
        }
        if (line.startsWith(ACCESS_LIST)) {
            logInfo.setAccessList(line.substring(ACCESS_LIST.length()).trim().intern());
        }
        if (line.startsWith(SYM_NAME)) {
            addingSymNames = true;
        }
        if (line.startsWith(TOTAL_REV)) {
            int ind = line.indexOf(';');
            if (ind < 0) {
                // no selected revisions here..
                logInfo.setTotalRevisions(line.substring(TOTAL_REV.length()).trim().intern());
                logInfo.setSelectedRevisions("0"); //NOI18N
            }
            else {
                String total = line.substring(0, ind);
                String select = line.substring(ind, line.length());
                logInfo.setTotalRevisions(total.substring(TOTAL_REV.length()).trim().intern());
                logInfo.setSelectedRevisions(select.substring(SEL_REV.length()).trim().intern());
            }
        }
    }

    private String findUniqueString(String name, List list) {
        if (name == null) {
            return null;
        }
        int index = list.indexOf(name);
        if (index >= 0) {
            return (String)list.get(index);
        }
        else {
            String newName = name;
            list.add(newName);
            return newName;
        }
    }
    
    private void processRcsFile(String line) {
        if (logInfo != null) {
            //do fire logcreated event;
        }
        logInfo = new LogInformation();
        logInfo.setRepositoryFilename(line.trim());
    }

    private void processWorkingFile(String line) {
        String fileName = line.trim();
        if (fileName.startsWith(NO_FILE)) {
            fileName = fileName.substring(8);
        }

        logInfo.setFile(createFile(line));
    }

    private void processBranches(String line) {
        int ind = line.lastIndexOf(';');
        if (ind > 0) {
            line = line.substring(0, ind);
        }
        revision.setBranches(line.trim());
    }

    private void processLogMessage(String line) {
        if (line.startsWith(SPLITTER)) {
            addingLogMessage = false;
            revision.setMessage(findUniqueString(tempBuffer.toString(), messageList));
            return;
        }
        tempBuffer.append(line + "\n"); //NOI18N
    }

    private void processSymbolicNames(String line) {
        if (!line.startsWith(KEYWORD_SUBST)) {
            line = line.trim();
            int index = line.indexOf(':');
            if (index > 0) {
                String symName = line.substring(0, index).trim();
                String revName = line.substring(index + 1, line.length()).trim();
                logInfo.addSymbolicName(symName.intern(), revName.intern());
            }
        }
    }

    private void processDescription(String line) {
        if (line.startsWith(SPLITTER)) {
            addingDescription = false;
            logInfo.setDescription(tempBuffer.toString());
            return;
        }
        tempBuffer.append(line);
    }

    private void processRevisionStart(String line) {
        if (revision != null) {
            logInfo.addRevision(revision);
        }
        revision = logInfo.createNewRevision(
                    line.substring(REVISION.length()).intern());
    }

    private void processRevisionDate(String line) {
        StringTokenizer token = new StringTokenizer(line, ";", false); //NOI18N
        if (token.hasMoreTokens()) {
            revision.setDateString(new String(token.nextToken().substring(DATE.length())));
        }
        if (token.hasMoreTokens()) {
            revision.setAuthor(token.nextToken().substring(AUTHOR.length()).intern());
        }
        if (token.hasMoreTokens()) {
            revision.setState(token.nextToken().substring(STATE.length()).intern());
        }
        if (token.hasMoreTokens()) {
            revision.setLines(token.nextToken().substring(LINES.length()).intern());
        }
        addingLogMessage = true;
        tempBuffer = new StringBuffer();
    }

    protected File createFile(String fileName) {
        StringBuffer path = new StringBuffer();
        path.append(logCommand.getLocalDirectory());
        path.append(File.separator);
        if (fileDirectory == null) {
            // happens for single files only
            // (for directories, the dir name is always sent before the actual files)
            File locFile = logCommand.getFileEndingWith(fileName);
            if (locFile == null) {
                BugLog.getInstance().bug("JAVACVS ERROR!! wrong algorithm for assigning path to single files(1)!!"); 
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
        return new File(path.toString());
    }

    public void parseEnhancedMessage(String key, Object value) {
    }
}

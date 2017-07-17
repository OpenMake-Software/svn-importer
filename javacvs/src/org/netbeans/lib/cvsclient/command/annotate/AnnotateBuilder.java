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
package org.netbeans.lib.cvsclient.command.annotate;

import java.io.*;

import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.event.*;

/**
 * Handles the building of a annotate information object and the firing of
 * events when complete objects are built.
 *
 * @author  Milos Kleint
 */
public class AnnotateBuilder implements Builder {
    private static final String UNKNOWN = ": nothing known about";  //NOI18N
    private static final String ANNOTATING = "Annotations for ";  //NOI18N
    private static final String STARS = "***************";  //NOI18N

    /**
     * The Annotate object that is currently being built.
     */
    private AnnotateInformation annotateInformation;

    /**
     * The event manager to use.
     */
    private final EventManager eventManager;

    private final String localPath;
    private String relativeDirectory;
    private int lineNum;
    private File tempDir;

    public AnnotateBuilder(EventManager eventManager, BasicCommand annotateCommand) {
        this.eventManager = eventManager;
        this.localPath = annotateCommand.getLocalDirectory();
        tempDir = annotateCommand.getGlobalOptions().getTempDir();
    }

    public void outputDone() {
        if (annotateInformation == null) {
            return;
        }

        try {
            annotateInformation.closeTempFile();
        }
        catch (IOException exc) {
            // ignore
        }
        eventManager.fireCVSEvent(new FileInfoEvent(this, annotateInformation));
        annotateInformation = null;
    }

    public void parseLine(String line, boolean isErrorMessage) {
        if (isErrorMessage && line.startsWith(ANNOTATING)) {
            outputDone();
            annotateInformation = new AnnotateInformation(tempDir);
            annotateInformation.setFile(createFile(line.substring(ANNOTATING.length())));
            lineNum = 0;
            return;
        }

        if (isErrorMessage && line.startsWith(STARS)) {
            // skip
            return;
        }

        if (!isErrorMessage) {
            processLines(line);
        }
    }

    private File createFile(String fileName) {
        return new File(localPath, fileName);
    }

    public void parseEnhancedMessage(String key, Object value) {
    }

    private void processLines(String line) {
        if (annotateInformation != null) {
            try {
                annotateInformation.addToTempFile(line);
            }
            catch (IOException exc) {
                // just ignore, should not happen.. if it does the worst thing that happens is a annotate info without data..
            }
        }
/*
        AnnotateLine annLine = processLine(line);
        if (annotateInformation != null && annLine != null) {
            annLine.setLineNum(lineNum);
            annotateInformation.addLine(annLine);
            lineNum++;
        }
 */
    }

    public static AnnotateLine processLine(String line) {
        int indexOpeningBracket = line.indexOf('(');
        int indexClosingBracket = line.indexOf(')');
        AnnotateLine annLine = null;
        if (indexOpeningBracket > 0 && indexClosingBracket > indexOpeningBracket) {
            String revision = line.substring(0, indexOpeningBracket).trim();
            String userDate = line.substring(indexOpeningBracket + 1, indexClosingBracket);
            String contents = line.substring(indexClosingBracket + 3);
            int lastSpace = userDate.lastIndexOf(' ');
            String user = userDate;
            String date = userDate;
            if (lastSpace > 0) {
                user = userDate.substring(0, lastSpace).trim();
                date = userDate.substring(lastSpace).trim();
            }
            annLine = new AnnotateLine();
            annLine.setContent(contents);
            annLine.setAuthor(user);
            annLine.setDateString(date);
            annLine.setRevision(revision);
        }
        return annLine;
    }
}

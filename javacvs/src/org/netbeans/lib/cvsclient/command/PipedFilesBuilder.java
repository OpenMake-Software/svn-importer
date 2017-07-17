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
package org.netbeans.lib.cvsclient.command;

import java.io.*;

import org.netbeans.lib.cvsclient.event.*;

/**
 * Handles the building of "checkout with -p switch" information object and storing of
 * the checked out file to the temporary file and the firing of
 * events when complete objects are built.
 *
 * @author  Milos Kleint
 */
public class PipedFilesBuilder implements Builder {

    private static final String ERR_START = "======="; //NOI18N
    private static final String ERR_CHECK = "Checking out "; //NOI18N
    private static final String ERR_RCS = "RCS:  "; //NOI18N
    private static final String ERR_VERS = "VERS: "; //NOI18N
    private static final String EXAM_DIR = ": Updating"; //NOI18N

    /**
     * The module object that is currently being built.
     */
    private PipedFileInformation fileInformation;

    /**
     * The event manager to use.
     */
    private EventManager eventManager;

    /**
     * The directory in which the file being processed lives.
     * This is relative to the local directory.
     */
    private String fileDirectory;

    private BuildableCommand command;

    private TemporaryFileCreator tempFileCreator;

    /**
     * Creates a new Builder for the PipeFileResponse.
     */
    public PipedFilesBuilder(EventManager eventManager,
                             BuildableCommand command,
                             TemporaryFileCreator tempFileCreator) {
        this.eventManager = eventManager;
        this.command = command;
        this.tempFileCreator = tempFileCreator;
    }

    public void outputDone() {
        if (fileInformation == null) {
            return;
        }

        try {
            fileInformation.closeTempFile();
        }
        catch (IOException exc) {
            //TODO
        }
        eventManager.fireCVSEvent(new FileInfoEvent(this, fileInformation));
        fileInformation = null;
    }

    public void parseLine(String line, boolean isErrorMessage) {
        if (isErrorMessage) {
            if (line.indexOf(EXAM_DIR) >= 0) {
                fileDirectory = line.substring(line.indexOf(EXAM_DIR) + EXAM_DIR.length()).trim();
            }
            else if (line.startsWith(ERR_CHECK)) {
                processFile(line);
            }
            else if (line.startsWith(ERR_RCS)) {
                if (fileInformation != null) {
                    String repositoryName =
                            line.substring(ERR_RCS.length()).trim();
                    fileInformation.setRepositoryFileName(repositoryName);
                }
            }
            else if (line.startsWith(ERR_VERS)) {
                if (fileInformation != null) {
                    String repositoryRevision =
                            line.substring(ERR_RCS.length()).trim();
                    fileInformation.setRepositoryRevision(repositoryRevision);
                }
            }
            // header stuff..
        }
        else {
            if (fileInformation != null) {
                try {
                    fileInformation.addToTempFile(line);
                }
                catch (IOException exc) {
                    outputDone();
                }
            }
        }
    }

    private void processFile(String line) {
        outputDone();
        String filename = line.substring(ERR_CHECK.length());
        try {
            File temporaryFile = tempFileCreator.createTempFile(filename);
            fileInformation = new PipedFileInformation(temporaryFile);
        }
        catch (IOException ex) {
            fileInformation = null;
            return;
        }
        fileInformation.setFile(createFile(filename));
    }

    private File createFile(String fileName) {
        File file = new File(command.getLocalDirectory(), fileName);
        return file;
    }

    public void parseEnhancedMessage(String key, Object value) {
    }
}

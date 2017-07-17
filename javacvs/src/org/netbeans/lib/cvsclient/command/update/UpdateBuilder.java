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
package org.netbeans.lib.cvsclient.command.update;

import java.io.*;

import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.event.*;

/**
 * Handles the building of update information object and the firing of
 * events when complete objects are built.
 *
 * @author  Milos Kleint, Thomas Singer
 */
public class UpdateBuilder
        implements Builder {

    /**
     * Parsing constants..
     */
    public static final String UNKNOWN = ": nothing known about"; //NOI18N
    public static final String EXAM_DIR = ": Updating"; //NOI18N
    public static final String TO_ADD = ": use `cvs add' to create an entry for"; //NOI18N
    public static final String STATES = "U P A R M C ? "; //NOI18N
    public static final String WARNING = ": warning: "; //NOI18N
    public static final String SERVER = "server: "; //NOI18N
    public static final String PERTINENT = "is not (any longer) pertinent"; //NOI18N
    public static final String MERGING = "Merging differences between "; //NOI18N
    public static final String CONFLICTS = "rcsmerge: warning: conflicts during merge"; //NOI18N
    public static final String NOT_IN_REPOSITORY = "is no longer in the repository"; //NOI18N;
            
    /**
     * The status object that is currently being built.
     */
    private DefaultFileInfoContainer fileInfoContainer;

    /**
     * The event manager to use.
     */
    private EventManager eventManager;

    /**
     * The local path the command run in.
     */
    private String localPath;

    public UpdateBuilder(EventManager eventManager, String localPath) {
        this.eventManager = eventManager;
        this.localPath = localPath;
    }

    public void outputDone() {
        if (fileInfoContainer != null) {
            eventManager.fireCVSEvent(new FileInfoEvent(this, fileInfoContainer));
            fileInfoContainer = null;
        }
    }

    public void parseLine(String line, boolean isErrorMessage) {
        if (line.indexOf(UNKNOWN) >= 0) {
            processUnknownFile(line, line.indexOf(UNKNOWN) + UNKNOWN.length());
        }
        else if (line.indexOf(TO_ADD) >= 0) {
            processUnknownFile(line, line.indexOf(TO_ADD) + TO_ADD.length());
        }
        else if (line.indexOf(EXAM_DIR) >= 0) {
            return;
        }
        else if (line.startsWith(MERGING)) {
            outputDone();
            if (fileInfoContainer == null) {
                fileInfoContainer = new DefaultFileInfoContainer();
            }
            fileInfoContainer.setType(DefaultFileInfoContainer.MERGED_FILE);
        }
        else if (line.startsWith(CONFLICTS)) {
            if (fileInfoContainer != null) {
                fileInfoContainer.setType("C"); //NOI18N
            }
        }
        else if (line.indexOf(WARNING) >= 0) {
            if (line.indexOf(PERTINENT) > 0) {
                String filename = line.substring(line.indexOf(WARNING) + WARNING.length(),
                                                 line.indexOf(PERTINENT)).trim();
                processNotPertinent(filename);
            }
            return;
        }
        else if (line.indexOf(NOT_IN_REPOSITORY) > 0) {
            String filename = line.substring(line.indexOf(SERVER) + SERVER.length(),
                                             line.indexOf(NOT_IN_REPOSITORY)).trim();
            processNotPertinent(filename);
            return;
        }
        else {
            // otherwise
            if (line.length() > 2) {
                String firstChar = line.substring(0, 2);
                if (STATES.indexOf(firstChar) >= 0) {
                    processFile(line);
                    return;
                }
            }
        }
    }

    private File createFile(String fileName) {
        return new File(localPath, fileName);
    }

    private void ensureExistingFileInfoContainer() {
        if (fileInfoContainer != null) {
            return;
        }
        fileInfoContainer = new DefaultFileInfoContainer();
    }

    private void processUnknownFile(String line, int index) {
        outputDone();
        fileInfoContainer = new DefaultFileInfoContainer();
        fileInfoContainer.setType("?"); //NOI18N
        String fileName = (line.substring(index)).trim();
        fileInfoContainer.setFile(createFile(fileName));
    }

    private void processFile(String line) {
        String fileName = line.substring(2).trim();

        if (fileName.startsWith("no file")) { //NOI18N
            fileName = fileName.substring(8);
        }

        if (fileName.startsWith("./")) { //NOI18N
            fileName = fileName.substring(2);
        }

        File file = createFile(fileName);
        if (fileInfoContainer != null) {
            // sometimes (when locally modified.. the merged response is followed by mesage M <file> or C <file>..
            // check the file.. if equals.. it's the same one.. don't send again.. the prior type has preference
            if (fileInfoContainer.getFile() == null) {
                // is null in case the global switch -n is used - then no Enhanced message is sent, and no
                // file is assigned the merged file..
                fileInfoContainer.setFile(file);
            }
            if (file.equals(fileInfoContainer.getFile())) {
                outputDone();
                return;
            }
        }

        outputDone();
        ensureExistingFileInfoContainer();

        fileInfoContainer.setType(line.substring(0, 1));
        fileInfoContainer.setFile(file);
    }

    private void processLog(String line) {
        ensureExistingFileInfoContainer();
    }

    private void processNotPertinent(String fileName) {
        outputDone();
        File fileToDelete = createFile(fileName);

        ensureExistingFileInfoContainer();

        // HACK - will create a non-cvs status in order to be able to have consistent info format
        fileInfoContainer.setType(DefaultFileInfoContainer.PERTINENT_STATE);
        fileInfoContainer.setFile(fileToDelete);
    }

    public void parseEnhancedMessage(String key, Object value) {
        if (key.equals(EnhancedMessageEvent.MERGED_PATH)) {
            if (fileInfoContainer != null) {
                String path = value.toString();
                File newFile = new File(path);
                fileInfoContainer.setFile(newFile);
            }
        }
    }
}


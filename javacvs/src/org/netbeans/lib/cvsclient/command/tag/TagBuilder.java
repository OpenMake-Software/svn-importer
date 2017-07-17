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
package org.netbeans.lib.cvsclient.command.tag;

import java.io.*;

import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.event.*;

/**
 * @author  Thomas Singer
 */
public class TagBuilder
        implements Builder {
            
    public static final String STATES = "T D ? "; //NOI18N
    public static final String CVS_SERVER = "server: "; //NOI18N
    public static final String EXAM_DIR = "server: "; //NOI18N
            
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

    public TagBuilder(EventManager eventManager, String localPath) {
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
        if (isErrorMessage) {
            return;
        }

        if (line.indexOf(CVS_SERVER) < 0) {
            if (line.length() < 3) {
                return;
            }

            String firstChar = line.substring(0, 2);
            if (STATES.indexOf(firstChar) >= 0) {
                processFile(line);
            }
        }
    }

    private void processFile(String line) {
        if (fileInfoContainer == null) {
            fileInfoContainer = new DefaultFileInfoContainer();
        }
        fileInfoContainer.setType(line.substring(0, 1));

        String fileName = line.substring(2).trim();
        if (fileName.startsWith("no file")) { //NOI18N
            fileName = fileName.substring(8);
        }
        fileInfoContainer.setFile(createFile(fileName));
        eventManager.fireCVSEvent(new FileInfoEvent(this, fileInfoContainer));
        fileInfoContainer = null;
    }

    private File createFile(String fileName) {
        return new File(localPath, fileName);
    }

    public void parseEnhancedMessage(String key, Object value) {
    }
}

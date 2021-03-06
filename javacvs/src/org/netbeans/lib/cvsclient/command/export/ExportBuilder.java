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

package org.netbeans.lib.cvsclient.command.export;

import java.io.*;

import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.event.*;

/**
 * @author  Milos Kleint
 */
public class ExportBuilder implements Builder {

    private static final String FILE_INFOS = "MUARC?"; //NOI18N

    private final EventManager eventManager;
    private final String localPath;

    private DefaultFileInfoContainer fileInfoContainer;

    public ExportBuilder(EventManager eventManager, ExportCommand exportCommand) {
        this.eventManager = eventManager;

        this.localPath = exportCommand.getLocalDirectory();
    }

    public void outputDone() {
        if (fileInfoContainer == null) {
            return;
        }

        FileInfoEvent event = new FileInfoEvent(this, fileInfoContainer);
        eventManager.fireCVSEvent(event);

        fileInfoContainer = null;
    }

    public void parseLine(String line, boolean isErrorMessage) {
        if (line.length() > 2 && line.charAt(1) == ' ') {
            String firstChar = line.substring(0, 1);
            if (FILE_INFOS.indexOf(firstChar) >= 0) {
                String filename = line.substring(2).trim();
                processFile(firstChar, filename);
            }
            else {
                error(line);
            }
        }
    }

    public void parseEnhancedMessage(String key, Object value) {
    }

    private void error(String line) {
        System.err.println("Don't know anything about: " + line);
    }

    private void processFile(String type, String filename) {
        outputDone();

        File file = new File(localPath, filename);

        fileInfoContainer = new DefaultFileInfoContainer();
        fileInfoContainer.setType(type);
        fileInfoContainer.setFile(file);

        outputDone();
    }
}

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
package org.netbeans.lib.cvsclient.command.watchers;

import java.io.*;

import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.event.*;
import org.netbeans.lib.cvsclient.util.*;

/**
 * Handles the building of a watchers information object and the firing of
 * events when complete objects are built.
 *
 * @author  Milos Kleint
 */
public class WatchersBuilder implements Builder {

    private static final String UNKNOWN_FILE = "? "; //NOI18N

    /**
     * The status object that is currently being built.
     */
    private WatchersInformation watchersInfo;

    /**
     * The event manager to use.
     */
    private final EventManager eventManager;

    /**
     * The directory where the command was executed.
     * Used to compute absolute path to the file.
     */
    private final String localPath;

    /**
     * Creates a WatchersBuilder.
     * @param eventManager the event manager that will fire events.
     * @param localPath absolute path to the directory where the command was executed.
     */
    public WatchersBuilder(EventManager eventManager, String localPath) {
        this.eventManager = eventManager;
        this.localPath = localPath;
    }

    public void outputDone() {
        if (watchersInfo != null) {
            eventManager.fireCVSEvent(new FileInfoEvent(this, watchersInfo));
            watchersInfo = null;
        }
    }

    public void parseLine(String line, boolean isErrorMessage) {
        if (line.startsWith(UNKNOWN_FILE)) {
            File file = new File(localPath, line.substring(UNKNOWN_FILE.length()));
            watchersInfo = new WatchersInformation(file);
            outputDone();
            return;
        }

        if (isErrorMessage) {
            return;
        }

        if (line.startsWith(" ") || line.startsWith("\t")) { // NOI18N
            BugLog.getInstance().assertNotNull(watchersInfo);

            watchersInfo.addWatcher(line);
            return;
        }

        // the line starts with file..
        outputDone();
        String trimmedLine = line.trim().replace('\t', ' ');
        int spaceIndex = trimmedLine.indexOf(' ');

        BugLog.getInstance().assertTrue(spaceIndex > 0, "Wrong line = " + line);

        File file = new File(localPath,
                             trimmedLine.substring(0, spaceIndex));
        String watcher = trimmedLine.substring(spaceIndex + 1);
        watchersInfo = new WatchersInformation(file);
        watchersInfo.addWatcher(watcher);
    }

    public void parseEnhancedMessage(String key, Object value) {
    }
}

/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.cvsclient.command.watchers;

import java.io.*;
import java.util.*;

import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.util.*;

/**
 * Describes "cvs watchers" commands' parsed information for a file.
 * The fields in instances of this object are populated
 * by response handlers.
 *
 * @author  Milos Kleint
 */
public class WatchersInformation extends FileInfoContainer {

    public static final String WATCH_EDIT = "edit"; //NOI18N
    public static final String WATCH_UNEDIT = "unedit"; //NOI18N
    public static final String WATCH_COMMIT = "commit"; //NOI18N
    public static final String WATCH_TEMP_EDIT = "tedit"; //NOI18N
    public static final String WATCH_TEMP_UNEDIT = "tunedit"; //NOI18N
    public static final String WATCH_TEMP_COMMIT = "tcommit"; //NOI18N

    /**
     * Holds the file that this info belongs to.
     */
    private final File file;

    /**
     * List of users (Watchers instances) that are listening
     * on events for this file.
     */
    private final List userList = new LinkedList();

    /**
     * Creates new istance of the WatchersInformation class.
     */
    public WatchersInformation(File file) {
        this.file = file;
    }

    /**
     * Getter for file concerned in this instance.
     */
    public File getFile() {
        return file;
    }

    /**
     * Adds a watcher to the watchers list.
     * @param watchingInfo a String that's first word is a user name and the
     *                     rest are watching types.
     */
    void addWatcher(String watchingInfo) {
        String temp = watchingInfo.trim();
        temp = temp.replace('\t', ' ');
        int spaceIndex = temp.indexOf(' ');
        if (spaceIndex < 0) {
            //BUGLOG assert.
        }
        else {
            String user = temp.substring(0, spaceIndex);
            String watches = temp.substring(spaceIndex + 1);
            this.userList.add(new WatchersInformation.Watcher(user, watches));
        }
    }

    /**
     * Returns the Iterator with WatchersInformation.Watcher instances.
     * Never returns null.
     */
    public Iterator getWatchersIterator() {
        return this.userList.iterator();
    }

    /**
     * Inner class that holds information about single user and his watches
     * on the file.
     */
    public static class Watcher {

        private final String userName;
        private final String watches;
        private boolean watchingEdit;
        private boolean watchingUnedit;
        private boolean watchingCommit;
        private boolean temporaryEdit;
        private boolean temporaryUnedit;
        private boolean temporaryCommit;

        /**
         * Package private constuctor that creates a new instance of the Watcher.
         * To Be called from outerclass only.
         */
        Watcher(String userName, String watches) {
            this.userName = userName;
            this.watches = watches;

            final StringTokenizer tok = new StringTokenizer(watches, " ", false);
            while (tok.hasMoreTokens()) {
                String token = tok.nextToken();
                if (WATCH_EDIT.equals(token)) {
                    watchingEdit = true;
                }
                else if (WATCH_UNEDIT.equals(token)) {
                    watchingUnedit = true;
                }
                else if (WATCH_COMMIT.equals(token)) {
                    watchingCommit = true;
                }
                else if (WATCH_TEMP_COMMIT.equals(token)) {
                    temporaryCommit = true;
                }
                else if (WATCH_TEMP_EDIT.equals(token)) {
                    temporaryEdit = true;
                }
                else if (WATCH_TEMP_UNEDIT.equals(token)) {
                    temporaryUnedit = true;
                }
                else {
                    BugLog.getInstance().bug("unknown = " + token);
                }
            }
        }

        /**
         * Gets the user that is watching the file.
         */
        public String getUserName() {
            return userName;
        }

        /**
         * Returns all the watches defined on the file.
         */
        public String getWatches() {
            return watches;
        }

        /**
         * User is/isn't watching commit opration.
         */
        public boolean isWatchingCommit() {
            return watchingCommit;
        }

        /**
         * User is/isn't watching edit opration.
         */
        public boolean isWatchingEdit() {
            return watchingEdit;
        }

        /**
         * User is/isn't watching unedit opration.
         */
        public boolean isWatchingUnedit() {
            return watchingUnedit;
        }

        /**
         * User is/isn't temporary watching commit opration.
         */
        public boolean isTempWatchingCommit() {
            return temporaryCommit;
        }

        /**
         * User is/isn't temporary watching edit opration.
         */
        public boolean isTempWatchingEdit() {
            return temporaryEdit;
        }

        /**
         * User is/isn't temporary watching unedit opration.
         */
        public boolean isTempWatchingUnedit() {
            return temporaryUnedit;
        }
    }
}

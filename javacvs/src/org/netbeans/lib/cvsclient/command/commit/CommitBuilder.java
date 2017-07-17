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

package org.netbeans.lib.cvsclient.command.commit;

import java.io.*;

import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.event.*;

/**
 * Handles the building of update information object and the firing of
 * events when complete objects are built.
 *
 * @author  Milos Kleint
 */
public class CommitBuilder
        implements Builder {
            
    /**
     * Parsing constants.
     */           
    public static final String UNKNOWN = "commit: nothing known about `"; //NOI18N
    public static final String EXAM_DIR = ": Examining"; //NOI18N
    public static final String REMOVING = "Removing "; //NOI18N
    public static final String NEW_REVISION = "new revision:"; //NOI18N
    public static final String INITIAL_REVISION = "initial revision:"; //NOI18N
    public static final String DELETED_REVISION = "delete"; //NOI18N
    public static final String DONE = "done"; //NOI18N
    public static final String RCS_FILE = "RCS file: "; //NOI18N
    public static final String ADD = "commit: use `cvs add' to create an entry for "; //NOI18N
    public static final String COMMITTED = " <-- "; // NOI18N

    /**
     * The status object that is currently being built.
     */
    private CommitInformation commitInformation;

    /**
     * The directory in which the file being processed lives. This is
     * absolute inside the local directory
     */
    private File fileDirectory;

    /**
     * The event manager to use.
     */
    private final EventManager eventManager;

    private final String localPath;
    
    private final String repositoryRoot;

    private boolean isAdding;

    public CommitBuilder(EventManager eventManager, String localPath, String repositoryRoot) {
        this.eventManager = eventManager;
        this.localPath = localPath;
        this.repositoryRoot = repositoryRoot;
    }

    public void outputDone() {
        if (commitInformation != null) {
            eventManager.fireCVSEvent(new FileInfoEvent(this, commitInformation));
            commitInformation = null;
        }
    }

    public void parseLine(String line, boolean isErrorMessage) {
        int c;
        if (line.indexOf(UNKNOWN) >= 0) {
            outputDone();
            processUnknownFile(line.substring(line.indexOf(UNKNOWN) + UNKNOWN.length()).trim());
        }
        else if (line.indexOf(ADD) > 0) {
            processToAddFile(line.substring(line.indexOf(ADD) + ADD.length()).trim());
        }
        else if ((c = line.indexOf(COMMITTED)) > 0) {
            outputDone();
            String fileName = line.substring(c + COMMITTED.length()).trim();
            File file;
            if (fileDirectory == null) {
                String reposPath = line.substring(0, c).trim();
                if (reposPath.startsWith(repositoryRoot)) {
                    reposPath = reposPath.substring(repositoryRoot.length());
                    if (reposPath.startsWith("/")) reposPath = reposPath.substring(1);
                }
                c = reposPath.lastIndexOf('/');
                if (c > 0) reposPath = reposPath.substring(0, c); // remove the file name
                file = findFile(fileName, reposPath);
            } else {
                file = new File(fileDirectory, fileName);
            }
            processFile(file);
            if (isAdding) {
                commitInformation.setType(commitInformation.ADDED);
                isAdding = false;
            }
            else {
                commitInformation.setType(commitInformation.CHANGED);
            }
        }
        else if (line.startsWith(REMOVING)) {
            outputDone();
            processFile(line.substring(REMOVING.length(), line.length() - 1));
            // - 1 means to cut the ';' character
            commitInformation.setType(commitInformation.REMOVED);
        }
        else if (line.indexOf(EXAM_DIR) >= 0) {
            fileDirectory = new File(localPath, line.substring(line.indexOf(EXAM_DIR) + EXAM_DIR.length()).trim());
        }
        else if (line.startsWith(RCS_FILE)) {
            isAdding = true;
        }
        else if (line.startsWith(DONE)) {
            outputDone();
        }
        else if (line.startsWith(INITIAL_REVISION)) {
            processRevision(line.substring(INITIAL_REVISION.length()));
            commitInformation.setType(commitInformation.ADDED);
        }
        else if (line.startsWith(NEW_REVISION)) {
            processRevision(line.substring(NEW_REVISION.length()));
        }
    }

    private File createFile(String fileName) {
        return new File(localPath, fileName);
    }

    private void processUnknownFile(String line) {
        commitInformation = new CommitInformation();
        commitInformation.setType(commitInformation.UNKNOWN);
        int index = line.indexOf('\'');
        String fileName = line.substring(0, index).trim();
        commitInformation.setFile(createFile(fileName));
        outputDone();
    }

    private void processToAddFile(String line) {
        commitInformation = new CommitInformation();
        commitInformation.setType(commitInformation.TO_ADD);
        String fileName = line.trim();
        if (fileName.endsWith(";")) { //NOI18N
            fileName = fileName.substring(0, fileName.length() - 2);
        }
        commitInformation.setFile(createFile(fileName));
        outputDone();
    }

    private void processFile(String filename) {
        if (commitInformation == null) {
            commitInformation = new CommitInformation();
        }

        if (filename.startsWith("no file")) { //NOI18N
            filename = filename.substring(8);
        }
        commitInformation.setFile(createFile(filename));
    }

    private void processFile(File file) {
        if (commitInformation == null) {
            commitInformation = new CommitInformation();
        }

        commitInformation.setFile(file);
    }

    private void processRevision(String revision) {
        int index = revision.indexOf(';');
        if (index >= 0) {
            revision = revision.substring(0, index);
        }
        revision = revision.trim();
        if (DELETED_REVISION.equals(revision)) {
            commitInformation.setType(commitInformation.REMOVED);
        }
        commitInformation.setRevision(revision);
    }

    public void parseEnhancedMessage(String key, Object value) {
    }
    
    private File findFile(String fileName, String reposPath) {
        File dir = new File(localPath);
        return findFile(dir, fileName, reposPath);
    }
    
    private File findFile(File dir, String fileName, String reposPath) {
        if (isWorkForRepository(dir, reposPath)) {
            return new File(dir, fileName);
        } else {
            File file = null;
            File[] subFiles = dir.listFiles();
            if (subFiles != null) {
                for (int i = 0; i < subFiles.length; i++) {
                    if (subFiles[i].isDirectory()) {
                        file = findFile(subFiles[i], fileName, reposPath);
                        if (file != null) break;
                    }
                }
            }
            return file;
        }
    }
    
    private static boolean isWorkForRepository(File dir, String reposPath) {
        File reposFile = new File(dir, "CVS/Repository");
        if (!reposFile.exists()) return false;
        BufferedReader r = null;
        try {
            r = new BufferedReader(new FileReader(reposFile));
            String repos = r.readLine();
            System.out.println("  '"+reposPath+"' == '"+repos+"': "+reposPath.equals(repos));
            return reposPath.equals(repos);
        } catch (IOException ioex) {
            // give up
            return false;
        } finally {
            if (r != null) {
                try {
                    r.close();
                } catch (IOException ioex) {}
            }
        }
    }
}

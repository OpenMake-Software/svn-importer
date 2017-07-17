/*****************************************************************************
 * Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is available at http://www.sun.com/
 *
 * The Original Code is the CVS Client Library.
 * The Initial Developer of the Original Code is Robert Greig.
 * Portions created by Robert Greig are Copyright (C) 2000.
 * All Rights Reserved.
 *
 * Contributor(s): Robert Greig.
 *****************************************************************************/
package org.netbeans.lib.cvsclient.admin;

import java.io.*;
import java.util.*;

import org.netbeans.lib.cvsclient.command.*;

/**
 * Handles the maintaining and reading of administration information on the
 * local machine. The standard CVS client does this by putting various files in
 * a CVS directory underneath each checked-out directory. How the files are
 * laid out and managed is not specified by the protocol document. <P>Hence it
 * is envisaged that, eventually, a client could add additional files for
 * higher performance or even change the mechanism for storing the information
 * completely.
 * @author  Robert Greig
 */
public interface AdminHandler {
    /**
     * Create or update the administration files for a particular file.
     * This will create the CVS directory if necessary, and the
     * Root and Repository files if necessary. It will also update
     * the Entries file with the new entry
     * @param localDirectory the local directory where the file in question
     * lives (the absolute path). Must not end with a slash.
     * @param repositoryPath the path of the file in the repository
     * @param entry the entry object for that file
     * @param globalOptions the global command options
     */
    void updateAdminData(String localDirectory, String repositoryPath,
                         Entry entry, GlobalOptions globalOptions)
            throws IOException;

    /**
     * Get the Entry for the specified file, if one exists
     * @param file the file
     * @throws IOException if the Entries file cannot be read
     */
    Entry getEntry(File file) throws IOException;

    /**
     * Get the entries for a specified directory.
     * @param directory the directory for which to get the entries
     * @return an iterator of Entry objects
     */
    Iterator getEntries(File directory) throws IOException;

    /**
     * Set the Entry for the specified file
     * @param file the file
     * @param entry the new entry
     * @throws IOException if an error occurs writing the details
     */
    void setEntry(File file, Entry entry) throws IOException;

    /**
     * Get the repository path for a given directory, for example in
     * the directory /home/project/foo/bar, the repository directory
     * might be /usr/cvs/foo/bar. The repository directory is commonly
     * stored in the file <pre>Repository</pre> in the CVS directory on
     * the client. (This is the case in the standard CVS command-line tool)
     * @param directory the directory
     * @param the repository path on the server, e.g. /home/bob/cvs. Must not
     * end with a slash.
     */
    String getRepositoryForDirectory(String directory, String repository)
            throws IOException;

    /**
     * Remove the Entry for the specified file
     * @param file the file whose entry is to be removed
     * @throws IOException if an error occurs writing the Entries file
     */
    void removeEntry(File file) throws IOException;

    /**
     * Get all the files contained within a given
     * directory that are <b>known to CVS</b>.
     * @param directory the directory to look in
     * @return a set of all files.
     */
    Set getAllFiles(File directory) throws IOException;

    /**
     * Checks for presence of CVS/Tag file and returns it's value.
     * @returns the value of CVS/Tag file for the specified directory
     *          null if file doesn't exist
     */
    String getStickyTagForDirectory(File directory);
}
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
package org.netbeans.lib.cvsclient;

import java.io.*;
import java.util.*;

import org.netbeans.lib.cvsclient.admin.*;
import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.connection.*;
import org.netbeans.lib.cvsclient.file.*;
import org.netbeans.lib.cvsclient.request.*;
import org.netbeans.lib.cvsclient.response.*;
import org.netbeans.lib.cvsclient.util.*;

/**
 * Clients that provide the ability to execute commands must implement this
 * interface. All commands use this interface to get details about the
 * environment in which it is being run, and to perform administrative
 * functions such as obtaining Entry lines for specified files.
 * @author  Robert Greig
 */
public interface ClientServices {
    /**
     * Process all the requests.
     *
     * @param requests the requets to process
     */
    void processRequests(List requests) throws IOException,
            UnconfiguredRequestException, ResponseException,
            CommandAbortedException;

    /**
     * Get the repository used for this connection.
     *
     * @return the repository, for example /home/bob/cvs
     */
    String getRepository();

    /**
     * Get the repository path for a given directory, for example in
     * the directory /home/project/foo/bar, the repository directory
     * might be /usr/cvs/foo/bar. The repository directory is commonly
     * stored in the file <pre>Repository</pre> in the CVS directory on
     * the client. (This is the case in the standard CVS command-line tool)
     *
     * @param directory the directory
     */
    String getRepositoryForDirectory(String directory)
            throws IOException;

    /**
     * Get the local path that the command is executing in.
     *
     * @return the local path
     */
    String getLocalPath();

    /**
     * Get the Entry for the specified file, if one exists.
     *
     * @param file the file
     *
     * @throws IOException if the Entries file cannot be read
     */
    Entry getEntry(File file) throws IOException;

    /**
     * Get the entries for a specified directory.
     *
     * @param directory the directory for which to get the entries
     *
     * @return an iterator of Entry objects
     */
    Iterator getEntries(File directory) throws IOException;

    /**
     * Create or update the administration files for a particular file
     * This will create the CVS directory if necessary, and the
     * Root and Repository files if necessary. It will also update
     * the Entries file with the new entry
     *
     * @param localDirectory the local directory, relative to the directory
     *                       in which the command was given, where the file in
     *                       question lives
     * @param entry the entry object for that file
     *
     * @throws IOException if there is an error writing the files
     */
    void updateAdminData(String localDirectory, String repositoryPath,
                         Entry entry)
            throws IOException;

    /**
     * Get all the files contained within a given
     * directory that are <b>known to CVS</b>.
     *
     * @param directory the directory to look in
     *
     * @return a set of all files.
     */
    Set getAllFiles(File directory) throws IOException;

    /**
     * Returns true if no command was sent before.
     * This is used, because the server rejects some doubled commands.
     */
    boolean isFirstCommand();

    /**
     * Set whether this is the first command. Normally you do not need to set
     * this yourself - after execution the first command will have set this to
     * false.
     */
    void setIsFirstCommand(boolean first);

    /**
     * Removes the Entry for the specified file.
     */
    void removeEntry(File file) throws IOException;

    /**
     * Sets the specified IgnoreFileFilter to use to ignore non-cvs files.
     * TS, 2001-11-23: really needed in the interface (it's never used)?
     */
    void setIgnoreFileFilter(IgnoreFileFilter filter);

    /**
     * Returns the IgnoreFileFilter used to ignore non-cvs files.
     * TS, 2001-11-23: really needed in the interface (it's never used)?
     */
    IgnoreFileFilter getIgnoreFileFilter();

    /**
     * Returnes true to indicate, that the file specified by directory and nonCvsFile
     * should be ignored.
     */
    boolean shouldBeIgnored(File directory, String nonCvsFile);

    //
    // allow the user of the Client to define the FileHandlers
    //

    /**
     * Set the uncompressed file handler.
     */
    void setUncompressedFileHandler(FileHandler handler);

    /**
     * Set the handler for Gzip data.
     */
    void setGzipFileHandler(FileHandler handler);

    /**
     * Checks for presence of CVS/Tag file and returns it's value.
     *
     * @returns the value of CVS/Tag file for the specified directory
     *          null if file doesn't exist
     */
    String getStickyTagForDirectory(File directory);

    /**
     * Ensures, that the connection is open.
     *
     * @throws AuthenticationException if it wasn't possible to connect
     */
    void ensureConnection() throws AuthenticationException;
    
    /**
     * Returns the wrappers map associated with the CVS server
     * The map is valid only after the connection is established
     */
    Map getWrappersMap() throws CommandException;
    
    /**
     * Get the global options that are set to this client.
     * Individual commands can get the global options via this method.
     */
    GlobalOptions getGlobalOptions();

}

/*****************************************************************************
 * Sun Public License Notice
 *
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
package org.netbeans.lib.cvsclient.command;

import java.io.*;
import java.util.*;

import org.netbeans.lib.cvsclient.*;
import org.netbeans.lib.cvsclient.admin.*;
import org.netbeans.lib.cvsclient.connection.*;
import org.netbeans.lib.cvsclient.event.*;
import org.netbeans.lib.cvsclient.request.*;

/**
 * A class that provides common functionality for many of the CVS command
 * that send similar sequences of requests.
 * @author  Robert Greig
 */
public abstract class BasicCommand extends BuildableCommand {
    /**
     * The requests that are sent and processed.
     */
    protected List requests = new LinkedList();

    /**
     * The client services that are provided to this command.
     */
    protected ClientServices clientServices;

    /**
     * Whether to update recursively.
     */
    private boolean recursive = true;

    /**
     * The files and/or directories to operate on.
     */
    protected File[] files;

    /**
     * Gets the value of the recursive option.
     * @return true if recursive, false if not
     * @deprecated use isRecursive instead
     */
    public boolean getRecursive() {
        return recursive;
    }

    /**
     * Gets the value of the recursive option.
     * @return true if recursive, false if not
     */
    public boolean isRecursive() {
        return recursive;
    }

    /**
     * Sets the value of the recursive option.
     * @param r true if the command should recurse, false otherwise
     */
    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    /**
     * Set the files and/or directories on which to execute the command.
     * The way these are processed is:<P>
     * <UL><LI>Default action (i.e. not setting the files explicitly or
     * setting them to <pre>null</pre>) is to use the directory in which
     * the command was executed (see how directories are treated, below)</LI>
     * <LI>Files are handled how you would expect</LI>
     * <LI>For directories, all files within the directory are sent</LI></UL>
     * @param theFiles the files to operate on. May be null to indicate that the
     * local directory specified in the client should be used. Full, absolute
     * canonical pathnames <b>must</b> be supplied.
     */
    public void setFiles(File[] theFiles) {
        // sort array.. files first, directories follow
        if (theFiles == null) {
            files = theFiles;
            return;
        }

        files = new File[theFiles.length];
        int fileCount = 0;
        int dirCount = 0;
        int totalCount = theFiles.length;
        for (int index = 0; index < totalCount; index++) {
            File currentFile = theFiles[index];
            if (currentFile.isDirectory()) {
                files[totalCount - (1 + dirCount)] = currentFile;
                dirCount = dirCount + 1;
            }
            else {
                files[fileCount] = currentFile;
                fileCount = fileCount + 1;
            }
        }
    }

    /**
     * Get the files and/or directories specified for this command to operate
     * on.
     * @return the array of Files
     */
    public File[] getFiles() {
        return files;
    }

    /**
     * Get a single file from the "files" list. returns only files, not directories.
     * This method is used from within the builders, because for single file requests, the
     * cvs server doesn't return us enough information to identify what file has been returned.
     * Thus we sort the "files" array (files come before directories. Then the response froms erver comes in the same order
     * and the files can be found this way.
     *
     * @param index the index of the file in the list.
     */
    public File getXthFile(int index) {
        if (index < 0 || index >= files.length) {
            return null;
        }
        File file = files[index];
        if (!file.isFile()) {
            return null;
        }
        return file;
    }

    /**
     * @param ending - the ending part of the file's pathname.. path separator is cvs's default '/'
     */
    public File getFileEndingWith(String ending) {
        String locEnding = ending.replace('\\', '/');
        String localDir = getLocalDirectory().replace('\\','/');
        int index = 0;
        for (index = 0; index < files.length; index++) {
            String path = files[index].getAbsolutePath();
            String parentPath = files[index].getParentFile().getAbsolutePath().replace('\\', '/');
            path = path.replace('\\', '/');
            if ((path.endsWith(locEnding) && locEnding.indexOf('/') >= 0) || 
                   (files[index].getName().equals(locEnding) && parentPath.equals(localDir))) {
                return files[index];
            }
        }
        return null;
    }


    /**
     * Add the appropriate requests for a specified path. For a directory,
     * process all the files within that directory and for a single file,
     * just send it. For each directory, send a directory request. For each
     * file send an Entry request followed by a Modified request.
     * @param path the particular path to issue requests for. May be
     * either a file or a directory.
     */
    private void addRequests(File path)
            throws FileNotFoundException, IOException {
        if (path == null) {
            throw new IllegalArgumentException("Cannot add requests for a " +
                                               "null path.");
        }

        if (!path.exists() || path.isFile()) {
            addRequestsForFile(path);
        }
        else {
            addRequestsForDirectory(path);
        }
    }

    /**
     * Should return true if unchanged files should not be sent to server.
     * If false is returned, all files will be sent to server
     * This method is used by <code>sendEntryAndModifiedRequests</code>.
     */
    protected boolean doesCheckFileTime() {
        return true;
    }

    /**
     * Send an Entry followed by a Modified or Unchanged request based on
     * whether the file has been untouched on the local machine.
     *
     * @param entry         the entry for the file
     * @param file          the file in question
     */
    protected void sendEntryAndModifiedRequests(Entry entry,
                                                File file) {
        if (entry == null) {
            return;
        }

        // for deleted added files, don't send anything..
        if (file != null && !file.exists() && entry.isNewUserFile()) {
            return;
        }

        Date entryLastModified = entry.getLastModified();
        boolean hadConflicts = entry.hadConflicts();
        if (!hadConflicts) {
            // we null out the conflict field if there is no conflict
            // because we use that field to store the timestamp of the
            // file (just like command-line CVS). There is no point
            // in sending this information to the CVS server, even
            // though it should be ignored by the server.
            entry.setConflict(null);
        }
        addRequest(new EntryRequest(entry));

        if (file == null || !file.exists() || entry.isUserFileToBeRemoved()) {
            return;
        }

        if (doesCheckFileTime() && !hadConflicts && entryLastModified != null) {
            if (DateComparator.getInstance().equals(file.lastModified(),
                                                    entryLastModified.getTime())) {
                addRequest(new UnchangedRequest(file.getName()));
                return;
            }
        }

        addRequest(new ModifiedRequest(file, entry.isBinary()));
    }

    /**
     * Adds the appropriate requests for a given directory. Sends a
     * directory request followed by as many Entry and Modified requests
     * as required
     * @param directory the directory to send requests for
     * @throws IOException if an error occurs constructing the requests
     */
    protected void addRequestsForDirectory(File directory)
            throws IOException {
        if (!directory.exists()) {
            return;
        }

        addDirectoryRequest(directory);

        List localFiles = createLocalFileList(directory);

        List subDirectories = null;
        if (isRecursive()) {
            subDirectories = new LinkedList();
        }

        // get all the entries we know about, and process them
        for (Iterator it = clientServices.getEntries(directory); it.hasNext();) {
            final Entry entry = (Entry)it.next();
            if (entry.isDirectory()) {
                if (isRecursive()) {
                    subDirectories.add(new File(directory, entry.getName()));
                }
            }
            else {
                final File file = new File(directory, entry.getName());
                addRequestForFile(file, entry);
                localFiles.remove(file);
            }
        }
        
        // In case that CVS folder does not exist, we need to process all
        // directories that have CVS subfolders:
        if (isRecursive() && !new File(directory, "CVS").exists()) {
            File[] subFiles = directory.listFiles();
            if (subFiles != null) {
                for (int i = 0; i < subFiles.length; i++) {
                    if (subFiles[i].isDirectory() && new File(subFiles[i], "CVS").exists()) {
                        subDirectories.add(subFiles[i]);
                    }
                }
            }
        }

        for (Iterator it = localFiles.iterator(); it.hasNext();) {
            String localFileName = ((File)it.next()).getName();
            if (!clientServices.shouldBeIgnored(directory, localFileName)) {
                addRequest(new QuestionableRequest(localFileName));
            }
        }

        if (isRecursive()) {
            for (Iterator it = subDirectories.iterator(); it.hasNext();) {
                File subdirectory = (File)it.next();
                File cvsSubDir = new File(subdirectory, "CVS"); //NOI18N
                if (cvsSubDir.exists()) {
                    addRequestsForDirectory(subdirectory);
                }
            }
        }
    }

    private List createLocalFileList(File directory) {
        List localFiles = new LinkedList();
        File[] files = directory.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (!files[i].isDirectory()) {
                    localFiles.add(files[i]);
                }
            }
        }
        return localFiles;
    }

    /**
     * This method is called for each explicit file and for files within a
     * directory.
     */
    protected void addRequestForFile(File file, Entry entry) {
        sendEntryAndModifiedRequests(entry, file);
    }

    /**
     * Add the appropriate requests for a single file. A directory request
     * is sent, followed by an Entry and Modified request
     * @param file the file to send requests for
     * @throws IOException if an error occurs constructing the requests
     */
    protected void addRequestsForFile(File file) throws IOException {
        addDirectoryRequest(file.getParentFile());

        try {
            final Entry entry = clientServices.getEntry(file);
            // a non-null entry means the file does exist in the
            // Entries file for this directory
            if (entry != null) {
                addRequestForFile(file, entry);
            }
        }
        catch (IOException ex) {
            System.err.println("An error occurred getting the Entry " +
                               "for file " + file + ": " + ex);
            ex.printStackTrace();
        }
    }

    /**
     * Adds a DirectoryRequest (and maybe a StickyRequest) to the request list.
     */
    protected final void addDirectoryRequest(File directory) {
        // remove localPath prefix from directory. If left with
        // nothing, use dot (".") in the directory request
        String dir = getRelativeToLocalPathInUnixStyle(directory);

        try {
            String repository = clientServices.getRepositoryForDirectory(
                    directory.getAbsolutePath());
            addRequest(new DirectoryRequest(dir, repository));
            String tag = clientServices.getStickyTagForDirectory(directory);
            if (tag != null) {
                addRequest(new StickyRequest(tag));
            }
        }
        catch (FileNotFoundException ex) {
            // we can ignore this exception safely because it just means
            // that the user has deleted a directory referenced in a
            // CVS/Entries file
        }
        catch (IOException ex) {
            System.err.println("An error occurred reading the respository " +
                               "for the directory " + dir + ": " + ex);
            ex.printStackTrace();
        }
    }

    /**
     * Add the argument requests. The argument requests are created using
     * the original set of files/directories passed in. Subclasses of this
     * class should call this method at the appropriate point in their
     * execute() method. Note that arguments are appended to the list.
     */
    protected void addArgumentRequests() {
        if (files == null) {
            return;
        }

        for (int i = 0; i < files.length; i++) {
            final File file = files[i];
            String relativePath = getRelativeToLocalPathInUnixStyle(file);
            addRequest(new ArgumentRequest(relativePath));
        }
    }

    /**
     * Execute a command. This implementation sends a Root request, followed
     * by as many Directory and Entry requests as is required by the recurse
     * setting and the file arguments that have been set. Subclasses should
     * call this first, and tag on the end of the requests list any further
     * requests and, finally, the actually request that does the command (e.g.
     * <pre>update</pre>, <pre>status</pre> etc.)
     * @param client the client services object that provides any necessary
     * services to this command, including the ability to actually process
     * all the requests
     * @throws CommandException if an error occurs executing the command
     */
    public void execute(ClientServices client, EventManager em)
            throws CommandException, AuthenticationException {
        requests.clear();
        clientServices = client;
        super.execute(client, em);

        if (client.isFirstCommand()) {
            addRequest(new RootRequest(client.getRepository()));
        }

        addFileRequests();
    }

    private void addFileRequests() throws CommandException {
        try {
            if (files != null && files.length > 0) {
                for (int i = 0; i < files.length; i++) {
                    addRequests(files[i]);
                }
            }
            else {
                // if no arguments have been specified, then specify the
                // local directory - the "top level" for this command
                if (assumeLocalPathWhenUnspecified()) {
                    addRequests(new File(getLocalDirectory()));
                }
            }
        }
        catch (Exception ex) {
            throw new CommandException(ex, ex.getLocalizedMessage());
        }
    }

    /**
     * The result from this command is used only when the getFiles() returns null or empty array.
     * in such a case and when this method returns true, it is assumed the localpath should be taken
     * as the 'default' file for the building of requests.
     * Generally assumed to be true. Can be overriden by subclasses. However make sure you know what you are doing. :)
     */
    protected boolean assumeLocalPathWhenUnspecified() {
        return true;
    }

    /**
     * Adds the specified request to the request list.
     */
    protected final void addRequest(Request request) {
        requests.add(request);
    }

    /**
     * Adds the request for the current working directory.
     */
    protected final void addRequestForWorkingDirectory(ClientServices clientServices)
            throws IOException {
        addRequest(new DirectoryRequest(".", //NOI18N
                                        clientServices.getRepositoryForDirectory(getLocalDirectory())));
    }

    /**
     * If the specified value is true, add a ArgumentRequest for the specified
     * argument.
     */
    protected final void addArgumentRequest(boolean value, String argument) {
        if (!value) {
            return;
        }

        addRequest(new ArgumentRequest(argument));
    }

    /**
     * Appends the file's names to the specified buffer.
     */
    protected final void appendFileArguments(StringBuffer buffer) {
        File[] files = getFiles();
        if (files == null) {
            return;
        }

        for (int index = 0; index < files.length; index++) {
            if (index > 0) {
                buffer.append(' ');
            }
            buffer.append(files[index].getName());
        }
    }
}


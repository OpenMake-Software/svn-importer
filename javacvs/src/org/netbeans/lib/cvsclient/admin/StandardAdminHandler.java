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
 * A handler for administrative information that maintains full compatibility
 * with the one employed by the original C implementation of a CVS client.
 * <p>This implementation strives to provide complete compatibility with
 * the standard CVS client, so that operations on locally checked-out
 * files can be carried out by either this library or the standard client
 * without causing the other to fail. Any such failure should be considered
 * a bug in this library.
 * @author  Robert Greig
 */
public class StandardAdminHandler implements AdminHandler {
    /**
     * Create or update the administration files for a particular file.
     * This will create the CVS directory if necessary, and the
     * Root and Repository files if necessary. It will also update
     * the Entries file with the new entry
     * @param localDirectory the local directory where the file in question
     * lives (the absolute path). Must not end with a slash.
     * @param entry the entry object for that file. If null, there is no
     * entry to add, and the Entries file will not have any entries added to
     * it (it will be created if it does not exist, however).
     */
    public void updateAdminData(String localDirectory, String repositoryPath,
                                Entry entry, GlobalOptions globalOptions)
            throws IOException {
        // add this directory to the list of those to check for emptiness if
        // the prune option is specified

        final File CVSdir = new File(localDirectory, "CVS"); //NOI18N

        CVSdir.mkdirs();

        // now ensure that the Root and Repository files exist
        File rootFile = new File(CVSdir, "Root"); //NOI18N
        if (!rootFile.exists()) {
            final Writer w = new BufferedWriter(new FileWriter(rootFile));
            try {
                w.write(globalOptions.getCVSRoot());
            } finally {
                w.close();
            }
        }
        File repositoryFile = new File(CVSdir, "Repository"); //NOI18N
        if (!repositoryFile.exists()) {
            final Writer w = new BufferedWriter(new FileWriter(repositoryFile));
            try {
                if (entry != null && !entry.isDirectory()) {
                    // If there is a file entry, the repository path is for a file!
                    int length = entry.getName().length();
                    repositoryPath = repositoryPath.substring(0, repositoryPath.length() - length);
                }
                if (repositoryPath.endsWith("/")) { //NOI18N
                    repositoryPath = repositoryPath.substring(0,
                                                              repositoryPath.length() - 1);
                }
                if (repositoryPath.length() == 0) {
                    repositoryPath = "."; //NOI18N
                }
                // we write out the relative path to the repository file
                w.write(repositoryPath);
            } finally {
                w.close();
            }
        }

        final File entriesFile = new File(CVSdir, "Entries"); //NOI18N

        // We assume that if we do not have an Entries file, we need to add
        // the file to the parent CVS directory as well as create the
        // Entries file for this directory
        if (entriesFile.createNewFile()) {
            // need to know if we had to create any directories so that we can
            // update the CVS/Entries file in the *parent* director
            addDirectoryToParentEntriesFile(CVSdir);

            // We have created a new Entries file, so put a D in it to
            // indicate that we understand directories.
            // TODO: investigate what the point of this is. The command-line
            // CVS client does it
            final Writer w = new BufferedWriter(new FileWriter(entriesFile));
            try {
                w.write("D"); //NOI18N
            } finally {
                w.close();
            }
        }

        // Update the Entries file
        if (entry != null) {
            updateEntriesFile(entriesFile, entry);
        }
    }

    /**
     * Add a directory entry to the parent directory's Entries file, if it
     * exists. Any line containing only a D is deleted from the parent
     * Entries file.
     * @param CVSdir the full path to the CVS directory for the directory
     * that has just been added
     */
    private void addDirectoryToParentEntriesFile(File CVSdir)
            throws IOException {
        File parentCVSEntries = new File(CVSdir.getParentFile().
                                         getParentFile(),
                                         "CVS/Entries"); //NOI18N

        // only update if the file exists. The file will not exist in the
        // case where this is the top level of the module
        if (parentCVSEntries.exists()) {
            final File directory = parentCVSEntries.getParentFile();
            final File tempFile = new File(directory, "Entries.Backup"); //NOI18N
            tempFile.createNewFile();
            BufferedReader reader = null;
            BufferedWriter writer = null;
            try {
                reader = new BufferedReader(new FileReader(parentCVSEntries));
                writer = new BufferedWriter(new FileWriter(tempFile));
                
                String line;

                // As in the updateEntriesFile method the new Entry
                // only may be written, if it does not exist
                boolean written = false;

                Entry directoryEntry = new Entry();
                directoryEntry.setName(CVSdir.getParentFile().getName());
                directoryEntry.setDirectory(true);

                while ((line = reader.readLine()) != null) {
                    if (line.trim().equals("D")) { //NOI18N
                        // do not write out this line
                        continue;
                    }

                    final Entry currentEntry = new Entry(line);
                    if (currentEntry.getName() != null
                            && currentEntry.getName().equals(directoryEntry.getName())) {
                        writer.write(directoryEntry.toString());
                        written = true;
                    }
                    else {
                        writer.write(line);
                    }
                    writer.newLine();
                }

                if (!written) {
                    writer.write(directoryEntry.toString());
                    writer.newLine();
                }
            } finally {
                try {
                    if (writer != null) {
                        writer.close();
                    }
                } finally {
                    if (reader != null) {
                        reader.close();
                    }
                }
            }

            // now move the files around. Note that when you rename a file,
            // although it changes the file on disk, the File object still
            // points to the original file.
            final File temp2 = new File(directory, "Entries.Backup2"); //NOI18N
            parentCVSEntries.renameTo(temp2);
            tempFile.renameTo(parentCVSEntries);
            temp2.delete();
        }
    }

    /**
     * Update the specified Entries file with a given entry
     * This method currently does the following, in order:
     * <OL><LI>Create a new temporary file, Entries.temp</LI>
     * <LI>Iterate through each line of the original file, checking
     * if it matches the entry of the entry to be updated. If not, simply
     * copy the line otherwise write the new entry and discard the old one</LI>
     * <LI>Once all lines have been written, close both files</LI>
     * <LI>Move the original file to Entries.temp2</LI>
     * <LI>Move the file Entries.temp to Entries</LI>
     * <LI>Delete Entries.temp2</LI></UL>
     * Note that in the case where the Entries file does not exist, it
     * is simply created and the entry appended immediately.
     * This all ensures that if a failure occurs at any stage, the original
     * Entries file can be retrieved
     * @param originalFile the original Entries file, which need not exist yet
     * @param entry the specific entry to update
     * @throws IOException if an error occurs writing the files
     */
    private void updateEntriesFile(File originalFile, Entry entry)
            throws IOException {
        final File directory = originalFile.getParentFile();
        final File tempFile = new File(directory, "Entries.Backup"); //NOI18N
        tempFile.createNewFile();

        BufferedReader reader = null;
        BufferedWriter writer = null;
        try {
            reader = new BufferedReader(new FileReader(originalFile));
            writer = new BufferedWriter(new FileWriter(tempFile));
            String line;
            // indicates whether we have written the entry that was passed in
            // if we finish copying the file without writing it, then it is
            // a new entry and must simply be append at the end
            boolean written = false;
            while ((line = reader.readLine()) != null) {
                final Entry currentEntry = new Entry(line);
                if ((currentEntry.getName() != null) &&
                        currentEntry.getName().equals(entry.getName())) {
                    writer.write(entry.toString());
                    written = true;
                }
                else {
                    writer.write(line);
                }
                writer.newLine();
            }
            if (!written) {
                writer.write(entry.toString());
                writer.newLine();
            }
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
        }

        // now move the files around. Note that when you rename a file,
        // although it changes the file on disk, the File object still
        // points to the original file.
        final File temp2 = new File(directory, "Entries.Backup2"); //NOI18N
        originalFile.renameTo(temp2);
        tempFile.renameTo(originalFile);
        temp2.delete();
    }

    /**
     * Get the Entry for the specified file, if one exists
     * @param f the file
     * @throws IOException if the Entries file cannot be read
     */
    public Entry getEntry(File file) throws IOException {
        final File entriesFile = new File(file.getParent(), "CVS/Entries"); //NOI18N
        // if there is no Entries file we cannot very well get any Entry
        // from it
        if (entriesFile == null || !entriesFile.exists()) {
            return null;
        }

        processEntriesDotLog(new File(file.getParent(), "CVS")); //NOI18N

        BufferedReader reader = null;
        Entry entry = null;
        boolean found = false;
        try {
            reader = new BufferedReader(new FileReader(entriesFile));
            String line;
            while (!found && ((line = reader.readLine()) != null)) {
                entry = new Entry(line);
                // can have a name of null in the case of the single
                // D entry line spec, indicating no subdirectories
                if (entry.getName() != null) {
                    found = entry.getName().equals(file.getName());
                }
            }
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }

        if (!found) {
            return null;
        }

        return entry;
    }

    /**
     * Get the entries for a specified directory.
     * @param directory the directory for which to get the entries
     * @return an array of Entry objects
     */
    public Entry[] getEntriesAsArray(File directory)
            throws IOException {
        List entries = new LinkedList();

        final File entriesFile = new File(directory, "CVS/Entries"); //NOI18N
        // if there is no Entries file we just return the empty iterator
        if (entriesFile == null || !entriesFile.exists()) {
            return new Entry[0];
        }

        processEntriesDotLog(new File(directory, "CVS")); //NOI18N

        BufferedReader reader = null;
        Entry entry = null;
        try {
            reader = new BufferedReader(new FileReader(entriesFile));
            String line;
            while ((line = reader.readLine()) != null) {
                entry = new Entry(line);
                // can have a name of null in the case of the single
                // D entry line spec, indicating no subdirectories
                if (entry.getName() != null) {
                    entries.add(entry);
                }
            }
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }
        Entry[] toReturnArray = new Entry[entries.size()];
        toReturnArray = (Entry[])entries.toArray(toReturnArray);
        return toReturnArray;
    }

    /**
     * Get the entries for a specified directory.
     * @param directory the directory for which to get the entries
     * @return an iterator of Entry objects
     */
    public Iterator getEntries(File directory)
            throws IOException {
        List entries = new LinkedList();

        final File entriesFile = new File(directory, "CVS/Entries"); //NOI18N
        // if there is no Entries file we just return the empty iterator
        if (entriesFile == null || !entriesFile.exists()) {
            return entries.iterator();
        }

        processEntriesDotLog(new File(directory, "CVS")); //NOI18N

        BufferedReader reader = null;
        Entry entry = null;
        try {
            reader = new BufferedReader(new FileReader(entriesFile));
            String line;
            while ((line = reader.readLine()) != null) {
                entry = new Entry(line);
                // can have a name of null in the case of the single
                // D entry line spec, indicating no subdirectories
                if (entry.getName() != null) {
                    entries.add(entry);
                }
            }
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }
        return entries.iterator();
    }

    /**
     * Set the Entry for the specified file
     * @param f the file whose entry is being updated
     * @throws IOException if an error occurs writing the details
     */
    public void setEntry(File file, Entry entry)
            throws IOException {
        String parent = file.getParent();
        final File entriesFile = new File(parent, "CVS/Entries"); //NOI18N
        processEntriesDotLog(new File(parent, "CVS")); //NOI18N
        updateEntriesFile(entriesFile, entry);
    }

    /**
     * Remove the Entry for the specified file
     * @param f the file whose entry is to be removed
     * @throws IOException if an error occurs writing the Entries file
     */
    public void removeEntry(File file) throws IOException {
        final File entriesFile = new File(file.getParent(), "CVS/Entries"); //NOI18N

        // if there is no Entries file we cannot very well remove an Entry
        // from it
        if (entriesFile == null || !entriesFile.exists()) {
            return;
        }

        processEntriesDotLog(new File(file.getParent(), "CVS")); //NOI18N

        final File directory = file.getParentFile();
        final File tempFile = new File(directory, "Entries.Backup"); //NOI18N
        tempFile.createNewFile();
        BufferedReader reader = null;
        BufferedWriter writer = null;
        try {
            reader = new BufferedReader(new FileReader(entriesFile));
            writer = new BufferedWriter(new FileWriter(tempFile));
            String line;
            // allows us to determine whether we need to put in a "D" line. We do
            // that if we remove the last directory from the Entries file
            boolean directoriesExist = false;
            while ((line = reader.readLine()) != null) {
                final Entry currentEntry = new Entry(line);
                if ((currentEntry.getName() != null) &&

                        !currentEntry.getName().equals(file.getName())) {
                    writer.write(currentEntry.toString());
                    writer.newLine();
                    directoriesExist = directoriesExist ||
                            currentEntry.isDirectory();
                }
            }
            if (!directoriesExist) {
                writer.write("D"); //NOI18N
                writer.newLine();
            }
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
        }
        // now move the files around. Note that when you rename a file,
        // although it changes the file on disk, the File object still
        // points to the original file.
        final File temp2 = new File(directory, "Entries.Backup2"); //NOI18N
        entriesFile.renameTo(temp2);
        tempFile.renameTo(entriesFile);
        temp2.delete();
    }

    /**
     * Get the repository path for a given directory, for example in
     * the directory /home/project/foo/bar, the repository directory
     * might be /usr/cvs/foo/bar. The repository directory is commonly
     * stored in the file <pre>Repository</pre> in the CVS directory on
     * the client. (This is the case in the standard CVS command-line tool).
     * However, the path stored in that file is relative to the repository
     * path
     * @param directory the directory
     * @param the repository path on the server, e.g. /home/bob/cvs. Must not
     * end with a slash.
     */
    public String getRepositoryForDirectory(String directory,
                                            String repository)
            throws IOException {
        // if there is no "CVS/Repository" file, try to search up the file-
        // hierarchy
        File repositoryFile = null;
        String repositoryDirs = ""; //NOI18N
        File dirFile = new File(directory);
        while (true) {
            // if there is no Repository file we cannot very well get any
            // repository from it
            if (dirFile == null
                    || dirFile.getName().length() == 0
                    || !dirFile.exists()) {
                throw new FileNotFoundException("Repository file not found " + //NOI18N
                                                "for directory " + directory); //NOI18N
            }

            repositoryFile = new File(dirFile, "CVS/Repository"); //NOI18N
            if (repositoryFile.exists()) {
                break;
            }
            repositoryDirs = '/' + dirFile.getName() + repositoryDirs;
            dirFile = dirFile.getParentFile();
        }
        BufferedReader reader = null;
        // fileRepository is the value of the repository read from the
        // Repository file
        String fileRepository = null;
        try {
            reader = new BufferedReader(new FileReader(repositoryFile));
            fileRepository = reader.readLine();
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }

        if (fileRepository == null) {
            fileRepository = ""; //NOI18N
        }

        fileRepository += repositoryDirs;
        // absolute repository path ?
        if (fileRepository.startsWith("/")) { //NOI18N
            return fileRepository;
        }

        // otherwise the cvs is using relative repository path
        // must be a forward slash, regardless of the local filing system
        return repository + '/' + fileRepository;
    }

    /**
     * Update the Entries file using information in the Entries.Log file
     * (if present). If Entries.Log is not present, this method does
     * nothing.
     * @param directory the directory that contains the Entries file
     * @throws IOException if an error occurs reading or writing the files
     */
    private void processEntriesDotLog(File directory) throws IOException {
        final File entriesDotLogFile = new File(directory, "Entries.Log"); //NOI18N
        if (!entriesDotLogFile.exists()) {
            return;
        }

        BufferedReader reader = new BufferedReader(new FileReader(
                entriesDotLogFile));

        // make up a list of changes to be made based on what is in
        // the .log file. Then apply them all later
        List additionsList = new LinkedList();
        HashSet removalSet = new HashSet();

        String line;

        try {
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("A ")) { //NOI18N
                    final Entry entry = new Entry(line.substring(2));
                    additionsList.add(entry);
                }
                else if (line.startsWith("R ")) { //NOI18N
                    final Entry entry = new Entry(line.substring(2));
                    removalSet.add(entry.getName());
                }
                // otherwise ignore the line since we don't understand it
            }
        } finally {
            reader.close();
        }

        if ((additionsList.size() > 0) || (removalSet.size() > 0)) {
            final File backup = new File(directory, "Entries.Backup"); //NOI18N
            final BufferedWriter writer = new BufferedWriter(new FileWriter(
                    backup));
            final File entriesFile = new File(directory, "Entries"); //NOI18N
            reader = new BufferedReader(new FileReader(entriesFile));

            try {
                // maintain a count of the number of directories so that
                // we know whether to write the "D" line
                int directoryCount = 0;

                while ((line = reader.readLine()) != null) {
                    // we will write out the directory "understanding" line
                    // later, if necessary
                    if (line.trim().equals("D")) { //NOI18N
                        continue;
                    }

                    final Entry entry = new Entry(line);

                    if (entry.isDirectory()) {
                        directoryCount++;
                    }

                    if (!removalSet.contains(entry.getName())) {
                        writer.write(entry.toString());
                        writer.newLine();
                        if (entry.isDirectory()) {
                            directoryCount--;
                        }
                    }
                }
                Iterator it = additionsList.iterator();
                while (it.hasNext()) {
                    final Entry entry = (Entry)it.next();
                    if (entry.isDirectory()) {
                        directoryCount++;
                    }
                    writer.write(entry.toString());
                    writer.newLine();
                }
                if (directoryCount == 0) {
                    writer.write("D"); //NOI18N
                    writer.newLine();
                }
            } finally {
                try {
                    reader.close();
                } finally {
                    writer.close();
                }
            }

            // now move the files around. Note that when you rename a file,
            // although it changes the file on disk, the File object still
            // points to the original file.
            final File temp2 = new File(directory, "Entries.Backup2"); //NOI18N
            entriesFile.renameTo(temp2);
            backup.renameTo(entriesFile);
            temp2.delete();
        }
        entriesDotLogFile.delete();
    }

    /**
     * Get all the files contained within a given
     * directory that are <b>known to CVS</b>.
     * @param directory the directory to look in
     * @return a set of all files.
     */
    public Set getAllFiles(File directory) throws IOException {
        TreeSet fileSet = new TreeSet();
        BufferedReader reader = null;
        try {
            final File entriesFile = new File(directory, "CVS/Entries"); //NOI18N

            // if for any reason we don't have an Entries file just return
            // with the empty set.
            if (!entriesFile.exists()) {
                return fileSet; // Premature return
            }

            reader = new BufferedReader(new FileReader(entriesFile));
            String line;

            while ((line = reader.readLine()) != null) {
                final Entry entry = new Entry(line);

                if (entry.getName() != null) {
                    final File f = new File(directory, entry.getName());
                    if (!f.isDirectory()) {
                        fileSet.add(f);
                    }
                }
            }
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }

        return fileSet;
    }

    /**
     * Checks for presence of CVS/Tag file and returns it's value.
     * @returns the value of CVS/Tag file for the specified directory
     *         null if file doesn't exist
     */
    public String getStickyTagForDirectory(File directory) {
        BufferedReader reader = null;
        File tagFile = new File(directory, "CVS/Tag"); //NOI18N

        try {
            reader = new BufferedReader(new FileReader(tagFile));
            String tag = reader.readLine();
            return tag;
        }
        catch (IOException ex) {
            // silently ignore??
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException ex) {
                    // silently ignore
                }
            }
        }
        return null;
    }

}

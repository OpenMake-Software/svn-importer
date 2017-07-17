/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.lib.cvsclient.command;

import java.io.*;

public class PipedFileInformation extends FileInfoContainer {
    private File file;

    private String repositoryRevision;

    private String repositoryFileName;

    private File tempFile;

    private Writer tempWriter;

    public PipedFileInformation(File tempFile) {
        this.tempFile = tempFile;
        this.tempFile.deleteOnExit();
        try {
            tempWriter = new BufferedWriter(new FileWriter(tempFile));
        }
        catch (IOException ex) {
            // TODO
        }
    }

    /**
     * Returns the original file.
     */
    public File getFile() {
        return file;
    }

    /**
     * Sets the original file.
     */
    protected void setFile(File file) {
        this.file = file;
    }

    /**
     * Returns the revision of the incoming file.
     */
    public String getRepositoryRevision() {
        return repositoryRevision;
    }

    /**
     * Sets the revision of the incoming file.
     */
    protected void setRepositoryRevision(String repositoryRevision) {
        this.repositoryRevision = repositoryRevision;
    }

    /**
     * Returns the filename in the repository.
     */
    public String getRepositoryFileName() {
        return repositoryFileName;
    }

    /**
     * Sets the repository filename.
     */
    protected void setRepositoryFileName(String repositoryFileName) {
        this.repositoryFileName = repositoryFileName;
    }

    /**
     * Adds the specified line to the temporary file.
     */
    protected void addToTempFile(String line) throws IOException {
        if (tempWriter != null) {
            tempWriter.write(line + '\n');
        }
    }

    protected void closeTempFile() throws IOException {
        if (tempWriter != null) {
            tempWriter.flush();
            tempWriter.close();
        }
    }

    public File getTempFile() {
        return tempFile;
    }

}

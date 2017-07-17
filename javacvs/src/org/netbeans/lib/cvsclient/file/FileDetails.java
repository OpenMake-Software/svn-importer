/*****************************************************************************
 * Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is available at http://www.sun.com/

 * The Original Code is the CVS Client Library.
 * The Initial Developer of the Original Code is Robert Greig.
 * Portions created by Robert Greig are Copyright (C) 2000.
 * All Rights Reserved.

 * Contributor(s): Robert Greig.
 *****************************************************************************/
package org.netbeans.lib.cvsclient.file;

import java.io.*;

/**
 * A wrapper class that describes a file.
 * @author  Robert Greig
 */
public class FileDetails {
    /**
     * The file
     */
    private File file;

    /**
     * Whether the file is binary
     */
    private boolean isBinary;

    /**
     * Construct a FileDetails object
     * @param theFile the file
     * @param binary true if the file is binary, false if it is text
     */
    public FileDetails(File file, boolean isBinary) {
        this.file = file;
        this.isBinary = isBinary;
    }

    /**
     * Return the file.
     * @return the file
     */
    public File getFile() {
        return file;
    }

    /**
     * Return the file type.
     * @return true if the file is binary, false if it is text
     */
    public boolean isBinary() {
        return isBinary;
    }
}
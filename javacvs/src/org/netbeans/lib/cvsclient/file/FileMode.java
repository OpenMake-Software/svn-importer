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
 * Implements the CVS concept of File Modes (permissions).
 * @author  Robert Greig
 */
public class FileMode {
    /**
     * The underlying file
     */
    private File file;

    /**
     * Construct a new file mode from a file.
     */
    public FileMode(File file) {
        this.file = file;
    }

    /**
     * Returns a CVS-compatible file mode string
     */
    public String toString() {
        // TODO: really implement this!
        return "u=rw,g=r,o=r"; //NOI18N
    }
}
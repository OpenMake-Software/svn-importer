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
package org.netbeans.lib.cvsclient.request;

import java.io.*;

import org.netbeans.lib.cvsclient.file.*;

/**
 * Sends the server a copy of a locally modified file.
 * @author  Robert Greig
 */
public class ModifiedRequest extends Request {
    /**
     * The file details
     */
    private FileDetails fileDetails;

    /**
     * Construct a new modified request.
     * @param theFile the file that has been modified
     */
    public ModifiedRequest(File file, boolean isBinary) {
        fileDetails = new FileDetails(file, isBinary);
    }

    /**
     * Get the request String that will be passed to the server
     * @return the request String
     * @throws UnconfiguredRequestException if the request has not been
     * properly configured
     */
    public String getRequestString() throws UnconfiguredRequestException {
        if (fileDetails == null) {
            throw new UnconfiguredRequestException("FileDetails is null in " +
                                                   "ModifiedRequest");
        }
        final FileMode mode = new FileMode(fileDetails.getFile());
        return "Modified " + fileDetails.getFile().getName() + "\n" + //NOI18N
                mode.toString() + "\n"; //NOI18N
    }

    /**
     * Is a response expected from the server?
     * @return true if a response is expected, false if no response if
     * expected
     */
    public boolean isResponseExpected() {
        return false;
    }

    /**
     * If a file transmission is required, get the file object representing
     * the file to transmit after the request string. The default
     * implementation returns null, indicating no file is to be transmitted
     * @return the file details object, if one should be transmitted, or null
     * if no file object is to be transmitted.
     */
    public FileDetails getFileForTransmission() {
        return fileDetails;
    }
}

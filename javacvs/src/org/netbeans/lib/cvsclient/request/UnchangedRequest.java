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

/**
 * Tells the server that a particular filename has not been modified in the
 * checked-out directory.
 * @author  Robert Greig
 */
public class UnchangedRequest extends Request {
    /**
     * The filename of the file that is unchanged
     */
    private String filename;

    /**
     * Construct an Unchanged request
     * @param theFilename the unchanged file's name
     */
    public UnchangedRequest(String theFilename) {
        filename = theFilename;
    }

    /**
     * Construct an Unchanged request
     * @param file the unchanged file
     */
    public UnchangedRequest(File file) {
        filename = file.getName();
    }

    /**
     * Get the request String that will be passed to the server
     * @return the request String
     * @throws UnconfiguredRequestException if the request has not been
     * properly configured
     */
    public String getRequestString() throws UnconfiguredRequestException {
        if (filename == null)
            throw new UnconfiguredRequestException("Filename must be set");
        return "Unchanged " + filename + "\n"; //NOI18N
    }

    /**
     * Is a response expected from the server?
     * @return true if a response is expected, false if no response if
     * expected
     */
    public boolean isResponseExpected() {
        return false;
    }
}
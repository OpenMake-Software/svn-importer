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

import org.netbeans.lib.cvsclient.connection.*;

/**
 * This class implements the Gzip-Stream request that is used to indicate that
 * all further communication with the server is to be gzipped.
 * @author  Robert Greig
 */
public class GzipStreamRequest extends Request {

    /**
     * The level of gzipping to specify
     */
    private int level = 6;

    /**
     * Creates new GzipStreamRequest with gzip level 6
     */
    public GzipStreamRequest() {
    }

    /**
     * Creates new GzipStreamRequest
     * @param level the level of zipping to use (between 1 and 9)
     */
    public GzipStreamRequest(int level) {
        this.level = level;
    }

    /**
     * Get the request String that will be passed to the server
     * @return the request String
     * @throws UnconfiguredRequestException if the request has not been
     * properly configured
     */
    public String getRequestString() throws UnconfiguredRequestException {
        return "Gzip-stream " + level + "\n"; //NOI18N
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
     * Modify streams on the connection if necessary
     */
    public void modifyOutputStream(Connection connection) throws IOException {
        connection.modifyOutputStream(new GzipModifier());
    }

    /**
     * Modify streams on the connection if necessary
     */
    public void modifyInputStream(Connection connection) throws IOException {
        connection.modifyInputStream(new GzipModifier());
    }

    /**
     * Does this request modify the input stream?
     * @return true if it does, false otherwise
     */
    public boolean modifiesInputStream() {
        return true;
    }
}

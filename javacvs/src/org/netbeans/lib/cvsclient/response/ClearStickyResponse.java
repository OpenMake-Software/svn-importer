/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.lib.cvsclient.response;

import java.io.*;

import org.netbeans.lib.cvsclient.util.*;

/**
 * Handles the Clear-sticky response.
 * @author  Milos Kleint
 */

class ClearStickyResponse implements Response {
    /**
     * Process the data for the response.
     * @param dis the data inputstream allowing the client to read the server's
     * response. Note that the actual response name has already been read
     * and the input stream is positioned just before the first argument, if
     * any.
     */
    public void process(LoggedDataInputStream dis, ResponseServices services)
            throws ResponseException {
        try {
            final String localPath = dis.readLine();

            final String repositoryPath = dis.readLine();
            //System.err.println("Repository path is: " + repositoryPath);
            services.updateAdminData(localPath, repositoryPath, null);
            // TODO: remove the Entries.static file (check user manual for
            // file name first)
            final String absPath = services.convertPathname(localPath, repositoryPath);
            File absFile = new File(absPath, "CVS/Tag"); //NOI18N
            if (absFile.exists()) {
                absFile.delete();
            }
        }
        catch (IOException e) {
            throw new ResponseException(e);
        }
    }

    /**
     * Is this a terminal response, i.e. should reading of responses stop
     * after this response. This is true for responses such as OK or
     * an error response
     */
    public boolean isTerminalResponse() {
        return false;
    }
}

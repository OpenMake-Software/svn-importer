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
package org.netbeans.lib.cvsclient.response;

import java.io.*;

import org.netbeans.lib.cvsclient.util.*;

/**
 * Handles the Set-static-directory response.
 * @author  Robert Greig
 */

class SetStaticDirectoryResponse implements Response {

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
            String localPath = dis.readLine();

            String repositoryPath = dis.readLine();
            //System.err.println("Repository path is: " + repositoryPath);
            services.updateAdminData(localPath, repositoryPath, null);
            String absPath = services.convertPathname(localPath, repositoryPath);
            absPath = absPath + "/CVS"; //NOI18N
            File absFile = new File(absPath);
            if (absFile.exists()) {
                File staticFile = new File(absPath, "Entries.Static"); //NOI18N
                staticFile.createNewFile();
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

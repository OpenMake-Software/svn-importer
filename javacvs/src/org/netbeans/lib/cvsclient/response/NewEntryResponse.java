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

import org.netbeans.lib.cvsclient.admin.*;
import org.netbeans.lib.cvsclient.util.*;

/**
 * Indicates that a file has been successfully operated on, e.g. checked in,
 * added etc. is the same as Checked-in but operates on modified files..
 * @author  Milos Kleint
 */
class NewEntryResponse implements Response {
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
            //System.err.println("Pathname is: " + localPath);
            String repositoryPath = dis.readLine();
            //System.err.println("Repository path is: " + repositoryPath);
            String entriesLine = dis.readLine();
            //System.err.println("New entries line is: " + entriesLine);
            // we set the date the file was last modified in the Entry line
            // so that we can easily determine whether the file has been
            // untouched
            final File theFile = new File(services.convertPathname(localPath,
                                                                   repositoryPath));
//            final Date d = new Date(theFile.lastModified());
            final Entry entry = new Entry(entriesLine);
            entry.setConflict(Entry.DUMMY_TIMESTAMP);

            services.setEntry(theFile, entry);
        }
        catch (IOException e) {
            throw new ResponseException((Exception)e.fillInStackTrace(), e.getLocalizedMessage());
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

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
package org.netbeans.lib.cvsclient.response;

import java.io.*;
import java.util.*;
import java.text.*;

import org.netbeans.lib.cvsclient.admin.*;
import org.netbeans.lib.cvsclient.util.*;

/**
 * Indicates that a file has been successfully operated on, e.g. checked in,
 * added etc.
 * @author  Robert Greig
 */
class CheckedInResponse implements Response {
    
    /** 
     * The date Formatter used to parse and format dates.
     * Format is: "EEE MMM dd HH:mm:ss yyyy"
     */
    private DateFormat dateFormatter;
    
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
            final Date date = new Date(theFile.lastModified());
            final Entry entry = new Entry(entriesLine);
            entry.setConflict(getDateFormatter().format(date));

            //  for added and removed entries set the conflict to Dummy timestamp.
            if (entry.isNewUserFile() ||
                    entry.isUserFileToBeRemoved()) {
                entry.setConflict(Entry.DUMMY_TIMESTAMP);
            }

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
    
    /**
     * Returns the DateFormatter instance that parses and formats date Strings.
     * The exact format matches the one in Entry.getLastModifiedDateFormatter() method.
     *
     */
    protected DateFormat getDateFormatter() {
        if (dateFormatter == null) {
            dateFormatter = new SimpleDateFormat(Entry.getLastModifiedDateFormatter().toPattern(), Locale.US);
            dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT+0000")); //NOI18N
        }
        return dateFormatter;
    }
    
}

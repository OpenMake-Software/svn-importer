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

import org.netbeans.lib.cvsclient.event.FileRemovedEvent;
import org.netbeans.lib.cvsclient.event.FileToRemoveEvent;
import org.netbeans.lib.cvsclient.util.*;

/**
 * Indicates that a file has been removed from the repository.
 * @author Robert Greig
 */
class RemovedResponse implements Response {
    
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

            String filePath =
                    services.convertPathname(localPath, repositoryPath);
            filePath = new File(filePath).getAbsolutePath();

            FileToRemoveEvent e1 = new FileToRemoveEvent(this, filePath);
            FileRemovedEvent e2 = new FileRemovedEvent(this, filePath);

            //Fire the event before removing the local file. This is done
            //so that event listeners have a chance to access the file one 
            //last time before the file is removed.
            services.getEventManager().fireCVSEvent(e1);
            services.removeLocalFile(localPath, repositoryPath);
            services.getEventManager().fireCVSEvent(e2);
        }
        catch (EOFException ex) {
            throw new ResponseException(ex,
                                        ResponseException.getLocalMessage(
                                            "CommandException.EndOfFile", // NOI18N
                                            null));
        }
        catch (IOException ex) {
            throw new ResponseException(ex);
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

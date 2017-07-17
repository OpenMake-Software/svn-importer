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
 * This Template response allows the server to send a template file that is
 * used when committing changes. The client tools can read the Template file
 * which is stored in CVS/Template and display it to the user to be used as a
 * prompt for commit comments.
 * @author Robert Greig
 */
class TemplateResponse
        implements Response {
    /**
     * A reference to an uncompressed file handler
     */
/*
    // TODO: Should this be taken from ResponseServices???
    protected static FileHandler uncompressedFileHandler;
*/

    /**
     * The local path of the new file
     */
    protected String localPath;

    /**
     * The full repository path of the file
     */
    protected String repositoryPath;

    /**
     * Creates new TemplateResponse
     */
    public TemplateResponse() {
    }

/*
    // TODO: replace this with a call to ResponseSerivices::getUncompr....ler?
    protected static FileHandler getUncompressedFileHandler()
    {
        if (uncompressedFileHandler == null) {
            uncompressedFileHandler = new DefaultFileHandler();
        }
        return uncompressedFileHandler;
    }
*/

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
            localPath = dis.readLine();
            repositoryPath = dis.readLine();

            int length = Integer.parseInt(dis.readLine());

            // now read in the file
            final String filePath = services.convertPathname(localPath,
                                                             repositoryPath) +
                    "CVS/Template"; //NOI18N

            // just pass in null for the file's mode
            services.getUncompressedFileHandler().writeTextFile(filePath,
                                                                null, dis, length);
        }
        catch (EOFException ex) {
            String localMessage =
                    ResponseException.getLocalMessage("CommandException.EndOfFile"); //NOI18N
            throw new ResponseException(ex, localMessage);
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

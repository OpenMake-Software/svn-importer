/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.cvsclient.response;

import java.io.*;

import org.netbeans.lib.cvsclient.event.*;
import org.netbeans.lib.cvsclient.util.*;

/**
 * Handles binary message responses
 * @author  Martin Entlicher
 */
class MessageBinaryResponse implements Response {

    private String firstWord;
    
    public MessageBinaryResponse() {
        // do nothing
    }
    
    /**
     * Process the data for the response.
     * @param dis the data inputstream allowing the client to read the server's
     * response. Note that the actual response name has already been read
     * and the inputstream is positioned just before the first argument, if any.
     */
    public void process(LoggedDataInputStream dis, ResponseServices services)
            throws ResponseException {
        try {
            String numBytesStr = dis.readLine();
            int numBytes;
            try {
                numBytes = Integer.parseInt(numBytesStr);
            } catch (NumberFormatException nfex) {
                throw new ResponseException(nfex);
            }
            byte[] bytes = new byte[numBytes];
            dis.read(bytes);
            BinaryMessageEvent event = new BinaryMessageEvent(this, bytes);
            services.getEventManager().fireCVSEvent(event);
        }
        catch (EOFException ex) {
            throw new ResponseException(ex, ResponseException.getLocalMessage("CommandException.EndOfFile", null)); //NOI18N
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
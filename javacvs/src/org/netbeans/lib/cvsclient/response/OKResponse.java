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

import org.netbeans.lib.cvsclient.event.*;
import org.netbeans.lib.cvsclient.util.*;

/**
 * Handles the OK response
 * @author  Robert Greig
 */
class OKResponse implements Response {

    /**
     * Process the data for the response.
     * @param dis the data inputstream allowing the client to read the server's
     * response. Note that the actual response name has already been read
     * and the input stream is positioned just before the first argument, if
     * any.
     * @return true if processing should continue, false if processing should
     * now stop
     */
    public void process(LoggedDataInputStream dis, ResponseServices services)
            throws ResponseException {
        TerminationEvent termEvent = new TerminationEvent(this, false);
        services.getEventManager().fireCVSEvent(termEvent);
    }

    /**
     * Is this a terminal response, i.e. should reading of responses stop
     * after this response. This is true for responses such as OK or
     * an error response
     */
    public boolean isTerminalResponse() {
        return true;
    }
}
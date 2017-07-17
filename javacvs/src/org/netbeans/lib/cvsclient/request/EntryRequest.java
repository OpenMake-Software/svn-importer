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

import org.netbeans.lib.cvsclient.admin.*;

/**
 * Sends an entry to the server, to tell the server which version of a file
 * is on the local machine. The filename is relative to the most recent
 * Directory request. Note that if an <pre>Entry</pre> request is sent
 * without <pre>Modified</pre>, <pre>Is-Modified</pre> or <pre>Unchanged</pre>
 * it means that the file is lost. Also note that if <pre>Modified</pre>,
 * <pre>Is-Modified</pre> or </pre>Unchanged</pre> is sent with <pre>Entry
 * </pre> then </pre>Entry</pre> must be sent first.
 * @author  Robert Greig
 * @see org.netbeans.lib.cvsclient.request.DirectoryRequest
 * @see org.netbeans.lib.cvsclient.request.ModifiedRequest
 * @see org.netbeans.lib.cvsclient.request.ModifiedRequest
 * @see org.netbeans.lib.cvsclient.request.UnchangedRequest
 */
public class EntryRequest extends Request {
    /**
     * The Entry sent by this request
     */
    private Entry entry;

    /**
     * Create an EntryRequest
     * @param theEntry the Entry to send
     */
    public EntryRequest(Entry theEntry) {
        if (theEntry == null)
            throw new IllegalArgumentException("EntryRequest: Entry must not " +
                                               "be null");
        entry = theEntry;
    }

    /**
     * Get the request String that will be passed to the server
     * @return the request String
     * @throws UnconfiguredRequestException if the request has not been
     * properly configured
     */
    public String getRequestString() throws UnconfiguredRequestException {
        return "Entry " + entry.toString() + "\n"; //NOI18N
    }

    /**
     * Is a response expected from the server?
     * @return true if a response is expected, false if no response if
     * expected
     */
    public boolean isResponseExpected() {
        return false;
    }

    public Entry getEntry() {
        return entry;
    }

}
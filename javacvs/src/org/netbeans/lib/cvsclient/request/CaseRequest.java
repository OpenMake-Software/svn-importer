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

/**
 * Tells the server that filenames should be matched in a case-insensitive
 * fashion.
 * @author  Robert Greig
 */
public class CaseRequest extends Request {
    /**
     * Get the request String that will be passed to the server
     * @return the request String
     * @throws UnconfiguredRequestException if the request has not been
     * properly configured
     */
    public String getRequestString() throws UnconfiguredRequestException {
        return "Case \n"; //NOI18N
    }

    /**
     * Is a response expected from the server?
     * @return true if a response is expected, false if no response if
     * expected
     */
    public boolean isResponseExpected() {
        return false;
    }
}
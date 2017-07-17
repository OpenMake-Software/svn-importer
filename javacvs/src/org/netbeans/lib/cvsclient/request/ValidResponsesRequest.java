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
 * Sends a request telling the server which responses this client understands.
 * This request should be sent before any commands are executed. This is done
 * automatically by the Client class.
 * @see org.netbeans.lib.cvsclient.Client
 * @author  Robert Greig
 */
public class ValidResponsesRequest extends Request {
    /**
     * Get the request String that will be passed to the server
     * @return the request String
     * @throws UnconfiguredRequestException if the request has not been
     * properly configured
     */
    public String getRequestString() throws UnconfiguredRequestException {
        return "Valid-responses E M MT Mbinary Updated Rcs-diff Checked-in ok error " + //NOI18N
                "Clear-static-directory Valid-requests Merged Removed " + //NOI18N
                "Copy-file Mod-time Template Set-static-directory " + //NOI18N
                "Module-expansion Clear-sticky Set-sticky New-entry\n"; //NOI18N
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

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
package org.netbeans.lib.cvsclient.request;

/**
 * Implements the Root request
 * @author  Robert Greig
 */
public final class RootRequest extends Request {
    /**
     * The CVS root to send.
     */
    private final String cvsRoot;

    /**
     * Construct a new Root request
     * @param cvsRoot the CVS root to use as an argument in the request
     */
    public RootRequest(String cvsRoot) {
        if (cvsRoot == null) {
            throw new IllegalArgumentException("cvsRoot must not be null!"); // NOI18N
        }

        this.cvsRoot = cvsRoot;
    }

    /**
     * Get the request String that will be passed to the server
     * @return the request String
     */
    public String getRequestString() throws UnconfiguredRequestException {
        return "Root " + cvsRoot + "\n"; //NOI18N
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
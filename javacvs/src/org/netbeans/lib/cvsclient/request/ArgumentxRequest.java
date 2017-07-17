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
 * Appends \n followed by the text specified in the request to the previous
 * argument sent.
 * @author  Robert Greig
 */
public class ArgumentxRequest extends Request {
    /**
     * The argument to pass in
     */
    private String argument;

    /**
     * Create a new request
     * @param theArgument the argument to use
     */
    public ArgumentxRequest(String theArgument) {
        argument = theArgument;
    }

    /**
     * Set the argument
     */
    public void setArgument(String theArgument) {
        argument = theArgument;
    }

    /**
     * Get the request String that will be passed to the server
     * @return the request String
     */
    public String getRequestString() throws UnconfiguredRequestException {
        if (argument == null) {
            throw new UnconfiguredRequestException(
                    "Argument has not been set");
        }

        return "Argumentx " + argument + "\n"; //NOI18N
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
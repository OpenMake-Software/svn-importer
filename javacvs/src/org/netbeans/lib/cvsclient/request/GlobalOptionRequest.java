/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.lib.cvsclient.request;

/**
 * The global options request.
 * Sends global switch to the server.
 *
 * @author  Milos Kleint
 */
public class GlobalOptionRequest extends Request {
    /**
     * The option to pass in.
     */
    private final String option;

    /**
     * Create a new request
     * @param theOption the option to use
     */
    public GlobalOptionRequest(String option) {
        this.option = option;
    }

    /**
     * Get the request String that will be passed to the server.
     * @return the request String
     */
    public String getRequestString() throws UnconfiguredRequestException {
        if (option == null) {
            throw new UnconfiguredRequestException(
                    "Global option has not been set");
        }

        return "Global_option " + option + "\n"; //NOI18N
    }

    /**
     * Is a response expected from the server?
     * @return true if a response is expected, false if no response if
     *         expected
     */
    public boolean isResponseExpected() {
        return false;
    }
}
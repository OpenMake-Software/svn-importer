/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.lib.cvsclient.request;

import org.netbeans.lib.cvsclient.util.*;

/**
 * Tells the server about the eviroment variables to set for CVSROOT/*info executions.
 * @author  Milos Kleint
 */
public class SetRequest extends Request {
    
    private String keyValue;
    /**
     * Creates a new SetResponse with the key-value pair for one enviroment variable.
     */
    public SetRequest(String keyValue) {
        BugLog.getInstance().assertTrue(keyValue.indexOf('=') > 0, "Wrong SetRequest=" + keyValue);
        this.keyValue = keyValue;
    }
    /**
     * Get the request String that will be passed to the server
     * @return the request String
     * @throws UnconfiguredRequestException if the request has not been
     * properly configured
     */
    public String getRequestString() throws UnconfiguredRequestException {
        String toReturn = "Set " + keyValue + "\n"; //NOI18N
        return toReturn; //NOI18N
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
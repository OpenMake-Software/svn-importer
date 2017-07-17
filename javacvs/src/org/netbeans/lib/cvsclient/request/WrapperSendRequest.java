/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.cvsclient.request;

/**
 * This class encapsulates the wrapper-sendme-rcsOptions request in the 
 * CVS client-server protocol. This request is used by the client to get 
 * the wrapper settings on the server.
 * @author  Sriram Seshan
 */
public class WrapperSendRequest extends Request {
    
    /** Get the request String that will be passed to the server
     * @return the request String
     * @throws UnconfiguredRequestException if the request has not been
     * properly configured
     *
     */
    public String getRequestString() throws UnconfiguredRequestException {
        return "wrapper-sendme-rcsOptions \n";
    }
    
    /** Is a response expected from the server?
     * @return true if a response is expected, false if no response if
     * expected
     *
     */
    public boolean isResponseExpected() {
        return true;
    }
    
}

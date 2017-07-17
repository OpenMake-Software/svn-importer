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
 * This exception indicates that a request has not been properly configured
 * hence cannot be sent to the server for processing
 * @author  Robert Greig
 */
public class UnconfiguredRequestException extends Exception {
    public UnconfiguredRequestException() {
        super();
    }

    public UnconfiguredRequestException(String s) {
        super(s);
    }
}
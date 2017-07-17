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
package org.netbeans.lib.cvsclient.connection;

import java.util.*;

import org.netbeans.lib.cvsclient.util.*;

/**
 * This exception is thrown when a connection with the server cannot be made,
 * for whatever reason.
 * It may be that the username and/or password are incorrect or it could be
 * that the port number is incorrect. Note that authentication is not
 * restricted here to mean security.
 * @author  Robert Greig
 */
public class AuthenticationException extends Exception {
    /**
     * The underlying cause of this exception, if any.
     */
    private Throwable underlyingThrowable;

    private String message;

    private String localizedMessage;

    /**
     * Construct an AuthenticationException with a message giving more details
     * of what went wrong.
     * @param message the message describing the error
     **/
    public AuthenticationException(String message, String localizedMessage) {
        super(message);
        this.message = message;
        this.localizedMessage = localizedMessage;
    }

    /**
     * Construct an AuthenticationException with a message and an
     * underlying exception.
     * @param message the message describing what went wrong
     * @param e the underlying exception
     */
    public AuthenticationException(String message,
                                   Throwable underlyingThrowable,
                                   String localizedMessage) {
        this(message, localizedMessage);
        this.underlyingThrowable = underlyingThrowable;
    }

    /**
     * Construct an AuthenticationException with an underlying
     * exception.
     * @param t the underlying throwable that caused this exception
     */
    public AuthenticationException(Throwable underlyingThrowable,
                                   String localizedMessage) {
        this.underlyingThrowable = underlyingThrowable;
        this.localizedMessage = localizedMessage;
    }

    /**
     * Get the underlying throwable that is responsible for this exception.
     * @return the underlying throwable, if any (may be null).
     */
    public Throwable getUnderlyingThrowable() {
        return underlyingThrowable;
    }

    public String getLocalizedMessage() {
        if (localizedMessage == null) {
            return message;
        }
        return localizedMessage;
    }

    public String getMessage() {
        return message;
    }

    protected static String getBundleString(String key) {
        String value = null;
        try {
            ResourceBundle bundle = BundleUtilities.getResourceBundle(AuthenticationException.class, "Bundle"); //NOI18N
            if (bundle != null) {
                value = bundle.getString(key);
            }
        }
        catch (MissingResourceException exc) {
        }
        return value;
    }
}

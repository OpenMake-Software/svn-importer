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
package org.netbeans.lib.cvsclient.command;

import java.io.*;
import java.text.*;
import java.util.*;

import org.netbeans.lib.cvsclient.util.*;

/**
 * This exception is thrown when an error occurs while executing a command.
 * It is nearly always a container for another exception.
 * @author  Robert Greig
 */
public class CommandException extends Exception {
    private Exception underlyingException;
    private String localizedMessage;
    private String message;

    public CommandException(Exception underlyingException, String localizedMessage) {
        this.underlyingException = underlyingException;
        this.localizedMessage = localizedMessage;
    }

    public CommandException(String message, String localizedMessage) {
        super(message);
        this.message = message;
        this.localizedMessage = localizedMessage;
    }

    public Exception getUnderlyingException() {
        return underlyingException;
    }

    public void printStackTrace() {
        if (underlyingException != null) {
            underlyingException.printStackTrace();
        }
        else {
            super.printStackTrace();
        }
    }

    public void printStackTrace(PrintStream stream) {
        if (underlyingException != null) {
            underlyingException.printStackTrace(stream);
        }
        else {
            super.printStackTrace(stream);
        }
    }

    public void printStackTrace(PrintWriter writer) {
        if (underlyingException != null) {
            underlyingException.printStackTrace(writer);
        }
        else {
            super.printStackTrace(writer);
        }
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
            ResourceBundle bundle = BundleUtilities.getResourceBundle(CommandException.class, "Bundle"); // NOI18N
            if (bundle != null) {
                value = bundle.getString(key);
            }
        }
        catch (MissingResourceException exc) {
        }
        return value;
    }

    public static String getLocalMessage(String key) {
        return getLocalMessage(key, null);
    }

    public static String getLocalMessage(String key, Object[] arguments) {
        String locMessage = CommandException.getBundleString(key);
        if (locMessage == null) {
            return null;
        }
        if (arguments != null) {
            locMessage = MessageFormat.format(locMessage, arguments);
        }
        return locMessage;
    }
}

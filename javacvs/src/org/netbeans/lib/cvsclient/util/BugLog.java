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
package org.netbeans.lib.cvsclient.util;

/**
 * Assertion tool class.
 * @author  Thomas Singer
 */
public class BugLog {

    private static BugLog instance;

    public synchronized static BugLog getInstance() {
        if (instance == null) {
            instance = new BugLog();
        }
        return instance;
    }

    public synchronized static void setInstance(BugLog instance) {
        BugLog.instance = instance;
    }

    public BugLog() {
    }

    public void showException(Exception ex) {
        ex.printStackTrace();
    }

    public void assertTrue(boolean value, String message) {
        if (value) {
            return;
        }

        throw new BugException(message);
    }

    public void assertNotNull(Object obj) {
        if (obj != null) {
            return;
        }

        throw new BugException("Value must not be null!"); // NOI18N
    }

    public void bug(String message) {
        new Exception(message).printStackTrace();
    }

    public static class BugException extends RuntimeException {
        public BugException(String message) {
            super(message);
        }
    }
}

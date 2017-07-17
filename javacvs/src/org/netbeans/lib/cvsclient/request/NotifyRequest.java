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

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

/**
 * Notify Entry.java
 *  E   Sun Nov 11 10:25:40 2001 GMT    worker  E:\test\admin   EUC
 *
 * @author  Thomas Singer
 * @version Nov 14, 2001
 */
public class NotifyRequest extends Request {
    // Constants ==============================================================

    private static final DateFormat DATE_FORMAT;
    private static final String HOST_NAME;

    static {
        DATE_FORMAT = new SimpleDateFormat("EEE MMM dd hh:mm:ss yyyy z", Locale.US);

        // detect host name
        String hostName = "";
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        HOST_NAME = hostName;
    }

    // Fields =================================================================

    private final String request;

    // Setup ==================================================================

    /**
     * Creates an NotifyRequest for the specified file.
     * If the specified file is null, a IllegalArgumentException is thrown.
     */
    public NotifyRequest(File file, String command, String parameters) {
        if (file == null) {
            throw new IllegalArgumentException("File must not be null!");
        }

        StringBuffer buffer = new StringBuffer();
        buffer.append("Notify "); // NOI18N
        buffer.append(file.getName());
        buffer.append('\n');
        buffer.append(command);
        buffer.append('\t');
        buffer.append(DATE_FORMAT.format(new Date()));
        buffer.append('\t');
        buffer.append(HOST_NAME);
        buffer.append('\t');
        buffer.append(file.getParent());
        buffer.append('\t');
        buffer.append(parameters);
        buffer.append('\n');
        this.request = buffer.toString();
    }

    // Implemented ============================================================

    public String getRequestString() {
        return request;
    }

    public boolean isResponseExpected() {
        return false;
    }
}

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
package org.netbeans.lib.cvsclient.util;

import java.io.*;

/**
 * Handles the logging of communication to and from the server
 * @author  Robert Greig
 */
public final class Logger {
    /**
     * The writer to use to write communication sent to the server
     */
    private static Writer outLog;

    /**
     * The writer to use to write communication received from the server
     */
    private static Writer inLog;

    /**
     * The log files path. If the property is set to the constant "system"
     * then it uses System.err, otherwise it tries to create a file at the
     * specified path
     */
    private static final String LOG_PROPERTY = "cvsClientLog"; // NOI18N

    /**
     * Whether we are logging or not
     */
    private static boolean logging;

    static {
        setLogging(System.getProperty(LOG_PROPERTY));
    }

    public static void setLogging(String logPath) {
        logging = (logPath != null);

        try {
            if (logging) {
                if (logPath.equals("system")) { // NOI18N
                    outLog = new BufferedWriter(new OutputStreamWriter(System.err));
                    inLog = new BufferedWriter(new OutputStreamWriter(System.err));
                }
                else {
                    outLog = new BufferedWriter(new FileWriter(logPath + ".out")); // NOI18N
                    inLog = new BufferedWriter(new FileWriter(logPath + ".in")); // NOI18N
                }
            }
        }
        catch (IOException e) {
            System.err.println("Unable to create log files: " + e); // NOI18N
            System.err.println("Logging DISABLED"); // NOI18N
            logging = false;
            try {
                if (outLog != null) {
                    outLog.close();
                }
            }
            catch (IOException ex2) {
                // ignore, if we get one here we really are screwed
            }

            try {
                if (inLog != null) {
                    inLog.close();
                }
            }
            catch (IOException ex2) {
                // ignore, if we get one here we really are screwed
            }
        }
    }

    /**
     * Log a message received from the server. The message is logged if
     * logging is enabled
     * @param received the data received from the server
     */
    public static void logInput(String received) {
        if (!logging) {
            return;
        }

        try {
            inLog.write(received);
            inLog.flush();
        }
        catch (IOException ex) {
            System.err.println("Could not write to log file: " + ex); // NOI18N
            System.err.println("Logging DISABLED."); // NOI18N
            logging = false;
        }
    }

    /**
     * Log a character received from the server. The message is logged if
     * logging is enabled
     * @param received the data received from the server
     */
    public static void logInput(char received) {
        if (!logging) {
            return;
        }

        try {
            inLog.write(received);
            inLog.flush();
        }
        catch (IOException ex) {
            System.err.println("Could not write to log file: " + ex); // NOI18N
            System.err.println("Logging DISABLED."); // NOI18N
            logging = false;
        }
    }

    /**
     * Log a message sent to the server. The message is logged if
     * logging is enabled
     * @param sent the data sent to the server
     */
    public static void logOutput(String sent) {
        if (!logging) {
            return;
        }

        try {
            outLog.write(sent);
            outLog.flush();
        }
        catch (IOException ex) {
            System.err.println("Could not write to log file: " + ex); // NOI18N
            System.err.println("Logging DISABLED."); // NOI18N
            logging = false;
        }
    }
}


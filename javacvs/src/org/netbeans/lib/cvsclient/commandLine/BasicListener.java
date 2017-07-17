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
package org.netbeans.lib.cvsclient.commandLine;

import java.io.*;

import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.event.*;

/**
 * A basic implementation of a CVS listener. Is really only interested in
 * message events. This listener is suitable for command line clients and
 * clients that don't "persist".
 * @author  Robert Greig
 */
public class BasicListener extends CVSAdapter {
    private final StringBuffer taggedLine = new StringBuffer();
    private PrintStream stdout;
    private PrintStream stderr;
    
    public BasicListener() {
        this(System.out, System.err);
    }
    
    public BasicListener(PrintStream stdout, PrintStream stderr) {
        this.stdout = stdout;
        this.stderr = stderr;
    }

    /**
     * Called when the server wants to send a message to be displayed to
     * the user. The message is only for information purposes and clients
     * can choose to ignore these messages if they wish.
     * @param e the event
     */
    public void messageSent(MessageEvent e) {
        String line = e.getMessage();
        if (e instanceof EnhancedMessageEvent) {
            return ;
        }
        PrintStream stream = e.isError() ? stderr : stdout;

        if (e.isTagged()) {
            String message = MessageEvent.parseTaggedMessage(taggedLine, e.getMessage());
            if (message != null) {
                stream.println(message);
            }
        }
        else {
            stream.println(line);
        }
    }

    /**
     * Called when the server wants to send a binary message to be displayed to
     * the user. The message is only for information purposes and clients
     * can choose to ignore these messages if they wish.
     * @param e the event
     */
    public void messageSent(BinaryMessageEvent e) {
        byte[] bytes = e.getMessage();
        try {
            stdout.write(bytes);
        } catch (IOException ioex) {
            ioex.printStackTrace();
        }
    }

    /**
     * Called when file status information has been received
     */
    public void fileInfoGenerated(FileInfoEvent e) {
//      FileInfoContainer fileInfo = e.getInfoContainer();
//        if (fileInfo.getClass().equals(StatusInformation.class)) {
//          System.err.println("A file status event was received.");
//          System.err.println("The status information object is: " +
//                             fileInfo);
//        }
    }
}
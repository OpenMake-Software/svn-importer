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
 * The request for a command.
 * Always a response is expected.
 *
 * @author  Thomas Singer
 */
public class CommandRequest extends Request {

    public static final CommandRequest ADD = new CommandRequest("add\n"); //NOI18N
    public static final CommandRequest ANNOTATE = new CommandRequest("annotate\n"); //NOI18N
    public static final CommandRequest CHECKOUT = new CommandRequest("co\n"); //NOI18N
    public static final CommandRequest COMMIT = new CommandRequest("ci\n"); //NOI18N
    public static final CommandRequest DIFF = new CommandRequest("diff\n"); //NOI18N
    public static final CommandRequest EDITORS = new CommandRequest("editors\n"); //NOI18N
    public static final CommandRequest EXPORT = new CommandRequest("export\n"); //NOI18N
    public static final CommandRequest HISTORY = new CommandRequest("history\n"); //NOI18N
    public static final CommandRequest IMPORT = new CommandRequest("import\n"); //NOI18N
    public static final CommandRequest LOG = new CommandRequest("log\n"); //NOI18N
    public static final CommandRequest NOOP = new CommandRequest("noop\n"); //NOI18N
    public static final CommandRequest RANNOTATE = new CommandRequest("rannotate\n"); //NOI18N
    public static final CommandRequest REMOVE = new CommandRequest("remove\n"); //NOI18N
    public static final CommandRequest RLOG = new CommandRequest("rlog\n"); //NOI18N
    public static final CommandRequest RTAG = new CommandRequest("rtag\n"); //NOI18N
    public static final CommandRequest STATUS = new CommandRequest("status\n"); //NOI18N
    public static final CommandRequest TAG = new CommandRequest("tag\n"); //NOI18N
    public static final CommandRequest UPDATE = new CommandRequest("update\n"); //NOI18N
    public static final CommandRequest WATCH_ADD = new CommandRequest("watch-add\n"); //NOI18N
    public static final CommandRequest WATCH_ON = new CommandRequest("watch-on\n"); //NOI18N
    public static final CommandRequest WATCH_OFF = new CommandRequest("watch-off\n"); //NOI18N
    public static final CommandRequest WATCH_REMOVE = new CommandRequest("watch-remove\n"); //NOI18N
    public static final CommandRequest WATCHERS = new CommandRequest("watchers\n"); //NOI18N

    private final String request;

    private CommandRequest(String request) {
        this.request = request;
    }

    /**
     * Get the request String that will be passed to the server.
     */
    public String getRequestString() {
        return request;
    }

    /**
     * Returns true if a response from the server is expected.
     */
    public boolean isResponseExpected() {
        return true;
    }
}

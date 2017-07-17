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
package org.netbeans.lib.cvsclient.command;


/**
 * @author  Thomas Singer
 * @version Dec 2, 2001
 */
public class Watch {
    public static final Watch EDIT = new Watch("edit", "E", // NOI18N
                                               new String[]{"edit"}); // NOI18N

    public static final Watch UNEDIT = new Watch("unedit", "U", // NOI18N
                                                 new String[]{"unedit"}); // NOI18N

    public static final Watch COMMIT = new Watch("commit", "C", // NOI18N
                                                 new String[]{"commit"}); // NOI18N

    public static final Watch ALL = new Watch("all", "EUC", // NOI18N
                                              new String[]{"edit", "unedit", "commit"}); // NOI18N

    public static final Watch NONE = new Watch("none", "", // NOI18N
                                               new String[0]);

    /**
     * Returns the temporary watch value used in the Notify request.
     */
    public static String getWatchString(Watch watch) {
        if (watch == null) {
            return NONE.getValue();
        }
        return watch.getValue();
    }

    private final String name;
    private final String value;
    private final String[] arguments;

    private Watch(String name, String value, String[] arguments) {
        this.name = name;
        this.value = value;
        this.arguments = arguments;
    }

    public String[] getArguments() {
        return arguments;
    }

    public String toString() {
        return name;
    }

    private String getValue() {
        return value;
    }
}

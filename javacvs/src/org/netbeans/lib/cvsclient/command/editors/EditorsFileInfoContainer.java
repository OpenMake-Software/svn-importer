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

package org.netbeans.lib.cvsclient.command.editors;

import java.io.*;
import java.util.*;

import org.netbeans.lib.cvsclient.command.*;

/**
 * Data object created by parsing the output of the Editors command.
 * @author  Thomas Singer
 */
public class EditorsFileInfoContainer extends FileInfoContainer {

    private final String client;
    private final Date date;
    private final File file;
    private final String user;

    EditorsFileInfoContainer(File file, String user, Date date, String client) {
        this.file = file;
        this.user = user;
        this.date = date;
        this.client = client;
    }

    public File getFile() {
        return file;
    }

    public String getClient() {
        return client;
    }

    public Date getDate() {
        return date;
    }

    public String getUser() {
        return user;
    }
}

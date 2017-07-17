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
package org.netbeans.lib.cvsclient.file;

import java.io.*;

import org.netbeans.lib.cvsclient.util.*;

/**
 * @author  Thomas Singer
 * @version Nov 17, 2001
 */
public class WindowsFileReadOnlyHandler
        implements FileReadOnlyHandler {
    // Implemented ============================================================

    /**
     * Makes the specified file read-only or writable, depending on the specified
     * readOnly flag.
     * @throws IOException if something gone wrong
     */
    public void setFileReadOnly(File file, boolean readOnly) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("file must not be null"); // NOI18N
        }

        try {
            String[] arguments = {
                "attrib", // NOI18N
                readOnly ? "+r": "-r", // NOI18N
                file.getName()
            };
            Process process = Runtime.getRuntime().exec(arguments, null, file.getParentFile());
            process.waitFor();
        }
        catch (InterruptedException ex) {
            BugLog.getInstance().showException(ex);
        }
    }
}

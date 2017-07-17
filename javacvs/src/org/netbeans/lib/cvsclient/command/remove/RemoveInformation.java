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
package org.netbeans.lib.cvsclient.command.remove;

import java.io.*;

import org.netbeans.lib.cvsclient.command.*;

/**
 * Describes remove information for a file. This is the result of doing a
 * cvs remove command. The fields in instances of this object are populated
 * by response handlers.
 *
 * @author  Milos Kleint
 */
public class RemoveInformation extends FileInfoContainer {
    private File file;
    private boolean removed;

    public RemoveInformation() {
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setRemoved(boolean rem) {
        removed = rem;
    }

    public boolean isRemoved() {
        return removed;
    }

    /**
     * Return a string representation of this object. Useful for debugging.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer(30);
        buf.append("  "); //NOI18N
        buf.append((file != null)
                           ? file.getAbsolutePath()
                           :"null"); //NOI18N
        return buf.toString();
    }
}

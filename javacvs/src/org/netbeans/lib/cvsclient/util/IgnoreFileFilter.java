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

import java.io.*;

/**
 * interface for recognizing if the local files are to be ignored.
 * Implements the functionality of the .cvsignore files..
 *
 * @author  Milos Kleint
 */
public interface IgnoreFileFilter {

    /**
     * A file is checked against the patterns in the filter.
     * If any of these matches, the file should be ignored.
     *
     * @param directory is a file object that refers to the directory the file resides in.
     * @param noneCvsFile is the name of the file to be checked.
     */
    boolean shouldBeIgnored(File directory, String noneCvsFile);
}


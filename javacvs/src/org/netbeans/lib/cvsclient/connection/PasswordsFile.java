/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.cvsclient.connection;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Represents passwords file.
 *
 * @author Petr Kuzel
 */
public final class PasswordsFile {

    /**
     * Locates scrambled password for given CVS Root.
     *
     * @param cvsRootString [:method:][[user][:password]@][hostname[:[port]]]/path/to/repository
     * @return scampled password or <code>null</code>
     */
    public static String findPassword(String cvsRootString) {
        File passFile = new File(System.getProperty("cvs.passfile", System.getProperty("user.home") + "/.cvspass"));
        BufferedReader reader = null;
        String password = null;

        String CVSRootWithPort = cvsRootString;
        try {
            reader = new BufferedReader(new FileReader(passFile));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("/1 ")) line = line.substring("/1 ".length());
                if (line.startsWith(cvsRootString+" ")) {
                    password = line.substring(cvsRootString.length() + 1);
                    break;
                } else if (line.startsWith(CVSRootWithPort+" ")) {
                    password = line.substring(CVSRootWithPort.length() + 1);
                    break;
                }
            }
        } catch (IOException e) {
            return null;
        }
        finally {
            if (reader != null) {
                try { reader.close(); } catch (IOException e) {}
            }
        }
        return password;

    }
}

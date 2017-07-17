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
package org.netbeans.lib.cvsclient.connection;

import java.io.*;
import java.util.zip.*;

import org.netbeans.lib.cvsclient.util.*;

/**
 * This class modifies a connection by gzipping all client/server communication
 * @author  Robert Greig
 */
public class GzipModifier extends Object implements ConnectionModifier {
    /**
     * Creates new GzipModifier
     */
    public GzipModifier() {
    }

    public void modifyInputStream(LoggedDataInputStream ldis)
            throws IOException {
//        System.err.println("Setting the underlying stream for the IS");
        GZIPInputStream gzis = new GZIPInputStream(ldis.
                                                   getUnderlyingStream());
//        System.err.println("Finished constructing the gzipinputstream");
        ldis.setUnderlyingStream(gzis);
    }

    public void modifyOutputStream(LoggedDataOutputStream ldos)
            throws IOException {
//        System.err.println("Setting the underlying stream for the OS");
        ldos.setUnderlyingStream(new GZIPOutputStream(ldos.
                                                      getUnderlyingStream()));
    }
}

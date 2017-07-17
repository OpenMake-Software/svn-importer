/*****************************************************************************
 * Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is available at http://www.sun.com/
 *
 * The Original Code is the CVS Client Library.
 * The Initial Developer of the Original Code is Robert Greig.
 *
 * Contributor(s): Robert Greig, Milos Kleint.
 *****************************************************************************/
package org.netbeans.lib.cvsclient.file;

import java.io.*;
import java.util.zip.*;

import org.netbeans.lib.cvsclient.request.*;

/**
 * Handles the reading and writing of Compressed files to and from the
 * server.
 * @author  Milos Kleint
 */
public class GzippedFileHandler extends DefaultFileHandler {
    /**
     * Indicates whether the file is actually compressed
     */
    private boolean isCompressed;

    /**
     * Get any requests that must be sent before commands are sent, to init
     * this file handler.
     * @return an array of Requests that must be sent
     */
    public Request[] getInitialisationRequests() {
        return new Request[]{
            new GzipStreamRequest()
        };
    }

    protected Reader getProcessedReader(File f) throws IOException {
        return new InputStreamReader(new GZIPInputStream(new
                FileInputStream(f)));
    }

    protected InputStream getProcessedInputStream(File f) throws IOException {
        return new GZIPInputStream(new FileInputStream(f));
    }
}
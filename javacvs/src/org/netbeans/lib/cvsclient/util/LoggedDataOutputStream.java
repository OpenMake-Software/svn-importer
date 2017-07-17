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
package org.netbeans.lib.cvsclient.util;

import java.io.*;

/**
 * A data output stream that also logs everything sent to a Writer (via the
 * logger).
 * @author  Robert Greig
 */
public class LoggedDataOutputStream extends FilterOutputStream {

    /**
     * Construct a logged stream using the specified underlying stream
     * @param in the stream
     */
    public LoggedDataOutputStream(OutputStream out) {
        super(out);
    }

    /**
     * Write a line to the stream, logging it too. For compatibility reasons
     * only. Does exactly the same what writeBytes() does.
     */
    public void writeChars(String line) throws IOException {
        writeBytes(line);
    }
    
    /**
     * Write a line to the stream, logging it too
     */
    public void writeBytes(String line) throws IOException {
        byte[] bytes = line.getBytes();
        out.write(bytes);
        Logger.logOutput(line);
    }

    /**
     * Closes this input stream and releases any system resources associated
     * with the stream.
     */
    public void close() throws IOException {
        out.close();
    }

    public OutputStream getUnderlyingStream() {
        return out;
    }

    public void setUnderlyingStream(OutputStream os) {
        out = os;
    }
}
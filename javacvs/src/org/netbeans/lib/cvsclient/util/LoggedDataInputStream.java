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
 * This input stream worked exactly like the normal DataInputStream except that
 * it logs anything read to a file
 * @author  Robert Greig
 */
public class LoggedDataInputStream extends FilterInputStream {

    /**
     * Construct a logged stream using the specified underlying stream
     * @param in the stream
     */
    public LoggedDataInputStream(InputStream in) {
        super(in);
    }

    /**
     * Read a line (up to the newline character) from the stream, logging
     * it too
     */
    public String readLine() throws IOException {
        int ch;
        ByteArray byteArray = new ByteArray();
        loop: while (true) {
            if (Thread.interrupted()) {
                Thread.currentThread().interrupt();
                break;
            }
            if (in.available() == 0) {
                try {
                    Thread.currentThread().sleep(100);
                } catch (InterruptedException iex) {
                    Thread.currentThread().interrupt();
                    break loop;
                }
                continue;
            }
            ch = in.read();
            switch (ch) {
                case -1:
                case '\n':
                    break loop;
                default:
                    byteArray.add((byte) ch);
            }
        }
        final String value = byteArray.getStringFromBytes();//dis.readLine();
        Logger.logInput(value + "\n"); //NOI18N
        return value;
    }

    /**
     * Closes this input stream and releases any system resources associated
     * with the stream.
     */
    public void close() throws IOException {
        in.close();
    }

    /**
     * Reads up to byte.length bytes of data from this input stream into an
     * array of bytes.
     */
    public int read(byte[] b) throws IOException {
        return in.read(b);
    }

    /**
     * Reads up to len bytes of data from this input stream into an array of
     * bytes
     */
    public int read(byte[] b, int off, int len) throws IOException {
        return in.read(b, off, len);
    }

    /**
     * Read a single byte from this stream
     */
    public byte readByte() throws IOException {
		while (in.available() == 0) {
            try {
                Thread.currentThread().sleep(100);
            } catch (InterruptedException iex) {
                Thread.currentThread().interrupt();
                return '\0';
            }
        }
        byte val = (byte) in.read();
        Logger.logInput((char)val);
        return val;
    }

    public InputStream getUnderlyingStream() {
        return in;
    }

    public void setUnderlyingStream(InputStream is) {
        in = is;
    }
}
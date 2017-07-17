/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.cvsclient.file;

import java.io.*;

/**
 * @author  Thomas Singer
 * @version Sep 26, 2001
 */
public class DefaultWriteTextFilePreprocessor
        implements WriteTextFilePreprocessor {

    private static final int CHUNK_SIZE = 32768;
    
    public void copyTextFileToLocation(InputStream processedInputStream, File fileToWrite) throws IOException {
        // Here we read the temp file in again, doing any processing required
        // (for example, unzipping). We must not convert the bytes to characters
        // because the file may not be written in the current encoding.
        // We would corrupt it's content when characters would be written!
        InputStream tempInput = null;
        OutputStream out = null;
        byte[] newLine = System.getProperty("line.separator").getBytes();
        try {
            tempInput = new BufferedInputStream(processedInputStream);
            out = new BufferedOutputStream(new FileOutputStream(fileToWrite));
            // this chunk is directly read from the temp file
            byte[] cchunk = new byte[CHUNK_SIZE];
            for (int readLength = tempInput.read(cchunk);
                 readLength > 0;
                 readLength = tempInput.read(cchunk)) {

                // we must perform our own newline conversion. The file will
                // definitely have unix style CRLF conventions, so if we have
                // a \n this code will write out a \n or \r\n as appropriate for
                // the platform we are running on
                for (int i = 0; i < readLength; i++) {
                    if (cchunk[i] == '\n') {
                        out.write(newLine);
                    }
                    else {
                        out.write(cchunk[i]);
                    }
                }
            }
        }
        finally {
            if (tempInput != null) {
                try {
                    tempInput.close();
                }
                catch (IOException ex) {
                    // ignore
                }
            }
            if (out != null) {
                try {
                    out.close();
                }
                catch (IOException ex) {
                    // ignore
                }
            }
        }
    }
}

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
public class DefaultTransmitTextFilePreprocessor
        implements TransmitTextFilePreprocessor {

    private static final int CHUNK_SIZE = 32768;
    
    private File tempDir;
    
    public void setTempDir(File tempDir) {
        this.tempDir = tempDir;
    }
    
    public File getPreprocessedTextFile(File originalTextFile) throws IOException {
        // must write file to temp location first because size might change
        // due to CR/LF changes
        File preprocessedTextFile = File.createTempFile("cvs", null, tempDir); // NOI18N

        byte[] newLine = System.getProperty("line.separator").getBytes();
        boolean doConversion = newLine.length != 1 || newLine[0] != '\n';
        
        OutputStream out = null;
        InputStream in = null;

        try {
            in = new BufferedInputStream(new FileInputStream(originalTextFile));
            out = new BufferedOutputStream(new FileOutputStream(preprocessedTextFile));

            byte[] fileChunk = new byte[CHUNK_SIZE];
            byte[] fileWriteChunk = new byte[CHUNK_SIZE];

            for (int readLength = in.read(fileChunk);
                 readLength > 0;
                 readLength = in.read(fileChunk)) {

                if (doConversion) {
                    int writeLength = 0;
                    for (int i = 0; i < readLength; ) {
                        int pos = findIndexOf(fileChunk, newLine, i);
                        if (pos >= i && pos < readLength) {
                            System.arraycopy(fileChunk, i, fileWriteChunk, writeLength, pos - i);
                            writeLength += pos - i;
                            i = pos + newLine.length;
                            fileWriteChunk[writeLength++] = '\n';
                        } else {
                            System.arraycopy(fileChunk, i, fileWriteChunk, writeLength, readLength - i);
                            writeLength += readLength - i;
                            i = readLength;
                        }
                    }
                    out.write(fileWriteChunk, 0, writeLength);
                } else {
                    out.write(fileChunk, 0, readLength);
                }
            }
            return preprocessedTextFile;
        }
        catch (IOException ex) {
            if (preprocessedTextFile != null) {
                cleanup(preprocessedTextFile);
            }
            throw ex;
        }
        finally {
            if (in != null) {
                try {
                    in.close();
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
        
    private static int findIndexOf(byte[] array, byte[] pattern, int start) {
        int subPosition = 0;
        for (int i = start; i < array.length; i++) {
            if (array[i] == pattern[subPosition]) {
                if (++subPosition == pattern.length) {
                    return i - subPosition + 1;
                }
            } else {
                subPosition = 0;
            }
        }
        return -1;
    }

    public void cleanup(File preprocessedTextFile) {
        if (preprocessedTextFile != null) {
            preprocessedTextFile.delete();
        }
    }
    
}

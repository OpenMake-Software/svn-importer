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

/**
 * A utility class for file based operations.
 *
 * @author  Thomas Singer
 * @version Nov 23, 2001
 */
public class FileUtils {
    private static FileReadOnlyHandler fileReadOnlyHandler;

    /**
     * Returns the current FileReadOnlyHandler used by setFileReadOnly().
     */
    public static FileReadOnlyHandler getFileReadOnlyHandler() {
        return fileReadOnlyHandler;
    }

    /**
     * Sets the specified fileReadOnlyHandler to be used with setFileReadOnly().
     */
    public static void setFileReadOnlyHandler(FileReadOnlyHandler fileReadOnlyHandler) {
        FileUtils.fileReadOnlyHandler = fileReadOnlyHandler;
    }

    /**
     * Sets the specified file read-only (readOnly == true) or writable (readOnly == false).
     * If no fileReadOnlyHandler is set, nothing happens.
     *
     * @throws IOException if the operation failed
     */
    public static void setFileReadOnly(File file, boolean readOnly) throws IOException {
        if (getFileReadOnlyHandler() == null) {
            return;
        }

        getFileReadOnlyHandler().setFileReadOnly(file, readOnly);
    }

    /**
     * Copies the specified sourceFile to the specified targetFile.
     */
    public static void copyFile(File sourceFile, File targetFile) throws IOException {
        if (sourceFile == null || targetFile == null) {
            throw new NullPointerException("sourceFile and targetFile must not be null"); // NOI18N
        }

        // ensure existing parent directories
        File directory = targetFile.getParentFile();
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Could not create directory '" + directory + "'"); // NOI18N
        }

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(sourceFile));
            outputStream = new BufferedOutputStream(new FileOutputStream(targetFile));

            byte[] buffer = new byte[32768];
            for (int readBytes = inputStream.read(buffer);
                 readBytes > 0;
                 readBytes = inputStream.read(buffer)) {
                outputStream.write(buffer, 0, readBytes);
            }
        }
        finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                }
                catch (IOException ex) {
                    // ignore
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                }
                catch (IOException ex) {
                    // ignore
                }
            }
        }
    }

    /**
     * This utility class needs not to be instantiated anywhere.
     */
    private FileUtils() {
    }
}

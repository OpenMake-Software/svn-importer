/*
 * Copyright (c) 2004, 2005 Polarion Software, All rights reserved. 
 * Email: community@polarion.org
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the Apache License, Version 2.0 (the "License"). You may not use 
 * this file except in compliance with the License. Copy of the License is
 * located in the file LICENSE.txt in the project distribution. You may also
 * obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  
 * POLARION SOFTWARE MAKES NO REPRESENTATIONS OR WARRANTIES
 * ABOUT THE SUITABILITY OF THE SOFTWARE, EITHER EXPRESSED OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. POLARION SOFTWARE
 * SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT
 * OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 * 
 */
/*
 * $Log$
 */

package org.polarion.svnimporter.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.Enumeration;

/**
 * 
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class Util {
    private static final Log LOG = Log.getLog(Util.class);

    private static final DateFormat DEFAULT_DATE_FORMAT = SimpleDateFormat.getDateTimeInstance();

    private static final int BUFFER_SIZE = 2048;

    public static String toString(Date date) {
        if (date == null)
            return null;
        return DEFAULT_DATE_FORMAT.format(date);
    }


    /**
     * Convert message digest (usually MD5) to string format
     *
     * @param md
     * @return
     */
    public static String toString(MessageDigest md) {
        byte[] buf = md.digest();
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(0xFF & buf[i]);
            if (hex.length() == 1) {
                hexString.append("0" + hex);
            } else {
                hexString.append(hex);
            }
        }
        return hexString.toString();
    }


    public static String toString(Collection c) {
        StringBuffer b = new StringBuffer();
        for (Iterator i = c.iterator(); i.hasNext();) {
            if (b.length() > 0) b.append(", ");
            b.append(i.next().toString());
        }
        return b.toString();
    }


    public static String toString(long val, int stringLength) {
        StringBuffer b = new StringBuffer();
        b.append(val);
        while (b.length() < stringLength)
            b.insert(0, " ");
        return b.toString();
    }

    public static String toString(String[] arr, String separator) {
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < arr.length; i++) {
            String s = arr[i];
            if (b.length() > 0) b.append(separator);
            b.append(s);
        }
        return b.toString();
    }

    public static String escape(String s) {
        if (s == null) return null;
        s = s.replaceAll("\n", "\\\\n");
        s = s.replaceAll("\r", "\\\\r");
        return s;
    }

    /**
     * Load properties from file
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static Properties loadProperties(String file) throws IOException {
        return loadProperties(new FileInputStream(file));
    }

    /**
     * Load properties from file
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static Properties loadProperties(File file) throws IOException {
        return loadProperties(new FileInputStream(file));
    }

    /**
     * Load properties from input stream
     *
     * @param in
     * @return
     * @throws IOException
     */
    public static Properties loadProperties(InputStream in) throws IOException {
        try {
            Properties p = new Properties();
            p.load(in);
            return p;
        } finally {
            close(in);
        }
    }

    public static void close(InputStream in) {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                LOG.error(e);
            }
        }
    }

    public static void close(OutputStream in) {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                LOG.error(e);
            }
        }
    }

    public static void close(Reader in) {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                LOG.error(e);
            }
        }
    }

    /**
     * Delete file or folder with all including files
     *
     * @return true if operation successful
     */
    public static boolean delete(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    if (!delete(files[i])) {
                        return false;
                    }
                }
            }
        }
        return file.delete();
    }

    /**
     * Save string buffer to file
     *
     * @param file
     * @param s
     * @throws FileNotFoundException
     */
    public static void save(File file, StringBuffer s) throws FileNotFoundException {
        PrintWriter out = new PrintWriter(new FileOutputStream(file));
        try {
            out.write(s.toString());
        } finally {
            out.close();
        }
    }

    public static String toString(Collection collection, String separator) {
        StringBuffer b = new StringBuffer();
        for (Iterator j = collection.iterator(); j.hasNext();) {
            String s = (String) j.next();
            if (b.length() > 0) b.append(separator);
            b.append(s);
        }
        return b.toString();
    }

    public static String getHash(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.reset();
            md.update(str.getBytes("UTF-8"));
            byte[] bytes = md.digest();
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < bytes.length; i++) {
                String hex = Integer.toHexString(0xFF & bytes[i]);
                if (hex.length() == 1) {
                    hexString.append("0" + hex);
                } else {
                    hexString.append(hex);
                }
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            LOG.error("can't generate hash for " + str + "'", e);
        } catch (UnsupportedEncodingException e) {
            LOG.error("can't generate hash for '" + str + "'", e);
        }
        return null;
    }

    public static String getClassName(Class c) {
        String name = c.getName();
        return name.substring(c.getPackage().getName().length() + 1);
    }

    public static BufferedReader openReader(File file, String charset) {
        try {
            return new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));
        } catch (Exception e) {
            throw new CommonException("can't open file " + file.getAbsolutePath() + " (charset=" + charset + ")", e);
        }
    }

    public static BufferedReader openReader(File file) {
        try {
            return new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        } catch (FileNotFoundException e1) {
            throw new CommonException("can't open file " + file.getAbsolutePath(), e1);
        }
    }

    public static PrintStream openPrintStream(String file) {
        try {
            return new PrintStream(new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            LOG.error("can't open file: " + file, e);
            return null;
        }
    }

    /**
     * copy only $size bytes from in to out
     *
     * @param in
     * @param out
     * @throws IOException
     */
    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte buf[] = new byte[BUFFER_SIZE];
        int bytes;
        while ((bytes = in.read(buf)) != -1)
            out.write(buf, 0, bytes);
    }

    /**
     * Get exception's printable stack trace
     *
     * @param t
     * @return
     */
    public static String getStackTrace(Throwable t) {
        StringBuffer b = new StringBuffer();
        b.append(t + "\n");
        StackTraceElement[] trace = t.getStackTrace();
        for (int i = 0; i < trace.length; i++) {
            b.append("\tat " + trace[i] + "\n");
        }
        if (t.getCause() != null) {
            b.append(getCauseStackTrace(t.getCause(), 1));
        }
        return b.toString();
    }

    public static String getCauseStackTrace(Throwable t, int deep) {
        if (deep > 5) return "";
        StringBuffer b = new StringBuffer();
        b.append("Caused by: " + t + "\n");
        StackTraceElement[] trace = t.getStackTrace();
        for (int i = 0; i < trace.length; i++) {
            b.append("\tat " + trace[i] + "\n");
        }
        if (t.getCause() != null) {
            b.append(getCauseStackTrace(t.getCause(), deep + 1));
        }
        return b.toString();
    }

    public static void appendProperties(Properties source, Properties dest) {
        Enumeration e = source.propertyNames();
        while(e.hasMoreElements()) {
            String p = (String) e.nextElement();
            dest.setProperty(p, source.getProperty(p));
        }
    }

    public static String md5checksum(InputStream in) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buf = new byte[BUFFER_SIZE];
            try {
                int rsize;
                while ((rsize = in.read(buf)) != -1) {
                    md.update(buf, 0, rsize);
                }
            } finally {
                in.close();
            }
            return toString(md);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String md5checksum(File file) throws IOException {
        InputStream in = new FileInputStream(file);
        try {
            return md5checksum(in);
        } finally {
            close(in);
        }
    }
    
    /**
     * Clean up lables of any strange characters that would interfer
     * with them turning into file or directory names
     * @param label
     * @return
     */
    private static final String INVALID_FILE_CHARS = " :/\\";
    public static String cleanLabel(String label) {
        label = label.trim();
        if (label.equals(".")) {
            return "_DOT";
        } else if (label.equals("..")) {
            return "_DOT_DOT";
        } else {
            char[] carry = label.toCharArray();
            for (int j = 0; j<carry.length; j++) {
                if (INVALID_FILE_CHARS.indexOf(carry[j]) >= 0) {
                    carry[j] = '_';
                }
            }
            return new String(carry);
        }
    }
}

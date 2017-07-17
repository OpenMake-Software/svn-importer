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
import java.util.*;

/**
 * @author   Milos Kleint, Thomas Singer
 */
public class DefaultIgnoreFileFilter implements IgnoreFileFilter {
    private final List patterns = new LinkedList();

    private final List localPatterns = new LinkedList();
    private boolean processGlobalPatterns = true;
    private boolean processLocalPatterns = false;
    private File lastDirectory = null;
        
    public DefaultIgnoreFileFilter() {
    }

    /**
     * Creates new DefaultIgnoreFileFilter and fills in patterns.
     * @param patternList - list of objects, patterns are retrieved
     *                      via the Object.toString() method.
     */
    public DefaultIgnoreFileFilter(List patternList) {
        for (Iterator it = patternList.iterator(); it.hasNext();) {
            String patternString = it.next().toString();
            SimpleStringPattern pattern = new SimpleStringPattern(patternString);
            addPattern(pattern);
        }
    }

    /**
     * Adds a StringPattern to the list of ignore file patters.
     */
    public void addPattern(StringPattern pattern) {
        if (pattern.toString().equals("!")) { //NOI18N
            clearPatterns();
        }
        else {
            patterns.add(pattern);
        }
    }

    /**
     * Adds a string to the list of ignore file patters using the SimpleStringPattern.
     */
    public void addPattern(String pattern) {
        if (pattern.equals("!")) { //NOI18N
            clearPatterns();
        }
        else {
            patterns.add(new SimpleStringPattern(pattern));
        }
    }

    /**
     * Clears the list of patters.
     * To be used when the "!" character is used in any of the .cvsignore lists.
     */
    public void clearPatterns() {
        patterns.clear();
    }

    /**
     * A file is checked against the patterns in the filter.
     * If any of these matches, the file should be ignored. A file will also
     * be ignored, if its name matches any local <code>.cvsignore</code> file
     * entry.
     * @param directory is a file object that refers to the directory the file resides in.
     * @param noneCvsFile is the name of the file to be checked.
     */
    public boolean shouldBeIgnored(File directory, String noneCvsFile) {
        // current implementation ignores the directory parameter.
        // in future or different implementations can add the directory dependant .cvsignore lists
        if (lastDirectory != directory) {
            lastDirectory = directory;
            processGlobalPatterns = true;
            processLocalPatterns = false;
            localPatterns.clear();
            String filename = directory.getPath() + File.separator + ".cvsignore"; //NOI18N
            File cvsIgnoreFile = new File(filename);
            if (cvsIgnoreFile.exists()) {
                try {
                    List list = parseCvsIgnoreFile(cvsIgnoreFile);
                    for (Iterator it = list.iterator(); it.hasNext();) {
                        String s = it.next().toString();
                        if (s.equals("!")) { //NOI18N
                            processGlobalPatterns = false;
                            localPatterns.clear();
                        } else {
                            localPatterns.add(new SimpleStringPattern(s));
                        }
                    }
                }
                catch (IOException ex) {
                    // ignore exception
                }
            }
            processLocalPatterns = localPatterns.size() > 0;
        }
        if (processGlobalPatterns) {
            for (Iterator it = patterns.iterator(); it.hasNext();) {
                StringPattern pattern = (StringPattern)it.next();
                if (pattern.doesMatch(noneCvsFile)) {
                    return true;
                }
            }
        }
        if (processLocalPatterns) {
            for (Iterator it = localPatterns.iterator(); it.hasNext();) {
                StringPattern pattern = (StringPattern)it.next();
                if (pattern.doesMatch(noneCvsFile)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Utility method that reads the .cvsignore file and returns a list of Strings.
     * These strings represent the patterns read from the file.
     */
    public static List parseCvsIgnoreFile(File cvsIgnoreFile)
            throws IOException, FileNotFoundException {
        BufferedReader reader = null;
        List toReturn = new LinkedList();
        try {
            reader = new BufferedReader(new FileReader(cvsIgnoreFile));
            String line;
            while ((line = reader.readLine()) != null) {
                StringTokenizer token = new StringTokenizer(line, " ", false); //NOI18N
                while (token.hasMoreTokens()) {
                    String tok = token.nextToken();
                    toReturn.add(tok);
                }
            }
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }
        return toReturn;
    }
}

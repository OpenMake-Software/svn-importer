/*****************************************************************************
 * Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is available at http://www.sun.com/
 *
 * The Original Code is the CVS Client Library.
 * The Initial Developer of the Original Code is Thomas Singer.
 * Portions created by Robert Greig are Copyright (C) 2001.
 * All Rights Reserved.
 *
 * Contributor(s): Thomas Singer.
 *****************************************************************************/
package org.netbeans.lib.cvsclient.command;

import java.util.*;

/**
 * @author  Thomas Singer
 */
public class CommandUtils {
    /**
     * Returns the directory relative to local path from the specified message.
     * This method returns null, if the specified message isn't a EXAM_DIR-
     * message.
     */
    public static String getExaminedDirectory(String message, String examDirPattern) {
        final int index = message.indexOf(examDirPattern);
        final int startIndex = index + examDirPattern.length() + 1;
        if (index < 0 || message.length() < startIndex + 1) {
            return null;
        }

        return message.substring(startIndex);
    }
    
    /**
     * for a list of string will return the string that equals the name parameter.
     * To be used everywhere you need to have only one string occupying teh memory space,
     * eg. in Builders to have the revision number strings not repeatedly in memory.
     */
    public static String findUniqueString(String name, List list) {
        if (name == null) {
            return null;
        }
        int index = list.indexOf(name);
        if (index >= 0) {
            return (String)list.get(index);
        }
        else {
            String newName = new String(name);
            list.add(newName);
            return newName;
        }
    }    
}

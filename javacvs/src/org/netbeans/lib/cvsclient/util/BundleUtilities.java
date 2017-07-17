/*****************************************************************************
 * Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is available at http://www.sun.com/
 *
 * The Original Code is the CVS Client Library.
 * The Initial Developer of the Original Code is Thomas Singer.
 * Portions created by Thomas Singer are Copyright (C) 2001.
 * All Rights Reserved.
 *
 * Contributor(s): Thomas Singer.
 *****************************************************************************/
package org.netbeans.lib.cvsclient.util;

import java.util.*;

/**
 * @author  Thomas Singer
 * @version Sep 26, 2001
 */
public class BundleUtilities {

    /**
     * Returns the package name of the specified class.
     * An empty String is returned, if the class is in the default package.
     */
    public static String getPackageName(Class clazz) {
        String fullClassName = clazz.getName();
        int lastDotIndex = fullClassName.lastIndexOf('.');
        if (lastDotIndex < 0) {
            return ""; // NOI18N
        }
        return fullClassName.substring(0, lastDotIndex);
    }

    /**
     * Returns the resourcename for the resource' shortName relative to the classInSamePackage.
     */
    public static String getResourceName(Class classInSamePackage, String shortName) {
        String packageName = getPackageName(classInSamePackage);
        String resourceName = packageName.replace('.', '/') + '/' + shortName;
        return resourceName;
    }

    /**
     * Returns the resource bundle for the specified resource' shortName relative to classInSamePackage.
     */
    public static ResourceBundle getResourceBundle(Class classInSamePackage, String shortName) {
        String resourceName = getResourceName(classInSamePackage, shortName);
        return ResourceBundle.getBundle(resourceName);
    }
}

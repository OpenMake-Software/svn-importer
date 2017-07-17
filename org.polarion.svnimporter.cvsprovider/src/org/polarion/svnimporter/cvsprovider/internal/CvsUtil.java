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

package org.polarion.svnimporter.cvsprovider.internal;

import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.common.RevisionNumber;
import org.netbeans.lib.cvsclient.Client;

/**
 * @author <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class CvsUtil {
    private static final Log LOG = Log.getLog(CvsUtil.class);

    private static final char PATH_SEPARATOR_CHAR = '/';
    private static final String PATH_SEPARATOR = "" + PATH_SEPARATOR_CHAR;

    private static final String BRANCH_TAG_RE = "^[0-9.]+\\.0\\.[0-9]+$";
    private static final String VENDOR_TAG_RE = "^[0-9]+\\.[0-9]+\\.[0-9]+$";

    /**
     * return true if revision number is branch number
     *
     * @param revNum
     * @return
     */
    public static boolean isBranch(String revNum) {
        return revNum.matches(BRANCH_TAG_RE);
    }

    /**
     * return true if revision number is vendor tag number
     *
     * @param revNum
     * @return
     */
    public static boolean isVendorTag(String revNum) {
        return revNum.matches(VENDOR_TAG_RE);
    }

    /**
     * 1.7.0.2 -> 1.7.2
     *
     * @param branchRevNum
     * @return
     */
    public static String rawRevNum2BranchNum(String branchRevNum) {
        int[] na = RevisionNumber.parse(branchRevNum);
        if (na[na.length - 2] != 0)
            LOG.warn("Wrong branch revision: " + branchRevNum);
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < na.length; i++) {
            if (i == na.length - 2) continue; // skip '0'
            if (b.length() > 0) b.append(".");
            b.append(na[i]);
        }
        return b.toString();
    }

    /**
     * "1.2.3" -> "1.2"
     *
     * @param branchNumber
     * @return
     */
    public static String branchNum2sproutRevNum(String branchNumber) {
        int[] n = RevisionNumber.parse(branchNumber);
        return RevisionNumber.getSubNumber(n, 0, n.length - 1);
    }

    /**
     * Get branch number from revision number
     */
    public static String revNum2branchNum(String revNum) {
        int[] n = RevisionNumber.parse(revNum);
        return RevisionNumber.getSubNumber(n, 0, n.length - 1);
    }

    public static void close(Client client) {
        try {
            client.getConnection().close();
        } catch (Exception e) {
            LOG.error("can't close psconnection", e);
        }
    }

    /**
     * Split branches list
     *
     * @param s
     * @return
     */
    public static String[] splitBranches(String s) {
        return s.split("[ \t\n]*;[ \t\n]*");
    }

    /**
     * Determine relative repository filename
     *
     * @param repositoryPath   (Example: /var/cvs/)
     * @param absoluteFilepath (Example: /var/cvs/test4/dir/two,v)
     * @param moduleName       (Example: test4)
     * @return relative filename (without "attic" and ",v" suffix) (Example: dir/two)
     */
    public static String getRelativeFilename(String repositoryPath,
                                             String absoluteFilepath,
                                             String moduleName) {

        String path = absoluteFilepath.replaceAll(",v$", "").replaceAll("Attic/([^/]+)$", "$1");
        StringBuffer b = new StringBuffer();

        b.append(repositoryPath);
        if (b.charAt(b.length() - 1) != PATH_SEPARATOR_CHAR)
            b.append(PATH_SEPARATOR_CHAR);

        if (".".equals(moduleName)) {
            if (!absoluteFilepath.startsWith(b.toString())) {
                LOG.error("Wrong absoluteFilepath '" + absoluteFilepath + "': it doesn't start with repositoryPath "+repositoryPath);
                return null;
            }
            return path.substring(b.length());
        } else {
            b.append(moduleName);
            if (b.charAt(b.length() - 1) != PATH_SEPARATOR_CHAR)
                b.append(PATH_SEPARATOR_CHAR);
            if (!absoluteFilepath.startsWith(b.toString())) {
                LOG.error("Wrong absoluteFilepath '" + absoluteFilepath + "': it doesn't start with repositoryPath + module name");
                return null;
            }
            String relPath = path.substring(b.length());
            return relPath;
        }
    }

    /**
     * Get filename from path
     *
     * @param path
     * @return
     */
    public static String getFilename(String path) {
        int i;
        if ((i = path.lastIndexOf(PATH_SEPARATOR)) == -1) {
            return path;
        }
        if (i == path.length() - 1) {
            return null;
        }
        return path.substring(i + 1);
    }

    /**
     * Get CVS path
     *
     * @param moduleName
     * @param relativePath
     * @return
     */
    public static String getCvsPath(String moduleName, String relativePath) {
        if (".".equals(moduleName) || moduleName.length() == 0) {
            return relativePath;
        }
        return moduleName + PATH_SEPARATOR + relativePath;
    }
}

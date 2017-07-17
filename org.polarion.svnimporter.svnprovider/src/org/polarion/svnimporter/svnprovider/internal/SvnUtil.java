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

package org.polarion.svnimporter.svnprovider.internal;

import org.polarion.svnimporter.svnprovider.SvnException;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * 
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class SvnUtil {
	private static DateFormat SVN_DATE_FORMAT = null;

	static {
		// initialize date format
		SVN_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.000000Z'");
      SVN_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
   }

	/**
	 * Format date in svn format
	 *
	 * @param date
	 * @return
	 */
	public static String formatSvnDate(Date date) {
		if (date == null) return "";
		return SVN_DATE_FORMAT.format(date);
	}

	/**
	 * return parent path of path
	 * (for example '/a/b/c' -> '/a/b')
	 *
	 * @param path
	 * @return
	 */
	public static String getParentPath(String path) {
		if (path.indexOf(SvnConst.PATH_SEPARATOR) == -1) return "";
		return path.replaceAll("/[^/]*$", "");
	}

	/**
	 * Convert string to utf-8
	 *
	 * @param s
	 * @return
	 */
	public static String toUtf8(String s) {
		try {
			return new String(s.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			throw new SvnException(e);
		}
	}

    /**
     * Replace path separators in branch/tag name
     *
     * @param s - branch name
     * @return escaped branch name
     */
    public static String escapeBranchName(String s) {
        return s.replaceAll(SvnConst.PATH_SEPARATOR, "_");
    }
}


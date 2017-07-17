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
package org.polarion.svnimporter.harprovider.internal;

import org.polarion.svnimporter.common.RevisionNumber;
import org.polarion.svnimporter.harprovider.HarException;

import java.io.UnsupportedEncodingException;

/**
 *
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class HarUtil {
	/**
	 * Get branch number from revisionNumber
	 * ("1.2.3.4" -> "1.2.3")
	 *
	 * @param revisionNumber
	 * @return
	 */
	public static String getBranchNumber(final String revisionNumber) {
		final int[] n = RevisionNumber.parse(revisionNumber);
		return RevisionNumber.getSubNumber(n, 0, n.length - 1);
	}

	/**
	 * Get branch's sprout revision number
	 * ("1.2.3" -> "1.2")
	 *
	 * @param branchNumber
	 * @return
	 */
	public static String getSproutRevisionNumber(final String branchNumber) {
		final int[] n = RevisionNumber.parse(branchNumber);
		return RevisionNumber.getSubNumber(n, 0, n.length - 1);
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
			throw new HarException(e);
		}
	}

}


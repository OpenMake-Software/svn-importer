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

package org.polarion.svnimporter.cvsprovider.internal.model;

import org.polarion.svnimporter.common.model.ModelFile;

import java.io.PrintStream;
import java.util.Iterator;

/**
 * 
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class CvsFile extends ModelFile {
	/**
	 * Relative path in cvs repository (starts with module name)
	 */
	public CvsFile(String filename) {
		super(filename);
	}

	/**
	 * Get branch by number
	 *
	 * @param number
	 * @return
	 */
	public CvsBranch getBranch(String number) {
		return (CvsBranch) getBranches().get(number);
	}

	/**
	 * Get revision by number
	 *
	 * @param number
	 * @return
	 */
	public CvsRevision getRevision(String number) {
		return (CvsRevision) getRevisions().get(number);
	}

	/**
	 * Dump debug information to stream
	 *
	 * @param out
	 */
	public void saveDebugInfo(PrintStream out) {
		out.println("File: " + getPath());
		out.println("BRANCHES:");
		for (Iterator it = getBranches().values().iterator(); it.hasNext();) {
			CvsBranch cvsBranch = (CvsBranch) it.next();
			out.println("\t" + cvsBranch.getDebugInfo());
		}

		out.println();
		out.println("TAGS:");
		for (Iterator i = getRevisions().values().iterator(); i.hasNext();) {
			CvsRevision revision = (CvsRevision) i.next();
			for (Iterator j = revision.getTags().iterator(); j.hasNext();) {
				CvsTag tag = (CvsTag) j.next();
				out.println("\t" + tag.getName() + " " + revision.getNumber());
			}
		}
	}
}

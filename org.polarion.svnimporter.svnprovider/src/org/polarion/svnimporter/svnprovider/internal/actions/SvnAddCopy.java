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

package org.polarion.svnimporter.svnprovider.internal.actions;

import org.polarion.svnimporter.svnprovider.internal.SvnBranch;
import org.polarion.svnimporter.svnprovider.internal.SvnConst;
import org.polarion.svnimporter.svnprovider.internal.SvnNodeAction;
import org.polarion.svnimporter.svnprovider.internal.SvnRevision;
import org.polarion.svnimporter.svnprovider.internal.SvnUtil;

import java.io.PrintStream;

/**
 *
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class SvnAddCopy extends SvnNodeAction {

	private SvnBranch srcBranch;
	private SvnRevision srcRevision;
	private String srcPath;

	/**
	 * @param path        - target file path
	 * @param branch      - target branch
	 * @param srcBranch
	 * @param srcRevision
	 * @param srcPath     - source path
	 */
	public SvnAddCopy(String kind,
                      String path,
                      SvnBranch branch,
					  SvnBranch srcBranch,
                      SvnRevision srcRevision,
                      String srcPath) {
		super(path, kind, "add", branch);
		this.srcBranch = srcBranch;
		this.srcRevision = srcRevision;
		this.srcPath = srcPath;
	}

	public String getDebugInfo() {
		String s = super.getDebugInfo();
		s += " [copy from "
				+ srcBranch.getPath() + SvnConst.PATH_SEPARATOR + srcPath
				+ " " + srcRevision.getNumber() + "]";
		return s;
	}

	public void dump(PrintStream out) {
		writeNodePath(out);
		writeNodeKind(out);
		writeNodeAction(out);
		out.print("Node-copyfrom-rev: " + srcRevision.getNumber() + "\n");
		String copyFrom = /* SvnConst.PATH_SEPARATOR + */ srcBranch.getPath();
		if (srcPath != null)
			copyFrom += SvnConst.PATH_SEPARATOR + srcPath;
		out.print("Node-copyfrom-path: " + SvnUtil.toUtf8(copyFrom) + "\n");
		out.print("\n");
	}
}

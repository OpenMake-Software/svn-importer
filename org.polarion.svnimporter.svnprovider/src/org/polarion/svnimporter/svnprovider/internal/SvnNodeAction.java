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

import java.io.PrintStream;

/**
 *
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public abstract class SvnNodeAction {
	/**
	 * Node path (relative to branch path)
	 */
	private final String path;

	/**
	 *
	 */
	//private String forceAbsolutePath;

	/**
	 * Node kind ('file' or 'dir')
	 */
	private final String nodeKind;

	/**
	 * Node action ('add', 'change', 'delete')
	 */
	private final String nodeAction;

	/**
	 * Branch
	 */
	private final SvnBranch branch;

	/**
	 * Properties of the NodeAction
	 *
	 */
	private SvnProperties properties;

	/**
	 * Constructor
	 *
	 * @param path
	 * @param nodeKind
	 * @param nodeAction
	 * @param branch
	 */
	protected SvnNodeAction(String path,
							String nodeKind,
							String nodeAction,
							SvnBranch branch) {
		this.path = path;
		this.nodeKind = nodeKind;
		this.nodeAction = nodeAction;
		this.branch = branch;
	}

	/**
	 * Get absolute path in repository
     * (for example 'trunk/one', 'branches/start/one')
	 * encoded in utf-8
	 *
	 * @return
	 */
    public String getAbsolutePath() {
        String p;
        if (branch != null && branch.getPath().length() > 0) {
            p = branch.getPath() + SvnConst.PATH_SEPARATOR + path;
        } else {
            p = path;
        }
        return SvnUtil.toUtf8(p);
    }

    public String getNodeKind() {
		return nodeKind;
	}

	public String getNodeAction() {
		return nodeAction;
	}

	public SvnBranch getBranch() {
		return branch;
	}

	public SvnProperties getProperties() {
		return properties;
	}

	/**
	 * Record property
	 */
	public void setProperty(String key, String value) {
		properties.set(key, value);
	}

	public void setProperties(SvnProperties props) {
		properties = props;
	}

	public String getDebugInfo() {
		return "[" + nodeAction + " " + nodeKind + "] " + getAbsolutePath();
	}

//	public void setForceAbsolutePath(String forceAbsolutePath) {
//		this.forceAbsolutePath = forceAbsolutePath;
//	}


	public void writeContentLength(PrintStream out, int len) {
		out.print("Content-length: " + len + "\n");
		//out.print("Content-length: " + Util.toString(len, 16) + "\n");
	}

	public void writeTextContentLength(PrintStream out, int len) {
		out.print("Text-content-length: " + len + "\n");
//		out.print("Text-content-length: " + Util.toString(len, 16) + "\n");
	}

	public void writeNodePath(PrintStream out) {
		out.print("Node-path: " + getAbsolutePath() + "\n");
	}

	public void writeNodeKind(PrintStream out) {
		out.print("Node-kind: " + getNodeKind() + "\n");
	}

	public void writeNodeAction(PrintStream out) {
		out.print("Node-action: " + getNodeAction() + "\n");
	}


	/**
	 * Write action information in dump format
	 *
	 * @param out
	 */
	public abstract void dump(PrintStream out);
}


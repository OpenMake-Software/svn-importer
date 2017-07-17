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

import java.util.Collection;
import java.util.HashSet;

/**
 * 
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class SvnBranch {
	/**
	 * Name of branch
	 */
	private String name;

	/**
	 * Path to branch (usually "/branches/"+name)
	 */
	private String path;

	/**
	 * Collection of created dirs
	 */
	private Collection createdDirs = new HashSet();

	public SvnBranch(String name, String path) {
		this.name = name;
		this.path = path;
	}

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}

	/**
	 * Return true if path already created in branch
	 *
	 * @param path relative to branch path
	 * @return
	 */
	public boolean isPathCreated(String path) {
		return createdDirs.contains(path);
	}

	/**
	 * Signalize what path has been created in branch
	 *
	 * @param path
	 */
	public void pathCreated(String path) {
		createdDirs.add(path);
	}
}


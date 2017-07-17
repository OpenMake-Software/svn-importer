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
package org.polarion.svnimporter.vssprovider.internal.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class VssProject {
	private String name;
	private VssProject parent;
	private Map files = new HashMap();
	private Map subprojects = new HashMap();
	private boolean root = false;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setParent(VssProject parent) {
		this.parent = parent;
	}

	public void addFile(VssFile vssFile) {
		files.put(vssFile.getPath(), vssFile);
	}

	public void addSubproject(VssProject vssProject) {
		subprojects.put(vssProject.getName(), vssProject);
	}

	public Collection getFiles() {
		return files.values();
	}

	public Collection getSubprojects() {
		return subprojects.values();
	}

	public String getVssPath() {
		if (parent != null)
			return parent.getVssPath() + "/" + getName();
		else
			return getName();
	}

	public boolean isRoot() {
		return root;
	}

	public void setRoot(boolean root) {
		this.root = root;
	}
}


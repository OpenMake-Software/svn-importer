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

import org.polarion.svnimporter.common.model.CommitsCollection;
import org.polarion.svnimporter.common.model.Model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class VssModel extends Model {
	private List commits = new ArrayList();

	public VssModel() {
	}

	public void loadFilesFromProject(VssProject project) {
		for (Iterator i = project.getFiles().iterator(); i.hasNext();)
			addFile((VssFile) i.next());
		for (Iterator i = project.getSubprojects().iterator(); i.hasNext();)
			loadFilesFromProject((VssProject) i.next());
	}

	public List getCommits() {
		return commits;
	}

	public void finishModel() {
		separateCommits();
	}

	private void separateCommits() {
		CommitsCollection cc = new CommitsCollection(VssCommit.class);
		cc.addFiles(getFiles().values());
		getCommits().addAll(cc.separateCommits());
	}
}


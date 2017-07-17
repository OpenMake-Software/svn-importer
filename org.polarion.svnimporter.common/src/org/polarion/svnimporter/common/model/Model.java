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

package org.polarion.svnimporter.common.model;

import org.polarion.svnimporter.common.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public abstract class Model {
	private static final Log LOG = Log.getLog(Model.class);
	private Map files = new HashMap();
	private List commits = new ArrayList();

	/**
	 * Return model files map (path->ModelFile)
	 *
	 * @return
	 */
	public Map getFiles() {
		return files;
	}

	/**
	 * Return model's commits
	 *
	 * @return
	 */
	public List getCommits() {
		return commits;
	}

	/**
	 * Add model file to model
	 * (will not be added if model already has another modelFile with same path)
	 *
	 * @param file
	 */
	public void addFile(ModelFile file) {
		if (files.containsKey(file.getPath())) {
			LOG.error("duplicate file: " + file.getPath());
		} else {
			files.put(file.getPath(), file);
		}
	}

	/**
	 * Remove model file from model
	 *
	 * @param file
	 */
	public void removeFile(ModelFile file) {
		files.remove(file.getPath());
	}

	public abstract void finishModel();

	/**
	 * Prints model summary to log
	 */
	public void printSummary() {
		LOG.info("Summary: ");
		LOG.info(" Files: " + getFiles().size());
		LOG.info(" Revisions: " + getTotalRevisionsCount());
		LOG.info(" Commits: " + getCommits().size());
	}

	/**
	 * Return sum of each file revisions count
	 *
	 * @return
	 */
	private int getTotalRevisionsCount() {
		int count = 0;
		for (Iterator i = getFiles().values().iterator(); i.hasNext();) {
			ModelFile file = (ModelFile) i.next();
			count += file.getRevisions().size();
		}
		return count;
	}
}


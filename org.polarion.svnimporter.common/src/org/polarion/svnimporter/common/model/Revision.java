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

import org.polarion.svnimporter.common.Util;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

/**
 *
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class Revision {
	/**
	 * Revision number
	 */
	private String number;
	/**
	 * Revision's branch
	 */
	private Branch branch;
	private Date date;
	private String author;
	private String message;

	/* this is set during the transformation into the SvnModel. It is used to determine
	 * to which SVN revision a source file revision was associated.
	 */
	private int svnRevisionNumber;

	/**
	 * Revision's file
	 */
	private ModelFile modelFile;

	private Collection childBranches = new HashSet(); // branch name -> branch

	public Revision(String number) {
		this.number = number;
	}

	public String getNumber() {
		return number;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public ModelFile getModelFile() {
		return modelFile;
	}

	public void setModelFile(ModelFile modelFile) {
		this.modelFile = modelFile;
	}

	public Branch getBranch() {
		return branch;
	}

	public void setBranch(Branch branch) {
		this.branch = branch;
	}

	public void addChildBranch(Branch newBranch) {
		childBranches.add(newBranch);
	}

	public Collection getChildBranches() {
		return childBranches;
	}

	public String getPath() {
		return modelFile.getPath();
	}

  /**
   * So far, only called from ST.
   * @return
   */
  public String getAbsolutePath() {
    String path = modelFile.getPath();
    int index = path.indexOf("/");
    if (index > 0) {
      path = path.substring(index + 1, path.length());
    }

    return path;
  }

  public int getSvnRevisionNumber() {
		return svnRevisionNumber;
	}

	public void setSvnRevisionNumber(int svnrevno) {
		svnRevisionNumber = svnrevno;
	}

	public String getDebugInfo() {
		String s = "rev: n[" + number + "] a[" + author + "] c[" + message + "] b[" + branch.getNumber() + " " + branch.getName() + "]";
		s += " D[" + Util.toString(date) + "]";
		return s;
	}

	/**
	 * Return true if revision is last revision on branch
	 *
	 * @return
	 */
	public boolean isLastRevision() {
		Revision last = (Revision) getBranch().getRevisions().last();
		return (this == last);
	}

	/**
	 * Return true if revision is first revision on branch
	 *
	 * @return
	 */
	public boolean isFirstRevision() {
		Revision first = (Revision) getBranch().getRevisions().first();
		return (this == first);

	}

}


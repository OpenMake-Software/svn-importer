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
import org.polarion.svnimporter.common.RevisionNumber;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class ModelFile implements Comparable {
	private static final Log LOG = Log.getLog(ModelFile.class);

	/**
	 * Filename in repository (relative to module directory)
	 */
	private String path;

	/**
	 * Map revision number  to Revision
	 */
	private Map revisions = new HashMap();

	/**
	 * Map branch number to Branch
	 */
	private Map branches = new HashMap();

	/**
	 * Map property key to value
	 */
	private Map properties = new HashMap();


	public ModelFile(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}


	public Map getRevisions() {
		return revisions;
	}

	public boolean addRevision(Revision revision) {
		if (revisions.containsKey(revision.getNumber())) {
			LOG.error(path + ": duplicate revision " + revision.getNumber());
			return false;
		}
		revisions.put(revision.getNumber(), revision);
		revision.setModelFile(this);
		return true;
	}
	
	public boolean deleteRevision(Revision revision) {
	    revision.setModelFile(null);
	    return (revisions.remove(revision.getNumber()) != null);
	}

	public void addBranch(Branch branch) {
		if (branches.containsKey(branch.getNumber())) {
			LOG.error(path + ": duplicated branch " + branch.getNumber() + " " + branch.getName());
		} else {
			branches.put(branch.getNumber(), branch);
		}
	}

	public void addProperty(String key, String value) {
		if (properties.containsKey(key)) {
			LOG.error(path + ": duplicated property " + key);
		} else {
			properties.put(key, value);
		}
	}

	public Map getBranches() {
		return branches;
	}

	public Map getProperties() {
		return properties;
	}

	protected void setPath( String path )
	{
		this.path = path;
	}
	
	/**
	 * Make sure all revision time stamps are in chronological order
	 */
	public void checkSequenceDates() {
	  
    Branch[] branches = (Branch[])getBranches().values().toArray(new Branch[getBranches().size()]);
    Arrays.sort(branches, new Comparator<Branch>(){
      public int compare(Branch b1, Branch b2) {
        return RevisionNumber.compare(b1.getNumber(), b2.getNumber());
      }});
    for (Branch branch : branches) {
      branch.checkSequenceDates();
    }
	}

  public int compareTo(Object arg) {
      String path1 = (path == null ? "" : path);
      String path2 = ((ModelFile)arg).path;
      if (path2 == null) path2 = "";
      return path1.compareTo(path2);
  }
}


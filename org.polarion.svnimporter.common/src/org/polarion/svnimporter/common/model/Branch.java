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

import java.util.Date;
import java.util.Iterator;
import java.util.SortedSet;

import org.polarion.svnimporter.common.Log;

/**
 *
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public abstract class Branch {
  
  private static final Log LOG = Log.getLog(Branch.class);

	private String number;
	private String name;
	private boolean trunk = false;
	private Revision sproutRevision;

	public abstract SortedSet getRevisions();

	public Branch(String number) {
		this.number = number;
	}

	public String getNumber() {
		return number;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isTrunk() {
		return trunk;
	}
	
	public String getBranchName() {
	  return trunk ? null : name;
	}

	public void setTrunk(boolean trunk) {
		this.trunk = trunk;
	}

	public void addRevision(Revision revision) {
		getRevisions().add(revision);
	}
	
	public void deleteRevision(Revision revision) {
	    getRevisions().remove(revision);
	}

	public Revision getSproutRevision() {
		return sproutRevision;
	}

	public void setSproutRevision(Revision revision) {
		sproutRevision = revision;
	}

  /**
   * Confirm that all revisions in this branch are in chronological order
   */
  public void checkSequenceDates() {
      
    Date lastDate = 
      (getSproutRevision() == null ? null 
                                   : new Date(getSproutRevision().getDate().getTime()+1));
      
    // Loop through the revisions that make up this branch
    for (Revision revision : (SortedSet<Revision>)getRevisions()) {
      
      // It is critical that the revisions dates be in chronological order
      // or things will get messed up when we assign them date ordered
      // SVN revisions.  SO we will make a special check to force out of
      // order revisions into correct chronological order.
      Date curDate = revision.getDate();
      if (lastDate != null && curDate.getTime() <= lastDate.getTime()) {
        LOG.warn("Date adjustment required for revision: " +
                 revision.getModelFile().getPath() + ":" +
                 revision.getNumber());
        curDate.setTime(lastDate.getTime()+1);
      }
      lastDate = curDate;
    }
  }

	public String getDebugInfo() {
		StringBuffer b = new StringBuffer();
		if ("1".equals(number))
			b.append("Branch [TRUNK]: ");
		else
			b.append("Branch name=" + name + " number=" + number + " " +
					" (sprout from " + ((sproutRevision != null) ? sproutRevision.getNumber() : null) + "): ");

		boolean first = true;
		for (Iterator i = getRevisions().iterator(); i.hasNext();) {
			Revision revision = (Revision) i.next();
			if (!first) b.append(", ");
			first = false;
			b.append(revision.getNumber());
		}
		return b.toString();
	}
}


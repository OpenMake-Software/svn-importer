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
package org.polarion.svnimporter.stprovider.internal.model;

import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.common.model.Branch;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;



/**
 * 
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class STBranch extends Branch {
	private static final Log LOG = Log.getLog(STBranch.class);
	private SortedSet revisions = new TreeSet(STRevisionComparator.INSTANCE);
	private Collection deletedRevisions = new HashSet();

	public STBranch(String number) {
		super(number);
	}

	public SortedSet getRevisions() {
		return revisions;
	}

	public void addDeletedRevision(STRevision revision) {
		revision.setBranch(this);
		deletedRevisions.add(revision);
	}

	public void handleDeletedRevisions() {
		for (Iterator i = deletedRevisions.iterator(); i.hasNext();) {
			STRevision del = (STRevision) i.next();
			STRevision prev = searchPrevRevision(del);
			if (prev == null) {
				LOG.error("can't find previous revision for deleted revision(skip): " + del.getDebugInfo());
				continue;
			}
			STRevision delClone = new STRevision(prev.getNumber() + "deleted");

			delClone.setAuthor(del.getAuthor());
			delClone.setDate(del.getDate());
			delClone.setMessage(del.getMessage());
			delClone.setNumberInBranch(prev.getNumberInBranch());
			delClone.setState(del.getState());

			delClone.setBranch(this);
			addRevision(delClone);
			prev.getModelFile().addRevision(delClone);
		}
	}

	/**
	 * Search previous revision (not deleted) by date
	 *
	 * @param revision
	 * @return
	 */
	private STRevision searchPrevRevision(STRevision revision) {
		long time = revision.getDate().getTime();
		STRevision prev = null;
		for (Iterator i = revisions.iterator(); i.hasNext();) {
			STRevision cur = (STRevision) i.next();
			long curTime = cur.getDate().getTime();
			if (prev == null) {
				if (curTime < time)
					prev = cur;
			} else {
				long prevTime = prev.getDate().getTime();
				if (curTime < time && curTime > prevTime)
					prev = cur;
			}
		}
		return prev;
	}

	/**
	 * Assign revision states to revisions
	 */
	public void resolveRevisionStates() {
		boolean first = true;
		STRevision prev = null;
		for (Iterator i = revisions.iterator(); i.hasNext();) {
			STRevision revision = (STRevision) i.next();
			if (first) {
				revision.setState(STRevisionState.ADD);
				first = false;
			} else {
				if (STRevisionState.DELETE.equals(prev.getState())) {
					revision.setState(STRevisionState.ADD);
				} else { // prev is not "delete"
					if (STRevisionState.DELETE.equals(revision.getState())) {
						// nothing;
					} else {
						revision.setState(STRevisionState.CHANGE);
					}
				}
			}
			prev = revision;
		}
	}
}


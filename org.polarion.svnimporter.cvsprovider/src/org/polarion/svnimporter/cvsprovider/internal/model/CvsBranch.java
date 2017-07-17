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

package org.polarion.svnimporter.cvsprovider.internal.model;

import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.common.model.Branch;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * 
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class CvsBranch extends Branch {
	private static final Log LOG = Log.getLog(CvsBranch.class);

	/**
	 * Revisions sorted by number
	 */
	private SortedSet revisions = new TreeSet(CvsRevisionComparator.INSTANCE);

	/**
	 * Constructor
	 *
	 * @param number - branch number
	 */
	public CvsBranch(String number) {
		super(number);
	}

	/**
	 * Return revisions sorted by revision number
	 *
	 * @return
	 */
	public SortedSet getRevisions() {
		return revisions;
	}

	/**
	 * For each revision in branch determine it's state (CvsRevisionState)
	 * <p/>
	 * Must be called only after when all revisions added
	 */
	public void resolveRevisionStates() {
		CvsRevision prev = null;
		Iterator i = getRevisions().iterator();
		while (i.hasNext()) {
			CvsRevision rev = (CvsRevision) i.next();
			String rcsState = rev.getRcsState();
			if (!"dead".equals(rcsState) && !"Exp".equals(rcsState)) {
				throw new CvsModelException("unknown rcs state: " + rcsState);
			}
			if (prev == null) {
				if(isTrunk())
					rev.setState(CvsRevisionState.ADD);
				else
					rev.setState(CvsRevisionState.CHANGE);
			} else {
				if ("dead".equals(rev.getRcsState())) {
					if (prev.getState() == CvsRevisionState.ADD || prev.getState() == CvsRevisionState.CHANGE) {
						rev.setState(CvsRevisionState.DELETE);
					} else {
						rev.setState(CvsRevisionState.ADD);
					}
				} else { //exp
					if (prev.getState() == CvsRevisionState.DELETE)
						rev.setState(CvsRevisionState.ADD);
					else
						rev.setState(CvsRevisionState.CHANGE);
				}
			}
			if (rev.getState() == null)
				throw new CvsModelException("ERROR IN PROGRAM");

			prev = rev;
		}
	}

	/**
	 * Fix revisions data
	 */
	public void fixRevisions() {
		List list = new ArrayList(getRevisions());
		for (int i = 0; i < list.size(); i++) {
			CvsRevision prev = (CvsRevision) ((i != 0) ? list.get(i - 1) : null);
			CvsRevision rev = (CvsRevision) list.get(i);
			CvsRevision next = (CvsRevision) ((i != list.size() - 1) ? list.get(i + 1) : null);



            if (rev.getAuthor() == null) {
				LOG.warn("revision with null author: \"" + rev.getModelFile().getPath() + "\" " + rev.getNumber());
				rev.setAuthor("UNKNOWN AUTHOR");
			}
			if (rev.getMessage() == null) {
				LOG.warn("revision with null message: \"" + rev.getModelFile().getPath() + "\" " + rev.getNumber());
				rev.setMessage("");
			}
			if (rev.getDate() == null) {
				LOG.warn("revision with null date: " + rev.getModelFile().getPath() + " " + rev.getNumber());
				if (prev == null && next != null) {
					if (next.getDate() != null)
						rev.setDate(new Date(next.getDate().getTime() - 1000));
					else
						rev.setDate(new Date(1));
				} else if (next == null && prev != null) {
					rev.setDate(new Date(prev.getDate().getTime() + 1000));
				} else if (prev != null && next != null) {
					if (next.getDate() != null) {
						long t = Math.abs(next.getDate().getTime() - prev.getDate().getTime()) / 2;
						rev.setDate(new Date(t));
					} else {
						rev.setDate(new Date(prev.getDate().getTime() + 1000));
					}
				} else {
					rev.setDate(new Date(1));
				}
			}

            // fix "resync timestamp" bug
            // Here we must ensure, that under any circumstances, the revisions have increasing
            // dates. We don't try to eliminate single invalid dates, we simply change any invalid
            // date to first possible valid one. (SVNIMP-19)
            if(next!=null && rev.getDate().after(next.getDate())) {
                LOG.warn("Fixing \"resync time\" bug for " + rev.getModelFile().getPath()
                        + " revisions: " + rev.getNumber() + " " + next.getNumber());
                next.setDate(new Date(rev.getDate().getTime() + 1000));
            }
        }
	}
}


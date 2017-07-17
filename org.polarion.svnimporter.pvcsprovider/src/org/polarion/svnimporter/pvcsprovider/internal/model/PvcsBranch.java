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
package org.polarion.svnimporter.pvcsprovider.internal.model;

import org.polarion.svnimporter.common.model.Branch;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * 
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class PvcsBranch extends Branch {
	/**
	 * Revisions sorted by revision number
	 */
	private SortedSet revisions = new TreeSet(PvcsRevisionComparator.INSTANCE);

	/**
	 * Constructor
	 *
	 * @param number - branch number
	 */
	public PvcsBranch(String number) {
		super(number);
	}

	/**
	 * Revisions sorted by revision number
	 *
	 * @return
	 */
	public SortedSet getRevisions() {
		return revisions;
	}

	/**
	 * Assign states to revisions
	 * ('add' to first, 'change' to following
	 */
	public void resolveRevisionStates() {
		boolean first = true;
		for (Iterator i = getRevisions().iterator(); i.hasNext();) {
			PvcsRevision revision = (PvcsRevision) i.next();
			if (first) {
				revision.setState(PvcsRevisionState.ADD);
				first = false;
			} else {
				revision.setState(PvcsRevisionState.CHANGE);
			}
		}
	}
}


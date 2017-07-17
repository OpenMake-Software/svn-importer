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

package org.polarion.svnimporter.svnprovider;

import org.polarion.svnimporter.svnprovider.internal.SvnRevision;

/**
 * The revision numbers of an incremental SVN Model need not start with 1.
 * They start with 1 + last revision number of the previous incremental model.
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class SvnIncrModel extends SvnModel {



	public SvnIncrModel(int startrevision) {
		super(startrevision);
	}

	/**
	 * Add revision from other model
	 *
	 * @param revision
	 */
	public void addRevision(SvnRevision revision) {
		int revNumber = getLastRevisionNumber() + 1;
		revision.setNumber(revNumber);
		getRevisions().add(revision);
	}
	/**
	 * Create and add clone of revision
	 *
	 * @param origRevision
	 * @return
	 */
	public SvnRevision createRevisionClone(SvnRevision origRevision) {
        SvnRevision revision = new SvnRevision();
        addRevision(revision);
        revision.setAuthor(origRevision.getAuthor());
        revision.setMessage(origRevision.getMessage());
        revision.setDate(origRevision.getRevisionDate());
        revision.setModuleName(origRevision.getModuleName());
        return revision;
    }
}


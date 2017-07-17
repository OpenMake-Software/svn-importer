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

import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;
import org.polarion.svnimporter.common.Util;
import org.polarion.svnimporter.common.model.Commit;
import org.polarion.svnimporter.common.model.Revision;

/**
 * 
 * 
 * @author <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class STCommit extends Commit implements STChangeRequestLinkTarget  {

	private final SortedSet linkedCRs = new TreeSet();

	/* (non-Javadoc)
	 * @see org.polarion.svnimporter.common.model.Commit#addRevision(org.polarion.svnimporter.common.model.Revision)
	 */
	public void addRevision(Revision revision) {
		super.addRevision(revision);
		linkedCRs.addAll(Arrays.asList(((STRevision)revision).getLinkedChangeRequests()));
	}
	
	/* (non-Javadoc)
	 * @see org.polarion.svnimporter.stprovider.internal.model.STChangeRequestLinkTarget#getLinkedChangeRequests()
	 */
	public STChangeRequest[] getLinkedChangeRequests() {
		return (STChangeRequest[]) linkedCRs.toArray(new STChangeRequest[linkedCRs.size()]);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "STCommit[ User " + getAuthor() + ", " + Util.toString(getDate()) + ", "+ super.getMessage() + ", "  + getRevisions().size() + " Revisions" + "]";
	}
}

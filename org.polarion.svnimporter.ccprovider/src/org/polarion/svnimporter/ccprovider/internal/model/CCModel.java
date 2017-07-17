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
package org.polarion.svnimporter.ccprovider.internal.model;

import org.polarion.svnimporter.common.model.Model;

import java.util.Date;
import java.util.Iterator;

/**
 * 
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class CCModel extends Model {
	public void finishModel() {
		for (Iterator i = getFiles().values().iterator(); i.hasNext();) {
			CCFile file = (CCFile) i.next();
			for (Iterator j = file.getBranches().values().iterator(); j.hasNext();) {
				CCBranch branch = (CCBranch) j.next();
				branch.handleDeletedRevisions();
				branch.resolveRevisionStates();
			}
            
            fixDates(null, file.getTrunk());
		}
		separateCommits();
	}

	/**
	 * Fix any date problems in this branch, and in any branches spawned from 
	 * this one
	 * @param sproutRev - Branch sprout revision or null if this branch started a new file
	 * @param branch branch being fixed
	 */
	private void fixDates(CCRevision sproutRev, CCBranch branch) {
	    long minTime = 0;
	    if (sproutRev != null) minTime = sproutRev.getDate().getTime()+2;
	    for (Iterator ir = branch.getRevisions().iterator(); ir.hasNext(); ) {
	        CCRevision rev = (CCRevision)ir.next();
	        Date date = rev.getDate();
	        long time = date.getTime();
	        if (time < minTime) {
	            time = minTime;
	            date.setTime(time);
	        }
	        minTime = time + 1;
	        
	        for (Iterator ib = rev.getChildBranches().iterator(); ib.hasNext(); ) {
	            fixDates(rev, (CCBranch)ib.next());
	        }
	    }
    }

    private void separateCommits() {
		CCCommitsCollection c = new CCCommitsCollection(CCCommit.class);
		c.addFiles(getFiles().values());
		getCommits().clear();
		getCommits().addAll(c.separateCommits());
	}
}


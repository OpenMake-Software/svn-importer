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
package org.polarion.svnimporter.mksprovider.internal.model;

import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.common.model.Branch;

import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * 
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class MksBranch extends Branch {
  private static final Log LOG = Log.getLog(MksBranch.class);
	private SortedSet<MksRevision> revisions = new TreeSet<MksRevision>(MksRevisionComparator.INSTANCE);

	public MksBranch(String number) {
		super(number);
	}

	public SortedSet<MksRevision> getRevisions() {
		return revisions;
	}
	
	/**
	 * Determine if this branch has been assigned to either the main trunk
	 * or an alternate development path name
	 * @return true if it has been assigned
	 */
	public boolean isAssigned() {
	    return (isTrunk() || getName() != null);
	}
	
	/**
	 * This method is called we want to assign the branch to the trunk or an
	 * alternate development path.  This requires that the branch must either not
	 * have a root branch, or have a root branch which has already been assigned
	 * to a development path.  If this condition is not met, the branch 
	 * hierarchy will be restructured to meet the basic condition.  The branch
	 * that is returned will be properly rooted.  It may not be the same as the
	 * original branch, but it will contain all of the revisions that were in 
	 * the original branch. 
	 * 
	 * @return a correctly rooted branch containing all of the revisions from
	 * the original branch.
	 */
	public MksBranch adjustRoot() {
        
        // The root adjustment is recursive, we may have to track it down through 
        // multiple sprouts
	    
	    MksBranch branch = this;
        while (true) {
            
            // Get the sprout revision for the branch, if there isn't one
            // we are finished
            MksRevision sproutRevision = (MksRevision)branch.getSproutRevision();
            if (sproutRevision == null) break;
            
            // Get the branch the sprout revision belongs to, if it has been
            // previously assigned to the trunk or an alternate devpath, we
            // are finished
            MksBranch headBranch = (MksBranch)sproutRevision.getBranch();
            if (headBranch.isAssigned()) break;
            
            // Otherwise we are going to have to swap these two branches
            LOG.info("Swapping branch " + branch.getNumber() + 
                     " with branch " + headBranch.getNumber());
            
            // Create a new sorted list and move all of the revisions
            // on the head branch that occur after the sprout revision
            // into it
            SortedSet<MksRevision> newList = new TreeSet<MksRevision>(MksRevisionComparator.INSTANCE);
            while (true) {
                MksRevision rev = (MksRevision)headBranch.revisions.last();
                if (rev == sproutRevision) break;
                rev.setBranch(branch);
                headBranch.revisions.remove(rev);
                newList.add(rev);
            }
            
            // Dump the revisions from the original branch into the head branch
            for (MksRevision rev : branch.revisions) {
                rev.setBranch(headBranch);
                headBranch.revisions.add(rev);
            }
            
            // Replace the original branch revision list with the SortedSet
            // containing the revisions we removed from the head branch
            branch.revisions = newList;
            
            // The original head branch now contains our working revisions
            // and becomes the current branch.  Then we loop back and 
            // start the process all over again
            branch = headBranch;
        }

        // Return the branch that now contains the reivions from the 
        // original branch
        return branch;
    }
}


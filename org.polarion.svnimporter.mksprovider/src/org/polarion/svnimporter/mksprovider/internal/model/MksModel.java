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

import java.util.Iterator;

import org.polarion.svnimporter.common.model.CommitsCollection;
import org.polarion.svnimporter.common.model.Model;

/**
 * 
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class MksModel extends Model {
    
    private MksCheckpointHistory checkpointHistory;
    
    public void setCheckpointHistory(MksCheckpointHistory checkpointHistory) {
        this.checkpointHistory = checkpointHistory;
    }
    
    public MksCheckpointHistory getCheckpointHistory() {
        return checkpointHistory;
    }

    /**
     * Assign orphan branch (devpath) names for any file branches that have
     * not been assigned to real development paths
     * @param orphanBranch orphan branch prefix
     */
    public void assignOrphanBranches(String orphanBranchPrefix) {
        for (Iterator itr = getFiles().values().iterator(); itr.hasNext(); ) {
            ((MksFile)itr.next()).assignOrphanBranches(orphanBranchPrefix);
        }
    }

	public void finishModel() {
		separateCommits();
	}

	private void separateCommits() {
		CommitsCollection c = new CommitsCollection(MksCommit.class);
		
		// Since checkpoint history is an extension of ModelFile, we can
		// conveniently add it to the commit collection like any other file.  
		c.addFile(checkpointHistory);
		c.addFiles(getFiles().values());
		getCommits().clear();
		getCommits().addAll(c.separateCommits());
	}
}

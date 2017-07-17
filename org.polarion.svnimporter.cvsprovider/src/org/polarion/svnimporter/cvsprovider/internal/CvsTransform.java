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

package org.polarion.svnimporter.cvsprovider.internal;

import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.cvsprovider.CvsException;
import org.polarion.svnimporter.cvsprovider.CvsProvider;
import org.polarion.svnimporter.cvsprovider.internal.model.CvsBranch;
import org.polarion.svnimporter.cvsprovider.internal.model.CvsCommit;
import org.polarion.svnimporter.cvsprovider.internal.model.CvsModel;
import org.polarion.svnimporter.cvsprovider.internal.model.CvsRevision;
import org.polarion.svnimporter.cvsprovider.internal.model.CvsRevisionState;
import org.polarion.svnimporter.cvsprovider.internal.model.CvsTag;
import org.polarion.svnimporter.svnprovider.SvnModel;
import org.polarion.svnimporter.svnprovider.internal.SvnProperties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class CvsTransform {
	private static final Log LOG = Log.getLog(CvsTransform.class);
    private static final String CVS_REVISION_NUMBER = "CVSRevisionNumber";

    private final CvsProvider provider;
	private SvnModel svnModel;

    public CvsTransform(CvsProvider cvsProvider) {
        this.provider = cvsProvider;
    }

	/**
	 * Transform cvsModel to SvnModel
	 *
	 * @return
	 */
	public SvnModel transform(CvsModel srcModel) {
		if (srcModel.getCommits().size() < 1) {
			return new SvnModel();
		}
        svnModel = new SvnModel();
        if (provider.getConfig() instanceof CvsConfig)
            svnModel.setModuleName(((CvsConfig) provider.getConfig()).getModuleName());
        svnModel.setSvnimporterUsername(provider.getConfig().getSvnimporterUsername());
		CvsCommit firstCommit = (CvsCommit) srcModel.getCommits().get(0);
        svnModel.createFirstRevision(firstCommit.getDate());
		svnModel.createTrunkPath(provider.getConfig().getTrunkPath());
		if (!isOnlyTrunk()) {
			svnModel.createBranchesPath(provider.getConfig().getBranchesPath());
			svnModel.createTagsPath(provider.getConfig().getTagsPath());
		}
		for (Iterator i = srcModel.getCommits().iterator(); i.hasNext();)
			transformCommit((CvsCommit) i.next());
		return svnModel;
	}

	/**
	 * @return
	 */
	private boolean isOnlyTrunk() {
		return provider.getConfig().isOnlyTrunk();
	}

	/**
	 * Transform CvsCommit to SvnRevision
	 *
	 * @param commit
	 */
	private void transformCommit(CvsCommit commit) {
		svnModel.createNewRevision(commit.getAuthor(), commit.getDate(), commit.getMessage());
        svnModel.getCurRevision().getProperties().set("CVSRevisionNumbers", commit.joinRevisionNumbers());
        Map childBranches = new HashMap();	// CvsBranch -> List of CvsRevisions
		Map childTags = new HashMap();		// CvsTag -> List of CvsRevisions

		for (Iterator i = commit.getRevisions().iterator(); i.hasNext();) {
			CvsRevision revision = (CvsRevision) i.next();
			transformRevision(revision);

			if (!isOnlyTrunk()) {
				//--- record branches ----
				for (Iterator b = revision.getChildBranches().iterator(); b.hasNext();) {
					CvsBranch childBranch = (CvsBranch) b.next();
					if (!childBranches.containsKey((childBranch.getName())))
						childBranches.put(childBranch.getName(), new ArrayList());
					((Collection) childBranches.get(childBranch.getName())).add(revision);
				}
				//--- record tags ---
				for (Iterator t = revision.getTags().iterator(); t.hasNext();) {
					CvsTag childTag = (CvsTag) t.next();
					if (!childTags.containsKey(childTag.getName()))
						childTags.put(childTag.getName(), new ArrayList());
					((Collection) childTags.get(childTag.getName())).add(revision);
				}
			}
		}
		if (!isOnlyTrunk()) {
            // Remember old SVN revision number.
            int oldRevno = svnModel.getCurRevisionNumber();
            if (provider.getConfig().useFileCopy()
                    && (!childBranches.isEmpty() || !childTags.isEmpty())) {
                // If we have child tags, we introduce a new SVN Revision. We must do this
                // since Subversion is able to copy files only if the source revision is smaller
                // than the actual revision.
                svnModel.createNewRevision(commit.getAuthor(),
                            commit.getDate(), "svnimporter: adding tags and branches to revision " + oldRevno);
            }

            // create branches
            for (Iterator i = childBranches.keySet().iterator(); i.hasNext();) {
                String branchName = (String) i.next();
                // create branch, if necessary
                if (!svnModel.isBranchCreated(branchName))
                    svnModel.createBranch(branchName, commit.getDate());
                for (Iterator j = ((Collection) childBranches.get(branchName)).iterator(); j.hasNext();) {
                    CvsRevision revision = (CvsRevision) j.next();
                    if (provider.getConfig().useFileCopy()) {
                        svnModel.addFileCopyToBranch(revision.getPath(),
                                branchName,
                                revision.getBranch().getName(),
                                revision.getPath(),
                                oldRevno);
                    } else {
                        svnModel.addFile(revision.getPath(),
                                branchName,
                                new CvsContentRetriever(provider, revision));
                    }
                }
            }
            // create tags
            for (Iterator i = childTags.keySet().iterator(); i.hasNext();) {
                String tagName = (String) i.next();
                // create tag, if necessary
                if (!svnModel.isTagCreated(tagName))
                    svnModel.createTag(tagName, commit.getDate());
                // copy files into tag folder
                for (Iterator j = ((Collection) childTags.get(tagName)).iterator(); j.hasNext();) {
                    CvsRevision revision = (CvsRevision) j.next();
                    if (provider.getConfig().useFileCopy()) {
                        svnModel.addFileCopyToTag(revision.getPath(),
                                tagName,
                                revision.getBranch().getName(),
                                revision.getPath(),
                                oldRevno);
                    } else {
                        svnModel.addFileToTag(revision.getPath(),
                                    tagName,
                                    new CvsContentRetriever(provider, revision));
                    }
                }
            }
        } // if(!isOnlyTrunk())
	}

	/**
	 * Transform CvsRevision to SvnNodeAction
	 *
	 * @param revision
	 */
	private void transformRevision(CvsRevision revision) {
		String path = revision.getPath();
		String branchName = revision.getBranch().getBranchName();

		if (isOnlyTrunk() && !revision.getBranch().isTrunk())
			return;

        SvnProperties p = new SvnProperties();
        p.set(CVS_REVISION_NUMBER, revision.getNumber());

        if (revision.getState() == CvsRevisionState.ADD) {
            svnModel.addFile(path, branchName, provider.createContentRetriever(revision), p);
		} else if (revision.getState() == CvsRevisionState.CHANGE) {
            svnModel.changeFile(path, branchName, provider.createContentRetriever(revision), p);
        } else if (revision.getState() == CvsRevisionState.DELETE) {
			svnModel.deleteFile(path, branchName, p);
		} else {
			LOG.error(revision.getDebugInfo());
			LOG.error(revision.getBranch().getDebugInfo());
			throw new CvsException("unknown cvs revision state: " + revision.getState());
		}
	}
}

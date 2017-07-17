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
package org.polarion.svnimporter.ccprovider.internal;

import org.polarion.svnimporter.ccprovider.CCException;
import org.polarion.svnimporter.ccprovider.CCProvider;
import org.polarion.svnimporter.ccprovider.internal.model.CCCommit;
import org.polarion.svnimporter.ccprovider.internal.model.CCModel;
import org.polarion.svnimporter.ccprovider.internal.model.CCRevision;
import org.polarion.svnimporter.ccprovider.internal.model.CCRevisionState;
import org.polarion.svnimporter.ccprovider.internal.model.CCLabel;
import org.polarion.svnimporter.svnprovider.SvnModel;
import org.polarion.svnimporter.svnprovider.internal.SvnProperties;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collection;

/**
 * 
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class CCTransform {
    private static final String CC_REVISION = "ClearcaseRevision";
    private static final String CC_LABELS = "ClearcaseLabels";
    private CCProvider provider;
    private SvnModel svnModel;

    /**
     * Constructor
     *
     * @param provider
     */
    public CCTransform(CCProvider provider) {
        this.provider = provider;
    }

    /**
     * Transform cc model to svn model
     *
     * @param srcModel
     * @return
     */
    public SvnModel transform(CCModel srcModel) {
        if (srcModel.getCommits().size() < 1) {
            return new SvnModel();
        }
        svnModel = new SvnModel();
        svnModel.setSvnimporterUsername(provider.getConfig().getSvnimporterUsername());
        CCCommit firstCommit = (CCCommit) srcModel.getCommits().get(0);
        svnModel.createFirstRevision(firstCommit.getDate());
        svnModel.createTrunkPath(provider.getConfig().getTrunkPath());
        if (!isOnlyTrunk()) {
            svnModel.createBranchesPath(provider.getConfig().getBranchesPath());
            svnModel.createTagsPath(provider.getConfig().getTagsPath());
        }
        for (Iterator i = srcModel.getCommits().iterator(); i.hasNext();)
            transformCommit((CCCommit) i.next());
        return svnModel;
    }

    /**
     * Transform cc commit to svn revision
     *
     * @param commit
     * @param svnModel
     */
    private void transformCommit(CCCommit commit) {
        svnModel.createNewRevision(commit.getAuthor(), commit.getDate(), commit.getMessage());
        svnModel.getCurRevision().getProperties().set("CCRevisionNumbers", commit.joinRevisionNumbers());
        Map childTags = new HashMap();        // CCLabel -> List of CCRevisions

        for (Iterator i = commit.getRevisions().iterator(); i.hasNext();) {
            CCRevision revision = (CCRevision) i.next();
            transformRevision(revision);

            if (!isOnlyTrunk()) {
                CCLabel[] labels = revision.getLabels();
                for (int j = 0; j < labels.length; j++) {
                    CCLabel label = labels[j];
                    String labelName = label.getName();
                    if (!childTags.containsKey(labelName)) {
                        childTags.put(labelName, new ArrayList());
                    }
                    ((Collection) childTags.get(labelName)).add(revision);
                }
            }
        }

        if (!isOnlyTrunk()) {
            // create child tags
            // If we have child tags, we introduce a new SVN Revision. We must do this
            // since Subversion is able to copy files only if the source revision is smaller
            // than the actual revision.
            if (!childTags.isEmpty()) {
                if (provider.getConfig().useFileCopy()) {
                    // Remember old SVN revision number.
                    int oldRevno = svnModel.getCurRevisionNumber();
                    svnModel.createNewRevision(commit.getAuthor(),
                            commit.getDate(), "svnimporter: adding tags to revision " + oldRevno);

                    for (Iterator i = childTags.keySet().iterator(); i.hasNext();) {
                        String tagName = (String) i.next();
                        // create tag, if necessary
                        if (!svnModel.isTagCreated(tagName))
                            svnModel.createTag(tagName, commit.getDate());
                        // copy files into tag folder
                        for (Iterator j = ((Collection) childTags.get(tagName)).iterator(); j.hasNext();) {
                            CCRevision revision = (CCRevision) j.next();
                            svnModel.addFileCopyToTag(revision.getPath(),
                                    tagName,
                                    revision.getBranch().getName(),
                                    revision.getPath(),
                                    oldRevno);
                        }
                    }
                } else {
                    // doesn't use file copy
                    for (Iterator i = childTags.keySet().iterator(); i.hasNext();) {
                        String tagName = (String) i.next();
                        if (!svnModel.isTagCreated(tagName))
                            svnModel.createTag(tagName, commit.getDate());

                        for (Iterator j = ((Collection) childTags.get(tagName)).iterator(); j.hasNext();) {
                            CCRevision ccRevision = (CCRevision) j.next();
                            svnModel.addFileToTag(
                                    ccRevision.getPath(),
                                    tagName,
                                    provider.createContentRetriever(ccRevision));
                        }
                    }
                }
            }
        }//!onlytrunk
    }

    /**
     * Transform cc revision to svn svn action
     *
     * @param revision
     * @param model
     */
    private void transformRevision(CCRevision revision) {
        String path = revision.getPath();
        CCRevisionState state = revision.getState();

        boolean trunk = revision.getBranch().isTrunk();

        String branchName = revision.getBranch().getBranchName();

        SvnProperties p = new SvnProperties();

        p.set(CC_REVISION, revision.getNumber());

        if (revision.getLabels().length > 0)
            p.set(CC_LABELS, formatLabels(revision));

        if (CCRevisionState.ADD.equals(state)) {
            if (!trunk && !svnModel.isBranchCreated(branchName)) {
                svnModel.createBranch(branchName, revision.getDate());
            }
            svnModel.addFile(path, branchName, provider.createContentRetriever(revision), p);
        } else if (CCRevisionState.CHANGE.equals(state)) {
            svnModel.changeFile(path, branchName, provider.createContentRetriever(revision), p);
        } else if (CCRevisionState.DELETE.equals(state)) {
            svnModel.deleteFile(path, branchName, p);
        } else {
            throw new CCException("unknown cc revision state: " + state);
        }
    }

    private String formatLabels(CCRevision revision) {
        CCLabel[] labels = revision.getLabels();
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < labels.length; i++) {
            if (b.length() > 0) b.append(", ");
            b.append(labels[i].getName());
        }
        return b.toString();
    }

    /**
     * @return
     */
    private boolean isOnlyTrunk() {
        return provider.getConfig().isOnlyTrunk();
    }
}

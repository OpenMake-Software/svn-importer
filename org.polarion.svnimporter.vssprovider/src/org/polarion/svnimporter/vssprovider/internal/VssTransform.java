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
package org.polarion.svnimporter.vssprovider.internal;

import org.polarion.svnimporter.svnprovider.SvnModel;
import org.polarion.svnimporter.svnprovider.internal.SvnProperties;
import org.polarion.svnimporter.vssprovider.VssProvider;
import org.polarion.svnimporter.vssprovider.internal.model.VssCommit;
import org.polarion.svnimporter.vssprovider.internal.model.VssFileActionType;
import org.polarion.svnimporter.vssprovider.internal.model.VssFileRevision;
import org.polarion.svnimporter.vssprovider.internal.model.VssModel;
import org.polarion.svnimporter.vssprovider.internal.model.VssRevision;
import org.polarion.svnimporter.vssprovider.internal.model.VssLabel;

import java.util.Iterator;

/**
 * @author <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class VssTransform {
    private static final String VSS_REVISION_NUMBER = "VSSRevisionNumber";
    private static final String VSS_REVISION_LABEL = "VSSRevisionLabel";

    private VssProvider provider;

    public VssTransform(VssProvider provider) {
        this.provider = provider;
    }

    /**
     * Transform vss model to svn model
     *
     * @param srcModel
     * @return
     */
    public SvnModel transform(VssModel srcModel) {
        if (srcModel.getCommits().size() < 1) {
            return new SvnModel();
//			throw new VssException("vss model without commits");
        }
        SvnModel svnModel = new SvnModel();
        svnModel.setSvnimporterUsername(provider.getConfig().getSvnimporterUsername());
        VssCommit firstCommit = (VssCommit) srcModel.getCommits().get(0);
        svnModel.createFirstRevision(firstCommit.getDate());
        svnModel.createTrunkPath(provider.getConfig().getTrunkPath());
        for (Iterator i = srcModel.getCommits().iterator(); i.hasNext();)
            transformCommit((VssCommit) i.next(), svnModel);
        return svnModel;
    }

    /**
     * Transform vss commit to svn revision
     *
     * @param commit
     * @param svnModel
     */
    private void transformCommit(VssCommit commit, SvnModel svnModel) {
        svnModel.getCurRevision().getProperties().set("VssRevisionNumbers", commit.joinRevisionNumbers());
        svnModel.createNewRevision(commit.getAuthor(), commit.getDate(), commit.getMessage());
        for (Iterator i = commit.getRevisions().iterator(); i.hasNext();) {
            VssRevision revision = (VssRevision) i.next();
            transformRevision(revision, svnModel);
        }
    }

    /**
     * Transform vss revision to svn svn action
     *
     * @param revision
     * @param model
     */
    private void transformRevision(VssRevision revision, SvnModel model) {
        String path = revision.getPath();
        if (revision instanceof VssFileRevision) {
            VssFileRevision f = (VssFileRevision) revision;
            SvnProperties properties = new SvnProperties();
            properties.set(VSS_REVISION_NUMBER, f.getNumber());
            if (!f.getLabels().isEmpty()) {
                properties.set(VSS_REVISION_LABEL, generateLabelsPropertyValue(f));
            }
            if (f.getType() == VssFileActionType.ADD) {
                model.addFile(path, null, provider.createContentRetriever(f), properties);
            } else if (f.getType() == VssFileActionType.CHANGE) {
                model.changeFile(path, null, provider.createContentRetriever(f), properties);
            } else {
                throw new IllegalStateException();
            }
        } else {
            throw new IllegalStateException();
        }
    }

    private String generateLabelsPropertyValue(VssFileRevision revision) {
        StringBuffer b = new StringBuffer();
        for (Iterator i = revision.getLabels().iterator(); i.hasNext();) {
            if (b.length() > 0) b.append(", ");
            VssLabel label = (VssLabel) i.next();
            b.append(label.getName());
        }
        return b.toString();
    }
}

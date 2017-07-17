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
package org.polarion.svnimporter.stprovider.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.polarion.svnimporter.stprovider.STException;
import org.polarion.svnimporter.stprovider.STProvider;
import org.polarion.svnimporter.stprovider.internal.model.STChangeRequest;
import org.polarion.svnimporter.stprovider.internal.model.STChangeRequestLinkTarget;
import org.polarion.svnimporter.stprovider.internal.model.STCommit;
import org.polarion.svnimporter.stprovider.internal.model.STLabel;
import org.polarion.svnimporter.stprovider.internal.model.STModel;
import org.polarion.svnimporter.stprovider.internal.model.STRevision;
import org.polarion.svnimporter.stprovider.internal.model.STRevisionState;
import org.polarion.svnimporter.svnprovider.SvnModel;
import org.polarion.svnimporter.svnprovider.internal.SvnProperties;

/**
 * 
 * 
 * @author <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class STTransform {
	private static final String ST_REVISION = "StarTeamRevision";
	private static final String ST_REVISION_NUMBERS = "StarTeamRevisionNumbers";
	private static final String ST_LABELS = "StarTeamLabels";
	private static final String ST_USER = "StarTeamUser";
	private static final String ST_MODIFICATION_TIME = "StarTeamModificationTime";
	private static final String ST_CRS = "StarTeamChangeRequests";
	private STProvider provider;
	private SvnModel svnModel;

	/**
	 * Constructor
	 * 
	 * @param provider
	 */
	public STTransform(STProvider provider) {
		this.provider = provider;
	}

	/**
	 * Transform cc model to svn model
	 * 
	 * @param srcModel
	 * @return
	 */
	public SvnModel transform(STModel srcModel) {
		if (srcModel.getCommits().size() < 1) {
			return new SvnModel();
		}
		svnModel = new SvnModel();
		svnModel.setSvnimporterUsername(provider.getConfig().getSvnimporterUsername());
		STCommit firstCommit = (STCommit) srcModel.getCommits().get(0);
		svnModel.createFirstRevision(firstCommit.getDate());
		svnModel.createTrunkPath(provider.getConfig().getTrunkPath());
		if (!isOnlyTrunk()) {
			svnModel.createBranchesPath(provider.getConfig().getBranchesPath());
			svnModel.createTagsPath(provider.getConfig().getTagsPath());
		}
		for (Iterator i = srcModel.getCommits().iterator(); i.hasNext();)
			transformCommit((STCommit) i.next());
		return svnModel;
	}

	/**
	 * Transform cc commit to svn revision
	 * 
	 * @param commit
	 */
	private void transformCommit(STCommit commit) {
		svnModel.createNewRevision(commit.getAuthor(), commit.getDate(), getCommitMessage(commit));
		svnModel.getCurRevision().getProperties().set(ST_REVISION_NUMBERS, commit.joinRevisionNumbers());
		svnModel.getCurRevision().getProperties().set(ST_CRS, getLinkedChangeRequestsUntranslated(commit));
		Map childTags = new HashMap(); // STLabel -> List of STRevisions

		for (Iterator i = commit.getRevisions().iterator(); i.hasNext();) {
			STRevision revision = (STRevision) i.next();
			transformRevision(revision);

			if (!isOnlyTrunk() && !isIgnoreLabels()) {
				STLabel[] labels = revision.getLabels();
				for (int j = 0; j < labels.length; j++) {
					STLabel label = labels[j];
					String labelName = label.getName();
					if (!childTags.containsKey(labelName)) {
						childTags.put(labelName, new ArrayList());
					}
					((Collection) childTags.get(labelName)).add(revision);
				}
			}
		}

		if (!isOnlyTrunk() && !isIgnoreLabels()) {
			// create child tags
			// If we have child tags, we introduce a new SVN Revision. We must
			// do this
			// since Subversion is able to copy files only if the source
			// revision is smaller
			// than the actual revision.
			if (!childTags.isEmpty()) {
				if (provider.getConfig().useFileCopy()) {
					// Remember old SVN revision number.
					int oldRevno = svnModel.getCurRevisionNumber();
					svnModel.createNewRevision(commit.getAuthor(), commit.getDate(), "svnimporter: adding tags to revision " + oldRevno);

					for (Iterator i = childTags.keySet().iterator(); i.hasNext();) {
						String tagName = (String) i.next();
						// create tag, if necessary
						if (!svnModel.isTagCreated(tagName))
							svnModel.createTag(tagName, commit.getDate());
						// copy files into tag folder
						for (Iterator j = ((Collection) childTags.get(tagName)).iterator(); j.hasNext();) {
							STRevision revision = (STRevision) j.next();
							svnModel.addFileCopyToTag(revision.getAbsolutePath(), tagName, revision.getBranch().getBranchName(), revision.getAbsolutePath(), oldRevno);
						}
					}
				} else {
					// doesn't use file copy
					for (Iterator i = childTags.keySet().iterator(); i.hasNext();) {
						String tagName = (String) i.next();
						if (!svnModel.isTagCreated(tagName))
							svnModel.createTag(tagName, commit.getDate());

						for (Iterator j = ((Collection) childTags.get(tagName)).iterator(); j.hasNext();) {
							STRevision ccRevision = (STRevision) j.next();
							svnModel.addFileToTag(ccRevision.getAbsolutePath(), tagName, provider.createContentRetriever(ccRevision));
						}
					}
				}
			}
		}// !onlytrunk
	}

	/**
	 * @param commit
	 * @return
	 */
	private String getLinkedChangeRequestsUntranslated(STChangeRequestLinkTarget commit) {
		StringBuffer message = new StringBuffer();
		STChangeRequest[] linkedCRs = commit.getLinkedChangeRequests();
		for (int i = 0; i < linkedCRs.length; i++) {
			STChangeRequest cr = linkedCRs[i];
			if (i > 0)
				message.append(",");
			message.append(cr.getNumber());
		}
		return message.toString();
	}

	/**
	 * Returns the transformed commit message according to the configuration.
	 * 
	 * @param commit
	 * @return
	 */
	private String getCommitMessage(STCommit commit) {
		if (!isAttachLinkedCRsToCommitMessage())
			return commit.getMessage();

		StringBuffer message = new StringBuffer();
		String originalMessage = commit.getMessage();
		if (null != originalMessage && originalMessage.trim().length() > 0) {
			message.append(originalMessage);
			message.append(' ');
		}
		message.append("[");
		STChangeRequest[] linkedCRs = commit.getLinkedChangeRequests();
		for (int i = 0; i < linkedCRs.length; i++) {
			STChangeRequest cr = linkedCRs[i];

			if (i > 0)
				message.append(", ");

			String translatedCrString = provider.getConfig().translateCrNumber(cr.getNumber());
			if (null != translatedCrString) {
				message.append(translatedCrString);
				message.append(" - ");
			}

			message.append("CR# ");
			message.append(cr.getNumber());
		}
		message.append("]");
		return message.toString();
	}

	/**
	 * @return
	 */
	private boolean isAttachLinkedCRsToCommitMessage() {
		return provider.getConfig().isAttachLinkedCRsToCommitMessage();
	}

	/**
	 * @return
	 */
	private boolean isIgnoreLabels() {
		return provider.getConfig().isIgnoreLables();
	}

	/**
	 * Transform cc revision to svn svn action
	 * 
	 * @param revision
	 */
	private void transformRevision(STRevision revision) {
		String path = revision.getAbsolutePath();
		STRevisionState state = revision.getState();

		boolean trunk = revision.getBranch().isTrunk();

		String branchName = revision.getBranch().getBranchName();

		SvnProperties p = new SvnProperties();

		p.set(ST_REVISION, revision.getNumber());
		p.set(ST_USER, revision.getStUserName());
		p.set(ST_MODIFICATION_TIME, revision.getStDate());
		p.set(ST_CRS, getLinkedChangeRequestsUntranslated(revision));

		if (revision.getLabels().length > 0)
			p.set(ST_LABELS, formatLabels(revision));

		if (STRevisionState.ADD.equals(state)) {
			if (!trunk && !svnModel.isBranchCreated(branchName)) {
				svnModel.createBranch(branchName, revision.getDate());
			}
			svnModel.addFile(path, branchName, provider.createContentRetriever(revision), p);
		} else if (STRevisionState.CHANGE.equals(state)) {
      if (svnModel.getBranch(branchName) != null) {
        svnModel.changeFile(path, branchName, provider.createContentRetriever(revision), p);
      }
		} else if (STRevisionState.DELETE.equals(state)) {
			svnModel.deleteFile(path, branchName, p);
		} else {
			throw new STException("unknown st revision state: " + state);
		}
	}

	private String formatLabels(STRevision revision) {
		STLabel[] labels = revision.getLabels();
		StringBuffer b = new StringBuffer();
		for (int i = 0; i < labels.length; i++) {
			if (b.length() > 0)
				b.append(", ");
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

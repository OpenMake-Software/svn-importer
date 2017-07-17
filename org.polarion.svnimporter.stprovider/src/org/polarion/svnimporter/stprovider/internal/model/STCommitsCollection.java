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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.common.Util;
import org.polarion.svnimporter.common.model.Commit;
import org.polarion.svnimporter.common.model.CommitsCollection;
import org.polarion.svnimporter.common.model.ModelFile;
import org.polarion.svnimporter.stprovider.STException;
import org.polarion.svnimporter.stprovider.internal.STConfig;

/**
 * @author <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class STCommitsCollection extends CommitsCollection {

	private static final Log LOG = Log.getLog(STCommitsCollection.class);

	private final Comparator COMMITS_BY_DATE_COMPARATOR = new Comparator() {
		public int compare(Object o1, Object o2) {
			Commit c1 = (Commit) o1;
			Commit c2 = (Commit) o2;
			int diff = c1.getDate().compareTo(c2.getDate());
			if (diff == 0) {
				// if commits have equal date - try to determine order by
				// revision numbers
				if (c1 != c2)
					diff = compareEqualDateCommits(c1, c2);
			}
			if (diff == 0)
				diff = 1;
			return diff;
		}
	};

	/**
	 * Commit id -> Commit
	 */
	private final Map commitsByCommitId = new HashMap();

	private final STConfig config;
	private final int checkinTimeSpanMills;

	public STCommitsCollection(STModel model) {
		super(STCommit.class);
		if (null == model.getConfig())
			throw new IllegalArgumentException("model has not been initialized");
		this.config = model.getConfig();
		this.checkinTimeSpanMills = 1000 * config.getCheckinTimeSpan();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.polarion.svnimporter.common.model.CommitsCollection#addFile(org.polarion.svnimporter.common.model.ModelFile)
	 */
	public void addFile(ModelFile modelFile) {
		for (Iterator i = modelFile.getRevisions().values().iterator(); i.hasNext();) {
			STRevision curRevision = (STRevision) i.next();
			Commit commit = getExistingCommit(curRevision);
			String commitId = getCommitId(curRevision);
			if (null == commit) {
				// create a new one
				commitsByCommitId.put(commitId, createCommit(curRevision));
			} else {
				commit.addRevision(curRevision);
			}
		}
	}

	/**
	 * @param revision
	 * @return
	 */
	private STCommit getExistingCommit(STRevision revision) {

		// lookup using the commit id
		String commitId = getCommitId(revision);
		if (commitsByCommitId.containsKey(commitId)) {
			return (STCommit) commitsByCommitId.get(commitId);
		}

		// before we really start a new commit lets see if we can find a
		// related checking within a certain timespan
		if (checkinTimeSpanMills > 0) {
			// go through all commits
			Object[] commits = commitsByCommitId.values().toArray();
			Arrays.sort(commits, COMMITS_BY_DATE_COMPARATOR);
			for (int i = 0; i < commits.length; i++) {
				STCommit commit = (STCommit) commits[i];
				long diff = commit.getDate().getTime() - revision.getDate().getTime();
				if (Math.abs(diff) <= checkinTimeSpanMills && commit.getAuthor().equals(revision.getAuthor()) && (config.isSeparateCommitsUsingCRs() || commit.getMessage().equals(revision.getMessage()))) {
					return commit;
				}
			}
		}
		return null;
	}

	protected int compareEqualDateCommits(Commit c1, Commit c2) {
		// if commits have equal date - try to determine order by
		// revision numbers
		Iterator i = c1.getRevisions().iterator();
		while (i.hasNext()) {
			STRevision r1 = (STRevision) i.next();
			List revisionsR2 = c2.getRevisions(r1.getModelFile());
			if (revisionsR2 == null || revisionsR2.isEmpty())
				continue;
			for (Iterator r2i = revisionsR2.iterator(); r2i.hasNext();) {
				STRevision r2 = (STRevision) r2i.next();
				if (r1.getBranch().equals(r2.getBranch())) {
					return r1.getNumberInBranch() - r2.getNumberInBranch();
				}
			}
		}
		return 0;
	}

	private STCommit createCommit(STRevision revision) {
		STCommit commit = new STCommit();
		commit.setAuthor(revision.getAuthor());
		commit.setMessage(revision.getMessage());
		commit.setDate(revision.getDate());
		commit.addRevision(revision);
		return commit;
	}

	private String getCommitId(STRevision revision) {
		if (config.isSeparateCommitsUsingCRs()) {
			STChangeRequest[] linkedCrs = revision.getLinkedChangeRequests();
			if (linkedCrs.length > 0) {
				// a revision can only be checked in once
				// thus, use the oldest cr
				Arrays.sort(linkedCrs, STChangeRequest.BY_LAST_CHECKIN_TIME_COMPERATOR);
				return Util.getHash(linkedCrs[0].getNumber() + revision.getAuthor());
			}
		}

		// use traditional commit id
		return Util.getHash(revision.getDate().getTime() + revision.getAuthor() + revision.getMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.polarion.svnimporter.common.model.CommitsCollection#separateCommits()
	 */
	public List separateCommits() {
		// sort commits by date
		TreeSet allCommitsSorted = new TreeSet(COMMITS_BY_DATE_COMPARATOR);
		allCommitsSorted.addAll(commitsByCommitId.values());

		// validate the commits
		LOG.info("Validating commits...");
		STCommit[] sortedCommits = (STCommit[]) allCommitsSorted.toArray(new STCommit[allCommitsSorted.size()]);
		validateCommits(sortedCommits);

		return Arrays.asList(sortedCommits);
	}

	/**
	 * @param sortedCommits
	 */
	private void validateCommits(STCommit[] sortedCommits) {
		for (int i = 0; i < sortedCommits.length; i++) {
			STCommit commit = sortedCommits[i];
			for (Iterator stream = commit.getRevisions().iterator(); stream.hasNext();) {
				STRevision rev = (STRevision) stream.next();
				for (int j = i; j < sortedCommits.length; j++) {
					STCommit nextCommit = sortedCommits[j];

					/*
					 * first, lets make sure that whatever happens any revision
					 * was checked in before the next commit
					 */
					if (commit.getDate().before(nextCommit.getDate()) && !rev.getDate().before(nextCommit.getDate())) {
						/*
						 * this actually might happen when commits are collect
						 * by the same user in a given time span; we just log a
						 * warning and continue with additional validation
						 * checks which will make sure that following commits
						 * don't touch older revisions
						 */
						LOG.warn("Found overlapping commits! You might consider reducing the time span configured in st.checkintimespan!\n\nFirst commit:\n" + commit.getDebugInfo() + "\n\nFollowing commit:\n" + nextCommit.getDebugInfo() + "\n\n");
					}

					/*
					 * now we check for a later commit but with a preceding
					 * revision for the current file; this should never happen
					 */
					List followingRevisions = nextCommit.getRevisions(rev.getModelFile());
					if (null != followingRevisions) {
						for (Iterator stream2 = followingRevisions.iterator(); stream2.hasNext();) {
							STRevision nextRev = (STRevision) stream2.next();
							if (nextRev.getNumberInBranch() <= rev.getNumberInBranch()) {
								if (commit == nextCommit) {
									// just log a message that multiple versions
									// will be collected into one change
									if(nextRev.getNumberInBranch() != rev.getNumberInBranch())
										LOG.info("The following revisions were made by the same author in the configure time span. They will be collected into one change: " + rev.getPath() + "  revision " + rev.getNumber() + " and " + nextRev.getNumber());
								} else {
									// this should never happen: a later commit
									// but with an earlier revision
									final String errorMessage = "Found later commit with preceding revision! If you are running with st.separatecommitsusingcrs turned on this may indicate that your repository cannot converted using this option. Please disable it. If it is already disabled you need to reduce the time span configured in st.checkintimespan. If the time span is already 0 may have a broken repository or found a bug in this import tool. You may want to report this case to use.";
									LOG.error("Found later commit with preceding revision for file: " + rev.getPath() + "\n\nFirst commit:\n" + commit.getDebugInfo() + "\n\nFollowing commit:\n" + nextCommit.getDebugInfo() + "\n\n" + errorMessage);
									throw new STException(
											errorMessage);
								}
							}
						}
					}
				}
			}
		}
	}

}

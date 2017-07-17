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

package org.polarion.svnimporter.cvsprovider.internal.model;

import org.netbeans.lib.cvsclient.command.log.LogInformation;
import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.common.Util;
import org.polarion.svnimporter.common.model.CommitsCollection;
import org.polarion.svnimporter.common.model.Model;
import org.polarion.svnimporter.cvsprovider.internal.CvsUtil;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * 
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class CvsModel extends Model {
	private static final Log LOG = Log.getLog(CvsModel.class);
	private boolean onlyTrunk = false;

	public CvsModel() {
	}

	public void setOnlyTrunk(boolean onlyTrunk) {
		this.onlyTrunk = onlyTrunk;
	}

	/**
	 * Record cvs log information (provided by javacvs)
	 *
	 * @param logInformation
	 */
	public void addLogInfo(String targetPath,  LogInformation logInformation) {
		CvsFile cvsFile = new CvsFile(targetPath);
		addFile(cvsFile);

		try {
			// add 'trunk' branch
			CvsBranch trunk = new CvsBranch("1");
			trunk.setTrunk(true);
			cvsFile.addBranch(trunk);

			// record revisions and branches
			List revlist = logInformation.getRevisionList();
			for (int i = 0; i < revlist.size(); i++) {
				LogInformation.Revision o = (LogInformation.Revision) revlist.get(i);
				if (o.getNumber() == null) {
					LOG.warn("file revision " + targetPath + " with null revision number, skip entire file");
					LOG.warn("author=" + o.getAuthor() + " date=" + Util.toString(o.getDate()) + " message=" + Util.escape(o.getMessage()));
					removeFile(cvsFile);
					return;
				}
				CvsRevision revision = new CvsRevision(o.getNumber());
				revision.setModelFile(cvsFile);
				revision.setAuthor(o.getAuthor());

                String message = o.getMessage();
                // javacvs adds an extra new-line at the end of the message,
                // remove it
                /* will break incremental dumps which had been maked by old version of svnimporter
                if(message.endsWith("\n"))
                    message = message.substring(0, message.length()-1);
                */
                revision.setMessage(message);

                revision.setDate(o.getDate());
				revision.setRcsState(o.getState());

				if (onlyTrunk) {
					String branchNumber = CvsUtil.revNum2branchNum(revision.getNumber());
					if (!trunk.getNumber().equals(branchNumber)) {
						LOG.debug("skip non trunk revision: " + targetPath + " " + revision.getNumber());
						continue;
					}
				}

				if (!cvsFile.addRevision(revision))
					continue;

				if (!onlyTrunk) {
					if (o.getBranches() != null) {
						String[] ss = CvsUtil.splitBranches(o.getBranches());
						for (int j = 0; j < ss.length; j++) {
							String branchNumber = ss[j];
							CvsBranch branch = new CvsBranch(branchNumber);
							cvsFile.addBranch(branch);
							revision.addChildBranch(branch);
						}
					}
				}
			}
			if (!onlyTrunk) {
				// record symbols
				List symNames = logInformation.getAllSymbolicNames();
				for (int i = 0; i < symNames.size(); i++) {
					LogInformation.SymName symname = (LogInformation.SymName) symNames.get(i);
					String name = symname.getName();
					String revNum = symname.getRevision();

					if (CvsUtil.isBranch(revNum) || CvsUtil.isVendorTag(revNum)) {
						String branchNumber = (CvsUtil.isBranch(revNum)) ? CvsUtil.rawRevNum2BranchNum(revNum) : revNum;
						CvsBranch branch = cvsFile.getBranch(branchNumber);
						if (branch == null) {
							LOG.debug(targetPath + ": create branch by symbol " + revNum + " " + name);
							String sproutRevNum = CvsUtil.branchNum2sproutRevNum(branchNumber);
							CvsRevision sproutRevision = cvsFile.getRevision(sproutRevNum);
							if (sproutRevision == null) {
								LOG.warn(targetPath + ": unknown sprout revision " + sproutRevNum + " for branch " + name + " " + branchNumber);
								continue;
							}
							branch = new CvsBranch(branchNumber);
							cvsFile.addBranch(branch);
							sproutRevision.addChildBranch(branch);
						}
						branch.setName(name);
					} else { // tag
						if (cvsFile.getRevision(revNum) == null) {
							LOG.warn(targetPath + ": unknown revision for tag " + revNum + " " + name);
							continue;
						}
						CvsRevision revision = cvsFile.getRevision(revNum);
						CvsTag tag = new CvsTag(name);
						revision.addTag(tag);
					}
				}
			}//if(!onlyTrunk)

			// assign revisions to branches
			Collection revisions = cvsFile.getRevisions().values();
			for (Iterator i = revisions.iterator(); i.hasNext();) {
				CvsRevision revision = (CvsRevision) i.next();
				String branchNumber = CvsUtil.revNum2branchNum(revision.getNumber());
				if (cvsFile.getBranch(branchNumber) == null) {
					//log.error(targetPath + ": unknown branch number " + branchNumber + " for revision " + revision.getNumber());
					throw new CvsModelException(targetPath + ": unknown branch number " + branchNumber + " for revision " + revision.getNumber());
				}
				CvsBranch branch = cvsFile.getBranch(branchNumber);
				revision.setBranch(branch);
				branch.addRevision(revision);
			}

			// fix branches without names, determine revision states by rcs states
			for (Iterator i = cvsFile.getBranches().values().iterator(); i.hasNext();) {
				CvsBranch branch = (CvsBranch) i.next();
				if (!branch.isTrunk() && branch.getName() == null) {
					LOG.warn(targetPath + ": branch without name " + branch.getNumber() + ", set name=number");
					branch.setName(branch.getNumber());
				}
				branch.fixRevisions();
				branch.resolveRevisionStates();
			}
		} catch (CvsModelException e) {
			LOG.error("caught model exception '" + e.getMessage() + "', skip file: " + cvsFile.getPath());
			removeFile(cvsFile);
		}
	}

	/**
	 * Finish model after collecting all repository information
	 */
	public void finishModel() {
		separateCommits();
	}

	/**
	 * Separate commits
	 */
	private void separateCommits() {
		CommitsCollection c = new CommitsCollection(CvsCommit.class);
		c.addFiles(getFiles().values());
		getCommits().clear();
		getCommits().addAll(c.separateCommits());
	}

	/**
	 * Write debug info to stream
	 *
	 * @param out
	 */
	public void saveDebugInfo(PrintStream out) {
		out.println("*** CVS MODEL ***");
		out.println("* files *");
		for (Iterator i = getFiles().values().iterator(); i.hasNext();) {
			CvsFile cvsFile = (CvsFile) i.next();
			out.println("");
			cvsFile.saveDebugInfo(out);
		}
		out.println();
		out.println("* commits *");
		for (Iterator i = getCommits().iterator(); i.hasNext();) {
			CvsCommit commit = (CvsCommit) i.next();
			out.println("\n" + commit.getDebugInfo());
		}
	}
}


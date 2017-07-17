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
package org.polarion.svnimporter.svnprovider;

import org.polarion.svnimporter.common.IContentRetriever;
import org.polarion.svnimporter.common.ISvnModel;
import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.svnprovider.internal.SvnBranch;
import org.polarion.svnimporter.svnprovider.internal.SvnConst;
import org.polarion.svnimporter.svnprovider.internal.SvnNodeAction;
import org.polarion.svnimporter.svnprovider.internal.SvnRevision;
import org.polarion.svnimporter.svnprovider.internal.SvnTag;
import org.polarion.svnimporter.svnprovider.internal.SvnUtil;
import org.polarion.svnimporter.svnprovider.internal.SvnProperties;
import org.polarion.svnimporter.svnprovider.internal.SvnAutoProps;
import org.polarion.svnimporter.svnprovider.internal.actions.SvnAddBranch;
import org.polarion.svnimporter.svnprovider.internal.actions.SvnAddDir;
import org.polarion.svnimporter.svnprovider.internal.actions.SvnAddFile;
import org.polarion.svnimporter.svnprovider.internal.actions.SvnAddRootDir;
import org.polarion.svnimporter.svnprovider.internal.actions.SvnAddTag;
import org.polarion.svnimporter.svnprovider.internal.actions.SvnAddCopy;
import org.polarion.svnimporter.svnprovider.internal.actions.SvnAddChangedCopy;
import org.polarion.svnimporter.svnprovider.internal.actions.SvnChangeFile;
import org.polarion.svnimporter.svnprovider.internal.actions.SvnDelete;

import java.util.*;


/**
 *
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class SvnModel implements ISvnModel {
	protected String svnimporterUsername = "SVNIMPORTER";
	private String branchesPath;
	private String tagsPath;
	private Set createdRootPaths = new HashSet();

    /**
     * Module for which this revision belongs
     * useful only for incremental import for cvs with multiple modules (when cvs.modulename=*)
     */
    private String moduleName="";

    private SvnAutoProps svnAutoProps;

	/**
	 * The revision numbers in a SvnModel start with 1 + revisionOffset.
	 * For full SvnModels, revisionOffset = 0.
	 */
	private int revisionOffset = 0;

	private static final Log LOG = Log.getLog(SvnModel.class);


	/**
	 * Collection (sorted) of SvnRevisions
	 */
	private List revisions = new ArrayList();

	/**
	 * Map branch name to SvnBranch
	 */
	private Map branches = new HashMap();

	/**
	 * Map tag name to SvnTag
	 */
	private Map tags = new HashMap();

	/**
	 * Current SvnRevision (all following SvnNodeActions will be written to curRevision)
	 */
	private SvnRevision curRevision;

  /*
   * Set to keep trace which files have been added to SVN
   */
  private Set addFileToTagSet = null;

  /**
	 * "Trunk" branch
	 */
	private SvnBranch trunk;

	public SvnModel(int startrevision) {
		svnAutoProps = new SvnAutoProps("config.autoprops");
		revisionOffset = startrevision - 1;
    addFileToTagSet = new TreeSet();
  }

	public SvnModel() {
		this(1);
	}

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setSvnimporterUsername(String svnimporterUsername) {
		this.svnimporterUsername = svnimporterUsername;
	}

	public void setAutoProps(SvnAutoProps autoProps) {
		svnAutoProps = autoProps;
	}

	public List getRevisions() {
		return revisions;
	}

	public int getLastRevisionNumber() {
		return getRevisions().size() + revisionOffset;
	}

	public boolean isEmpty() {
		return getRevisions().size() < 1;
	}

	/**
	 * Create first revision
	 */
	public final void createFirstRevision(Date date) {
		createNewRevision(svnimporterUsername, date, "Initial revision");
//		addAction(new SvnAddRootDir("trunk"));
//		addAction(new SvnAddRootDir("branches"));
//		addAction(new SvnAddRootDir("tags"));
	}

	public void createTrunkPath(String trunkPath) {
		this.trunk = new SvnBranch("trunk", trunkPath);
		if (!"".equals(trunkPath))
			createRootPath(trunkPath);
	}

	public void createTagsPath(String tagsPath) {
		this.tagsPath = tagsPath;
		createRootPath(tagsPath);
	}

	public void createBranchesPath(String branchesPath) {
		this.branchesPath = branchesPath;
		createRootPath(branchesPath);
	}

	/**
	 * Create new revision
	 */
	public void createNewRevision(String author, Date date, String log) {
		int newRevisionNumber = getLastRevisionNumber() + 1;
		LOG.debug("create new revision " + newRevisionNumber);
		SvnRevision revision = new SvnRevision();
		revision.setNumber(newRevisionNumber);
		revision.setAuthor(author);
		revision.setDate(date);
		revision.setMessage(log);
        revision.setModuleName(moduleName);
        revisions.add(revision);
		curRevision = revision;
	}

	/**
	 * Add action to current revision
	 *
	 * @param action
	 */
	private void addAction(SvnNodeAction action) {
		LOG.debug("add action: " + action.getDebugInfo());
    curRevision.addAction(action);
  }

	/**
	 * Get branch by name (if not exist - throw exception)
	 *
	 * @param branchName
	 * @return
	 */
	public SvnBranch getBranch(String branchName) {
		if (branchName == null || trunk.getPath().equals(branchName)) {
			return trunk;
		} else {
			SvnBranch branch = (SvnBranch) branches.get(branchName);
			if (branch == null) {
				throw new SvnException("Unknown branch: " + branchName);
      }
			return branch;
		}
	}

	/**
	 * Get tag by name (if tag does not exist - throw SvnException)
	 *
	 * @param tagName
	 * @return
	 */
	private SvnTag getTag(String tagName) {
		SvnTag tag = (SvnTag) tags.get(tagName);
		if (tag == null)
			throw new SvnException("Unknown tag: " + tagName);
		return tag;
	}

	private SvnRevision getRevision(int revisionNumber) {
		// the SVN revisions in the model start with 1 + revisionOffset, but the array
		// indizes start with 0.
		return (SvnRevision) revisions.get(revisionNumber - revisionOffset - 1);
	}

	public int getCurRevisionNumber() {
		return curRevision.getNumber();
	}

    public SvnRevision getCurRevision() {
        return curRevision;
    }

    /**
	 * Create 'add dir' action in current revision
	 *
	 * @param path
	 * @param branch
	 */
	public void addDir(String path, SvnBranch branch) {
		if (branch == null)
			branch = trunk;
		addAction(new SvnAddDir(path, branch));
	}


	/**
	 * a variant of addFile that allows to set properties
	 */
	public void addFile(String path, String branchName,
						IContentRetriever contentRetriever, SvnProperties properties) {
    SvnBranch branch = getBranch(branchName);
    createPathIfNeed(branch, path);
    SvnAddFile addFile = new SvnAddFile(path, branch, contentRetriever);
    addFile.setProperties(applyAutoprops(path, properties));
    addAction(addFile);
  }

	/**
	 * Create 'add file' action in current revision
	 *
	 * For downwards compatibility.
	 *
	 * @param path
	 * @param branchName - if null then file will be added to trunk
	 */
	public void addFile(String path, String branchName,
						IContentRetriever contentRetriever) {
		addFile(path, branchName, contentRetriever, new SvnProperties());
	}

	/**
	 * Create 'add file' action in current revision
	 *
	 * @param path
	 * @param tagName NOT NULL
	 */
	public void addFileToTag(String path, String tagName,
							 IContentRetriever contentRetriever) {
		addFileToTag(path, tagName,  contentRetriever, new SvnProperties());
	}

    /**
	 * Create 'add file' action in current revision
	 *
	 * @param path
	 * @param tagName NOT NULL
	 */
	public void addFileToTag(String path, String tagName,
							 IContentRetriever contentRetriever, SvnProperties properties) {
    if (! fileAlreadyAddedToTag(tagName, path)) {
      SvnTag tag = getTag(tagName);
      createPathIfNeed(tag, path);
      SvnAddFile action = new SvnAddFile(path, tag, contentRetriever);
      action.setProperties(applyAutoprops(path, properties));
      addAction(action);
    } else {
      LOG.debug(path + " already added to tag '" + tagName + "' in some revision. Skipping addition in " + curRevision.getNumber());
    }
  }

    /**
	 * @param path
	 * @param branchName
	 * @param srcBranch
	 * @param srcPath
	 * @param srcRevisionNum
	 */
	public void addFileCopy(String path,
							String branchName,
							String srcBranch,
							String srcPath,
							int srcRevisionNum) {
		SvnBranch branch = getBranch(branchName);
		createPathIfNeed(branch, path);
		SvnAddCopy action = new SvnAddCopy("file", path, branch,
				getBranch(srcBranch), getRevision(srcRevisionNum), srcPath);
		addAction(action);
	}

	public void addFileCopyToTag(String path,
								 String tagName,
								 String srcBranch,
								 String srcPath,
								 int srcRevisionNum) {
		if (! fileAlreadyAddedToTag(tagName, path))
		{
			SvnTag tag = getTag(tagName);
			createPathIfNeed(tag, path);
			SvnAddCopy action = new SvnAddCopy("file", path, tag,
			getBranch(srcBranch), getRevision(srcRevisionNum), srcPath);
			addAction(action);

		}
		else
		{
			LOG.debug(path + " already added to tag '" + tagName + "' in some revision. Skipping addition in " + curRevision.getNumber());
		}
	}

	public void addFileCopyToBranch(String path,
								 String branchName,
								 String srcBranch,
								 String srcPath,
								 int srcRevisionNum,
								 IContentRetriever contentRetriever) {
		SvnBranch branch = getBranch(branchName);
		createPathIfNeed(branch, path);
		SvnAddChangedCopy action = new SvnAddChangedCopy(path, branch,
				getBranch(srcBranch), getRevision(srcRevisionNum), srcPath,
        contentRetriever);
		addAction(action);
	}

    public void addFileCopyToBranch(String path,
								 String branchName,
								 String srcBranch,
								 String srcPath,
								 int srcRevisionNum
								 ) {
		SvnBranch branch = getBranch(branchName);
		createPathIfNeed(branch, path);
    SvnAddCopy action = new SvnAddCopy("file", path, branch,
        getBranch(srcBranch), getRevision(srcRevisionNum), srcPath);
    addAction(action);
	}

    /**
	 * Create directory in branch (add addDir action to curRevision) if not exists
	 *
	 * @param branch
	 * @param path
	 */
	private void createPathIfNeed(SvnBranch branch, String path) {
		String parentPath = path;
		List toCreate = new ArrayList();
		while (true) {
			parentPath = SvnUtil.getParentPath(parentPath);
			if ("".equals(parentPath) || branch.isPathCreated(parentPath)) break;
			toCreate.add(0, parentPath);
		}
		for (Iterator i = toCreate.iterator(); i.hasNext();) {
			String pp = (String) i.next();
			addDir(pp, branch);
			branch.pathCreated(pp);
		}
	}

	private void createRootPath(String path) {
		String parentPath = path;
		List toCreate = new ArrayList();
		while (true) {
			parentPath = SvnUtil.getParentPath(parentPath);
			if ("".equals(parentPath)) break;
			toCreate.add(0, parentPath);
		}
		toCreate.add(path);
		for (Iterator i = toCreate.iterator(); i.hasNext();) {
			String pp = (String) i.next();
			if (!createdRootPaths.contains(pp)) {
				addAction(new SvnAddRootDir(pp));
				createdRootPaths.add(pp);
			}
		}
	}

	/**
	 * Create 'change file' action in current revision
	 *
	 * @param path
	 * @param branchName NOT NULL
	 */
	public void changeFile(String path, String branchName,
						   IContentRetriever contentRetriever) {
		changeFile(path, branchName,  contentRetriever, new SvnProperties());
	}

    /**
	 * Create 'change file' action in current revision
	 *
	 * @param path
	 * @param branchName NOT NULL
	 */
	public void changeFile(String path, String branchName,
						   IContentRetriever contentRetriever, SvnProperties properties) {
      SvnChangeFile action = new SvnChangeFile(path, getBranch(branchName), contentRetriever);
      action.setProperties(applyAutoprops(path, properties));
      addAction(action);
	}

    /**
	 * Create 'delete' action in current revision
	 *
	 * @param path
	 * @param branchName
	 */
	public void deleteFile(String path, String branchName) {
		deleteFile(path, branchName, new SvnProperties());
	}

    /**
	 * Create 'delete' action in current revision
	 *
	 * @param path
	 * @param branchName
	 */
	public void deleteFile(String path, String branchName, SvnProperties properties) {
        SvnDelete action = new SvnDelete(path, getBranch(branchName));
        action.setProperties(applyAutoprops(path, properties));
        addAction(action);
	}

    /**
	 * @param newBranchName
	 * @return
	 */
	public boolean isBranchCreated(String newBranchName) {
		return branches.containsKey(newBranchName);
	}

	/**
	 * Create new SvnBranch
	 *
	 * @param newBranchName
	 */
	public void createBranch(String newBranchName, Date date) {
		LOG.debug("create branch " + newBranchName);
		if (branches.containsKey(newBranchName))
			throw new SvnException("duplicated branch: " + newBranchName);
//		createNewRevision(svnimporterUsername, date,
//				"This commit was manufactured by svnimporter to create branch '" + newBranchName + "'.");
		String newBranchPath;
		if ("".equals(branchesPath)) {
			newBranchPath = newBranchName;
		} else {
			newBranchPath = branchesPath + SvnConst.PATH_SEPARATOR + SvnUtil.escapeBranchName(newBranchName);
		}
		SvnBranch newBranch = new SvnBranch(newBranchName, newBranchPath);
		branches.put(newBranchName, newBranch);
		addAction(new SvnAddBranch(newBranchPath, newBranchName));
	}

	public boolean isTagCreated(String tagName) {
		return tags.containsKey(tagName);
	}

	/**
	 * Create new SvnTag
	 *
	 * @param newTagName
	 */
	public void createTag(String newTagName, Date date) {
		LOG.debug("create tag " + newTagName);
		if (tags.containsKey(newTagName))
			throw new SvnException("duplicated tag: " + newTagName);
//		createNewRevision(svnimporterUsername, date,
//				"This commit was manufactured by svnimporter to create tag '" + newTagName + "'.");
		SvnTag newTag = new SvnTag(newTagName, tagsPath + SvnConst.PATH_SEPARATOR + SvnUtil.escapeBranchName(newTagName));
		tags.put(newTagName, newTag);
		addAction(new SvnAddTag(newTag.getPath(), newTagName));
	}

	/**
	 * Dump model to stdout (for debug)
	 */
	public void dump() {
		System.out.println("*** SVN MODEL ***");
		for (Iterator i = revisions.iterator(); i.hasNext();) {
			SvnRevision revision = (SvnRevision) i.next();
			System.out.println("\n" + revision.getDebugInfo());
		}
	}

  private SvnProperties applyAutoprops(String path, SvnProperties sp) {
    SvnProperties props = svnAutoProps.getProperties(path);
    // autoprops are overridden by the properties given as argument
    props.setAll(sp);
    return props;
  }

  private boolean fileAlreadyAddedToTag(String tag, String path) {
    boolean found = false;
    String absPath = tag + ":" + path;

    if (! addFileToTagSet.contains(absPath)) {
      addFileToTagSet.add(absPath);
    } else {
      found = true;
    }

    return found;
  }
}

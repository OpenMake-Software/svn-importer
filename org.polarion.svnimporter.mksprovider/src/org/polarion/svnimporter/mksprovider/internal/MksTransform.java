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
package org.polarion.svnimporter.mksprovider.internal;

import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.common.model.Revision;
import org.polarion.svnimporter.mksprovider.MksException;
import org.polarion.svnimporter.mksprovider.MksProvider;
import org.polarion.svnimporter.mksprovider.internal.model.MksBranch;
import org.polarion.svnimporter.mksprovider.internal.model.MksCheckpoint;
import org.polarion.svnimporter.mksprovider.internal.model.MksCommit;
import org.polarion.svnimporter.mksprovider.internal.model.MksModel;
import org.polarion.svnimporter.mksprovider.internal.model.MksRevision;
import org.polarion.svnimporter.mksprovider.internal.model.MksRevisionState;
import org.polarion.svnimporter.svnprovider.SvnModel;
import org.polarion.svnimporter.svnprovider.internal.SvnProperties;

import java.util.Iterator;
import java.util.Map;

/**
 *
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class MksTransform {
	private static final Log LOG = Log.getLog(MksTransform.class);

	private final MksProvider provider;
	private String mksRootDir;
	private SvnModel svnModel;

	public MksTransform(MksProvider mksProvider) {
		this.provider = mksProvider;
		this.mksRootDir = mksProvider.getConfig().getMksRootDir();
	}

	/**
	 * Transform mksModel to SvnModel
	 *
	 * @return
	 */
	public SvnModel transform(MksModel srcModel) {
		if (srcModel.getCommits().size() < 1) {
			return new SvnModel();
		}
		svnModel = new SvnModel();
		svnModel.setSvnimporterUsername(provider.getConfig().getSvnimporterUsername());
		MksCommit firstCommit = (MksCommit) srcModel.getCommits().get(0);
		svnModel.createFirstRevision(firstCommit.getDate());
		svnModel.createTrunkPath(provider.getConfig().getTrunkPath());
		if (!isOnlyTrunk()) {
			svnModel.createBranchesPath(provider.getConfig().getBranchesPath());
			svnModel.createTagsPath(provider.getConfig().getTagsPath());
		}
		for (Iterator i = srcModel.getCommits().iterator(); i.hasNext();)
			transformCommit((MksCommit) i.next());
		return svnModel;
	}

	/**
	 * @return
	 */
	private boolean isOnlyTrunk() {
		return provider.getConfig().isOnlyTrunk();
	}

	/**
	 * Transform MksCommit to SvnRevision
	 *
	 * @param commit
	 */
	private void transformCommit(MksCommit commit) {
	    
	    // Set flag indicating a new SVN revision is needed
	    // we won't actually build this revision until we find something in this
	    // commit that requires the new revision
	    boolean buildSvnRevision = true;

		for (Iterator i = commit.getRevisions().iterator(); i.hasNext();) {
		    
		    // Things get a bit sticky here.  The revision list may be a
		    // combination of MksRevision and MksCheckpoint objects which
		    // have to be sorted out
			Revision baseRevision = (Revision) i.next();
			if (baseRevision instanceof MksCheckpoint) {
			    
			    // If it is a checkpoint, Skip if orphaned.
			    MksCheckpoint checkpoint = (MksCheckpoint)baseRevision;
			    if (checkpoint.isAssigned()) {

			        // Otherwise, see if any tags are being generated
	                // for this checkpoint. If they are, build the SVN revision
	                // (if necessary) and generate the checkpoint tags
    			    String[] tags = checkpoint.getTags(provider.getConfig());
    			    if (tags != null) {
    			        if (buildSvnRevision) {
    	                    createSvnRevision(commit);
    	                    buildSvnRevision = false;
    			        }
    			        buildCheckpointTags(checkpoint, tags);
    			    }
			    }
			} else {
			    
			    // Normal revisions are only skipped if they belong to a branch
			    // and only trunk revisions are being processed.
                MksRevision revision = (MksRevision)baseRevision;
                if (revision.isAssigned()) {
                    if (! isOnlyTrunk() || revision.getBranch().isTrunk()) {
        			    if (buildSvnRevision) {
        			        createSvnRevision(commit);
        			        buildSvnRevision = false;
        			    }
            			transformRevision(revision);
                    }
                }
			}
		}
	}

	/**
	 * create a new SVN revision based on MksCommit
	 * @param commit MksCommit object used to construct SVN revision
	 */
	private void createSvnRevision(MksCommit commit) {
        svnModel.createNewRevision(commit.getAuthor(), commit.getDate(), commit.getMessage());
        svnModel.getCurRevision().getProperties().set("MKSRevisionNumbers", commit.joinRevisionNumbers());
	}
	
	/**
	 * Build a specific tag based on checkpoint
	 * @param checkpoint the checkpoint
	 * @param tagName the tag name
	 */
	private void buildCheckpointTags(MksCheckpoint checkpoint, String[] tagNames) {
	    
	    // Loop through the tag names
	    for (int ii = 0; ii < tagNames.length; ii++) {
	        String tagName = tagNames[ii];
            
    	    // create tag, if necessary
            if (!svnModel.isTagCreated(tagName)) {
                svnModel.createTag(tagName, checkpoint.getDate());
            }
            
            // Loop through all of the revisions in this checkpoint
    	    for (Iterator itr = checkpoint.getRevisions().iterator(); itr.hasNext(); ) {
    	        MksRevision revision = (MksRevision)itr.next();
    	        
    	        // Skip if orphaned
    	        if (! revision.isAssigned()) {
    	            LOG.warn("Checkpoint " + checkpoint.getNumber() +
    	                     " contains orphaned revision " +
    	                     revision.getModelFile().getPath() + ":" +
    	                     revision.getNumber());
    	        } else {
        	        
        	        // Otherwise, create the tag file as a file copy or new file
        	        if (provider.getConfig().useFileCopy()) {
        	            if (revision.getSvnRevisionNumber() == 0) {
        	                throw new MksException("Source revision " +
        	                        revision.getModelFile().getPath() + ":" + revision.getNumber() +
        	                        " has not been assigned an SVN revision number");
        	            }
                        svnModel.addFileCopyToTag(revision.getPath(),
                                tagName,
                                revision.getBranch().getBranchName(),
                                revision.getPath(),
                                revision.getSvnRevisionNumber());
        	        } else {
                        svnModel.addFileToTag(revision.getPath(),
                                tagName,
                                provider.createContentRetriever(revision));
        	        }
    	        }
    	    }
	    }
	}

	/**
	 * Transform MksRevision to SvnNodeAction
	 *
	 * @param revision
	 */
	private void transformRevision(MksRevision revision) {
		String path = convertPath(revision.getPath());
		MksBranch branch = (MksBranch)revision.getBranch();
		String branchName = branch.getBranchName();
		
		// Set svn revision number
		revision.setSvnRevisionNumber(svnModel.getCurRevisionNumber());
		
		// Create branch if needed
		if (! branch.isTrunk() && ! svnModel.isBranchCreated(branchName)) {
            svnModel.createBranch(branchName, revision.getDate());
		}
		
		// Get the properties associated with this revision
        SvnProperties props = transformProperties(revision);
		
		if (revision.getState() == MksRevisionState.COPY) {
            MksRevision source = revision.getSource();
            if (source.getSvnRevisionNumber() == 0) {
                throw new MksException("Source revision " +
                        source.getModelFile().getPath() + ":" + source.getNumber() +
                        " has not been assigned an SVN revision number");
            }
            if (provider.getConfig().useFileCopy()) {
                svnModel.addFileCopyToBranch(path,
                        branchName,
                        source.getBranch().getName(),
                        convertPath(source.getPath()),
                        source.getSvnRevisionNumber());
            } else {
                svnModel.addFile(path,
                        branchName,
                        provider.createContentRetriever(source),
                        props);
            }
		} else if (revision.getState() == MksRevisionState.ADD) {
			svnModel.addFile(path, branchName, 
			                 provider.createContentRetriever(revision),
			                 props);
		} else if (revision.getState() == MksRevisionState.CHANGE) {
			svnModel.changeFile(path, branchName, 
			                    provider.createContentRetriever(revision), 
			                    props);
		} else if (revision.getState() == MksRevisionState.DELETE) {
			svnModel.deleteFile(path, branchName);
		} else {
			LOG.error(revision.getDebugInfo());
			LOG.error(revision.getBranch().getDebugInfo());
			throw new MksException("unknown mks revision state: " + revision.getState());
		}
	}
	
	private String convertPath(String path) {
	    
	    // If no root directory specified, just return the path
	    if (mksRootDir == null) return path;
	    
	    // If one was specified, it needs to be stripped off the front of
	    // the path name.  If the path name doesn't start with that directory
	    // something bad must have happened
	    if (! path.startsWith(mksRootDir)) {
	        throw new MksException("Path name:" + path + " is not in MKS root directory");
	    }
	    return path.substring(mksRootDir.length());
	}

	/**
	 * Transform MKS file properties to SVN properties
	 * @param revision revision being converted
	 * @return set of SVN properties
	 */
	private SvnProperties transformProperties(MksRevision revision) {
	    Map mksProps = revision.getModelFile().getProperties();
	    SvnProperties svnProps = new SvnProperties();
	    for (Iterator ii = mksProps.keySet().iterator(); ii.hasNext();) {
	        Object key = ii.next();
	        Object value = mksProps.get(key);
	        String keyStr = key.toString();
	        String valueStr = (value == null ? null : value.toString());
	        svnProps.set(keyStr, valueStr);
	    }
	    return svnProps;
	}
}


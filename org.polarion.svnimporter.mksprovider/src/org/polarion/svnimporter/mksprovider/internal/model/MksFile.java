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

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.common.RevisionNumber;
import org.polarion.svnimporter.common.model.ModelFile;
import org.polarion.svnimporter.mksprovider.MksException;

/**
 * 
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class MksFile extends ModelFile {
    private static final Log LOG = Log.getLog(MksFile.class);
    
    private MksSubproject project;
    private String relativePath;
    
    // Maps branches by development path
    private Map<String,MksBranch> branchPathMap = new HashMap<String,MksBranch>();

    /**
     * Construct file object
     * @param path File path relative to main project root
     * @param relativePath file path relative to parent subproject
     * @param attributes String describing file attributes
     * @param binary true if binary file
     */
    public MksFile(String path, String relativePath, 
                   String attributes, boolean binary) {
		super(path);
		this.project = null; // Project will be filled in latter
		this.relativePath = relativePath;
		parseAttributes(attributes);
		if (binary) {
		    addProperty("svn:mime-type", "application/octet-stream");
		} else {
		    addProperty("svn:eol-style", "native");
		}
	}
    
    private void parseAttributes(String attributes) {
        if (attributes.length() > 0) {
            String[] attrList = attributes.split(",");
            for (int ii = 0; ii<attrList.length; ii++) {
                String attr = attrList[ii];
                String key = attr;
                String value = null;
                int brk = attr.indexOf('=');
                if (brk >= 0) {
                    key = attr.substring(0,brk);
                    value = attr.substring(brk+1);
                }
                if (key.equals("executable")) key = "svn:executable";
                addProperty(key, value);
            }
        }
    }
    
    public String getRelativePath() {
        return relativePath;
    }

    /**
     * Find file branch by number
     * @param number branch number
     * @return branch if found, null otherwise
     */
	public MksBranch getBranch(String number) {
		return (MksBranch) getBranches().get(number);
	}
	
	/**
	 * Find file revision by number
	 * @param number revision number
	 * @return revision object if found, null otherwise
	 */
	public MksRevision getRevision(String number) {
	    return (MksRevision) getRevisions().get(number);
	}

	/**
	 * Find a particular file revision, or if the requested revision
	 * was a phantom lock copy locate the original version
	 * @param number requested revision number
	 * @return the requested revision number or null if one cannot be found
	 */
	public MksRevision getSourceRevision(String number) {
	    String revNumber = number;
	    while (true) {
	        
	        // Try to find this revision
	        // If we get one, return it
	        MksRevision revision = getRevision(revNumber);
	        if (revision != null) return revision;
	        
            // If not, and the number doesn't end with ".1" then it cannot be
            // a lock copy so it is time to bail out
	        int[] narry = RevisionNumber.parse(revNumber);
	        if (narry.length <= 2 || narry[narry.length-1] != 1) return null;
	        
	        // Otherwise strip off the last two components and try again
	        revNumber = RevisionNumber.getSubNumber(narry, 0, narry.length-2);
	    }
	}
	
	/**
	 * Define the parent subproject for this file.
	 * This could not be done by the constructor because file objects are
	 * initially created during rlog processing, which defines the file and
	 * it's revision history but contains no information about the subproject
	 * structure.  Subproject structure is added durring viewproject processing.
	 * @param project parent subproject
	 */
	public  void setProject(MksSubproject project) {
	    if (this.project == null) {
	        this.project = project;
	        
	        // There are SI clients that do a poor job of returning the relative
	        // path in the rlog command.  So we will play it safe and recompute
	        // it here
	        String projectDir = project.getProjectDir();
	        if (getPath().startsWith(projectDir)) {
	            relativePath = getPath().substring(projectDir.length());
	        } else {
	            throw new MksException("getPath() is not contained within suproject " + 
	                                   project.getProjectPath());
	        }
	    }
	}

    public MksSubproject getProject() {
        return project;
    }
    
    /**
     * Set association between file branch and project development path
     * @param branch Branch
     * @param devPath development path or null for main path
     */
    public void setBranchDevPath(MksBranch branch, String devPath) {
        
        // remove old path mapping, if one exists
        if (branch.isAssigned()) branchPathMap.remove(branch.getName());
        
        // And set up mapping for the new path
        branch.setName(devPath);
        branch.setTrunk(devPath == null);
        branchPathMap.put(devPath, branch);
    }
    
    /**
     * Get branch associated with development path
     * @param devPath
     * @return
     */
    public MksBranch getBranchForDevPath(String devPath) {
        return (MksBranch)branchPathMap.get(devPath);
    }

    /**
     * Check for any unassigned (orphaned) branches.  if any are found
     * assign them to a new orphan development path
     * @param orphanBranchPrefix prefix used to consturct orphan devpath name
     */
    public void assignOrphanBranches(String orphanBranchPrefix) {
        
        // Scan through all the files branches, if any are unassigned, save
        // them in a sorted list.  (We need to process them in sorted order
        // which we can't do until we have them all)
        SortedSet<MksBranch> orphanBranches = null;
        for (Iterator itr = getBranches().values().iterator(); itr.hasNext(); ) {
            MksBranch branch = (MksBranch)itr.next();
            if (! branch.isAssigned()) {
                if (orphanBranches == null) {
                    orphanBranches = new TreeSet(new BranchComparator());
                }
                orphanBranches.add(branch);
            }
        }
        
        // If any were found, scan through them assigning each one to a 
        // new orphan development path
        if (orphanBranches != null) {
            int branchCount = 1;
            for (Iterator itr = orphanBranches.iterator(); itr.hasNext(); ) {
                MksBranch branch = (MksBranch)itr.next();
                String branchName = orphanBranchPrefix + "_" + (branchCount++);
                LOG.info("Assigning branch " + getPath() + ":" + branch.getNumber() +
                         " to devpath " + branchName);
                setBranchDevPath(branch, branchName);
                
                // If the branch is not empty, we need to do something to
                // to initialize the first revision
                if (! branch.getRevisions().isEmpty()) {
                    MksRevision first = (MksRevision)branch.getRevisions().first();
                    MksRevision source = (MksRevision)branch.getSproutRevision();
                    
                    // If there is not source for this branch, set the first
                    // revision state to ADD
                    if (source == null) {
                        LOG.debug("set revision state of " + first.getNumber() + " to ADD");
                        first.setState(MksRevisionState.ADD);
                    } 
                    
                    // If there is a source, we need to create a dummy initial
                    // revision on this branch that copies the source revision
                    // For lack of any other useful info we'll use the 
                    // source author, date, and description
                    else {
                        LOG.debug("Copy from source " + source.getModelFile().getPath() +
                                ":" + source.getNumber());
                        MksRevision copyRevision = 
                          new MksRevision(branch.getNumber() + ".0",
                                          first.getAuthor(),
                                          first.getDate(),
                                          "Create orphan branch");
                        copyRevision.setState(MksRevisionState.COPY);
                        copyRevision.setSource(source);
                        
                        addRevision(copyRevision);
                        copyRevision.setBranch(branch);
                        branch.addRevision(copyRevision);
                    }
                }
            }
        }
    }
    
    private static class BranchComparator implements Comparator {
        public int compare(Object p1, Object p2) {
            return RevisionNumber.compare(((MksBranch)p1).getNumber(), 
                                          ((MksBranch)p2).getNumber());
        }
    }
}

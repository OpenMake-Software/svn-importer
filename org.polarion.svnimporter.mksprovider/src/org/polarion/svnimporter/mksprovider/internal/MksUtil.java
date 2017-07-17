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

import org.polarion.svnimporter.common.RevisionNumber;
import org.polarion.svnimporter.common.Util;
import org.polarion.svnimporter.mksprovider.internal.model.MksBranch;
import org.polarion.svnimporter.mksprovider.internal.model.MksCommit;
import org.polarion.svnimporter.mksprovider.internal.model.MksFile;
import org.polarion.svnimporter.mksprovider.internal.model.MksModel;
import org.polarion.svnimporter.mksprovider.internal.model.MksRevision;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * 
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class MksUtil {
	/**
	 * Print mks model
	 *
	 * @param model
	 */
	public static void print(MksModel model) {
		Collection files = model.getFiles().keySet();
		for (Iterator i = files.iterator(); i.hasNext();) {
			MksFile modelFile = (MksFile) model.getFiles().get(i.next());
			print(modelFile);
		}
		System.out.println();
		System.out.println();
		System.out.println("-----------------------------");
		System.out.println("commits: ");
		model.finishModel();
		List commits = model.getCommits();

		for (Iterator i = commits.iterator(); i.hasNext();) {
			MksCommit commit = (MksCommit) i.next();
			print(commit);
		}
	}

	/**
	 * Print mks model file
	 *
	 * @param modelFile
	 */
	public static void print(MksFile modelFile) {
		System.out.println("----------------");
		System.out.println(modelFile.getPath());
		Collection brkeys = modelFile.getBranches().keySet();
		for (Iterator i = brkeys.iterator(); i.hasNext();) {
			MksBranch branch = (MksBranch) modelFile.getBranches().get(i.next());
			print(branch);
		}
	}

	/**
	 * Print mks branch
	 *
	 * @param branch
	 */
	public static void print(MksBranch branch) {
		System.out.println("branch " + branch.getNumber());
		for (Iterator i = branch.getRevisions().iterator(); i.hasNext();) {
			MksRevision revision = (MksRevision) i.next();
			print(revision);
		}
	}

	/**
	 * Print mks revision
	 *
	 * @param revision
	 */
	public static void print(MksRevision revision) {
		System.out.println("\t Rev " + revision.getNumber());
		System.out.println("\t  date=" + Util.toString(revision.getDate()));
		System.out.println("\t  author=" + revision.getAuthor());
		System.out.println("\t  message=" + Util.escape(revision.getMessage()));
	}

	/**
	 * Print mks commit
	 *
	 * @param commit
	 */
	public static void print(MksCommit commit) {
		System.out.println("commit: "
				+ Util.toString(commit.getDate())
				+ " " + commit.getAuthor()
				+ " " + Util.escape(commit.getMessage()));
		for (Iterator i = commit.getRevisions().iterator(); i.hasNext();) {
			MksRevision revision = (MksRevision) i.next();
			System.out.println("\t" + revision.getModelFile().getPath() + " " + revision.getNumber());
		}
	}
    
    /**
     * Extract branch number from revision number
     * @param revNum revision number
     * @return coresponding branch number
     */
    public static String revNum2BranchNum(String revNum) {
        
        // Users occasionally set trunk revisions numbers to odd values
        // As long as there are only two components in the revision
        // number, assume it is on the main branch and return branch '1'.
        String result = RevisionNumber.stripLastComponent(revNum);
        if (result.indexOf('.') < 0) result = "1";
        return result;
    }
}


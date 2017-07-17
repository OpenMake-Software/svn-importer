///*
// * Copyright (c) 2004, 2005 Polarion Software, All rights reserved. 
// * Email: community@polarion.org
// * 
// * This program and the accompanying materials are made available under the 
// * terms of the Apache License, Version 2.0 (the "License"). You may not use 
// * this file except in compliance with the License. Copy of the License is
// * located in the file LICENSE.txt in the project distribution. You may also
// * obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
// * 
// *  
// * POLARION SOFTWARE MAKES NO REPRESENTATIONS OR WARRANTIES
// * ABOUT THE SUITABILITY OF THE SOFTWARE, EITHER EXPRESSED OR IMPLIED,
// * INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. POLARION SOFTWARE
// * SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT
// * OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
// * 
// */
///*
// * $Log$
// */
//package org.polarion.svnimporter.pvcsprovider.internal.model;
//
//import org.polarion.svnimporter.common.model.CommitsCollection;
//import org.polarion.svnimporter.common.model.Model;
//
//import java.util.Collection;
//import java.util.Iterator;
//
///**
// * 
// *
// * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
// */
//public class PvcsModel extends Model {
//	/**
//	 * Resolve revision states, separate commits
//	 */
//	public void finishModel() {
//		Collection files = getFiles().values();
//		for (Iterator i = files.iterator(); i.hasNext();) {
//			PvcsFile file = (PvcsFile) i.next();
//			Collection branches = file.getBranches().values();
//			for (Iterator j = branches.iterator(); j.hasNext();) {
//				PvcsBranch branch = (PvcsBranch) j.next();
//				branch.resolveRevisionStates();
//			}
//		}
//		separateCommits();
//	}
//
//	/**
//	 * Separate commits
//	 */
//	private void separateCommits() {
//		CommitsCollection c = new CommitsCollection(PvcsCommit.class);
//		c.addFiles(getFiles().values());
//		getCommits().clear();
//		getCommits().addAll(c.separateCommits());
//	}
//}

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
package org.polarion.svnimporter.harprovider.internal.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Map.Entry;

import org.polarion.svnimporter.common.model.CommitsCollection;
import org.polarion.svnimporter.common.model.Model;

/**
 * 
 * 
 * @author <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class HarModel extends Model
{

 private HashMap allRevisions;

 public HashMap getAllRevisions()
 {
  return allRevisions;
 }

 /**
  * Resolve revision states, separate commits
  */
 public void finishModel()
 {
  Collection files = getFiles().values();
  ArrayList filePaths;
  allRevisions = new HashMap();
  for (Iterator i = files.iterator(); i.hasNext();)
  {
   HarFile file = (HarFile) i.next();
   Iterator iterator = file.getRevisions().entrySet().iterator();
   while (iterator.hasNext())
   {
    Entry entry = (Entry) iterator.next();
    filePaths = (ArrayList) allRevisions.get(entry.getKey().toString());
    if (filePaths == null)
    {
     filePaths = new ArrayList();
    }
    filePaths.add(file);
    allRevisions.put(entry.getKey().toString(), filePaths);
   }

   Collection branches = file.getBranches().values();
   for (Iterator j = branches.iterator(); j.hasNext();)
   {
    HarBranch branch = (HarBranch) j.next();
    branch.resolveRevisionStates();
   }
  }
  separateCommits();
 }

 /**
  * Separate commits
  */
 /*
  * private void separateCommits() { CommitsCollection c = new
  * CommitsCollection(PvcsCommit.class); c.addFiles(getFiles().values());
  * getCommits().clear(); getCommits().addAll(c.separateCommits()); }
  */

 /**
  * Compares two PVCS revision numbers
  * 
  * @param rev1
  *         revision number
  * @param rev2
  *         revision number
  * @returns -1,0,1 depending if first revision is less than, equal or greater
  *          than second revision
  */
 private int compareRevNumbers(String rev1, String rev2)
 {
  StringTokenizer rev1Tokenizer = new StringTokenizer(rev1, ".");
  StringTokenizer rev2Tokenizer = new StringTokenizer(rev2, ".");

  Vector rev1Tokens = new Vector();
  Vector rev2Tokens = new Vector();

  while (rev1Tokenizer.hasMoreTokens())
  {
   rev1Tokens.add(rev1Tokenizer.nextToken());
  }

  while (rev2Tokenizer.hasMoreTokens())
  {
   rev2Tokens.add(rev2Tokenizer.nextToken());
  }

  int index = 0;
  while (index < rev1Tokens.size() || index < rev2Tokens.size())
  {
   if (rev1Tokens.size() <= index)
   {
    return -1;
   } else if (rev2Tokens.size() <= index)
   {
    return 1;
   }
   int revInt1 = Integer.parseInt((String) rev1Tokens.elementAt(index));
   int revInt2 = Integer.parseInt((String) rev2Tokens.elementAt(index));

   if (revInt1 == revInt2)
   {
    index++;
    continue;
   } else if (revInt1 > revInt2)
   {
    return 1;
   } else
   {
    return -1;
   }
  }
  return 0;
 }

 /**
  * Generates a collection of commits. This was re-written to handle situations
  * where newer revisions are set to be committed before older revisions because
  * of messed up dates in PVCS.
  */
 private void separateCommits()
 {
  CommitsCollection c = new CommitsCollection(HarCommit.class);
  c.addFiles(getFiles().values());
  getCommits().clear();
  Collection separatedCommits = c.separateCommits();

  boolean problemFound = false;

  do
  {
   problemFound = false;

   HashMap lastRevMap = new HashMap();

   Iterator commitsIter = separatedCommits.iterator();
   while (commitsIter.hasNext())
   {
    HarCommit nextCommit = (HarCommit) commitsIter.next();
    Collection revisions = nextCommit.getRevisions();
    Iterator revisionIter = revisions.iterator();
    while (revisionIter.hasNext())
    {
     HarRevision revision = (HarRevision) revisionIter.next();
     String path = revision.getModelFile().getPath();

     HarRevision lastRevision = (HarRevision) lastRevMap.get(path);
     if (lastRevision == null)
     {
      lastRevMap.put(path, revision);
      continue;
     }

     // if we find a lower number revision coming after a higher
     // number revision, then adjust the date of the higher number
     // revision to be one minute after the lower number revision. Set
     // the problemFound flag so that the commits are re-sorted.
     if (compareRevNumbers(revision.getNumber(), lastRevision.getNumber()) < 0)
     {
      System.out.println("Revision problem found: " + revision.getPath());
      System.out.println("Last revision: " + lastRevision.getNumber());
      System.out.println("Last date: " + lastRevision.getDate());
      System.out.println("This revision: " + revision.getNumber());
      System.out.println("This date: " + revision.getDate());
      System.out.println("Adjusting date on higher number revision");
      System.out.println("----");
      java.util.Date thisRevDate = revision.getDate();
      java.util.Date minuteAfterThisRev = new java.util.Date(thisRevDate
        .getTime() + 60 * 1000);
      lastRevision.setDate(minuteAfterThisRev);
      problemFound = true;
      break;
     }

     lastRevMap.put(path, revision);
    }
    if (problemFound)
     break;
   }

   // If problems were found in the ordering of the revision, then re-sort the
   // commits
   if (problemFound)
   {
    c = new CommitsCollection(HarCommit.class);
    c.addFiles(getFiles().values());
    getCommits().clear();
    separatedCommits = c.separateCommits();
   }
  } while (problemFound);

  // At this point, the dates and rev numbers should be properly sorted
  getCommits().addAll(separatedCommits);
 }
}

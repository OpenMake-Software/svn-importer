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
package org.polarion.svnimporter.harprovider.internal;

import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.common.model.Revision;
import org.polarion.svnimporter.harprovider.HarException;
import org.polarion.svnimporter.harprovider.HarProvider;
import org.polarion.svnimporter.harprovider.internal.model.HarBranch;
import org.polarion.svnimporter.harprovider.internal.model.HarCommit;
import org.polarion.svnimporter.harprovider.internal.model.HarModel;
import org.polarion.svnimporter.harprovider.internal.model.HarRevision;
import org.polarion.svnimporter.harprovider.internal.model.HarRevisionState;
import org.polarion.svnimporter.harprovider.internal.model.HarTag;
import org.polarion.svnimporter.svnprovider.SvnModel;
import org.polarion.svnimporter.svnprovider.internal.SvnProperties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class HarTransform
{
 private static final Log LOG = Log.getLog(HarTransform.class);

 private static final String HAR_REVISION_NUMBER = "HarRevisionNumber";

 private HarProvider provider;

 public HarTransform(HarProvider provider) {
  this.provider = provider;
 }

 /**
  * Transform pvcs model to svn model
  * 
  * @param srcModel
  * @return
  */
 public SvnModel transform(HarModel srcModel)
 {
  if (srcModel.getCommits().size() < 1)
  {
   return new SvnModel();
  }
  SvnModel svnModel = new SvnModel();
  svnModel
    .setSvnimporterUsername(provider.getConfig().getSvnimporterUsername());
  HarCommit firstCommit = (HarCommit) srcModel.getCommits().get(0);
  svnModel.createFirstRevision(firstCommit.getDate());
  svnModel.createTrunkPath(provider.getConfig().getTrunkPath());

  if (!isOnlyTrunk())
  {
   svnModel.createBranchesPath(provider.getConfig().getBranchesPath());
   svnModel.createTagsPath(provider.getConfig().getTagsPath());
  }

  for (Iterator i = srcModel.getCommits().iterator(); i.hasNext();)
   transformCommit((HarCommit) i.next(), svnModel);
  return svnModel;
 }

 private boolean isOnlyTrunk()
 {
  return provider.getConfig().isOnlyTrunk();
 }

 /**
  * Transform pvcs commit to svn revision
  * 
  * @param commit
  * @param svnModel
  */
 private void transformCommit(HarCommit commit, SvnModel svnModel)
 {
  svnModel.createNewRevision(commit.getAuthor(), commit.getDate(),
    commit.getMessage());
  svnModel.getCurRevision().getProperties()
    .set("PVCSRevisionNumbers", commit.joinRevisionNumbers());
  Map childBranches = new HashMap(); // PvcsBranch -> List of PvcsRevisions
  Map childTags = new HashMap(); // PvcsTag -> List of PvcsRevisions

  for (Iterator i = commit.getRevisions().iterator(); i.hasNext();)
  {
   HarRevision revision = (HarRevision) i.next();
   transformRevision(revision, svnModel);
   if (!isOnlyTrunk())
   {
    // --- record branches ----
    for (Iterator b = revision.getChildBranches().iterator(); b.hasNext();)
    {
     HarBranch childBranch = (HarBranch) b.next();
     if (!childBranches.containsKey((childBranch.getName())))
      childBranches.put(childBranch.getName(), new ArrayList());
     ((Collection) childBranches.get(childBranch.getName())).add(revision);
    }
    // --- record tags ---
    for (Iterator t = revision.getTags().iterator(); t.hasNext();)
    {
     HarTag childTag = (HarTag) t.next();
     // create tag if necessary
     if (!childTags.containsKey(childTag.getName()))
      childTags.put(childTag.getName(), new ArrayList());
     // add file revision to tag
     ((Collection) childTags.get(childTag.getName())).add(revision);
    }
   }
  }
  if (!isOnlyTrunk())
  {

   // create child branches
   for (Iterator i = childBranches.keySet().iterator(); i.hasNext();)
   {
    String childBranchName = (String) i.next();
    if (!svnModel.isBranchCreated(childBranchName))
    {
     svnModel.createBranch(childBranchName, commit.getDate());
    }
   }

   // create child tags
   // If we have child tags, we introduce a new SVN Revision. We must do this
   // since Subversion is able to copy files only if the source revision is
   // smaller
   // than the actual revision.
   if (!childTags.isEmpty())
   {
    if (provider.getConfig().useFileCopy())
    {
     // Remember old SVN revision number.
     int oldRevno = svnModel.getCurRevisionNumber();
     svnModel.createNewRevision(commit.getAuthor(), commit.getDate(),
       "svnimporter: adding tags to revision " + oldRevno);
     for (Iterator i = childTags.keySet().iterator(); i.hasNext();)
     {
      String tagName = (String) i.next();
      // create tag, if necessary
      if (!svnModel.isTagCreated(tagName))
       svnModel.createTag(tagName, commit.getDate());
      // copy files into tag folder
      for (Iterator j = ((Collection) childTags.get(tagName)).iterator(); j
        .hasNext();)
      {
       HarRevision revision = (HarRevision) j.next();
       svnModel.addFileCopyToTag(revision.getPath(), tagName, revision
         .getBranch().getName(), revision.getPath(), oldRevno);
      }
     }
    } // use file copy
    else
    {
     // create child tags
     for (Iterator i = childTags.keySet().iterator(); i.hasNext();)
     {
      String tagName = (String) i.next();
      if (!svnModel.isTagCreated(tagName))
       svnModel.createTag(tagName, commit.getDate());

      for (Iterator j = ((Collection) childTags.get(tagName)).iterator(); j
        .hasNext();)
      {
       HarRevision revision = (HarRevision) j.next();
       SvnProperties properties = new SvnProperties();
       properties.set(HAR_REVISION_NUMBER, revision.getNumber());
       svnModel.addFileToTag(revision.getPath(), tagName,
         provider.createContentRetriever(revision), properties);
      }
     }

    } // don't use file copy
   } // !childTags.isEmpty()
  } // !isOnlyTrunk()
 }

 /**
  * Transform pvcs revision to svn svn action
  * 
  * @param revision
  * @param model
  */
 private void transformRevision(HarRevision revision, SvnModel model)
 {
  String path = revision.getPath();
  String branchName = revision.getBranch().getName();
  if (revision.getBranch().isTrunk())
   branchName = null;

  if (isOnlyTrunk() && !revision.getBranch().isTrunk())
   return;

  if (revision.getState() == HarRevisionState.ADD)
  {

   if (!revision.getBranch().isTrunk() && revision.isFirstRevision()
     && provider.getConfig().useFileCopy())
   {

    // first revision on its branch. We make a changed copy of the sprout
    // revision
    Revision sproutRevision = revision.getBranch().getSproutRevision();
    model.addFileCopyToBranch(path, branchName, sproutRevision.getBranch()
      .getName(), sproutRevision.getPath(), sproutRevision
      .getSvnRevisionNumber(), provider.createContentRetriever(revision));

   }
   else
   {
    SvnProperties props = new SvnProperties();
    props.set(HAR_REVISION_NUMBER, revision.getNumber());
    Map attrs = revision.getModelFile().getProperties();
    // Migrate the properties.
    if (attrs.containsKey("EXPANDKEYWORDS"))
    {
     props.set("svn:keywords", "URL Author Revision Date Id");
    }
    if (attrs.containsKey("NEWLINE"))
    {
     String pvcsval = (String) attrs.get("NEWLINE");
     String svnval = null;
     if (pvcsval.equals("\\r\\n"))
      svnval = "CRLF";
     else if (pvcsval.equals("\\n"))
      svnval = "LF";
     else if (pvcsval.equals("\\r"))
      svnval = "CR";
     if (svnval != null)
      props.set("svn:eol-style", svnval);
    }
    String descKey = provider.getConfig().getFileDescriptionPropKey();
    if (descKey != null && attrs.containsKey("description"))
    {
     // we transform the file description to UTF-8
     props.set(descKey, HarUtil.toUtf8((String) attrs.get("description")));
    }

    model.addFile(path, branchName, provider.createContentRetriever(revision),
      props);
   }
  }
  else if (revision.getState() == HarRevisionState.CHANGE)
  {
   SvnProperties properties = new SvnProperties();
   properties.set(HAR_REVISION_NUMBER, revision.getNumber());
   model.changeFile(path, branchName,
     provider.createContentRetriever(revision), properties);
  }
  else if (revision.getState() == HarRevisionState.DEL)
  {
   SvnProperties properties = new SvnProperties();
   properties.set(HAR_REVISION_NUMBER, revision.getNumber());
   model.deleteFile(path, branchName, properties);
  }
  else
  {
   LOG.error(revision.getDebugInfo());
   LOG.error(revision.getBranch().getDebugInfo());
   throw new HarException("unknown Harvest revision state: " + revision.getState());
  }
  revision.setSvnRevisionNumber(model.getCurRevisionNumber());
 }
}

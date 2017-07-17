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

import java.util.HashMap;
import java.util.Map;

import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.common.model.ModelFile;

/**
 * 
 * 
 * @author <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class HarFile extends ModelFile
{
 private String vPath;

 private String cPath;
 
 private String name;

 /**
  * Map property key to value
  */
 private Map views = new HashMap();

 public HarFile(String path) {
  super(path);
 }

 public void addView(String key, String value)
 {
  if (views.containsKey(key))
  {
   Log.getLog(super.getClass()).error("duplicated property " + key);
  } else
  {
   views.put(key, value);
  }
 }

 /**
  * Get branch by number
  * 
  * @param branchNumber
  * @return
  */
 public HarBranch getBranch(String branchNumber)
 {
  return (HarBranch) getBranches().get(branchNumber);
 }

 /**
  * Get revision by revision number
  * 
  * @param revisionNumber
  * @return
  */
 public HarRevision getRevision(String revisionNumber)
 {
  return (HarRevision) getRevisions().get(revisionNumber);
 }

 public String getVPath()
 {
  return vPath;
 }

 public void setVPath(String vPath)
 {
  this.vPath = vPath;
 }

 public String getCPath()
 {
  return cPath;
 }

 public void setCPath(String cPath)
 {
  this.cPath = cPath;
 }

 public String getName()
 {
  return name;
 }

 public void setName(String name)
 {
  this.name = name;
 }
}

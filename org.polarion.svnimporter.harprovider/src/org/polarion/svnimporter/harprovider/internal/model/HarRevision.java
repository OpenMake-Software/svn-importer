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

import org.polarion.svnimporter.common.model.Revision;
import org.polarion.svnimporter.common.Log;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

/**
 * @author <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class HarRevision extends Revision
{
 private static final Log LOG = Log.getLog(HarRevision.class);

 private Map tags = new HashMap(); // tag name -> tag
 
 private String verType;

 public String getVerType()
 {
  return verType;
 }

 public void setVerType(String verType)
 {
  this.verType = verType;
 }

 /**
  * Pvcs revision state
  */
 private HarRevisionState state;

 /**
  * Constructor
  * 
  * @param number
  *         - revision number
  */
 public HarRevision(String number) {
  super(number);
 }

 public HarRevisionState getState()
 {
  return state;
 }

 public void setState(HarRevisionState state)
 {
  this.state = state;
 }

 public Collection getTags()
 {
  return tags.values();
 }

 /**
  * Add tag
  * 
  * @param tag
  * @return
  */
 public boolean addTag(HarTag tag)
 {
  if (tags.containsKey(tag.getName()))
  {
   LOG.error(getPath() + ": duplicate tag " + tag.getName());
   return false;
  }
  tags.put(tag.getName(), tag);
  return true;
 }

 public String getDebugInfo()
 {
  String stateName = state == null ? null : state.getName();
  return super.getDebugInfo() + " s[" + stateName + "]";
 }
}

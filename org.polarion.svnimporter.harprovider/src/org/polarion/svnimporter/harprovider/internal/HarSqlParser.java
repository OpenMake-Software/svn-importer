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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.harprovider.HarException;
import org.polarion.svnimporter.harprovider.internal.model.HarBranch;
import org.polarion.svnimporter.harprovider.internal.model.HarFile;
import org.polarion.svnimporter.harprovider.internal.model.HarRevision;
import org.polarion.svnimporter.harprovider.internal.model.HarTag;

/**
 * @author <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class HarSqlParser
{
 private static final Log LOG = Log.getLog(HarSqlParser.class);

 private HarConfig config;

 /**
  * Repository filename -> PvcsFile
  */
 private ArrayList files;

 public HarSqlParser(HarConfig config) {
  this.config = config;
 }

 private HarFile curFile;

 private HarRevision curRevision;

 private String fileName;

 private String description;

 private HarBranch trunk;

 private boolean isSnapshot = false;

 private boolean revExists = false;

 // (Each tag label is assigned to exactly one revision number, but one
 // revision number
 // can have more than one tag.)

 // private BufferedReader filesReader; // reader for files.tmp containing the

 // names of the verioned files

 // (as opposed to the physical archive locations on disk).

 private BufferedReader hsqlReader; // The Vlog file reader

 /**
  * Parse log file
  * 
  * @param hsqlFile
  *         the file generated by the vlog command
  * @param filesFile
  *         the file generated by the listrevisions command. We rely on the fact
  *         that there is a one-to-one correspondence between the lines of
  *         filesFile and the sections in the vlog file that describe an
  *         archive. (with some robustness - see below)
  */
 public void parse(File hsqlFile)
 {
  files = new ArrayList();
  curFile = null;
  curRevision = null;
  fileName = null;

  try
  {
   InputStreamReader encReader = new InputStreamReader(new FileInputStream(
     hsqlFile), config.getLogEncoding());
   hsqlReader = new BufferedReader(encReader);
   /*
    * filesReader = new BufferedReader(new InputStreamReader( new
    * FileInputStream(filesFile), config.getLogEncoding()));
    */
   try
   {
    String line;
    while ((line = hsqlReader.readLine()) != null)
    {
     if (!line.equals(""))
      addLine(line);
    }
   }
   finally
   {
    hsqlReader.close();
    // filesReader.close();
   }
  }
  catch (IOException e)
  {
   throw new HarException(e);
  }
 }

 private void addLine(String line) throws IOException
 {
  // if (getSqlValue(line).equals("build.bat"))
  // System.out.print("here");
  if (line.startsWith("ITEMNAME"))// && parserState == STATE_NORMAL)
  {
   fileName = getSqlValue(line);
  }
  if (line.startsWith("PATHFULLNAME"))// && parserState == WAIT_FOR_PATH)
  {
   String vPath = getSqlValue(line);
   if (config.getHarVer() < 4)
    vPath = "/" + vPath;
   vPath.replaceAll("\\\\", "/");
   // if current file is not null and its vPath or name differs then add it to
   // the files list and construct anew
   if (curFile != null
     && (!curFile.getVPath().equals(vPath) || !curFile.getName().equals(
       fileName)))
   {
    files.add(curFile);
    curFile = null;
   }
   if (curFile == null)
   {
    String cPath = config.getCheckoutTempDir().getPath();
    String path = vPath + "/" + fileName;
    if (config.isStripRepoPath())
     path = path.replaceFirst(config.getRepository(), "");
    path = path.replaceAll("/{2,}", "/");
    if (path.startsWith("/"))
     path = path.replaceFirst("/", "");
    curFile = new HarFile(path);
    curFile.setVPath(vPath);
    curFile.setCPath(cPath);
    curFile.setName(fileName);
    trunk = new HarBranch("trunk");
    trunk.setTrunk(true);
    curFile.addBranch(trunk);
   }
   return;
  }
  // if (parserState == WAIT_FOR_VER)
  // {
  if (line.startsWith("MAPPEDVERSION"))
  {
   String revision = getSqlValue(line);
   description = "HAR_VERSION: " + revision + "\n";
   HarRevision existingRevision = curFile.getRevision(revision);
   if (existingRevision == null)
   {
    curRevision = new HarRevision(getSqlValue(line));
    revExists = false;
    curRevision.setBranch(trunk);
    trunk.addRevision(curRevision);
   }
   else
   {
    curRevision = existingRevision;
    revExists = true;
   }
  }
  else if (!revExists && line.startsWith("USERNAME"))
  {
   curRevision.setAuthor(getSqlValue(line));
  }
  else if (!revExists
    && (config.getHarVer() < 4 && line.startsWith("CREATIONTIME"))
    || line.startsWith("MODIFIEDTIME"))
  {
   Date date = parseDate(getSqlValue(line));
   if (date != null)
    curRevision.setDate(date);
  }
  else if (line.startsWith("VIEWTYPE") && getSqlValue(line).equals("Snapshot"))
  {
   isSnapshot = true;
  }
  else if (isSnapshot && line.startsWith("VIEWNAME"))
  {
   curRevision.addTag(new HarTag(getSqlValue(line)));
   isSnapshot = false;
  }
  else if (!revExists && line.startsWith("PACKAGENAME"))
  {
   description += "HAR_PACKAGE: " + getSqlValue(line) + "\n";
  }
  else if (!revExists && line.startsWith("DESCRIPTION"))
  {
   description += "HAR_DESCRIPTION: " + getSqlValue(line) + "\n";
   curRevision.setMessage(description);
   description = "";
  }
  else if (!revExists && line.startsWith("VERSIONSTATUS"))
  {
   curRevision.setVerType(getSqlValue(line));
   if (curFile.getRevision(curRevision.getNumber()) == null)
    curFile.addRevision(curRevision);
  }
 }

 private String getSqlValue(String line)
 {
  String sqlValue = line;
  sqlValue = sqlValue.substring(line.indexOf(" = ") + 3);
  return sqlValue.trim();
 }

 private Date parseDate(String sdate)
 {
  DateFormat df = config.getLogDateFormat();
  try
  {
   return df.parse(sdate);
  }
  catch (ParseException e)
  {
   LOG.error("wrong date: " + sdate + "(" + "sample format: "
     + df.format(new Date()) + ")");
   return null;
  }
 }

 public ArrayList getFiles()
 {
  return files;
 }
}

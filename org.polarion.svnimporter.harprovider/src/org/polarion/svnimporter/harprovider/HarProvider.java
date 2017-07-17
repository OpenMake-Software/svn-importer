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
package org.polarion.svnimporter.harprovider;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.polarion.svnimporter.common.IContentRetriever;
import org.polarion.svnimporter.common.IProvider;
import org.polarion.svnimporter.common.ISvnModel;
import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.common.ProviderConfig;
import org.polarion.svnimporter.common.Util;
import org.polarion.svnimporter.common.ZeroContentRetriever;
import org.polarion.svnimporter.harprovider.internal.HarConfig;
import org.polarion.svnimporter.harprovider.internal.HarContentRetriever;
import org.polarion.svnimporter.harprovider.internal.HarExec;
import org.polarion.svnimporter.harprovider.internal.HarSqlParser;
import org.polarion.svnimporter.harprovider.internal.HarTransform;
import org.polarion.svnimporter.harprovider.internal.model.HarFile;
import org.polarion.svnimporter.harprovider.internal.model.HarModel;
import org.polarion.svnimporter.harprovider.internal.model.HarRevision;
import org.polarion.svnimporter.svnprovider.SvnModel;

/**
 * @author <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class HarProvider implements IProvider
{
 private static final Log LOG = Log.getLog(HarProvider.class);

 /**
  * Provider's config
  */
 private HarConfig config;

 /**
  * 
  */
 private File m_InstructionsFile; // File for saving "harvest"

 // instructions

 private File m_InstructionsFileChecksum; // File for saving "harvest"

 // instructions checksum

 private Map m_File2rev2path; // maps file names and revisions to checked out
                              // files

 // number -> localFileChecksum)

 private boolean testMode = false;

 public void setTestMode(boolean testMode)
 {
  this.testMode = testMode;
 }

 /**
  * Configure provider
  * 
  * @param properties
  */
 public void configure(Properties properties)
 {
  config = new HarConfig(properties);
  m_InstructionsFile = new File(config.getTempDir(), "instr.tmp");
  m_InstructionsFileChecksum = new File(config.getTempDir(),
    "instrChecksum.tmp");
 }

 /**
  * Validate configuration
  * 
  * @return
  */
 public boolean validateConfig()
 {
  return config.validate();
 }

 /**
  * Get provider's configuration
  * 
  * @return
  */
 public ProviderConfig getConfig()
 {
  return config;
 }

 /**
  * Log environment information
  */
 public void logEnvironmentInformation()
 {
  config.logEnvironmentInformation();
 }

 /**
  * Build Harvest model
  * 
  * @return
  */
 private HarModel buildHarModel()
 {
  File hsqlFile = getHsqlFile();

  if (!config.useHsqlFile() || !hsqlFile.exists())
   getVersionInformation(hsqlFile);
  HarSqlParser parser = new HarSqlParser(config);
  parser.parse(hsqlFile);

  HarModel harModel = new HarModel();
  ArrayList files = parser.getFiles();
  for (Iterator i = files.iterator(); i.hasNext();)
   harModel.addFile((HarFile) i.next());

  harModel.finishModel();

  LOG.info("Harvest model has been created.");
  harModel.printSummary();

  return harModel;
 }

 protected File getHsqlFile()
 {
  return new File(config.getTempDir(), "hsql.tmp");
 }

 public void listFiles(PrintStream out)
 {
  Collection files = buildHarModel().getFiles().keySet();
  for (Iterator i = files.iterator(); i.hasNext();)
   out.println(i.next());
 }

 /**
  * Transform harvest model to svn model
  * 
  * @return
  */
 public ISvnModel buildSvnModel()
 {
  HarModel harModel = buildHarModel();
  HarTransform transform = new HarTransform(this);
  SvnModel svnModel = transform.transform(harModel);

  LOG.info("Svn model has been created");
  LOG.info("total number of revisions in svn model: "
    + svnModel.getRevisions().size());

  // XXX Hack to fix tests
  if (!testMode)
  {
   m_File2rev2path = getAllContents(harModel, config.getCheckoutTempDir());

  }

  return svnModel;
 }

 /**
  * Save version information (generated by pcli command) to targetVlogFile and
  * targetFilesFile. The targetFilesFile will contain the list of file names
  * belonging to the subproject to import. The file names are as defined by the
  * Harvest project structure. The VlogFile, on the other hand, uses the
  * physical archive locations. The nth line in the filesFile corresponds to the
  * nth archive described in the VlogFile. What we want to import is the
  * directory structure as defined by the Harvest project; the physical archive
  * locations are implementation details that will not be migrated.
  * 
  * @param targetHsqlFile
  */
 protected void getVersionInformation(File targetHsqlFile)
 {

  String hsqlFile = "get_versions.sql";
  try
  {
   // Create hsql query file
   FileWriter fstream = new FileWriter(config.getTempDir() + "/" + hsqlFile);
   BufferedWriter out = new BufferedWriter(fstream);
   if (config.getHarVer() >= 5)
   {
    out
      .write("SELECT DISTINCT hi.ITEMNAME, hpf.PATHFULLNAME, hvs.MAPPEDVERSION, hu.USERNAME, hvs.MODIFIEDTIME, hv.VIEWTYPE, hv.VIEWNAME, hp.PACKAGENAME, hvs.DESCRIPTION from HARREPOSITORY hr, HARVIEW hv, HARITEMS hi, HARVERSIONS hvs, HARENVIRONMENT he, HARVERSIONINVIEW hvv, HARPATHFULLNAME hpf, HARUSER hu, HARPACKAGE hp\n");
    out.write("WHERE (he.ENVIRONMENTNAME = '" + config.getProject()
      + "') AND\n");
    out.write("(he.ENVOBJID = hv.ENVOBJID) AND\n");
    out.write("(hvs.ITEMOBJID = hi.ITEMOBJID) AND\n");
    out.write("(hv.VIEWOBJID = hvv.VIEWOBJID) AND\n");
    out.write("(hvv.VERSIONOBJID = hvs.VERSIONOBJID) AND\n");
    out.write("(hvs.MODIFIERID = hu.MODIFIERID) AND\n");
    out.write("(hvs.PACKAGEOBJID = hp.PACKAGEOBJID) AND\n");
    out
      .write("((hv.VIEWNAME = '"
        + config.getState()
        + "') or (hv.VIEWTYPE = 'Baseline') or (hv.VIEWTYPE = 'Snapshot')) AND\n");
    out.write("(hpf.PATHFULLNAME LIKE '" + config.getVPath() + "%') and\n");
    out.write("(hvs.VERSIONSTATUS = 'N') AND\n");
    out.write("(hi.ITEMTYPE = 1) AND\n");
    out.write("(hpf.ITEMOBJID = hi.PARENTOBJID)\n");
    out
      .write("ORDER BY hpf.PATHFULLNAME ASC, hi.ITEMNAME ASC, hvs.MAPPEDVERSION ASC");
    out.close();
   }
   else if (config.getHarVer() < 4)
   {
    String pathMatch = config.getVPath();
    if (pathMatch.startsWith("/"))
     pathMatch = pathMatch.replaceFirst("/", "");
    // harvest 3 query
    out.write("SELECT DISTINCT hi.ITEMNAME,\n");
    out.write("hpf.PATHFULLNAME,\n");
    out.write("hv.MAPPEDVERSION,\n");
    out.write("hu.USERNAME,\n");
    out.write("hv.CREATIONTIME,\n");
    out.write("hrv.VIEWTYPE,\n");
    out.write("hrv.VIEWNAME,\n");
    out.write("hp.PACKAGENAME,\n");
    out.write("hv.DESCRIPTION,\n");
    out.write("hv.VERSIONSTATUS\n");
    out.write("FROM HARITEM hi,\n");
    out.write("HARVERSION hv,\n");
    out.write("HARENVIRONMENT he,\n");
    out.write("HARVIEW hrv,\n");
    out.write("HARPACKAGE hp,\n");
    out.write("HARPATHFULLNAME hpf,\n");
    out.write("HARREPOSITORY hr,\n");
    out.write("HARUSER hu\n");
    out.write("WHERE (hpf.PATHFULLNAME LIKE '" + pathMatch + "%') AND\n");
    out.write("(hi.PATHOBJID = hpf.PATHOBJID) AND\n");
    out.write("(hv.ITEMOBJID = hi.ITEMOBJID) AND\n");
    if (config.isUseOnlyLastRevisionContent())
     out.write("(hv.VERSIONOBJID = hi.LATESTVERSIONID) AND\n");
    out.write("((hv.VERSIONSTATUS = 'N') OR (hv.VERSIONSTATUS = 'D')) AND\n");
    out
      .write("(((he.ENVIRONMENTNAME = '"
        + config.getProject()
        + "') AND (hrv.VIEWNAME = '"
        + config.getState()
        + "')) OR ((hv.PARENTVERSIONID = 0) AND (he.ENVOBJID = 0) AND (hrv.VIEWOBJID = 0))) AND\n");
    out.write("(hrv.ENVOBJID = he.ENVOBJID) AND\n");
    out.write("(hv.ENVOBJID = he.ENVOBJID) AND\n");
    out.write("(hr.REPOSITNAME = '" + config.getRepository() + "') AND\n");
    out.write("(hi.REPOSITOBJID = hr.REPOSITOBJID) AND\n");
    out.write("(hu.USROBJID = hv.CREATORID) AND\n");
    out.write("(hp.PACKAGEOBJID = hv.PACKAGEOBJID)\n");
    out.close();
   }
  }
  catch (Exception e)
  {// Catch exception if any
   System.err.println("Error: " + e.getMessage());
   LOG.error("Error: " + e.getMessage());
  }

  executeCommand(new String[] { config.getHsqlExe(), "-b", config.getBroker(),
    "-usr", config.getUserName(), "-pw", config.getPassword(), "-f", hsqlFile,
    "-o", targetHsqlFile.getName() });
 }

 /**
  * Create content retriever for revision
  * 
  * @param revision
  * @return
  */
 public IContentRetriever createContentRetriever(HarRevision revision)
 {
  if (config.isUseOnlyLastRevisionContent() && !revision.isLastRevision())
  {
   return ZeroContentRetriever.INSTANCE;
  }
  else
  {
   return new HarContentRetriever(this, revision);
  }
 }

 private String getLogonString(boolean useQuotes)
 {
  String userId = config.getUserName();
  if (userId == null)
   return "";
  if (config.getPassword() != null)
  {
   userId += ":" + config.getPassword();
  }
  String quotes = (useQuotes ? "\"" : "");
  return ("-id" + quotes + userId + quotes);
 }

 /**
  * Retrieve all content of all revisions from model in temp dir
  * 
  * @param model
  */
 private Map getAllContents(HarModel model, File tempDir)
 {
  LOG.debug("get all contents for " + tempDir.getAbsolutePath());
  BufferedReader hcoReader;
  m_File2rev2path = new HashMap();
  // try
  // {
  // PrintWriter out = new PrintWriter(new FileOutputStream(instructionsFile));
  try
  {
   File hcoLog = new File(tempDir + "/hco.log");
   for (Iterator i = model.getAllRevisions().keySet().iterator(); i.hasNext();)
   {
    String revNum = (String) i.next();
    if (hcoLog.exists())
     hcoLog.delete();
    executeCommand(
      new String[] { config.getExecutable(), "-en", config.getProject(), "-st",
        config.getState(), "-usr", config.getUserName(), "-pw",
        config.getPassword(), "-br", "-op", "pc", "-r", "-vn", revNum, "-vp",
        config.getVPath(), "-o", "\"" + hcoLog.getPath() + "\"", "-s", "\"*\"" },
      tempDir);
    InputStreamReader encReader = new InputStreamReader(new FileInputStream(
      hcoLog), config.getLogEncoding());
    hcoReader = new BufferedReader(encReader);
    try
    {
     String line;
     Map rev2File;
     while ((line = hcoReader.readLine()) != null)
     {
      if (line.contains("checked out to"))
      {
       File coFile = getCheckoutFile(line);
       File renamedFile = getRenamedFile(revNum, coFile);
       String relPath = coFile.getPath().replaceAll("\\\\", "/")
         .replaceFirst("(?i)" + tempDir.getPath().replaceAll("\\\\", "/"), "");
       String pathKey = config.getVPath() + "/" + relPath;
       if (config.isStripRepoPath())
        pathKey = pathKey.replaceFirst(config.getRepository(), "");
       pathKey = pathKey.replaceAll("/{2,}", "/");
       if (pathKey.startsWith("/"))
        pathKey = pathKey.replaceFirst("/", "");
       rev2File = (Map) m_File2rev2path.get(pathKey);
       if (rev2File == null)
        rev2File = new HashMap();
       rev2File.put(revNum, renamedFile);
       m_File2rev2path.put(pathKey, rev2File);
      }
     }
    }
    catch (IOException e)
    {
     throw new HarException(e);
    }
    finally
    {
     hcoReader.close();
     // filesReader.close();
    }

   }
  }
  catch (IOException e)
  {
   throw new HarException(e);
  }

  return m_File2rev2path;
 }

 // I0301 Item /SybaseProduction/procs/CalcUnitBalances;36 checked out to
 // C:\temp\svnimporter\harvest\har.tempdir\procs\CalcUnitBalances
 private File getCheckoutFile(String checkoutLine)
 {
  String checkOutMatch = checkoutLine.substring(checkoutLine
    .indexOf("checked out to") + 15);
  return new File(checkOutMatch.trim());
 }

 /**
  * Checkout file revision into temp dir
  * 
  * @param revision
  * @return
  */
 public File getCheckedOut(HarRevision revision) throws IOException
 {
  File alreadyReceivedFile = getFileFromMap(revision, m_File2rev2path,
    "HarProvider.getCheckedOut:");

  if (alreadyReceivedFile == null)
  {
   return null;
  }

  LOG.info("  HarProvider.checkout() => : " + alreadyReceivedFile);

  return alreadyReceivedFile;
 }

 private File getFileFromMap(HarRevision revision, Map map, String logMessage)
 {
  Map rev2path = (Map) map.get(revision.getModelFile().getPath());

  if (rev2path == null)
  {
   LOG.error(logMessage + "rev2path == null => getContent - Problem !");
   return null;
  }

  File mapFile = (File) rev2path.get(revision.getNumber());

  if (mapFile == null)
  {
   LOG
     .error(logMessage + "File not found for revision " + revision.getNumber());
   return null;
  }
  if (mapFile.exists() == false)
  {
   LOG.error(logMessage + "File " + mapFile.getAbsolutePath()
     + " doesn't exist.");
   return null;
  }
  if (mapFile.isFile() == false)
  {
   LOG.error(logMessage + "File " + mapFile.getAbsolutePath()
     + " is not a file.");
   return null;
  }

  return mapFile;
 }

 private String getHarPath(String path, boolean replaceDollarByVariable)
 {
  StringBuffer buf = new StringBuffer();
  for (int i = 0; i < path.length(); ++i)
  {
   char c = path.charAt(i);
   switch (c)
   {
   // escape single quote
   case '\'':
    buf.append("\\'");
    break;
   // escpape double quotes. They cannot be part of a Windows file
   // name, but
   // of a Unix file name.
   case '"':
    buf.append("\\\"");
    break;
   // surround '$' with single quotes or replace it by '$DOLLAR' since
   // $ has a special
   // meaning to the pcli interpreter; if '$DOLLAR' is used the
   // variable must be set before
   case '$':
    if (replaceDollarByVariable == true)
    {
     buf.append("'$DOLLAR'");
    }
    else
    {
     buf.append("'$'");
    }
    break;
   // replace '\' by '/'. This works even on Windows. The advantage is
   // that
   // a single quote after backslash (as path separator) will not be
   // interpreted as
   // an escaped single quote. Thus "c:\path\$1.txt" will be
   // interpreted correctly
   // (transformed into "c:/path/'$'1.txt" and not into
   // "c:\path\'$'.txt").
   case '\\':
    buf.append('/');
    break;
   default:
    buf.append(c);
   }
  }
  return buf.toString();
 }

 /**
  * Rename and return path to local renamed copy
  * 
  * @param harRevision
  * @return
  */
 private File getRenamedFile(String revNum, File localFile)
 {
  File renamedFile = new File(localFile.getAbsolutePath() + "_rev"
    + revNum.replaceAll("[^0-9]", "_"));
  localFile.renameTo(renamedFile);
  return renamedFile;
 }

 /**
  * Exec pcli command
  * 
  * @param cmd
  */
 private void executeCommand(String[] cmd)
 {
  executeCommand(cmd, config.getTempDir());
 }

 private void executeCommand(String[] cmd, File cwd)
 {
  HarExec exec = new HarExec(cmd);
  exec.setWorkdir(cwd);
  exec.setVerboseExec(config.isVerboseExec());
  exec.exec();
  if (exec.getErrorCode() != 0)
  {
   if (exec.getErrorCode() == HarExec.ERROR_EXCEPTION)
    throw new HarException("error during execution command "
      + Util.toString(exec.getCmd(), " ") + ", exception caught",
      exec.getException());
   else if (exec.getErrorCode() == HarExec.ERROR_WRONG_PROJECT_PATH)
    throw new HarException("error during execution command "
      + Util.toString(exec.getCmd(), " ") + ": wrong project path \""
      + config.getVPath() + "\"");
   else
    throw new HarException("error during execution command "
      + Util.toString(exec.getCmd(), " "));
  }
 }

 /**
  * Cleanup
  */
 public void cleanup()
 {
  LOG.debug("cleanup");
  File tempDir = config.getTempDir();
  if (!Util.delete(tempDir))
  {
   LOG.error("can't delete temp dir: " + tempDir.getAbsolutePath());
  }

 }
}

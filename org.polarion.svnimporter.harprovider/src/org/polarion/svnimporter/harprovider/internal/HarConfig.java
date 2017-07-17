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

import org.polarion.svnimporter.common.ConfigUtil;
import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.common.ProviderConfig;

import java.io.File;
import java.text.DateFormat;
import java.util.Properties;

/**
 * 
 * 
 * @author <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class HarConfig extends ProviderConfig
{
 private static final Log LOG = Log.getLog(HarConfig.class);

 /**
  * Path to "pcli.exe"
  */
 private String checkoutExe;

 private String hsqlExe;

 /**
  * Harvest project path
  */
 private String broker;

 private String vPath;

 private String project;

 private String state;
 
 private String repository;

 private float harVer;

 private File tempDir;

 private DateFormat logDateFormat;

 private String logDateFormatString;

 private String logDateLocale;

 private String logDateTimeZone;

 private String logEncoding;

 private boolean verboseExec;

 private String userName;

 private String password;

 private boolean useHsqlFile;
 
 private boolean stripRepoPath;

 private File checkoutTempDir;

 public HarConfig(Properties properties) {
  super(properties);
 }

 protected void configure()
 {
  super.configure();

  harVer = new Float(getStringProperty("har.version", true));
  checkoutExe = getStringProperty("har.checkout.executable", true);
  hsqlExe = getStringProperty("har.sql.executable", true);

  broker = getStringProperty("har.broker", true);
  vPath = getStringProperty("har.vpath", true);
  vPath = vPath.replace("\\", "/");
  project = getStringProperty("har.project", true);
  state = getStringProperty("har.state", true);
  repository = getStringProperty("har.repository", true);
  tempDir = getTempDir("har.tempdir");

  logDateFormatString = getStringProperty("har.log.dateformat", true);
  logDateLocale = getStringProperty("har.log.datelocale", false);
  logDateTimeZone = getStringProperty("har.log.datetimezone", false);
  logDateFormat = getDateFormat(logDateFormatString, logDateLocale,
    logDateTimeZone);

  logEncoding = getStringProperty("har.log.encoding", true);
  verboseExec = getBooleanProperty("har.verbose_exec");
  userName = getStringProperty("har.username", false);
  password = getStringProperty("har.password", false);
  stripRepoPath = getBooleanProperty("har.strip.repository.path");
  useHsqlFile = getBooleanProperty("har.use_hsql_file");
  checkoutTempDir = getTempDir("har.checkouttempdir");
 }

 public String getRepository()
 {
  return repository;
 }

 public void setRepository(String repository)
 {
  this.repository = repository;
 }
 
 public float getHarVer()
 {
  return harVer;
 }

 public void setHarVer(float harVer)
 {
  this.harVer = harVer;
 }
 
 public String getExecutable()
 {
  return checkoutExe;
 }

 public boolean isStripRepoPath()
 {
  return stripRepoPath;
 }

 public void setStripRepoPath(boolean stripRepoPath)
 {
  this.stripRepoPath = stripRepoPath;
 }
 
 public boolean isVerboseExec()
 {
  return verboseExec;
 }

 public String getVPath()
 {
  return vPath;
 }

 /**
  * Get Harvest provider's temp dir
  * 
  * @return
  */
 public File getTempDir()
 {
  return tempDir;
 }

 public File getCheckoutTempDir()
 {
  return checkoutTempDir;
 }

 public DateFormat getLogDateFormat()
 {
  return logDateFormat;
 }

 public String getLogEncoding()
 {
  return logEncoding;
 }

 public String getUserName()
 {
  return userName;
 }

 public String getPassword()
 {
  return password;
 }

 public boolean useHsqlFile()
 {
  return useHsqlFile;
 }

 protected void printError(String error)
 {
  LOG.error(error);
 }

 /**
  * Log environment information
  */
 public void logEnvironmentInformation()
 {
  LOG.info("*** Harvest provider configuration ***");
  LOG.info("version = \"" + harVer + "\"");
  LOG.info("broker = \"" + broker + "\"");
  LOG.info("hsql executable = \"" + hsqlExe + "\"");
  LOG.info("checkout executable = \"" + checkoutExe + "\"");
  LOG.info("vPath = \"" + vPath + "\"");
  LOG.info("project = \"" + project + "\"");
  LOG.info("state = \"" + state + "\"");
  LOG.info("temp dir = \"" + tempDir.getAbsolutePath() + "\"");
  LOG.info("log date format = \"" + logDateFormatString + "\"");
  LOG.info("log date locale = \"" + logDateLocale + "\"");
  LOG.info("log date time zone = \"" + logDateTimeZone + "\"");
  LOG.info("log encoding = \"" + logEncoding + "\"");
  LOG.info("verbose exec = \"" + verboseExec + "\"");
  LOG.info("user name = \"" + userName + "\"");
  LOG.info("password = \"" + password + "\"");
  LOG.info("strip repository path = \"" + stripRepoPath + "\"");
  LOG.info("keep hsql file = \"" + useHsqlFile + "\"");
  LOG.info("checkouttempdir = \"" + checkoutTempDir + "\"");
  super.logEnvironmentInformation();
 }

 public String getProject()
 {
  return project;
 }

 public String getState()
 {
  return state;
 }

 public String getHsqlExe()
 {
  return hsqlExe;
 }

 public String getBroker()
 {
  return broker;
 }
}

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
package org.polarion.svnimporter.vssprovider.internal;

import java.io.File;
import java.text.DateFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;

import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.common.ProviderConfig;

/**
 *
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class VssConfig extends ProviderConfig {
	private static final Log LOG = Log.getLog(VssConfig.class);

	/**
	 * Path to "ss.exe"
	 */
	private String executable;
	private String path;
	private String project;
	private File tempDir;

	private DateFormat logDateFormat;
	private String logDateFormatString;
	private String logDateLocale;

	private String logEncoding;
	private String username;
	private String password;
	private boolean verboseExec;
	public boolean useComApi;
	private Collection projectsToIgnore;

	public VssConfig(Properties properties) {
		super(properties);
	}

	protected void configure() {
		super.configure();
		
		executable = getStringProperty("vss.executable", true);
		path = getStringProperty("vss.path", true);
		project = getStringProperty("vss.project", true);
		username = getStringProperty("vss.username", true);
		password = getStringProperty("vss.password", true);
		tempDir = getTempDir("vss.tempdir");

		logDateFormatString = getStringProperty("vss.log.dateformat", true);
		logDateLocale = getStringProperty("vss.log.datelocale", false);
		logDateFormat = getDateFormat(logDateFormatString, logDateLocale);

		logEncoding = getStringProperty("vss.log.encoding", true);
		verboseExec = getBooleanProperty("vss.verbose_exec");
		
		useComApi = getBooleanProperty( "vss.use.com.api" );
		
		// Create a list of (sub)projects to ignore
		projectsToIgnore = new HashSet();
		String projectsIgnoreList = getStringProperty( "vss.project.ignore.list", false );
		if(projectsIgnoreList!=null)
		{
			String tmpStr[] = projectsIgnoreList.split( ";" );
			for ( int i = 0 ; i < tmpStr.length ; i++ )
			{
				projectsToIgnore.add(tmpStr[i]);
			}
		}
	}


	public String getExecutable() {
		return executable;
	}

	public String getPath() {
		return path;
	}

	public File getTempDir() {
		return tempDir;
	}

	public DateFormat getLogDateFormat() {
		return logDateFormat;
	}

	public String getLogEncoding() {
		return logEncoding;
	}

	public String getProject() {
		return project;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public boolean isVerboseExec() {
		return verboseExec;
	}

	protected void printError(String error) {
		LOG.error(error);
	}

    /**
	 * Log environment information
	 */
	public void logEnvironmentInformation() {
		LOG.info("*** Vss provider configuration ***");
		LOG.info("executable = \"" + executable + "\"");
		LOG.info("path = \"" + path + "\"");
		LOG.info("project = \"" + project + "\"");
		LOG.info("temp dir = \"" + tempDir.getAbsolutePath() + "\"");
		LOG.info("log date format = \"" + logDateFormatString + "\"");
		LOG.info("log date locale = \"" + logDateLocale + "\"");
		LOG.info("log encoding = \"" + logEncoding + "\"");
		LOG.info("username = \"" + username + "\"");
		LOG.info("password = \"" + "*******" + "\"");
		LOG.info("verbose exec = \"" + verboseExec + "\"");
		super.logEnvironmentInformation();
	}

	public Collection getProjectsToIgnore()
	{
		return projectsToIgnore;
	}
}


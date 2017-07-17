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

package org.polarion.svnimporter.ccprovider.internal;

import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.common.ProviderConfig;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 *
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class CCConfig extends ProviderConfig {
	private static final Log LOG = Log.getLog(CCConfig.class);

	/**
	 * Path to "cleartool.exe"
	 */
	private String executable;
	/**
	 * CC project path
	 */
	private File projectPath;

	/**
	 * Temporary directory
	 */
	private File tempDir;
	/**
	 * Log date format
	 */
	private String logDateFormatString;
	private DateFormat logDateFormat;

	private String logEncoding;

	private boolean verboseExec;

	/**
	 * Configuration errors
	 */
	private List configErrors = new ArrayList();

	public CCConfig(Properties properties) {
		super(properties);
	}

	protected void configure() {
		super.configure();
		executable = getStringProperty("cc.executable", true);
		String pp = getStringProperty("cc.projectpath", true);
		if (pp != null)
			projectPath = new File(pp);
		tempDir = getTempDir("cc.tempdir");

		logDateFormatString = getStringProperty("cc.log.dateformat", false);
		logDateFormat = getDateFormat(logDateFormatString, null);
		logEncoding = getStringProperty("cc.log.encoding", true);
		verboseExec = getBooleanProperty("cc.verbose_exec");
	}

	protected void printError(String error) {
		LOG.error(error);
	}

	/**
	 * Return path to cleartool.exe
	 *
	 * @return
	 */
	public String getExecutable() {
		return executable;
	}

	/**
	 * Project path
	 *
	 * @return
	 */
	public File getProjectPath() {
		return projectPath;
	}

	/**
	 * Config errors
	 *
	 * @return
	 */
	public List getConfigErrors() {
		return configErrors;
	}

	/**
	 * Temp dir
	 *
	 * @return
	 */
	public File getTempDir() {
		return tempDir;
	}

	public boolean isVerboseExec() {
		return verboseExec;
	}

	public String getLogEncoding() {
		return logEncoding;
	}

	/**
	 * Log date format
	 *
	 * @return
	 */
	public DateFormat getLogDateFormat() {
		return logDateFormat;
	}

	/**
	 * Log environment information
	 */
	public void logEnvironmentInformation() {
		LOG.info("*** ClearCase provider configuration ***");
		LOG.info("executable = \"" + executable + "\"");
		LOG.info("projectPath = \"" + projectPath + "\"");
		LOG.info("temp dir = \"" + tempDir.getAbsolutePath() + "\"");
		LOG.info("log date format = \"" + logDateFormatString + "\"");
		LOG.info("verbose exec = \"" + verboseExec + "\"");
		super.logEnvironmentInformation();
	}
}

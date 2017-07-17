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
package org.polarion.svnimporter.pvcsprovider.internal;

import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.common.ProviderConfig;

import java.io.File;
import java.text.DateFormat;
import java.util.Properties;

/**
 *
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class PvcsConfig extends ProviderConfig {
	private static final Log LOG = Log.getLog(PvcsConfig.class);

	/**
	 * Path to "pcli.exe"
	 */
	private String executable;

	/**
	 * Pvcs project path
	 */
	private String projectPath;
	private String subproject;
	private File tempDir;
	private DateFormat logDateFormat;
	private String logDateFormatString;
	private String logDateLocale;
	private String logDateTimeZone;
	private String logEncoding;
	private boolean verboseExec;
	private String userName;
	private String password;
	private boolean keepVlogFile;
	private boolean importAttributes;
    private boolean validateCheckouts;
	private File checkoutTempDir;

    public PvcsConfig(Properties properties) {
		super(properties);
	}

	protected void configure() {
		super.configure();

		executable = getStringProperty("pvcs.executable", true);

		projectPath = getStringProperty("pvcs.projectpath", true);
		subproject = getStringProperty("pvcs.subproject", false);

        tempDir = getTempDir("pvcs.tempdir");

		logDateFormatString = getStringProperty("pvcs.log.dateformat", true);
		logDateLocale = getStringProperty("pvcs.log.datelocale", false);
		logDateTimeZone = getStringProperty("pvcs.log.datetimezone", false);
		logDateFormat = getDateFormat(logDateFormatString, logDateLocale, logDateTimeZone);

		logEncoding = getStringProperty("pvcs.log.encoding", true);
		verboseExec = getBooleanProperty("pvcs.verbose_exec");
		userName = getStringProperty("pvcs.username", false);
		password = getStringProperty("pvcs.password", false);
		keepVlogFile = getBooleanProperty("pvcs.keep_vlogfile");
		importAttributes = getBooleanProperty("pvcs.import_attributes");
        validateCheckouts = getBooleanProperty("pvcs.validate_checkouts");
        checkoutTempDir= getTempDir("pvcs.checkouttempdir");
    }

	public String getExecutable() {
		return executable;
	}

	public boolean isVerboseExec() {
		return verboseExec;
	}

	public String getProjectPath() {
		return projectPath;
	}

	public String getSubproject() {
		return subproject;
	}

	/**
	 * Get pvcs provider's temp dir
	 *
	 * @return
	 */
	public File getTempDir() {
		return tempDir;
	}

	public File getCheckoutTempDir() {
		return checkoutTempDir;
	}

	public DateFormat getLogDateFormat() {
		return logDateFormat;
	}

	public String getLogEncoding() {
		return logEncoding;
	}

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}

	public boolean keepVlogFile() {
		return keepVlogFile;
	}

	public boolean	importAttributes() {
		return importAttributes;
	}

    public boolean isValidateCheckouts() {
        return validateCheckouts;
    }

    protected void printError(String error) {
		LOG.error(error);
	}

	/**
	 * Log environment information
	 */
	public void logEnvironmentInformation() {
		LOG.info("*** PVCS provider configuration ***");
		LOG.info("executable = \"" + executable + "\"");
		LOG.info("projectPath = \"" + projectPath + "\"");
		LOG.info("subproject = \"" + subproject + "\"");
		LOG.info("temp dir = \"" + tempDir.getAbsolutePath() + "\"");
		LOG.info("log date format = \"" + logDateFormatString + "\"");
		LOG.info("log date locale = \"" + logDateLocale + "\"");
		LOG.info("log date time zone = \"" + logDateTimeZone + "\"");
		LOG.info("log encoding = \"" + logEncoding + "\"");
		LOG.info("verbose exec = \"" + verboseExec + "\"");
		LOG.info("user name = \"" + userName + "\"");
		LOG.info("password = \"" + password + "\"");
		LOG.info("import archive attributes = \"" + importAttributes + "\"");
		LOG.info("keep vlog file = \"" + keepVlogFile + "\"");
        LOG.info("validate checkouts = \"" + validateCheckouts + "\"");
        LOG.info("checkouttempdir = \"" + checkoutTempDir + "\"");
        super.logEnvironmentInformation();
	}
}


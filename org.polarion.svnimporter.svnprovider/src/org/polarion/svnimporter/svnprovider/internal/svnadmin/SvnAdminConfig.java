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
package org.polarion.svnimporter.svnprovider.internal.svnadmin;

import org.polarion.svnimporter.common.Config;
import org.polarion.svnimporter.common.Log;

import java.io.File;
import java.util.Properties;

/**
 *
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class SvnAdminConfig extends Config {
	private static final Log LOG = Log.getLog(SvnAdminConfig.class);

	private String adminExecutable;
	private String clientExecutable;
	private File repositoryPath;
	private String parentDir;
	private File tempDir;
	private boolean verboseExec;
    private long importTimeout;
    private String pathNotExistSignature;

    public SvnAdminConfig(Properties properties) {
		super(properties);
		configure();
	}

	protected void configure() {
		adminExecutable = getStringProperty("svnadmin.executable", true);
		clientExecutable = getStringProperty("svnclient.executable", true);
		repositoryPath = getFileProperty("svnadmin.repository_path", true);
		parentDir = getStringProperty("svnadmin.parent_dir", true);
        importTimeout = getLongProperty("svnadmin.import_timeout");
        if (parentDir != null) {
			if (parentDir.startsWith("/"))
				parentDir = parentDir.substring(1);
			if (parentDir.endsWith("/"))
				parentDir = parentDir.substring(0, parentDir.length() - 1);
		}
		tempDir = getTempDir("svnadmin.tempdir");
		verboseExec = getBooleanProperty("svnadmin.verbose_exec");
        pathNotExistSignature = getStringProperty("svnadmin.path_not_exist_signature", true);
    }

	protected void printError(String error) {
		LOG.error(error);
	}

	public String getAdminExecutable() {
		return adminExecutable;
	}

	public File getRepositoryPath() {
		return repositoryPath;
	}

	public String getParentDir() {
		return parentDir;
	}

	public File getTempDir() {
		return tempDir;
	}

	public String getClientExecutable() {
		return clientExecutable;
	}

	public boolean isVerboseExec() {
		return verboseExec;
	}

    public String getPathNotExistSignature() {
        return pathNotExistSignature;
    }

    /**
     * Sometimes "svnadmin load" hangs without reason.
     * if import timeout > 0 and svnadmin is not finished after timeout then it's process will be killed
     *
     * @return timeout for "svnadmin load"
     */
    public long getImportTimeout() {
        return importTimeout;
    }

    /**
     * Sometimes "svnadmin load" hangs without reason.
     * if import timeout > 0 and svnadmin is not finished after timeout then it's process will be killed
     *
     */
    public void setImportTimeout(long importTimeout) {
        this.importTimeout = importTimeout;
    }

    /**
	 * Log environment information
	 */
	public void logEnvironmentInformation() {
		LOG.info("*** SvnAdmin configuration *** ");
		LOG.info("svnadmin executable = \"" + adminExecutable + "\"");
		LOG.info("svn executable = \"" + clientExecutable + "\"");
		LOG.info("repositoryPath = \"" + repositoryPath + "\"");
		LOG.info("parent dir = \"" + parentDir + "\"");
		LOG.info("temp dir = \"" + tempDir.getAbsolutePath() + "\"");
		LOG.info("verbose exec = \"" + verboseExec + "\"");
        LOG.info("path_not_exist signature = \""+pathNotExistSignature+"\" ");
    }
}

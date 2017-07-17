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
package org.polarion.svnimporter.mksprovider.internal;

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
public class MksConfig extends ProviderConfig {
	private static final Log LOG = Log.getLog(MksConfig.class);

	/**
	 * Temporary directory
	 */
	private File tempDir;

	/**
	 * Path to "si" executable
	 */
	private String executable;

	/**
	 * Path to source integrity project
	 */
	private String projectPath;

	private boolean verboseExec;

	/**
	 * Format of dates from output of "si rlog" command
	 */
	private DateFormat logDateFormat;
	private String logDateFormatString;
	private String logDateLocale;

	private String logEncoding;

    /**
     * Name of project file ("project.pj" or like)
     */
    private String projectFilename;
    
    private String projectDir;
    
    // Boolean flag that control how tags are generated from project history
    private boolean tagLabels;
    private boolean tagNumbers;
    private boolean tagBoth;
    private boolean tagWith;
    
    // Maximum number of exec calls to make on MKS client before restarting it
    private int mksExecLimit;
    private boolean mksExecContinue;
    
    private String mksRootDir;
    private boolean noSplitBranchOnDelete;

    private boolean generateOrphanBranches;
    private String orphanBranchPrefix;

    public MksConfig(Properties properties) {
		super(properties);
	}

	protected void configure() {
		super.configure();
		tempDir = getTempDir("mks.tempdir");
		executable = getStringProperty("mks.executable", true);
		projectPath = getStringProperty("mks.project", true);
		int ndx = projectPath.lastIndexOf('/');
		projectFilename = projectPath.substring(ndx+1);
        projectDir = projectPath.substring(0,ndx+1);

		logDateFormatString = getStringProperty("mks.log.dateformat", true);
		logDateLocale = getStringProperty("mks.log.datelocale", false);
		logDateFormat = getDateFormat(logDateFormatString, logDateLocale);

		logEncoding = getStringProperty("mks.log.encoding", true);
		verboseExec = getBooleanProperty("mks.verbose_exec");
		
		generateOrphanBranches = getBooleanProperty("mks.orphan.branches");
		orphanBranchPrefix = getStringProperty("mks.oprhan.branch.prefix", false);
		if (orphanBranchPrefix == null) orphanBranchPrefix = "orphan";
		
		String tagOption = getStringProperty("mks.tag.option", false);
		if (tagOption== null) tagOption = "label_with_number";
		if (tagOption.equals("none")) {
		    tagLabels = false;
		    tagNumbers = false;
		    tagBoth = false;
		    tagWith = false;
		} else if (tagOption.equals("label_only")) {
		    tagLabels = true;
		    tagNumbers = false;
		    tagBoth = false;
		    tagWith = false;
		} else if (tagOption.equals("number_only")) {
		    tagLabels = false;
		    tagNumbers = true;
		    tagBoth = false;
		    tagWith = false;
		} else if (tagOption.equals("label_or_number")) {
		    tagLabels = true;
		    tagNumbers = true;
		    tagBoth = false;
		    tagWith = false;
		} else if (tagOption.equals("label_and_number")) {
		    tagLabels = true;
		    tagNumbers = true;
		    tagBoth = true;
		    tagWith = false;
        } else if (tagOption.equals("label_with_number")) {
            tagLabels = true;
            tagNumbers = true;
            tagBoth = true;
            tagWith = true;
		} else {
		   recordError("mks.tag.option value must be one of \'none\', \'label_only\', \'number_only\', \'label_or_number\', or \'label_and_number\'"); 
		}
		
		mksExecLimit = (int)getLongProperty("mks.exec.limit");
		mksExecContinue = getBooleanProperty("mks.exec.continue");
		
		mksRootDir = getStringProperty("mks.root.dir", false);
		if (mksRootDir != null && mksRootDir.charAt(mksRootDir.length()-1) != '/') {
		    mksRootDir = mksRootDir + "/";
		}
		
		noSplitBranchOnDelete = getBooleanProperty("mks.no.split.branch.on.delete");
    }

	public File getTempDir() {
		return tempDir;
	}

	public String getExecutable() {
		return executable;
	}

	public String getProjectPath() {
		return projectPath;
	}

	public boolean isVerboseExec() {
		return verboseExec;
	}

	public DateFormat getLogDateFormat() {
		return logDateFormat;
	}

	public String getLogEncoding() {
		return logEncoding;
	}

    public String getProjectFilename() {
        return projectFilename;
    }
    
    public String getProjectDir() {
        return projectDir;
    }
    
    public boolean isTagLabels() {
        return tagLabels;
    }
    
    public boolean isTagNumbers() {
        return tagNumbers;
    }
    
    public boolean isTagBoth() {
        return tagBoth;
    }
    
    public boolean isTagWith() {
        return tagWith;
    }
    
    public int getMksExecLimit() {
        return mksExecLimit;
    }
    
    public boolean isMksExecContinue() {
        return mksExecContinue;
    }
    

	/**
     * @return the mksRootDir
     */
    public String getMksRootDir() {
        return mksRootDir;
    }

    /**
     * @return the splitBranchOnDelete
     */
    public boolean isSplitBranchOnDelete() {
        return ! noSplitBranchOnDelete;
    }
    
    public boolean isGenerateOrphanBranches() {
        return generateOrphanBranches;
    }
    
    /*
     *@return orphanBranchPrefix setting
     */
    public String getOrphanBranchPrefix() {
        return orphanBranchPrefix;
    }

    protected void printError(String error) {
		LOG.error(error);
	}

	/**
	 * Log environment information
	 */
	public void logEnvironmentInformation() {
		LOG.info("*** Mks provider configuration ***");
		LOG.info("executable = \"" + executable + "\"");
		LOG.info("projectPath = \"" + projectPath + "\"");
		LOG.info("temp dir  = \"" + tempDir.getAbsolutePath() + "\"");
		LOG.info("log date format = \"" + logDateFormatString + "\"");
		LOG.info("log date locale = \"" + logDateLocale + "\"");
		LOG.info("log encoding = \"" + logEncoding + "\"");
		LOG.info("verbose exec = \"" + verboseExec + "\"");
		super.logEnvironmentInformation();
	}
}


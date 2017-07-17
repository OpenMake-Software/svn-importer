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
package org.polarion.svnimporter.ccprovider;

import org.polarion.svnimporter.ccprovider.internal.CCConfig;
import org.polarion.svnimporter.ccprovider.internal.CCContentRetriever;
import org.polarion.svnimporter.ccprovider.internal.CCExec;
import org.polarion.svnimporter.ccprovider.internal.CCHistoryParser;
import org.polarion.svnimporter.ccprovider.internal.CCTransform;
import org.polarion.svnimporter.ccprovider.internal.model.CCFile;
import org.polarion.svnimporter.ccprovider.internal.model.CCModel;
import org.polarion.svnimporter.ccprovider.internal.model.CCRevision;
import org.polarion.svnimporter.common.IContentRetriever;
import org.polarion.svnimporter.common.IProvider;
import org.polarion.svnimporter.common.ISvnModel;
import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.common.ProviderConfig;
import org.polarion.svnimporter.common.StreamConsumer;
import org.polarion.svnimporter.common.Util;
import org.polarion.svnimporter.common.ZeroContentRetriever;
import org.polarion.svnimporter.svnprovider.SvnModel;

import java.io.File;
import java.io.PrintStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

/**
 *
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class CCProvider implements IProvider {
	private static final Log LOG = Log.getLog(CCProvider.class);

	/**
	 * Configuration
	 */
	private CCConfig config;

	/**
	 * Constructor
	 */
	public CCProvider() {
	}

	/**
	 * Configure provider
	 *
	 * @param properties
	 */
	public void configure(Properties properties) {
		config = new CCConfig(properties);
	}

	/**
	 * Return false if configuration has errors
	 *
	 * @return
	 */
	public boolean validateConfig() {
		return config.validate();
	}

	/**
	 * Get provider's configuration
	 *
	 * @return
	 */
	public CCConfig getConfig() {
		return config;
	}

	/**
	 * Log environment information
	 */
	public void logEnvironmentInformation() {
		config.logEnvironmentInformation();
	}

	/**
	 * Return collection of configuration errors (Strings)
	 *
	 * @return
	 */
	public Collection getConfigErrors() {
		return config.getConfigErrors();
	}


	public void listFiles(PrintStream out) {
		Collection files = buildCCModel().getFiles().keySet();
		for (Iterator i = files.iterator(); i.hasNext();)
			out.println(i.next());
	}

	/**
	 * Build clear case model and to svn model
	 *
	 * @return
	 */
	public ISvnModel buildSvnModel() {
		CCModel model = buildCCModel();
		CCTransform transform = new CCTransform(this);
		SvnModel svnModel = transform.transform(model);
		LOG.info("Svn model has been created");
		LOG.info("total number of revisions in svn model: " + svnModel.getRevisions().size());
		return svnModel;
	}

    /**
     * Build clear case model
     *
     * @return
     */
    protected CCModel buildCCModel() {
        CCExec exec = new CCExec(new String[]{
                config.getExecutable(),
                "lshistory",
                "-recurse",
                "-fmt",
                CCHistoryParser.HISTORY_FORMAT
        });
        final CCHistoryParser historyParser = new CCHistoryParser();
        historyParser.setDateFormat(config.getLogDateFormat());

        // Save command output to this file for testing/debugging purposes
        File historyFile = new File(config.getTempDir(), "history.txt");
        final Writer writer;
        try {
            writer = new FileWriter(historyFile);
        } catch (IOException e) {
            throw new CCException("Cannot open file: " + historyFile.getAbsolutePath(), e);
        }

        try {
            exec.setStdoutConsumer(new StreamConsumer() {
                public void consumeLine(String line) {
                    try {
                        writer.write(line + "\n");
                    } catch (IOException e) {
                        //ignore
                    }
                    historyParser.parse(line);
                }
            });
            executeCommand(exec);
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
            }
        }
        CCModel model = historyParser.getModel();
        model.finishModel();
        LOG.info("ClearCase model has been created.");
        model.printSummary();

        return model;
    }

    /**
	 * Create content retriever for revision
	 *
	 * @param revision
	 * @return
	 */
	public IContentRetriever createContentRetriever(CCRevision revision) {
		if (config.isUseOnlyLastRevisionContent() && !revision.isLastRevision()) {
			return ZeroContentRetriever.INSTANCE;
		} else {
			return new CCContentRetriever(revision, this);
		}
	}

	/**
	 * Checkout revision to temp dir
	 *
	 * @param revision
	 * @return path to checked-out file
	 */
	public File checkout(CCRevision revision) {
		CCFile ccfile = (CCFile) revision.getModelFile();
		LOG.debug("checkout " + ccfile.getPath() + " version:" + revision.getNumber());
		File localFile = new File(config.getTempDir(), ccfile.getPath());
		if (localFile.exists() && !localFile.delete())
			throw new CCException("can't delete file: " + localFile.getAbsolutePath());
		localFile.getParentFile().mkdirs();

		CCExec exec = new CCExec(new String[]{
			config.getExecutable(),
			"get",
			"-to",
			localFile.getAbsolutePath(),
			ccfile.getCcpath() + "@@" + revision.getNumber()
		});
		executeCommand(exec);

		if (!localFile.exists()) {
			LOG.error("can't retrieve file \"" + ccfile.getCcpath() + "\" version " + revision.getNumber()
					+ " (file is not exist after checkout:" + localFile.getAbsolutePath() + " )");
			return null;
		}
		return localFile;
	}

	/**
	 * Exec cleartool command
	 *
	 * @param exec
	 */
	private void executeCommand(CCExec exec) {
		exec.setWorkdir(config.getProjectPath());
		exec.setVerboseExec(config.isVerboseExec());
		exec.setEnconding(config.getLogEncoding());
		exec.exec();

		if (exec.getErrorCode() != 0) {
			if (exec.getErrorCode() == CCExec.ERROR_EXCEPTION)
				throw new CCException("error during execution command "
						+ Util.toString(exec.getCmd(), " ") + ", exception caught", exec.getException());
			else if (exec.getErrorCode() == CCExec.ERROR_WRONG_PROJECT_PATH)
				throw new CCException("error during execution command "
						+ Util.toString(exec.getCmd(), " ")
						+ ": wrong project path \"" + config.getProjectPath() + "\"");
			else
				throw new CCException("error during execution command "
						+ Util.toString(exec.getCmd(), " "));
		}
	}

	/**
	 * Cleanup (delete temp files)
	 */
	public void cleanup() {
		LOG.debug("cleanup");
		File tempDir = config.getTempDir();
		if (!Util.delete(tempDir)) {
			LOG.error("can't delete temp dir: " + tempDir.getAbsolutePath());
		}
	}
}


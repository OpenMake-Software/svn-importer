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
package org.polarion.svnimporter.stprovider;

import com.starbase.starteam.Folder;
import com.starbase.starteam.Project;
import com.starbase.starteam.StarTeamFinder;
import com.starbase.starteam.StarTeamURL;
import com.starbase.starteam.View;
import com.starbase.starteam.Item.LockType;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import org.polarion.svnimporter.common.IContentRetriever;
import org.polarion.svnimporter.common.IProvider;
import org.polarion.svnimporter.common.ISvnModel;
import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.common.Util;
import org.polarion.svnimporter.common.ZeroContentRetriever;
import org.polarion.svnimporter.stprovider.internal.STConfig;
import org.polarion.svnimporter.stprovider.internal.STContentRetriever;
import org.polarion.svnimporter.stprovider.internal.STHistoryParser;
import org.polarion.svnimporter.stprovider.internal.STTransform;
import org.polarion.svnimporter.stprovider.internal.model.STFile;
import org.polarion.svnimporter.stprovider.internal.model.STModel;
import org.polarion.svnimporter.stprovider.internal.model.STRevision;
import org.polarion.svnimporter.svnprovider.SvnModel;

/**
 *
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class STProvider implements IProvider {
	private static final Log LOG = Log.getLog(STProvider.class);

	/**
	 * Configuration
	 */
	private STConfig config;

	private Folder folder;
	
	/**
	 * Configure provider
	 *
	 * @param properties
	 */
	public void configure(Properties properties) {
		config = new STConfig(properties);
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
	public STConfig getConfig() {
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
		Collection files = buildSTModel().getFiles().keySet();
		for (Iterator i = files.iterator(); i.hasNext();)
			out.println(i.next());
	}

	/**
	 * Build StarTeam model and transform to svn model
	 *
	 * @return
	 */
	public ISvnModel buildSvnModel() {
		
		STModel model = buildSTModel();
		STTransform transform = new STTransform(this);
		SvnModel svnModel = transform.transform(model);
		LOG.info("Svn model has been created");
		LOG.info("total number of revisions in svn model: " + svnModel.getRevisions().size());
	  
		return svnModel;
	}

    /**
     * Build StarTeam model
     *
     * @return
     */
    protected STModel buildSTModel() {
    	connect();
    	
        final STHistoryParser historyParser = new STHistoryParser();
        historyParser.setConfig(getConfig());
        historyParser.buildHistory(folder);
        STModel model = historyParser.getModel();
        model.finishModel();
        LOG.info("StarTeam model has been created.");
        model.printSummary();
	    
        return model;
    }

    /**
	 * Create content retriever for revision
	 *
	 * @param revision
	 * @return
	 */
	public IContentRetriever createContentRetriever(STRevision revision) {
		if (config.isUseOnlyLastRevisionContent() && !revision.isLastRevision()) {
			return ZeroContentRetriever.INSTANCE;
		} else {
			return new STContentRetriever(revision, this);
		}
	}
	
	/**
	 * Connects to StarTeam
	 */
	private synchronized void connect() {
		if(null != folder)
			return;
		
		String url = getConfig().getUrl();
		
		// NE: openFolder crashes with SDK2005_2 - if there is no folder in the url
		StarTeamURL sturl = new StarTeamURL (url);
		if (sturl.getFolders().hasMoreTokens()) {
			folder = StarTeamFinder.openFolder(url);
		}
		View view = null != folder ? folder.getView() : StarTeamFinder.openView(url);
		Project project = null != view ? view.getProject() : StarTeamFinder.openProject(url);

		if (null == project)
			throw new STException("No project found for URL: " + getConfig().getUrl());

		if (null == view)
			view = project.getDefaultView();

		if (null == folder)
			folder = view.getRootFolder();
	}
	
	private synchronized void disconnect() {
		if(folder != null) {
			Project project = folder.getView().getProject();
			folder = null;
			project.discard();
			project.getServer().disconnect();
		}
	}

	/**
	 * Checkout revision to temp dir
	 *
	 * Reworked by Quinn Bailey 2013.02.13
	 *		changes:  check if revision has already been fetched ... makes
	 * @param revision
	 * @return path to checked-out file
	 * @throws IOException 
	 */
	public File checkout(STRevision revision) throws IOException {
		
		STFile stfile = (STFile) revision.getModelFile();


		File localFile = new File(config.getTempDir(), stfile.getPath());
		if (localFile.exists())
		{
			if (revision.isFetched())
				return localFile;
			
			if (localFile.delete())
			{
				revision.setFetched(false);
			}
			else 
				throw new STException("can't delete file: " + localFile.getAbsolutePath());
		}	

		
		LOG.debug("checkout " + stfile.getPath() + " version:" + revision.getNumber());
		Folder lFolder = stfile.getFolder();
		revision.getDate();
		
		revision.getPath();
		com.starbase.starteam.File file = (com.starbase.starteam.File) lFolder.getView().findItem(folder.getServer().typeForName(lFolder.getServer().getTypeNames().FILE), stfile.getObjectId());
		
		if(null == file) {
			LOG.error("can't retrieve file \"" + stfile.getPath() + "\" version " + revision.getNumber()
					+ " (file not found in view  \"" + folder.getView().getFullName() + "\")");
			return null;
		}
		

		localFile.getParentFile().mkdirs();
		int retries = config.getRetries();  //the number of retries per revision
		int n = retries;
		boolean good = false;
		while (!good && retries > 0) {
			
			retries--;
			
			try {
							
				file.checkoutByVersion(localFile, revision.getNumberInBranch(), LockType.UNCHANGED, false, false, false);

				} catch (Exception e) {
					LOG.error("error while retrieving file \"" + stfile.getPath() + "\" version " + revision.getNumber() + ": " + e.toString());
					if (retries - 1 >= 0){
						LOG.error("Retrying retrieval of file \"" + stfile.getPath() + "\" version " + revision.getNumber());
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e1) {
							  Thread.currentThread().interrupt();
						}
						continue;
					}
					else
					{
						LOG.error("Could not retrieve the file \"" + stfile.getPath() + "\" version " + revision.getNumber() + " after " + n 
									+ " attempts, cancelling ...");
						return null;
					}
				}
			good = true;
			revision.setFetched(true);
		}
		if (!localFile.exists()) {
			LOG.error("can't retrieve file \"" + stfile.getPath() + "\" version " + revision.getNumber()
					+ " (file is not exist after checkout:" + localFile.getAbsolutePath() + " )");
			return null;
		}
		return localFile;
		
	}

	/**
	 * Cleanup (delete temp files)
	 */
	public void cleanup() {
		LOG.debug("cleanup");
		disconnect();
		File tempDir = config.getTempDir();
		if (!Util.delete(tempDir)) {
			LOG.error("can't delete temp dir: " + tempDir.getAbsolutePath());
		}
	}
}


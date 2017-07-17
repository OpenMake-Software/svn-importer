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

package org.polarion.svnimporter.stprovider.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.common.ProviderConfig;
import org.polarion.svnimporter.stprovider.STException;

/**
 * 
 * 
 * @author <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class STConfig extends ProviderConfig {
	private static final Log LOG = Log.getLog(STConfig.class);

	private List configErrors = new ArrayList();

	private String url;
	private File tempDir;

	/** user mappings */
	private Map userMappings;

	/** cr mappings */
	private Map crMappings;

	/** regular expression for includes */
	private String includesRegEx;

	/** regular expression for excludes */
	private String excludesRegEx;

	/** should we ignore labels */
	private boolean ignoreLables;

	/** should we attach a list of linked CRs to the commit message */
	private boolean attachLinkedCRsToCommitMessage;

	/** should we try separating commits using change requests? */
	private boolean separateCommitsUsingCRs;

	/** the time span in seconds for collecting multiple checkins into one commit */
	private int checkinTimeSpan;
	
	/** the number of times to retry StarTeam operations for which retry is enabled */
	private int retries;
	
	private ArrayList<String> derivedViewsList;

  /** Recurse to import views **/
  private boolean importDerivedViews;

  public STConfig(Properties properties) {
		super(properties);
	}

	protected void configure() {
		super.configure();
		url = getStringProperty("st.url", true);
		tempDir = getTempDir("st.tempdir");
		includesRegEx = getStringProperty("st.includes.regex", false);
		excludesRegEx = getStringProperty("st.excludes.regex", false);
		ignoreLables = getBooleanProperty("st.ignorelables");
		attachLinkedCRsToCommitMessage = getBooleanProperty("st.attachlinkedcrs");
		separateCommitsUsingCRs = getBooleanProperty("st.separatecommitsusingcrs");
		importDerivedViews = getBooleanProperty("st.import.derived.views");
		String derivedViewsString = getStringProperty("st.derived.views.list", false);
		if (derivedViewsString != null)
			derivedViewsList = new ArrayList<String>(Arrays.asList(derivedViewsString.split(";")));
		
    String checkinTimeSpanString = getStringProperty("st.checkintimespan", false);
		if (null != checkinTimeSpanString) {
			try {
				checkinTimeSpan = Integer.parseInt(checkinTimeSpanString);
			} catch (NumberFormatException e) {
				checkinTimeSpan = 0;
				recordError("st.checkintimespan is not a valid number: " + e.getMessage());
			}
		}
		
	String retriesString = getStringProperty("st.retries", false);
		if (null != retriesString) {
			try {
				setRetries(Integer.parseInt(retriesString));
			} catch (NumberFormatException e) {
				setRetries(2);
				recordError("st.retries is not a valid number: " + e.getMessage());
			}
		}		

		// read user mappings
		String userMappingsFile = getStringProperty("st.usermappings", false);
		if (null != userMappingsFile && userMappingsFile.trim().length() > 0) {
			configureUserMapping(userMappingsFile);
		}

		// read cr mappings
		String crMappingsFile = getStringProperty("st.crmappings", false);
		if (null != crMappingsFile && crMappingsFile.trim().length() > 0) {
			configureCrMapping(crMappingsFile);
		}
	}

  public boolean isImportDerivedViews() {
    return importDerivedViews;
  }

  private void configureUserMapping(String userMappingsFile) {
		userMappings = new HashMap();
		// read usermappings
		Properties properties = new Properties();
		InputStream userMappingsInput = null;
		try {
			userMappingsInput = new FileInputStream(userMappingsFile);
			if (null != userMappingsInput) {
				properties.load(userMappingsInput);
				Enumeration userLogins = properties.propertyNames();
				while (userLogins.hasMoreElements()) {
					String login = (String) userLogins.nextElement();
					userMappings.put(properties.getProperty(login).trim().toLowerCase(), login);
				}
			}

		} catch (Exception e) {
			if (null != userMappingsInput)
				try {
					userMappingsInput.close();
				} catch (IOException e1) {
					// ignore
				}
			throw new STException("Error reading usermappings", e);
		}
		userMappings = Collections.unmodifiableMap(userMappings);
	}

	private void configureCrMapping(String crMappingsFile) {
		crMappings = new HashMap();
		// read crMappings
		Properties properties = new Properties();
		InputStream crMappingsInput = null;
		try {
			crMappingsInput = new FileInputStream(crMappingsFile);
			if (null != crMappingsInput) {
				properties.load(crMappingsInput);
				Enumeration oldCrNumbers = properties.propertyNames();
				while (oldCrNumbers.hasMoreElements()) {
					String oldCrNumber = (String) oldCrNumbers.nextElement();
					crMappings.put(oldCrNumber, properties.getProperty(oldCrNumber).trim());
				}
			}

		} catch (Exception e) {
			if (null != crMappingsInput)
				try {
					crMappingsInput.close();
				} catch (IOException e1) {
					// ignore
				}
			throw new STException("Error reading crmappings", e);
		}
		crMappings = Collections.unmodifiableMap(crMappings);
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
	 * @return the excludesRegEx
	 */
	public String getExcludesRegEx() {
		return excludesRegEx;
	}

	/**
	 * @return the includesRegEx
	 */
	public String getIncludesRegEx() {
		return includesRegEx;
	}

	/**
	 * Temp dir
	 * 
	 * @return
	 */
	public File getTempDir() {
		return tempDir;
	}

	/**
	 * Return path to cleartool.exe
	 * 
	 * @return
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @return the attachLinkedCRsToCommitMessage
	 */
	public boolean isAttachLinkedCRsToCommitMessage() {
		return attachLinkedCRsToCommitMessage;
	}

	/**
	 * @return the ignoreLables
	 */
	public boolean isIgnoreLables() {
		return ignoreLables;
	}

	/**
	 * @return the separateCommitsUsingCRs
	 */
	public boolean isSeparateCommitsUsingCRs() {
		return separateCommitsUsingCRs;
	}

	/**
	 * @return the checkinTimeSpan
	 */
	public int getCheckinTimeSpan() {
		return checkinTimeSpan;
	}

	/**
	 * Log environment information
	 */
	public void logEnvironmentInformation() {
		LOG.info("*** StarTeam provider configuration ***");
		LOG.info("url = \"" + url + "\"");
		super.logEnvironmentInformation();
	}

	protected void printError(String error) {
		LOG.error(error);
	}

	public boolean isTranslateUserNames() {
		return null != userMappings;
	}

	public String translateUserName(String userName) {
		if (!isTranslateUserNames())
			return userName;

		if (null == userName)
			return getSvnimporterUsername();

		String result = (String) userMappings.get(userName.trim().toLowerCase());
		if (null != result)
			return result;

		return getSvnimporterUsername();

	}

	/**
	 * @param number
	 * @return the translated number or <code>null</code> if no translation is
	 *         available
	 */
	public String translateCrNumber(int number) {
		if (null == crMappings)
			return null;

		return (String) crMappings.get(String.valueOf(number));
	}

	public int getRetries() {
		return retries;
	}

	public void setRetries(int retries) {
		this.retries = retries;
	}

	public ArrayList<String> getDerivedViewsList() {
		return derivedViewsList;
	}

	public void setDerivedViewsList(ArrayList<String> derivedViewsList) {
		this.derivedViewsList = derivedViewsList;
	}

}

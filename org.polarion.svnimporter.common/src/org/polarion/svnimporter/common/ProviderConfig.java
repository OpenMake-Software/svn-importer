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
package org.polarion.svnimporter.common;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

/**
 *
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public abstract class ProviderConfig extends Config {
	private static final Log LOG = Log.getLog(ProviderConfig.class);

	private String svnimporterUsername;
	private boolean onlyTrunk;
	private boolean useOnlyLastRevisionContent;
	private String fileDescriptionPropKey;

	private String trunkPath;
	private String branchesPath;
	private String tagsPath;

	private boolean useFileCopy;

	protected ProviderConfig(Properties properties) {
		super(properties);
		configure();
	}

	protected void configure() {
		svnimporterUsername = getStringProperty("svnimporter_user_name", true);
		onlyTrunk = getBooleanProperty("only_trunk");
		trunkPath = getStringProperty("trunk_path", true);
		branchesPath = getStringProperty("branches_path", true);
		tagsPath = getStringProperty("tags_path", true);
		useOnlyLastRevisionContent = getBooleanProperty("use_only_last_revision_content");
		fileDescriptionPropKey = getStringProperty("file_description_property_key", false);
		useFileCopy = getBooleanProperty("use_file_copy");

		if (!hasErrors()) {
			if (trunkPath.equals(branchesPath))
				recordError("trunk path can't coincide with branches path");
			if (trunkPath.equals(tagsPath))
				recordError("trunk path can't coincide with tags path");
			if (branchesPath.equals(tagsPath))
				recordError("branches path can't coincide with tags path");
			if (".".equals(getTrunkPath())) {
				if (isOnlyTrunk()) {
					setTrunkPath("");
				} else {
					recordError("trunk path can't be '.' if option 'only_trunk' is disabled");
				}
			}
		}
        
        // Configure playback mode
        if (! Playback.getInstance().config(getProperties())) {
            recordError("Playback configuration errors");
        }
	}

	protected SimpleDateFormat getDateFormat(String format, String locale) {
		if (format == null) return null;
		if (locale != null) {
			return new SimpleDateFormat(format, new Locale(locale));
		} else {
			return new SimpleDateFormat(format);
		}
	}

    protected SimpleDateFormat getDateFormat(String format, String locale, String timeZone) {
        SimpleDateFormat sdf = getDateFormat(format, locale);
        if (sdf == null) return null;
        if (timeZone != null) {
            TimeZone tz = TimeZone.getTimeZone(timeZone);
            // NB.: If timeZone cannot be understood, tz is set to UTC. No error message is provided.
            sdf.setTimeZone(tz);
        }
        return sdf;
    }

	/**
	 * Validate configuration
	 *
	 * @return
	 */
	public boolean validate() {
		return !hasErrors();
	}

	public String getSvnimporterUsername() {
		return svnimporterUsername;
	}

	public boolean isOnlyTrunk() {
		return onlyTrunk;
	}

	public String getTrunkPath() {
		return trunkPath;
	}

	public String getBranchesPath() {
		return branchesPath;
	}

	public String getTagsPath() {
		return tagsPath;
	}

	public boolean isUseOnlyLastRevisionContent() {
		return useOnlyLastRevisionContent;
	}

	public boolean useFileCopy() {
		return useFileCopy;
	}

	public String getFileDescriptionPropKey() {
		return fileDescriptionPropKey;
	}

	protected void setTrunkPath(String trunkPath) {
		this.trunkPath = trunkPath;
	}

	protected void setBranchesPath(String branchesPath) {
		this.branchesPath = branchesPath;
	}

	protected void setTagsPath(String tagsPath) {
		this.tagsPath = tagsPath;
	}

	public void logEnvironmentInformation() {
		LOG.info("svnimporter_user_name = \"" + getSvnimporterUsername() + "\"");
		LOG.info("only_trunk = \"" + isOnlyTrunk() + "\"");
		LOG.info("trunk_path = \"" + getTrunkPath() + "\"");
		LOG.info("branches_path = \"" + getBranchesPath() + "\"");
		LOG.info("tags_path = \"" + getTagsPath() + "\"");
		LOG.info("use_only_last_revision_content = \"" + isUseOnlyLastRevisionContent() + "\"");
		LOG.info("file_description_property_key = \"" + getFileDescriptionPropKey() + "\"");
		LOG.info("use_file_copy = \"" + useFileCopy + "\"");
        if (useFileCopy) {
            LOG.warn("WARNING: you enabled use_file_copy, so you will not be able to make incremental imports to synchronize repositories");
        }
    }
}


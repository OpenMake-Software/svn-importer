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

import java.io.File;
import java.util.Properties;

/**
 *
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public abstract class Config {
	private final Properties srcProperties;
	private boolean hasErrors;

	public Config(Properties properties) {
		this.srcProperties = properties;
		this.hasErrors = false;
	}

	protected abstract void configure();

	/**
	 * Validate configuration
	 *
	 * @return
	 */
	public boolean validate() {
		return !hasErrors;
	}
	
	protected Properties getProperties() {
	    return srcProperties;
	}

	protected boolean getBooleanProperty(String key) {
		return ConfigUtil.getBooleanProperty(srcProperties, key);
	}

    protected long getLongProperty(String key/*, boolean recordError*/) {
        try {
            return Long.parseLong(srcProperties.getProperty(key));
        } catch (Exception e) {
            return 0;
        }
    }

    protected String getStringProperty(String key, boolean recordError) {
		String value = srcProperties.getProperty(key);
		if (value == null || value.length() < 1) {
			if (recordError)
				recordError("configuration property '" + key + "' is not set");
			value = null;
		} else {
			value = value.trim();
		}
		return value;
	}

	protected File getFileProperty(String key, boolean recordError) {
		String value = getStringProperty(key, recordError);
		if (value != null) {
			return new File(value);
		} else {
			return null;
		}
	}

	protected File getTempDir(String key) {
		File tempdir = getFileProperty(key, true);
		if (tempdir == null) return null;
		tempdir = new File(tempdir, key);
		tempdir.mkdirs();
		if (!tempdir.exists() || !tempdir.isDirectory())
			recordError(key + " tempdir '" + tempdir.getAbsolutePath() + "' is not exist (or not a directory)");
		return tempdir;
	}

	protected void recordError(String message) {
		hasErrors = true;
		printError(message);
	}

	protected abstract void printError(String error);

	protected boolean hasErrors() {
		return hasErrors;
	}
}


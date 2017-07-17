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
package org.polarion.svnimporter.vssprovider.comapi;

/**
 * @author Marcus Nylander
 * @author Charles Chahbazian
 */
public final class RepositoryConfiguration {
	private final String vssDataDir;
	private final String vssIniFile;
	private final String username;
	private final String password;
	
	/**
	 * Constructor for RepositoryConfiguration.
	 */
	public RepositoryConfiguration(final String vssDataDir, final String username, final String password) {
		super();
		if (vssDataDir.endsWith("\\srcsafe.ini")) {
			this.vssIniFile = vssDataDir;
			this.vssDataDir = vssDataDir.substring(0, vssDataDir.length() - "\\srcsafe.ini".length());
		} else {
			this.vssDataDir = vssDataDir;
			this.vssIniFile = vssDataDir + "\\srcsafe.ini";
		}
		this.username = username;
		this.password = password;
	}

	public String getVSSDataDir() {
		return this.vssDataDir;
	}

	public String getUsername() {
		return this.username;
	}

	public String getPassword() {
		return this.password;
	}

	/**
	 * Method getVSSIniFile.
	 * @return String
	 */
	public String getVSSIniFile() {
		return this.vssIniFile;
	}

	/**
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof RepositoryConfiguration) {
			RepositoryConfiguration config = (RepositoryConfiguration) obj;
			return config.vssDataDir.equals(vssDataDir) && config.username.equals(username);
		}
		return false;
	}

}
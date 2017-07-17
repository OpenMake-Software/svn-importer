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

import java.util.Date;

/**
 * @author Marcus Nylander
 * @author Charles Chahbazian
 */
public class VersionState
{
	private final String vssFilePath;
	private int versionNumber;
	private final String action;
	private final String userName;
	private final String label;
	private final String comment;
	private final Date modificationTime;

	public VersionState( String vssFilePath, int versionNumber, Date modificationTime, String action, String userName, String label, String comment )
	{
		this.vssFilePath = vssFilePath;
		this.versionNumber = versionNumber;
		this.modificationTime = modificationTime;
		this.action = action;
		this.userName = userName;
		this.label = label;
		this.comment = comment;
	}

	public String getVSSFilePath()
	{
		return this.vssFilePath;
	}

	public String getLabel()
	{
		return this.label;
	}

	public Date getDate()
	{
		return this.modificationTime;
	}

	public String getUserName()
	{
		return this.userName;
	}

	public int getVersionNumber()
	{
		return this.versionNumber;
	}

	public void setVersionNumber( int i )
	{
		this.versionNumber = i;
	}

	public String getAction()
	{
		return this.action;
	}

	public String getComment()
	{
		return this.comment;
	}
}

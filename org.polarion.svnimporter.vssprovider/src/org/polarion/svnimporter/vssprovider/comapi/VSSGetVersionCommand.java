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

import java.io.File;

import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.vssprovider.comapi.vss.IVSSItem;
import org.polarion.svnimporter.vssprovider.comapi.vss.VSSFlags;


/**
 * @author Marcus Nylander
 * @author Charles Chahbazian
 */
public class VSSGetVersionCommand extends VSSCommand implements Runnable
{
	private static final Log LOGGER = Log.getLog( VSSGetVersionCommand.class );
	private final int version;
	private final File localFile;
	private boolean setReadOnly = false;

	public VSSGetVersionCommand( RepositoryConfiguration configuration, String vssFile, final File localFile, final int version )
	{
		super( configuration );
		this.version = version;
		this.localFile = localFile;
		this.vssFilePath = vssFile;
	}

	/**
	 * @see org.vssplugin.core.olecommand.VSSCommand#execute()
	 */
	protected void execute()
	{
		IVSSItem item = null;
		IVSSItem oldItem = null;
		try
		{
			VSSDatabase db = VSSDatabase.getInstance( getConfiguration() );
			item = db.getItem( vssFilePath );
			oldItem = item.getVersion( version );
			int flags = VSSFlags.VSSFLAG_FORCEDIRNO | VSSFlags.VSSFLAG_REPREPLACE;
			flags |= ( setReadOnly ? VSSFlags.VSSFLAG_USERROYES : VSSFlags.VSSFLAG_USERRONO );
			oldItem.get( localFile.getAbsolutePath(), flags );

		}
		catch( Throwable e )
		{
			LOGGER.error( e );
		}
		finally
		{
			close( oldItem );
			close( item );
			VSSDatabase.destroy( getConfiguration() );
		}
	}

	/**
	 * Method getName.
	 * @return Object
	 */
	protected String getName()
	{
		return "GetVersion";
	}

	/**
	 * Sets the setReadOnly.
	 * @param setReadOnly The setReadOnly to set
	 */
	public void setReadOnly( boolean setReadOnly )
	{
		this.setReadOnly = setReadOnly;
	}

}
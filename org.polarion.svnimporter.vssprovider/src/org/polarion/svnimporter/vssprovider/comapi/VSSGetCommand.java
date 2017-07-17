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

import com.develop.jawin.COMException;

/**
 * @author Marcus Nylander
 * @author Charles Chahbazian
 */
public class VSSGetCommand extends VSSCommand implements Runnable
{
	private static final Log LOGGER = Log.getLog( VSSGetCommand.class );
	private final boolean overwrite;
	private File localFile = null;

	public VSSGetCommand( RepositoryConfiguration configuration )
	{
		super( configuration );
		overwrite = false;
	}

	public VSSGetCommand( RepositoryConfiguration configuration, boolean overwrite )
	{
		super( configuration );
		this.overwrite = overwrite;
	}

	protected void execute()
	{
		IVSSItem item = null;
		try
		{
			VSSDatabase db = VSSDatabase.getInstance( getConfiguration() );
			item = db.getItem( vssFilePath );
			int flags = VSSFlags.VSSFLAG_FORCEDIRNO | VSSFlags.VSSFLAG_USERROYES;
			if ( isRecursive() )
			{
				flags |= VSSFlags.VSSFLAG_RECURSYES;
			}
			flags |= ( overwrite ? VSSFlags.VSSFLAG_REPREPLACE : 0 );
			if ( localFile == null )
			{
				item.get( this.vssFilePath, flags );
			}
			else
			{
				flags |= VSSFlags.VSSFLAG_REPREPLACE;
				item.get( localFile.getAbsolutePath(), flags );
			}
		}
		catch( COMException e )
		{
			LOGGER.error( e );
		}
		catch( Exception e )
		{
			setError( e );
		}
		finally
		{
			close( item );
			VSSDatabase.destroy( getConfiguration() );
		}
	}

	protected String getName()
	{
		return "Get";
	}

	/**
	 * Sets the localFile.
	 * @param localFile The localFile to set
	 */
	public void setLocalFile( File localFile )
	{
		this.localFile = localFile;
	}

	public String getVssPath()
	{
		return vssFilePath;
	}

	public void setVssPath( String vssPath )
	{
		this.vssFilePath = vssPath;
	}

}
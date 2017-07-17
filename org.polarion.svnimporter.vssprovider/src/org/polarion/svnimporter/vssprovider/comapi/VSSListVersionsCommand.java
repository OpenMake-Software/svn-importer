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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.polarion.svnimporter.vssprovider.comapi.vss.IVSSItem;
import org.polarion.svnimporter.vssprovider.comapi.vss.IVSSVersion;
import org.polarion.svnimporter.vssprovider.comapi.vss.IVSSVersions;


/**
 * @author Marcus Nylander
 * @author Charles Chahbazian
 */
public class VSSListVersionsCommand extends VSSCommand
{
	private final static int MAX_HISTORY_ITEMS = 4000;

	private final boolean ignoreLabels;
	private final Collection versions;

	public VSSListVersionsCommand( RepositoryConfiguration configuration, String vssFile, final Collection versions, final boolean ignoreLabels )
	{
		super( configuration );
		this.vssFilePath = vssFile;
		this.versions = versions;
		this.ignoreLabels = ignoreLabels;
	}

	public VSSListVersionsCommand( RepositoryConfiguration configuration, String vssFile, final Collection versions )
	{
		super( configuration );
		this.vssFilePath = vssFile;
		this.versions = versions;
		this.ignoreLabels = false;
	}

	/**
	 * @see org.vssplugin.core.olecommand.VSSCommand#execute()
	 */
	protected void execute()
	{
		IVSSItem file = null;
		try
		{
			VSSDatabase db = VSSDatabase.getInstance( getConfiguration() );
			file = db.getItem( this.vssFilePath );
			IVSSVersions versionList = file.getVersions( 0 );
			for ( Iterator i = versionList.getNext( MAX_HISTORY_ITEMS ).iterator() ; i.hasNext() ; )
			{
				IVSSVersion version = (IVSSVersion)i.next();
				this.versions.add( new VersionState( this.vssFilePath, version.getVersionNumber(), version.getDate(), version.getAction(), version.getUsername(), version.getLabel(), version.getComment() ) );
			}
			versionList.close();

			// Fix label events in history..
			// for some strage reason, do label events have any valid version..
			if ( false ) // ne pas gerer les label de cette maniere?
			{
				List labelEvents = new ArrayList( 4 );
				for ( Iterator i = this.versions.iterator() ; i.hasNext() ; )
				{
					VersionState versionState = (VersionState)i.next();
					if ( ( versionState.getLabel() != null ) && ( versionState.getLabel().length() > 0 ) )
					{
						if ( ignoreLabels )
						{
							// this is a label event - remove it for the results.
							i.remove();
						}
						else
						{
							// this is a label event keep the reference for later version number update..
							labelEvents.add( versionState );
						}
					}
					else if ( !labelEvents.isEmpty() )
					{
						// This is the first valid version after any label events in the history chain.
						// update any labeled events with a valid version number
						for ( Iterator j = labelEvents.iterator() ; j.hasNext() ; )
						{
							VersionState versionState2 = (VersionState)j.next();
							versionState2.setVersionNumber( versionState.getVersionNumber() );
						}
						labelEvents.clear();
					}
				}
			}

		}
		catch( Exception e )
		{
			setError( e );
		}
		finally
		{
			close( file );
			VSSDatabase.destroy( getConfiguration() );
		}
	}

	/**
	 * Method getName.
	 * @return Object
	 */
	protected String getName()
	{
		return "ListVersions";
	}

	public Collection getVersions()
	{
		return this.versions;
	}
}
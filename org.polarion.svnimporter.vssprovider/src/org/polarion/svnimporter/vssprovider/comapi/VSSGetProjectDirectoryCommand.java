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

import java.util.Collection;
import java.util.Iterator;

import org.polarion.svnimporter.vssprovider.comapi.vss.IVSSItem;
import org.polarion.svnimporter.vssprovider.comapi.vss.IVSSItems;


/**
 * @author Charles Chahbazian
 * Get the listing of a project in source safe (project)
 */
public class VSSGetProjectDirectoryCommand extends VSSCommand
{
	private Collection projectContent;

	public VSSGetProjectDirectoryCommand( RepositoryConfiguration configuration, String vssFile, Collection projectContent)
	{
		super( configuration );
		vssFilePath = vssFile;
		this.projectContent = projectContent;
	}

	protected void execute()
	{
		IVSSItem item = null;
		try
		{
			VSSDatabase db = VSSDatabase.getInstance( getConfiguration() );
			item = db.getItem( vssFilePath );
			IVSSItems content = item.getItems( false );
			for ( Iterator iter = content.getCollection().iterator() ; iter.hasNext() ; )
			{
				item = (IVSSItem)iter.next();
				projectContent.add( new VSSProjectElement(item.getName(), item.isProject()) );
			}
			content.close();
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
		return "Dir project";
	}
}

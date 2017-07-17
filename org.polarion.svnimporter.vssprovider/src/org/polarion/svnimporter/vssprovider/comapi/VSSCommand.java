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

import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.vssprovider.comapi.vss.IVSSItem;


/**
 * @author Marcus Nylander
 * @author Charles Chahbazian
 */
public abstract class VSSCommand implements Runnable
{
	private static final Log LOGGER = Log.getLog( VSSCommand.class );
	public String vssFilePath;
	protected final RepositoryConfiguration configuration;
	protected boolean isRecursive = false;
	
	public VSSCommand( RepositoryConfiguration configuration )
	{
		this.configuration = configuration;
	}

	protected boolean isRecursive()
	{
		return isRecursive;
	}

	protected boolean hasPassword()
	{
		return ( this.configuration.getPassword() != null );
	}

	protected synchronized void setError( Exception e )
	{
		LOGGER.debug( e );
	}

	/**
	 * Method logCommand.
	 * @param command
	 * @param environment
	 */
	private void logCommand()
	{
		LOGGER.debug( "Running VSS COM API: " + toString() + " on " + this.configuration.getVSSDataDir() );
	}

	/**
	 * Method setRecursive.
	 * @param recursive
	 */
	public void setRecursive( boolean recursive )
	{
		isRecursive = recursive;
	}

	/**
	 * Runs this command.
	 */
	public final void run()
	{
		logCommand();
		execute();
	}

	/**
	 * Method execute.
	 */
	protected abstract void execute();


	/**
	 * Method getName.
	 * @return Object
	 */
	protected abstract String getName();

	/**
	 * Method close.
	 * @param project
	 */
	protected void close( IVSSItem item )
	{
		if ( item != null )
		{
			item.close();
		}
	}

	public String toString()
	{
		final StringBuffer buf = new StringBuffer( 128 );
		buf.append( getName() ).append( " " ).append( vssFilePath );
		return buf.toString();
	}

	/**
	 * Method getConfiguration.
	 * @return RepositoryConfiguration
	 */
	protected RepositoryConfiguration getConfiguration()
	{
		return this.configuration;
	}

}
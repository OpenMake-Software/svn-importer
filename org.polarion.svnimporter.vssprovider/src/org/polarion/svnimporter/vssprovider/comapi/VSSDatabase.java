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
import org.polarion.svnimporter.vssprovider.comapi.vss.IVSSDatabase;
import org.polarion.svnimporter.vssprovider.comapi.vss.IVSSItem;

import com.develop.jawin.COMException;
import com.develop.jawin.win32.Ole32;

/**
 * @author Marcus Nylander
 * @author Charles Chahbazian 
 */
public final class VSSDatabase
{
	private static final Log LOGGER = Log.getLog( VSSDatabase.class );
	private static final String VSS_OLE_PRG_NAME = "SourceSafe.0"; // COM Object name for Source Safe

  static
	{
		// Load jarwin.dll for VSS Com API use
		// it should be in lib folder
		String path=null;
		try
		{
			File currDir = new File(".");
			path = currDir.getCanonicalPath() + "/lib/jawin.dll";
			System.load( path );
		}
		catch( Throwable t )
		{
			System.err.println("ERROR : Unable to load jawin.dll in :" + path);
			LOGGER.fatal( t );
		}
	}

	private static final ThreadLocal database = new ThreadLocal();

	private static final ThreadLocal oleInitialized = new ThreadLocal();

	private IVSSDatabase db;

	private final RepositoryConfiguration config;

	/**
	 * Constructor for VSSDatabase.
	 */
	private VSSDatabase( RepositoryConfiguration configuration ) throws COMException
	{
		config = configuration;
		initialize();
	}

	private void initialize() throws COMException
	{
		db = new IVSSDatabase( VSS_OLE_PRG_NAME );
		db.open( config.getVSSIniFile(), config.getUsername(), config.getPassword() );
	}

	/**
	 * Returns the db.
	 * 
	 * @return IVSSDatabase
	 */
	public IVSSDatabase getDb()
	{
		return db;
	}

	public IVSSItem getItem( String vssPath ) throws COMException
	{
		return getDb().getVSSItem( vssPath );
	}

	public static void destroy( RepositoryConfiguration configuration )
	{
	}

	public static VSSDatabase getInstance( RepositoryConfiguration configuration ) throws COMException
	{
		try
		{
			VSSDatabase db = (VSSDatabase)database.get();
			if ( db == null )
			{
				initThread();
				db = new VSSDatabase( configuration );
				database.set( db );
			}
			else if ( !configuration.equals( db.getConfiguration() ) )
			{
				// Open new DB, close the old one (this is always done
				// internally, so there
				// is no need to have the IVSSDatabase object open.
				db.db.close();
				db = new VSSDatabase( configuration );
				database.set( db );
			}
			return db;
		}
		catch( COMException e )
		{
			LOGGER.error( e );
			throw e;
		}
	}

	/**
	 * Method getConfiguration.
	 * 
	 * @return Object
	 */
	private Object getConfiguration()
	{
		return config;
	}

	/**
	 * Method initThread.
	 */
	private static void initThread()
	{
		if ( oleInitialized.get() == null )
		{
			try
			{
				Ole32.CoInitialize();
				oleInitialized.set( Boolean.TRUE );
			}
			catch( COMException e )
			{
				LOGGER.error( e );
			}
		}
	}

	/**
	 * Method destroyThread.
	 */
	public static void destroyThread()
	{
		if ( oleInitialized.get() != null )
		{
			try
			{
				VSSDatabase db = (VSSDatabase)database.get();
				if ( db != null )
				{
					db.db.close();
					database.set( null );
				}
				Ole32.CoUninitialize();
				oleInitialized.set( null );
			}
			catch( COMException e )
			{
				// Never mind
			}
		}
	}
}

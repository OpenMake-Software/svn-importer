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

package org.polarion.svnimporter.vssprovider.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.common.Timer;
import org.polarion.svnimporter.vssprovider.comapi.RepositoryConfiguration;
import org.polarion.svnimporter.vssprovider.comapi.VSSCommand;
import org.polarion.svnimporter.vssprovider.comapi.VSSGetProjectDirectoryCommand;
import org.polarion.svnimporter.vssprovider.comapi.VSSGetVersionCommand;
import org.polarion.svnimporter.vssprovider.comapi.VSSListVersionsCommand;
import org.polarion.svnimporter.vssprovider.comapi.VSSProjectElement;
import org.polarion.svnimporter.vssprovider.comapi.VersionState;
import org.polarion.svnimporter.vssprovider.internal.model.VssFile;
import org.polarion.svnimporter.vssprovider.internal.model.VssFileActionType;
import org.polarion.svnimporter.vssprovider.internal.model.VssFileRevision;
import org.polarion.svnimporter.vssprovider.internal.model.VssLabel;
import org.polarion.svnimporter.vssprovider.internal.model.VssProject;

/**
 * @author Charles Chahbazian
 * 
 * VSS implementation using COM API (faster than SS.EXE)
 */
public class VssComApi
{
	private static final Log LOGGER = Log.getLog( VssComApi.class );
	private static final String CHECKED_IN = "Checked in";
	private static final String CREATED = "Created";
	private static final String LABELED = "Labeled";
	private static final String ARCHIVED = "Archived";

	public RepositoryConfiguration COMConfig;
	private VssConfig config;

	public VssComApi( VssConfig config, final String username, final String password )
	{
		this.config = config;
		this.COMConfig = new RepositoryConfiguration( config.getPath(), username, password );
	}

	/**
	 * Get a version of a file from Source Safe using COM API
	 * @param localFile local file path
	 * @param vssFile file path in VSS
	 * @param version version of the file in Source Safe
	 * @return
	 */
	public boolean getVersion( final File localFile, String vssFile, final int version )
	{
		boolean retval = true;
		Timer timer = new Timer();
		timer.start();
		// Run the VSS GET command for resource.
		try
		{
			final boolean isFile = true;
			VSSGetVersionCommand command = new VSSGetVersionCommand( this.COMConfig, vssFile, localFile, version );
			retval = processCommand( command, isFile, true );
		}
		catch( Exception e )
		{
			LOGGER.error( e );
			retval = false;
		}
		timer.stop();
		LOGGER.info( "VSS COM API:Exported in:" + timer.getDurationMilli() + " milli seconds." + vssFile + " version:" + version + " into:" + localFile );
		return retval;
	}

	/**
	 * Get the history of a file and put it in versions Collection
	 * @param vssFile  file path in VSS
	 * @param versions output argument: contains the history collection after calling this method. Collection of VersionState
	 * @return
	 */
	public boolean getListVersion( VssFile vssFile, Collection versions )
	{
		boolean retval = true;
		Timer timer = new Timer();
		timer.start();
		try
		{
			final boolean isFile = true;
			VSSListVersionsCommand command = new VSSListVersionsCommand( this.COMConfig, vssFile.getVssPath(), versions );
			retval = processCommand( command, isFile, true );
		}
		catch( Exception e )
		{
			LOGGER.error( e );
			retval = false;
		}
		timer.stop();
		LOGGER.info( "VSS COM API:History done in:" + timer.getDurationMilli() + " milli seconds." + vssFile.getVssPath() + " nb of history items : " + versions.size() );
		return retval;
	}

	/**
	 * Get the content of a Source Safe Project using COM API
	 * @param vssProject project path in VSS
	 * @param dirProject output argument: contains the project content collection after calling this method. Collection of VSSProjectElement
	 * @return
	 */
	public boolean getDirProject( VssProject vssProject, Collection dirProject )
	{
		boolean retval = true;
		Timer timer = new Timer();
		timer.start();
		try
		{
			final boolean isFile = true;
			VSSGetProjectDirectoryCommand command = new VSSGetProjectDirectoryCommand( this.COMConfig, vssProject.getVssPath(), dirProject );
			retval = processCommand( command, isFile, true );
		}
		catch( Exception e )
		{
			LOGGER.error( e );
			retval = false;
		}
		timer.stop();
		LOGGER.info( "VSS COM API:Project Dir done in:" + timer.getDurationMilli() + " milli seconds." + vssProject.getVssPath() + " nb of items : " + dirProject.size() );
		return retval;
	}

	private boolean processCommand( final VSSCommand command, final boolean isFile, final boolean recursive )
	{
		boolean retval = true;
		command.run();
		return retval;
	}

	public RepositoryConfiguration getConfiguration()
	{
		return COMConfig;
	}

	public void setConfiguration( RepositoryConfiguration configuration )
	{
		this.COMConfig = configuration;
	}


	/**
	 * creates the revisions of a file giving a collection of VersionState
	 * 
	 * Equivalent to VSSHistoryParser.parseFileHistory() but using COM API.
	 * @param vssFile file path in VSS
	 * @param versions collection of VersionState
	 */
	public void prepareRevisions( VssFile vssFile, Collection versions )
	{
		List notBoundLabels = new ArrayList();
		VssFileRevision revision;
		for ( Iterator iter = versions.iterator() ; iter.hasNext() ; )
		{
			VersionState version = (VersionState)iter.next();
			revision = new VssFileRevision( version.getVersionNumber() );
			if ( version.getAction().startsWith( LABELED ) )
			{
				VssLabel vssLabel = null;
				vssLabel = new VssLabel( version.getAction() );
				vssLabel.setAuthor( version.getUserName() );
				vssLabel.setComment( version.getComment() );
				vssLabel.setDate( version.getDate() );
				notBoundLabels.add( vssLabel );
			}
			else
			{
				// NOT A LABEL
				if ( version.getAction().startsWith( CHECKED_IN ) )
				{
					revision.setType( VssFileActionType.CHANGE );
				}
				else if ( version.getAction().startsWith( CREATED ) || version.getAction().startsWith( ARCHIVED ) )
				{
					revision.setType( VssFileActionType.ADD );
				}
				else
				{
					// Not a supported type, continue
					LOGGER.debug( "ignored action : " + version.getAction() );
					continue;
				}
				revision.setAuthor( version.getUserName() );
				revision.setDate( version.getDate() );
				revision.setMessage( version.getComment() );

				// Add not bound labels
				for ( Iterator i = notBoundLabels.iterator() ; i.hasNext() ; )
				{
					revision.addLabel( (VssLabel)i.next() );
					i.remove();
				}
				vssFile.addRevision( revision );
				revision.setModelFile( vssFile );
			}
		}
	}	
 
  /**
   * prepare the directories tree giving a collection of VSSProjectElement
   * 
   * Equivalent to VssProvider.parseTree() method but using COM API.
   * @param vssProject
   * @param projectContent
   */
	public void prepareProjectContent( VssProject vssProject, Collection projectContent )
	{
		try
		{
			for ( Iterator iter = projectContent.iterator() ; iter.hasNext() ; )
			{
				VSSProjectElement item = (VSSProjectElement)iter.next();
				String name = item.getName();
				if ( item.isProject() )
				{
					if ( config.getProjectsToIgnore().contains( name ) )
					{
						// Ignore list!
						LOGGER.warn( "Ignore project " + name + " found in : " + vssProject.getVssPath() );
						continue;
					}
					VssProject newProject = new VssProject();
					newProject.setName( name );
					newProject.setParent( vssProject );
					vssProject.addSubproject( newProject );
				}
				else
				{
					// it's a file
					String parent = vssProject.getVssPath().substring( config.getProject().length() );
					if ( parent.length() > 0 )
					{
						if ( parent.startsWith( "/" ) )
							parent = parent.substring( 1 );
						if ( !parent.endsWith( "/" ) )
							parent += "/";
					}
					String path = parent + name;
					VssFile newFile = new VssFile( path, name );
					newFile.setParent( vssProject );
					vssProject.addFile( newFile );
				}
			}
		}
		catch( Exception e )
		{
			LOGGER.error( e );
		}
	}
}

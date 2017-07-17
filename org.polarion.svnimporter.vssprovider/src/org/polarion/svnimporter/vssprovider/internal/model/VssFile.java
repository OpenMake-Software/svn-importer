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
package org.polarion.svnimporter.vssprovider.internal.model;

import org.polarion.svnimporter.common.model.ModelFile;
import org.polarion.svnimporter.common.model.Revision;

/**
 * 
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class VssFile extends ModelFile {
	private VssProject parent;

	/**
	 * without path
	 */
	private String filename;

	/**
	 * one branch for all revisions
	 */
	private VssBranch trunk;

	public VssFile(String path, String filename) {
		super(path);
		if(filename.indexOf( ";" )>0)
		{
			// we got a pinned version here like :
			// myFile.java;21 
			// that means the current file version is fixed to revision number 21 in that branch.
			// We must update the filename and filepath without this extra informations
			filename=filename.substring( 0, filename.indexOf( ";" ) );
			this.setPath( path.substring( 0, path.indexOf( ";" ) )); 
		}
		this.filename = filename;
		this.trunk = new VssBranch("");
		this.trunk.setTrunk(true);
	}

	public VssProject getParent() {
		return parent;
	}

	public void setParent(VssProject parent) {
		this.parent = parent;
	}

	public String getVssPath() {
		return getParent().getVssPath() + "/" + filename;
	}

	public String getFilename() {
		return filename;
	}

	public boolean addRevision(Revision revision) {
		boolean b = super.addRevision(revision);
		if (b) {
			trunk.addRevision(revision);
			revision.setBranch(trunk);
		}
		return b;
	}

	public void setFilename( String filename )
	{
		this.filename = filename;
	}
}


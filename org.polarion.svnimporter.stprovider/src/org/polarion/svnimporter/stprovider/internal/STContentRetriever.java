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
package org.polarion.svnimporter.stprovider.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.polarion.svnimporter.common.IContentRetriever;
import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.common.ZeroContentRetriever;
import org.polarion.svnimporter.stprovider.STException;
import org.polarion.svnimporter.stprovider.STProvider;
import org.polarion.svnimporter.stprovider.internal.model.STRevision;

/**
 * 
 * 
 * @author <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class STContentRetriever implements IContentRetriever {
	private static final Log LOG = Log.getLog(STContentRetriever.class);

	/**
	 * Revision
	 */
	private STRevision revision;

	/**
	 * Provider
	 */
	private STProvider provider;

	/**
	 * Constructor
	 * 
	 * @param revision
	 * @param provider
	 */
	public STContentRetriever(STRevision revision, STProvider provider) {
		this.revision = revision;
		this.provider = provider;
	}

	/**
	 * Checkout revision from repository to local file and return InputStream
	 * for that file
	 * 
	 * @return
	 */
	public InputStream getContent() {
		try {
			File file = provider.checkout(revision);
			if (file == null) {
				LOG.warn("using zeroContent for '" + revision.getPath() + "' [" + revision.getNumber() + "]");
				return zeroContent();
			} else {
				FileInputStream in = new FileInputStream(file);
				return in;
			}
		} catch (FileNotFoundException e) {
			throw new STException("", e);
		} catch (IOException e) {
			throw new STException("", e);
		}
	}

	/**
	 * If provider can't checkout revision then fake 'zero content' will be used
	 * 
	 * @return
	 */
	private InputStream zeroContent() {
		return ZeroContentRetriever.getZeroContent();
	}
}

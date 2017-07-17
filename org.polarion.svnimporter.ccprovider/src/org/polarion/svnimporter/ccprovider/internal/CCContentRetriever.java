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
package org.polarion.svnimporter.ccprovider.internal;

import org.polarion.svnimporter.ccprovider.CCException;
import org.polarion.svnimporter.ccprovider.CCProvider;
import org.polarion.svnimporter.ccprovider.internal.model.CCRevision;
import org.polarion.svnimporter.common.FileCache;
import org.polarion.svnimporter.common.IContentRetriever;
import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.common.RevisionFileCacheKey;
import org.polarion.svnimporter.common.ZeroContentRetriever;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class CCContentRetriever implements IContentRetriever {
    private static final Log LOG = Log.getLog(CCContentRetriever.class);

    /**
     * Revision
     */
    private CCRevision revision;
    /**
     * Provider
     */
    private CCProvider provider;
    private static FileCache cache;

    /**
     * Constructor
     *
     * @param revision
     * @param provider
     */
    public CCContentRetriever(CCRevision revision, CCProvider provider) {
        if (cache == null) {
            cache = new FileCache((provider.getConfig()).getTempDir());
        }
        this.revision = revision;
        this.provider = provider;
    }

    /**
     * Checkout revision from repository to local file and return InputStream for that file
     *
     * @return
     */
    public InputStream getContent() {
        LOG.debug("getContent: revision=" + revision.getNumber()+" file="+revision.getPath());
        try {
            RevisionFileCacheKey cacheKey = new RevisionFileCacheKey(revision);
            InputStream in = cache.getInputStream(cacheKey);
            if (in != null) {
                LOG.debug("Cache hit");
                return in;
            }
            File file = provider.checkout(revision);
            if (file == null) {
                //LOG.warn("using zeroContent for \"" + revision.getPath() + "\" [" + revision.getNumber() + "]");
                return zeroContent();
            } else {
                cache.put(cacheKey, file);
                return new FileInputStream(file);
            }
        } catch (IOException e) {
            throw new CCException(e);
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

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
package org.polarion.svnimporter.harprovider.internal;

import org.polarion.svnimporter.common.IContentRetriever;
import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.common.ZeroContentRetriever;
import org.polarion.svnimporter.harprovider.HarException;
import org.polarion.svnimporter.harprovider.HarProvider;
import org.polarion.svnimporter.harprovider.internal.model.HarRevision;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

/**
 * 
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class HarContentRetriever implements IContentRetriever {
    private static final Log LOG = Log.getLog(HarContentRetriever.class);

    private HarProvider provider;
    private HarRevision revision;

    /**
     * Constructor
     *
     * @param provider
     */
    public HarContentRetriever(HarProvider provider, HarRevision revision) {
        this.provider = provider;
        this.revision = revision;
    }

    /**
     * Get file content from repository, save to local file and return FileInputStream of local
     * copy
     *
     * @return
     */
    public InputStream getContent() {
        try {
            File file = provider.getCheckedOut(revision);
            if (file == null) {
                LOG.warn("using zeroContent for \"" + revision.getPath() + "\" [" + revision.getNumber() + "]");
                return zeroContent();
            } else {
                return new FileInputStream(file);
            }
        } catch (IOException e) {
            throw new HarException(e);
        }
    }

    private InputStream zeroContent() {
        return ZeroContentRetriever.getZeroContent();
    }
}


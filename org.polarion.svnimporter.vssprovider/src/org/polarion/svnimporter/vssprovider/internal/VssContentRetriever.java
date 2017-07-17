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

import org.polarion.svnimporter.common.IContentRetriever;
import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.common.ZeroContentRetriever;
import org.polarion.svnimporter.vssprovider.VssException;
import org.polarion.svnimporter.vssprovider.VssProvider;
import org.polarion.svnimporter.vssprovider.internal.model.VssFileRevision;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class VssContentRetriever implements IContentRetriever {
    private static final Log LOG = Log.getLog(VssContentRetriever.class);
    private VssProvider provider;
    private VssFileRevision revision;

    public VssContentRetriever(VssProvider provider, VssFileRevision revision) {
        this.provider = provider;
        this.revision = revision;
    }

    public InputStream getContent() {
        LOG.debug("getContent: revision=" + revision.getNumber()+" file="+revision.getPath());
        try {
            File file = provider.checkout(revision);
            if (file == null) {
                //LOG.warn("using zeroContent for \"" + revision.getPath() + "\" [" + revision.getNumber() + "]");
                return zeroContent();
            } else {
                return new FileInputStream(file);
            }
        } catch (IOException e) {
            throw new VssException(e);
        }
    }

    private InputStream zeroContent() {
        return ZeroContentRetriever.getZeroContent();
    }
}

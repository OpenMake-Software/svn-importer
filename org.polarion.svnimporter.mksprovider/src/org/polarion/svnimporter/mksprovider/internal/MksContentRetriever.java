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
package org.polarion.svnimporter.mksprovider.internal;

import org.polarion.svnimporter.common.FileCache;
import org.polarion.svnimporter.common.IContentRetriever;
import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.common.RevisionFileCacheKey;
import org.polarion.svnimporter.mksprovider.MksException;
import org.polarion.svnimporter.mksprovider.MksProvider;
import org.polarion.svnimporter.mksprovider.internal.model.MksFile;
import org.polarion.svnimporter.mksprovider.internal.model.MksRevision;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class MksContentRetriever implements IContentRetriever {
    private static final Log LOG = Log.getLog(MksContentRetriever.class);

    private MksProvider provider;
    private MksRevision revision;
    private static FileCache cache;

    /**
     * Constructor
     *
     * @param provider
     * @param revision
     */
    public MksContentRetriever(MksProvider provider, MksRevision revision) {
        if (cache == null) {
            cache = new FileCache((provider.getConfig()).getTempDir());
        }
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
        LOG.debug("getContent: revision=" + revision.getNumber()+" file="+revision.getPath());
        try {
            RevisionFileCacheKey cacheKey = new RevisionFileCacheKey(revision);
            InputStream in = cache.getInputStream(cacheKey);
            if (in != null) {
                LOG.debug("Cache hit");
                return in;
            }
            String filename = revision.getPath();
            int ndx = filename.lastIndexOf('/');
            if (ndx >= 0) filename = filename.substring(ndx+1);
            File revFile = new File(provider.getConfig().getTempDir(), filename);
            
            InputStream is = getRevisionFile(revFile);
            cache.put(cacheKey, revFile);
            return is;
            
        } catch (IOException e) {
            throw new MksException(e);
        }
    }
    
    private InputStream getRevisionFile(File revFile) {
        
        MksFile modelFile = (MksFile)revision.getModelFile();
        List cmdList = new ArrayList();
        cmdList.add(provider.getConfig().getExecutable());
        cmdList.add("viewrevision");
        modelFile.getProject().addCmdList(cmdList);
        cmdList.add("--batch");
        cmdList.add("-r");
        cmdList.add(revision.getNumber());
        cmdList.add(modelFile.getRelativePath());
        MksExec exec = new MksExec(cmdList);
        exec.setResultStub(modelFile.getPath() + ":" + revision.getNumber());
        return provider.executeCommand(exec, revFile);
    }
}

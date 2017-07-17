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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.polarion.svnimporter.common.FileCache;
import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.common.RevisionFileCacheKey;
import org.polarion.svnimporter.common.Util;
import org.polarion.svnimporter.vssprovider.VssException;
import org.polarion.svnimporter.vssprovider.internal.model.VssFile;
import org.polarion.svnimporter.vssprovider.internal.model.VssFileRevision;
import org.polarion.svnimporter.vssprovider.internal.model.VssProject;



/**
 * Interaction with VSS repository
 *
 * @author <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class Vss {
    private static final Log LOG = Log.getLog(Vss.class);

    private static final String TMPFILE = "tmpfile";
    private static final String DIR_HISTORY = "DIR_HISTORY:";
    private static final String FILE_HISTORY = "FILE_HISTORY:";
    private static final String FILES_IN_PROJECT = "FILES_IN_PROJECT:";
    private VssConfig config;
    protected FileCache historyCache;
    private FileCache checkoutCache;

    public Vss() {
    }

    /**
     * Initialize
     *
     * @param config
     * @param historyCacheDir
     * @param checkoutCacheDir
     * @param loadCaches - if cache already exist on disk it will be loaded(only for testing purposes)
     */
    public void init(VssConfig config, File historyCacheDir, File checkoutCacheDir, boolean loadCaches) {
        this.config = config;
        try {
            historyCacheDir.mkdirs();
            historyCache = new FileCache(historyCacheDir);
            historyCache.setSyncToDisc(true);
            if (loadCaches) {
                historyCache.load();
            }

            checkoutCacheDir.mkdirs();
            checkoutCache = new FileCache(checkoutCacheDir);
            checkoutCache.setSyncToDisc(true);
            if (loadCaches) {
                checkoutCache.load();
            }
        } catch (IOException e) {
            throw new VssException(e);
        }
    }

    /**
     * Get directory history
     *
     * @param project
     * @return temp. file with history in VSS format
     */
    public File getDirHistory(VssProject project) {
        try {
            String key = DIR_HISTORY + project.getVssPath();
            if (historyCache.containsKey(key)) {
                return historyCache.getFile(key);
            }
            File tmpfile = new File(config.getTempDir(), TMPFILE);
            VssUtil.deleteTempFile(tmpfile);
            executeCommand(new VssExec(new String[]{
                    config.getExecutable(),
                    "history",
                    project.getVssPath(),
                    "-N",
                    "-O" + tmpfile.getAbsolutePath()
            }));
            if (!tmpfile.exists())
                throw new VssException("Cannot get history of VSS project: " + project.getVssPath());


            historyCache.put(key, tmpfile);
            VssUtil.deleteTempFile(tmpfile);
            return historyCache.getFile(key);
        } catch (IOException e) {
            throw new VssException(e);
        }
    }

    /**
     * Get file history
     *
     * @param vssFile
     * @return temp. file with history in VSS format
     */
    public File getFileHistory(VssFile vssFile) {
        try {
            String key = FILE_HISTORY + vssFile.getVssPath();
            if (historyCache.containsKey(key)) {
                return historyCache.getFile(key);
            }

            File tmpfile = new File(config.getTempDir(), TMPFILE);
            VssUtil.deleteTempFile(tmpfile);
            executeCommand(new VssExec(new String[]{
                    config.getExecutable(),
                    "history",
                    vssFile.getVssPath(),
                    "-N",
                    "-O" + tmpfile.getAbsolutePath()
            }));
            if (!tmpfile.exists())
                throw new VssException("Cannot get history of VSS file: " + vssFile.getVssPath());
            historyCache.put(key, tmpfile);
            VssUtil.deleteTempFile(tmpfile);
            return historyCache.getFile(key);
        } catch (IOException e) {
            throw new VssException(e);
        }
    }

    /**
     * Checkout file revision
     *
     * @param revision
     * @return path to checked out file
     */
    public File checkout(VssFileRevision revision) {
        try {
            RevisionFileCacheKey cacheKey = new RevisionFileCacheKey(revision);
            if (checkoutCache.containsKey(cacheKey)) {
                LOG.debug("Cache hit");
                return checkoutCache.getFile(cacheKey);
            }
            File file=null;
            if(config.useComApi)
            {
            	file = comApiCheckout(revision);
            }
            else
            {
            	file = pureCheckout(revision);
            }
            if (file != null) {
                checkoutCache.put(cacheKey, file);
                VssUtil.deleteTempFile(file);
                return checkoutCache.getFile(cacheKey);
            } else {
                return null;
            }
        } catch (IOException e) {
            throw new VssException(e);
        }
    }

    private File pureCheckout(VssFileRevision revision) {
        String name = ((VssFile) revision.getModelFile()).getFilename();
        File file = new File(config.getTempDir(), name);
        VssUtil.deleteTempFile(file);
        file.getParentFile().mkdirs();
        String vssPath = ((VssFile) revision.getModelFile()).getVssPath();
        executeCommand(new VssExec(new String[]{
                config.getExecutable(),
                "get",
                vssPath,
                "-V" + revision.getNumber(),
                "-N",
                 "-I-Y"
        }));

        if (!file.exists()) {
            LOG.error("can't retrieve file " + vssPath + " version " + revision.getNumber()
                    + " (file is not exist after checkout:" + file.getAbsolutePath() + " )");
            return null;
        }
        return file;
    }

    /**
     * Execute "vss dir" command for project and save result to temp file
     * (list of all the files and projects in the specified project)
     *
     * @param projectPath
     * @return temp file with filelist (in VSS "dir" format)
     */
    public File listFilesInProject(String projectPath) {
        try {
            String key = FILES_IN_PROJECT + projectPath;
            if (historyCache.containsKey(key)) {
                return historyCache.getFile(key);
            }

            File tmpfile = new File(config.getTempDir(), TMPFILE);
            VssUtil.deleteTempFile(tmpfile);
            executeCommand(new VssExec(new String[]{
                    config.getExecutable(),
                    "dir",
                    projectPath,
                    "-N",
                    "-O" + tmpfile.getAbsolutePath()}));
            if (!tmpfile.exists())
                throw new VssException("Cannot list files in VSS project: " + projectPath);
            historyCache.put(key, tmpfile);
            VssUtil.deleteTempFile(tmpfile);
            return historyCache.getFile(key);
        } catch (IOException e) {
            throw new VssException(e);
        }
    }

    /**
     * Execute command
     *
     * @param exec
     */
    private void executeCommand(VssExec exec) {
        exec.setEnv(
                VssUtil.loadVSSEnvironment(
                        config.getPath(),
                        config.getUsername(),
                        config.getPassword()));
        exec.setWorkdir(config.getTempDir());
        exec.setVerboseExec(config.isVerboseExec());
        exec.setEnconding(config.getLogEncoding());
        exec.exec();

        if (exec.getErrorCode() != 0) {
            if (exec.getErrorCode() == VssExec.ERROR_EXCEPTION)
                throw new VssException("error during execution command "
                        + Util.toString(exec.getCmd(), " ") + ", exception caught", exec.getException());
            else if (exec.getErrorCode() == VssExec.ERROR_AUTH_FAILED)
                throw new VssException("error during execution command "
                        + Util.toString(exec.getCmd(), " ")
                        + ": wrong username or password");
            else
                throw new VssException("error during execution command "
                        + Util.toString(exec.getCmd(), " "));
        }
    }
    
    /// COM API PART =============================================================================
    
    /**
     * Check Out (source safe GET) a file using VSS COM API
     */
    private File comApiCheckout(VssFileRevision revision) 
    {
    	String name = ((VssFile) revision.getModelFile()).getFilename();
      File file = new File(config.getTempDir(), name);
      VssUtil.deleteTempFile(file);
      file.getParentFile().mkdirs();
      String vssPath = ((VssFile) revision.getModelFile()).getVssPath();
      VssComApi vssComApi = new VssComApi(config, config.getUsername(), config.getPassword());
      vssComApi.getVersion( file, vssPath, new Integer(revision.getNumber()).intValue() );
      if (!file.exists()) 
      {
        LOG.error("can't retrieve file " + vssPath + " version " + revision.getNumber()
                + " (file is not exist after checkout:" + file.getAbsolutePath() + " )");
        return null;
      }
      return file;
      
    }
    
    /**
     * Get file or directory history using VSS COM API
     * @param vssFile
     */
    public void getComApiFileHistory(VssFile vssFile) 
    {
    	String vssPath = vssFile.getVssPath();
      VssComApi vssComApi = new VssComApi(config, config.getUsername(), config.getPassword());
      Collection versions = new ArrayList();
      boolean retval = vssComApi.getListVersion( vssFile, versions );
      if (!retval) 
      {
        LOG.error("can't retrieve file history" + vssPath);        
      }
      else
      {
      	vssComApi.prepareRevisions(vssFile, versions);
      }
    }

    /**
     * Get directory (project) content using VSS COM API
     * @param vssFile
     */
    public void getComApiDirProject( VssProject vssProject )
		{
			String vssPath = vssProject.getVssPath();
      VssComApi vssComApi = new VssComApi(config, config.getUsername(), config.getPassword());
      Collection projectContent = new ArrayList();
      boolean retval = vssComApi.getDirProject( vssProject, projectContent );
      if (!retval) 
      {
        LOG.error("can't retrieve directory content" + vssPath);        
      }
      else
      {
      	vssComApi.prepareProjectContent(vssProject, projectContent);
      }
		}
}

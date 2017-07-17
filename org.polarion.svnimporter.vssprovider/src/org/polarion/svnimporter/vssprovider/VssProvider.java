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
package org.polarion.svnimporter.vssprovider;

import org.polarion.svnimporter.common.IContentRetriever;
import org.polarion.svnimporter.common.IProvider;
import org.polarion.svnimporter.common.ISvnModel;
import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.common.Util;
import org.polarion.svnimporter.common.ZeroContentRetriever;
import org.polarion.svnimporter.svnprovider.SvnModel;
import org.polarion.svnimporter.vssprovider.internal.Vss;
import org.polarion.svnimporter.vssprovider.internal.VssConfig;
import org.polarion.svnimporter.vssprovider.internal.VssContentRetriever;
import org.polarion.svnimporter.vssprovider.internal.VssHistoryParser;
import org.polarion.svnimporter.vssprovider.internal.VssTransform;
import org.polarion.svnimporter.vssprovider.internal.model.VssFile;
import org.polarion.svnimporter.vssprovider.internal.model.VssFileRevision;
import org.polarion.svnimporter.vssprovider.internal.model.VssModel;
import org.polarion.svnimporter.vssprovider.internal.model.VssProject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

/**
 * @author <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class VssProvider implements IProvider {
    private static final Log LOG = Log.getLog(VssProvider.class);

    private VssConfig config;
    private boolean incrementalWarning = false;
    private Vss vss;

    /**
     * Configure provider
     *
     * @param properties
     */
    public void configure(Properties properties) {
        config = new VssConfig(properties);
    }

    protected Vss getVss() {
        if(vss==null) {
            vss = new Vss();
            vss.init(config,
                    new File(config.getTempDir(), "history-cache"),
                    new File(config.getTempDir(), "checkout-cache"), false);
        }
        return vss;
    }

    /**
     * Validate configuration
     *
     * @return
     */
    public boolean validateConfig() {
        return config.validate();
    }

    /**
     * Get provider's config
     *
     * @return
     */
    public VssConfig getConfig() {
        return config;
    }

    /**
     * Log environment information
     */
    public void logEnvironmentInformation() {
        config.logEnvironmentInformation();
    }

    /**
     * Build svn model
     *
     * @return
     */
    private VssModel buildVssModel() {
        VssProject project = new VssProject();
        project.setName(config.getProject());
        project.setRoot(true);
        buildTree(config.getProject(), project);
        loadHistory(project);

        VssModel vssModel = new VssModel();
        vssModel.loadFilesFromProject(project);
        vssModel.finishModel();

        LOG.info("VSS model has been created.");
        vssModel.printSummary();

        return vssModel;
    }

    public void listFiles(PrintStream out) {
        Collection files = buildVssModel().getFiles().keySet();
        for (Iterator i = files.iterator(); i.hasNext();)
            out.println(i.next());
    }

    /**
     * Transform vss model to svn model
     *
     * @return
     */
    public ISvnModel buildSvnModel() {
        VssModel vssModel = buildVssModel();
        if (incrementalWarning)
            LOG.warn("unsupported operations(delete,rename,share,branch) detected in project, incremental dump can be broken");
        VssTransform tr = new VssTransform(this);
        SvnModel svnModel = tr.transform(vssModel);
        LOG.info("Svn model has been created");
        LOG.info("total number of revisions in svn model: " + svnModel.getRevisions().size());
        return svnModel;
    }

    /**
     * Create content retriever for revision
     *
     * @param revision
     * @return
     */
    public IContentRetriever createContentRetriever(VssFileRevision revision) {
        if (config.isUseOnlyLastRevisionContent() && !revision.isLastRevision()) {
            return ZeroContentRetriever.INSTANCE;
        } else {
            return new VssContentRetriever(this, revision);
        }
    }

    /**
     * Load version history for all files in project and all subprojects
     *
     * @param vssProject
     */
    private void loadHistory(VssProject vssProject) {
        if (!incrementalWarning)
            checkDirHistory(vssProject);

        for (Iterator i = vssProject.getFiles().iterator(); i.hasNext();) {
            VssFile file = (VssFile) i.next();
            loadHistory(file);
        }
        for (Iterator i = vssProject.getSubprojects().iterator(); i.hasNext();) {
            VssProject project = (VssProject) i.next();
            loadHistory(project);
        }
    }

    /**
     * Check project for existing of unsupported operations
     *
     * @param project
     */
    private void checkDirHistory(VssProject project) {
        try {
            File dirHistory = getVss().getDirHistory(project);
            BufferedReader r = Util.openReader(dirHistory, config.getLogEncoding());
            try {
                String s;
                while ((s = r.readLine()) != null) {
                    if (s.endsWith(" recovered")
                            || s.endsWith(" destroyed")
                            || s.endsWith(" branched")
                            || s.endsWith(" shared")
                            || s.indexOf(" renamed to ") != -1
                            ) {
                        incrementalWarning = true;
                        break;
                    }
                }
            } finally {
                r.close();
            }
        } catch (IOException e) {
            throw new VssException(e);
        }
    }

    /**
     * Load version history for file
     *
     * @param vssFile
     */
    private void loadHistory(VssFile vssFile) {
    	if(config.useComApi)
      {
    		getVss().getComApiFileHistory(vssFile);
      }
    	else
    	{
        File historyFile = getVss().getFileHistory(vssFile);
        VssHistoryParser p = new VssHistoryParser(config);
        p.parseFileHistory(historyFile, vssFile);
    	}
    }

    /**
     * Build project tree (tree of VssProject objects)
     *
     * @param projectPath
     * @param vssProject
     */
    private void buildTree(String projectPath, VssProject vssProject) {
    	if(config.useComApi)
      {
    		getVss().getComApiDirProject(vssProject);
      }
    	else
    	{
    		File file = getVss().listFilesInProject(projectPath);

	      BufferedReader in = Util.openReader(file, config.getLogEncoding());
	      try {
	          parseTree(in, vssProject);
	      } finally {
	          Util.close(in);
	      }
    	}
    	
      for (Iterator i = vssProject.getSubprojects().iterator(); i.hasNext();) {
          VssProject subproject = (VssProject) i.next();
          String path = projectPath + "/" + subproject.getName();
          buildTree(path, subproject);
      }
    }

    /**
     * Parse output of "ss.exe dir" command
     *
     * @param in
     * @param vssProject
     */
    private void parseTree(BufferedReader in, VssProject vssProject) {
        try {
            String line;
            boolean start = false;
            while ((line = in.readLine()) != null) {
                if (!start) {
                    if (line.startsWith("$") && line.endsWith(":")) {
                        start = true;
                        continue;
                    }
                } else {
                    if (line.length() == 0)
                        break;
                    if (line.startsWith("$")) {
                    	
                        String projectName = line.substring(1);
                        if ( config.getProjectsToIgnore().contains( projectName ) )
              					{
              						// Ignore list!
              						LOG.warn( "Ignore project " + projectName + " found in : " + vssProject.getVssPath() );
              						continue;
              					}
                        
                        VssProject newProject = new VssProject();
                        newProject.setName(projectName);
                        newProject.setParent(vssProject);
                        vssProject.addSubproject(newProject);
                        
                    } else 
                    {
                        if (line.startsWith("No items found under $")) {
                            continue;
                        }                        
                        String filename = line;
                        String parent = vssProject.getVssPath().substring(config.getProject().length());
                        if (parent.length() > 0) {
                            if (parent.startsWith("/"))
                                parent = parent.substring(1);
                            if (!parent.endsWith("/"))
                                parent += "/";
                        }
                        String path = parent + filename;
                        VssFile newFile = new VssFile(path, filename);
                        newFile.setParent(vssProject);
                        vssProject.addFile(newFile);
                    }
                }
            }
        } catch (IOException e) {
            throw new VssException("readLine", e);
        }
    }

    public File checkout(VssFileRevision revision) {
        return vss.checkout(revision);
    }

    /**
     * Cleanup
     */
    public void cleanup() {
        LOG.debug("cleanup");
        File tempDir = config.getTempDir();
        if (!Util.delete(tempDir)) {
            LOG.error("can't delete temp dir: " + tempDir.getAbsolutePath());
        }
    }
}

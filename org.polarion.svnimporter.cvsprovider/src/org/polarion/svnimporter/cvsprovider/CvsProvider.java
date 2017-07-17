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

package org.polarion.svnimporter.cvsprovider;

import org.netbeans.lib.cvsclient.Client;
import org.netbeans.lib.cvsclient.LsCommandNotSupported;

import org.netbeans.lib.cvsclient.admin.StandardAdminHandler;
import org.netbeans.lib.cvsclient.command.Command;
import org.netbeans.lib.cvsclient.command.CommandAbortedException;
import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.lib.cvsclient.command.FileInfoContainer;
import org.netbeans.lib.cvsclient.command.KeywordSubstitutionOptions;
import org.netbeans.lib.cvsclient.command.checkout.CheckoutCommand;
import org.netbeans.lib.cvsclient.command.log.LogInformation;
import org.netbeans.lib.cvsclient.command.log.LsCommand;
import org.netbeans.lib.cvsclient.command.log.RlogCommand;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.lib.cvsclient.event.CVSListener;
import org.netbeans.lib.cvsclient.event.FileInfoEvent;
import org.polarion.svnimporter.cvsprovider.internal.model.CvsModel;
import org.polarion.svnimporter.cvsprovider.internal.model.CvsRevision;
import org.polarion.svnimporter.cvsprovider.internal.CvsConfig;
import org.polarion.svnimporter.cvsprovider.internal.CvsLogListener;
import org.polarion.svnimporter.cvsprovider.internal.CvsUtil;
import org.polarion.svnimporter.cvsprovider.internal.CvsTransform;
import org.polarion.svnimporter.cvsprovider.internal.CvsContentRetriever;
import org.polarion.svnimporter.svnprovider.SvnCombinedModel;
import org.polarion.svnimporter.svnprovider.SvnModel;
import org.polarion.svnimporter.common.IProvider;
import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.common.ISvnModel;
import org.polarion.svnimporter.common.IContentRetriever;
import org.polarion.svnimporter.common.ZeroContentRetriever;
import org.polarion.svnimporter.common.Util;
import org.polarion.svnimporter.common.ProviderConfig;

import java.io.File;
import java.io.PrintStream;
import java.util.Properties;
import java.util.List;
import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;

/**
 *
 * @author <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class CvsProvider implements IProvider {
    private static final Log LOG = Log.getLog(CvsProvider.class);

    /**
     * Provider's config
     */
    private CvsConfig config;

    public CvsProvider() {
    }

    /**
     * Configure provider
     *
     * @param properties
     */
    public void configure(Properties properties) {
        config = new CvsConfig(properties);
        LOG.debug("using cvs repository " + config.getCvsRoot());
    }

    /**
     * Validate configuration
     *
     * @return false if configuration has errors
     */
    public boolean validateConfig() {
        return config.validate();
    }

    public ProviderConfig getConfig() {
        return config;
    }


    /**
     * Build csv model
     *
     * @return
     */
    public CvsModel buildCvsModel() {
        LOG.info("build cvs model for module "+config.getModuleName());
        RlogCommand command = new RlogCommand();
        command.setModule(config.getModuleName());
        command.setRecursive(true);
        command.setDefaultBranch(config.isOnlyTrunk());

        final CvsModel cvsModel = new CvsModel();
        cvsModel.setOnlyTrunk(config.isOnlyTrunk());

        CVSListener listener = new CvsLogListener() {
            public void addLogInfo(LogInformation logInformation) {
                String rcsFilename = logInformation.getRepositoryFilename();
                String relative = CvsUtil.getRelativeFilename(config.getRepositoryPath(), rcsFilename, config.getModuleName());
                if (relative == null) {
                    LOG.error("Skip file: " + logInformation.getRepositoryFilename());
                } else {
                    LOG.debug("Process history: " + relative);
                    cvsModel.addLogInfo(relative, logInformation);
                }
            }
        };
        if (!execCommand(command, listener))
            throw new CvsException("failed to exec CVS log command");

        cvsModel.finishModel();

        //LOG.info("CVS model has been created.");
        cvsModel.printSummary();
        return cvsModel;
    }

    /**
     * Exec cvs command
     *
     * @param command
     * @param listener
     * @return
     */
    private boolean execCommand(Command command, CVSListener listener) {
        Client client = new Client(config.createConnection(), new StandardAdminHandler());
        client.setLocalPath(config.getTempDir().getAbsolutePath());
        if (listener != null)
            client.getEventManager().addCVSListener(listener);
        try {
            LOG.debug("Execute command: " + command.getCVSCommand());
            return client.executeCommand(command, config.getCvsGlobalOptions());
        } catch (Exception e) {
            LOG.error("failed to exec command '" + command.getCVSCommand() + "' : " + e.getMessage());
            throw new CvsException("failed to exec " + command, e);
        } finally {
            CvsUtil.close(client);
        }
    }

    /**
     * List repository's files to stream (one file per line)
     *
     * @param out
     */
    public void listFiles(PrintStream out) {
        if (config.isConvertAllModules()) {
            List modules = getModulesList();
            //SvnCombinedModel combinedModel = new SvnCombinedModel();
            //combinedModel.setSvnimporterUsername(config.getSvnimporterUsername());

            for (int i = 0; i < modules.size(); i++) {
                String moduleName = (String) modules.get(i);
                LOG.info("process module: " + moduleName);
                CvsProvider provider = new CvsProvider();
                provider.config = config.cloneConfig(moduleName,
                        moduleName + "/" + config.getTrunkPath(),
                        moduleName + "/" + config.getBranchesPath(),
                        moduleName + "/" + config.getTagsPath(), false);

                CvsModel cvsModel = provider.buildCvsModel();
                for (Iterator j = cvsModel.getFiles().keySet().iterator(); j.hasNext();) {
                    out.println(moduleName + "/" + j.next());
                }
            }
        } else {
            Collection files = buildCvsModel().getFiles().keySet();
            for (Iterator i = files.iterator(); i.hasNext();)
                out.println(i.next());
        }
    }

    /**
     * Build svn model by cvs model
     *
     * @return
     */
    public ISvnModel buildSvnModel() {
        if (config.isConvertAllModules()) {
            List modules = getModulesList();
            SvnCombinedModel combinedModel = new SvnCombinedModel();
            combinedModel.setSvnimporterUsername(config.getSvnimporterUsername());

            for (int i = 0; i < modules.size(); i++) {
                String moduleName = (String) modules.get(i);
                LOG.info("process module: " + moduleName);
                CvsProvider provider = new CvsProvider();
                provider.config = config.cloneConfig(moduleName,
                        moduleName + "/" + config.getTrunkPath(),
                        moduleName + "/" + config.getBranchesPath(),
                        moduleName + "/" + config.getTagsPath(), false);

                ISvnModel iSvnModel = provider.buildSvnModel();
                combinedModel.addModel(moduleName, iSvnModel);
            }
            return combinedModel;
        } else {
            CvsTransform cvsTransform = new CvsTransform(this);
            CvsModel cvsModel = buildCvsModel();
            SvnModel svnModel = cvsTransform.transform(cvsModel);
            LOG.info("Svn model has been created");
            LOG.info("total number of revisions in svn model: " + svnModel.getRevisions().size());
            return svnModel;
        }
    }

    /**
     * Create content retriever for revision
     *
     * @param revision
     * @return
     */
    public IContentRetriever createContentRetriever(CvsRevision revision) {
        if (getConfig().isUseOnlyLastRevisionContent() && !revision.isLastRevision()) {
            return ZeroContentRetriever.INSTANCE;
        } else {
            return new CvsContentRetriever(this, revision);
        }
    }

    /**
     * Checkout file revision to local file (path)
     *
     * @param revision
     * @return
     */
    public File checkout(CvsRevision revision) {
        File tempDir = config.getTempDir();
        String path = revision.getPath();
        //String filename = CvsUtil.getFilename(path);
        String cvsPath = CvsUtil.getCvsPath(config.getModuleName(), path);
        
        String revisionNumber = revision.getNumber();


        LOG.debug("Checkout \"" + path + "\" rev." + revisionNumber);
        Client checkoutClient = new Client(config.createConnection(), new StandardAdminHandler());
        checkoutClient.setLocalPath(config.getTempDir().getAbsolutePath());
        
        try {
            //String checkoutDirectory = path + "/" + revisionNumber;
            File checkoutFile = new File(new File(tempDir, config.getModuleName()), path);
            //System.out.println("checkoutFile = " + checkoutFile.getAbsolutePath());
            if (checkoutFile.exists() && !checkoutFile.delete()) {
                throw new CvsException("Can't delete stale file: " + checkoutFile.getAbsolutePath());
            } else {
                checkoutFile.getParentFile().mkdirs();
            }
            //System.out.println("checkoutDirectory = " + checkoutDirectory);
            CheckoutCommand command = new CheckoutCommand(false, cvsPath);
            command.setCheckoutByRevision(revisionNumber);
            command.setKeywordSubst(KeywordSubstitutionOptions.BINARY);
            //command.setCheckoutDirectory(tempDir.getAbsolutePath());

            String revisionInfo = "\"" + path + "\" rev." + revisionNumber + " RCS State=" + revision.getRcsState();
            try {
                checkoutClient.executeCommand(command, config.getCvsGlobalOptions());
                if (!checkoutFile.exists()) {
                    LOG.warn("Can't checkout " + revisionInfo);
                    return null;
                }
                return checkoutFile;
            } catch (CommandAbortedException e) {
                LOG.error("Exception caught during checkout " + revisionInfo, e);
                return null;
            } catch (CommandException e) {
                LOG.error("Exception caught during checkout " + revisionInfo, e);
                return null;
            } catch (AuthenticationException e) {
                LOG.error("Exception caught during checkout " + revisionInfo, e);
                return null;
            }
        } finally {
            CvsUtil.close(checkoutClient);
        }
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

    /**
     * Log environment information
     */
    public void logEnvironmentInformation() {
        config.logEnvironmentInformation();
    }

    /**
     * Get modules list (using 'cvs ls' command)
     */
    public List getModulesList() {
        final List modules = new ArrayList();

        LsCommand command = new LsCommand();
        Client client = new Client(config.createConnection(), new StandardAdminHandler());
        client.setLocalPath(config.getTempDir().getAbsolutePath());
        client.getEventManager().addCVSListener(new CvsLogListener() {
            public void addLogInfo(LogInformation logInformation) {
            }

            public void fileInfoGenerated(FileInfoEvent e) {
                FileInfoContainer infoContainer = e.getInfoContainer();
                if (infoContainer instanceof LogInformation) {
                    LogInformation l = (LogInformation) infoContainer;
                    String moduleName = l.getRepositoryFilename();
                    if (isValidModuleName(moduleName)) {
                        modules.add(moduleName);
                    }
                }
            }
        });
        try {
            if (!client.executeCommand(command, config.getCvsGlobalOptions()))
                throw new CvsException("failed to exec CVS rls command");
        } catch (LsCommandNotSupported e) {
            throw new CvsException("The CVS server is not supporting the 'ls' command, \n"
                    + "hence you can't use the '*' value for the 'cvs.modulename' option for that CVS server\n");
        } catch (Exception e) {
            throw new CvsException("failed to exec the 'cvs ls' command: " + e.getMessage());
        } finally {
            CvsUtil.close(client);
        }
        return modules;
    }

    /**
     * Validate module name
     *
     * @param moduleName
     * @return true if module name is valid
     */
    protected boolean isValidModuleName(String moduleName) {
        // Sometimes cvs returns module name like "? proj/file.txt" which is not valid module name
        return moduleName != null && !"CVSROOT".equals(moduleName)
                && ! moduleName.startsWith("? ");
    }
}

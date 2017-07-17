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

package org.polarion.svnimporter.cvsprovider.internal;

import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.log.LogInformation;
import org.netbeans.lib.cvsclient.connection.Connection;
import org.netbeans.lib.cvsclient.connection.PServerConnection;
import org.netbeans.lib.cvsclient.connection.StandardScrambler;
import org.netbeans.lib.cvsclient.connection.LocalConnection;
import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.common.ProviderConfig;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Properties;

/**
 * @author <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class CvsConfig extends ProviderConfig implements Cloneable, ICvsConf {
    private static final Log LOG = Log.getLog(CvsConfig.class);

    private String userName;
    private String password;
    private String hostname;
    private String repositoryPath;
    private String moduleName;
    private File tempDir;
    private GlobalOptions cvsGlobalOptions;
    private String logDateFormatString;
    private boolean convertAllModules;
    private boolean localConnection;

    public CvsConfig(Properties properties) {
        super(properties);
    }

    public boolean isLocalConnection() {
        return localConnection;
    }

    public void setLocalConnection(boolean localConnection) {
        this.localConnection = localConnection;
    }

    protected void configure() {
        super.configure();
        userName = getStringProperty("cvs.username", true);
        password = getStringProperty("cvs.password", false);
        hostname = getStringProperty("cvs.hostname", true);
        localConnection = getBooleanProperty("cvs.localconnection");
        repositoryPath = getStringProperty("cvs.repository", true);
        if (localConnection) {
            File rp = new File(repositoryPath);
            if (!rp.isAbsolute()) {
                try {
                    repositoryPath = rp.getCanonicalPath();
                } catch (IOException e) {
                }
            }
        }

        parseModuleName();
        tempDir = getTempDir("cvs.tempdir");
        logDateFormatString = getStringProperty("cvs.logdateformat", false);
        SimpleDateFormat logDateFormat = getDateFormat(logDateFormatString, null);
        if (logDateFormat != null) {
            LogInformation.DATE_FORMAT = logDateFormat;
        }
    }

    private void parseModuleName() {
        moduleName = getStringProperty("cvs.modulename", true);
        if (moduleName != null && "*".equals(moduleName)) {
//			moduleName = "";
            convertAllModules = true;
        }
    }

    public boolean isConvertAllModules() {
        return convertAllModules;
    }

    public String getRepositoryPath() {
        return repositoryPath;
    }

    public String getCvsRoot() {
        if (localConnection) {
            return repositoryPath;
        } else {
            return ":pserver:" + userName + "@" + hostname + ":" + repositoryPath;
        }
    }

    public GlobalOptions getCvsGlobalOptions() {
        if (cvsGlobalOptions == null) {
            cvsGlobalOptions = new GlobalOptions();
            cvsGlobalOptions.setCVSRoot(getCvsRoot());
        }
        return cvsGlobalOptions;
    }

    /**
     * Create pserver connection
     *
     * @return
     */
    public Connection createConnection() {
        if (!localConnection) {
            PServerConnection connection;
            connection = new PServerConnection();
            connection.setUserName(userName);
            connection.setEncodedPassword(StandardScrambler.getInstance().scramble(password));
            connection.setHostName(hostname);
            connection.setRepository(repositoryPath);
            return connection;
        } else {
            LocalConnection localConnection = new LocalConnection();
            localConnection.setRepository(repositoryPath);
            return localConnection;
        }
    }

    /* (non-Javadoc)
	 * @see org.polarion.svnimporter.cvsprovider.internal.ICvsConf#getTempDir()
	 */
    public File getTempDir() {
        return tempDir;
    }

    public String getModuleName() {
        return moduleName;
    }

    protected void printError(String error) {
        LOG.error(error);
    }

    /**
     * Log environment information
     */
    public void logEnvironmentInformation() {
        LOG.info("*** CVS provider configuration ***");
        LOG.info("username = \"" + userName + "\"");
        LOG.info("password = \"" + "*******" + "\"");
        LOG.info("hostname = \"" + hostname + "\"");
        LOG.info("repository path = \"" + repositoryPath + "\"");
        LOG.info("module name = \"" + moduleName + "\"");
        LOG.info("local connection = \"" + localConnection + "\"");
        LOG.info("temp dir = \"" + tempDir.getAbsolutePath() + "\"");
        LOG.info("log date format = \"" + logDateFormatString + "\"");
        super.logEnvironmentInformation();
    }

    public CvsConfig cloneConfig(String newModuleName, String newTrunkPath,
                                 String newBranchesPath, String newTagsPath,
                                 boolean convertAllModules) {
        try {
            CvsConfig newConfig = (CvsConfig) this.clone();

            newConfig.moduleName = newModuleName;
            newConfig.setTrunkPath(newTrunkPath);
            newConfig.setBranchesPath(newBranchesPath);
            newConfig.setTagsPath(newTagsPath);
            newConfig.convertAllModules = convertAllModules;

            return newConfig;

        } catch (CloneNotSupportedException e) {
            LOG.error("can't clone config", e);
            return null;
        }
    }
}


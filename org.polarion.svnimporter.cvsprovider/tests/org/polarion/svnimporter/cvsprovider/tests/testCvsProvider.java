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

package org.polarion.svnimporter.cvsprovider.tests;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.polarion.svnimporter.common.BaseProviderTest;
import org.polarion.svnimporter.common.ISvnModel;
import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.common.Util;
import org.polarion.svnimporter.cvsprovider.CvsProvider;
import org.polarion.svnimporter.cvsprovider.CvsRcsProvider;
import org.polarion.svnimporter.svnprovider.SvnAdmin;
import org.polarion.svnimporter.svnprovider.SvnDump;
import org.polarion.svnimporter.svnprovider.SvnHistoryHelper;

/**
 * Important note: This tests uses special type of javacvs connection "Local Connection"
 * which runs external cvs process, therefore you must have "cvs" command available in $PATH
 *
 * @author <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class testCvsProvider extends BaseProviderTest {
    private static final Log log = Log.getLog(testCvsProvider.class);

    public void init() throws Exception {
        super.init();
        testsDir = new File(commonConfig.getProperty("cvs.tests"));

        svnAdmin = new SvnAdmin();
        svnAdmin.configure(commonConfig);
        if (!svnAdmin.validateConfig()) {
            throw new Exception("SvnAdmin configuration is not valid");
        }

        incrDump = new SvnDump(incrDumpFile.getPath());
        fullDump = new SvnDump(fullDumpFile.getPath());
    }

    protected void initProvider(File testDir) throws Exception {
        CvsProvider p = new CvsProvider2();

        Properties provCfg = Util.loadProperties(
                new File(testDir.getParentFile(), "test.properties"));

        File f = new File(".");
        System.out.println(this.getClass().getClassLoader().getResource(".").getFile());
        
        
        
        provCfg.setProperty("cvsrcs.tempdir", tempDir.getPath());
        Util.appendProperties(commonConfig, provCfg);
        provCfg.setProperty("cvsrcs.repository_path", testDir.getAbsolutePath());
        
        String currentPath = this.getClass().getClassLoader().getResource(".").getFile();
        provCfg.setProperty("cvsrcs.rlog_command",currentPath + "bin/rlog");
        provCfg.setProperty("cvsrcs.co_command",currentPath + "bin/co");

        p.configure(provCfg);

        log.info("Provider config:");
        p.logEnvironmentInformation();
        if (!p.validateConfig()) {
            throw new Exception("Provider configuration is not valid");
        }
        provider = p;
    }

    protected void saveIncrHistory(ISvnModel model) throws Exception {
        SvnHistoryHelper.saveIncrHistory(model, incrHistoryFile.getAbsolutePath());
    }

    protected ISvnModel createIncrementalModel(ISvnModel model) throws IOException, Exception {
        return SvnHistoryHelper.createIncrModel(model, incrHistoryFile.getAbsolutePath());
    }


    private class CvsProvider2 extends CvsRcsProvider {
        protected boolean isValidModuleName(String moduleName) {
            return super.isValidModuleName(moduleName)
                    && !moduleName.equalsIgnoreCase(".svn")
                    && !moduleName.equalsIgnoreCase("README");
        }
    }
}

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
package org.polarion.svnimporter.pvcsprovider.tests;

import org.polarion.svnimporter.common.BaseProviderTest;
import org.polarion.svnimporter.common.IContentRetriever;
import org.polarion.svnimporter.common.ISvnModel;
import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.common.Util;
import org.polarion.svnimporter.common.ZeroContentRetriever;
import org.polarion.svnimporter.pvcsprovider.PvcsProvider;
import org.polarion.svnimporter.pvcsprovider.internal.model.PvcsRevision;
import org.polarion.svnimporter.svnprovider.SvnAdmin;
import org.polarion.svnimporter.svnprovider.SvnDump;
import org.polarion.svnimporter.svnprovider.SvnHistoryHelper;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * Test pvcs provider.
 *
 * @author <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class testPvcsProvider extends BaseProviderTest {
    private static final Log log = Log.getLog(testPvcsProvider.class);

    protected void init() throws Exception {
        super.init();
        testsDir = new File(commonConfig.getProperty("pvcs.tests"));

        svnAdmin = new SvnAdmin();
        svnAdmin.configure(commonConfig);
        if (!svnAdmin.validateConfig()) {
            throw new Exception("SvnAdmin configuration is not valid");
        }

        incrDump = new SvnDump(incrDumpFile.getPath());
        fullDump = new SvnDump(fullDumpFile.getPath());
    }

    protected void initProvider(File testDir) throws Exception {

        PvcsProvider p = new PvcsProvider2(
                new File(testDir, "vlog.tmp"),
                new File(testDir, "files.tmp"));
        p.setTestMode(true);
        
        Properties provCfg = Util.loadProperties(new File(testDir, "test.properties"));
        provCfg.setProperty("pvcs.tempdir", tempDir.getPath());
        provCfg.setProperty("pvcs.checkouttempdir", tempDir.getPath());
        Util.appendProperties(commonConfig, provCfg);
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

    private class PvcsProvider2 extends PvcsProvider {
        private File vlogFile;
        private File filesFile;

        public PvcsProvider2(File vlogFile, File filesFile) {
            this.vlogFile = vlogFile;
            this.filesFile = filesFile;
        }

        protected File getVlogFile() {
            return vlogFile;
        }

        protected File getFilesFile() {
            return filesFile;
        }

        public IContentRetriever createContentRetriever(PvcsRevision revision) {
            return ZeroContentRetriever.INSTANCE;
        }

        protected void getLogInformation(File targetFilesFile, File targetVlogFile) {
            ;
        }
    }
}

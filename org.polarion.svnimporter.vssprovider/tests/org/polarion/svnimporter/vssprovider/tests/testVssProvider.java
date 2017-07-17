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

package org.polarion.svnimporter.vssprovider.tests;

import org.polarion.svnimporter.common.BaseProviderTest;
import org.polarion.svnimporter.common.IContentRetriever;
import org.polarion.svnimporter.common.ISvnModel;
import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.common.Util;
import org.polarion.svnimporter.common.ZeroContentRetriever;
import org.polarion.svnimporter.svnprovider.SvnAdmin;
import org.polarion.svnimporter.svnprovider.SvnDump;
import org.polarion.svnimporter.svnprovider.SvnHistoryHelper;
import org.polarion.svnimporter.vssprovider.VssProvider;
import org.polarion.svnimporter.vssprovider.internal.Vss;
import org.polarion.svnimporter.vssprovider.internal.model.VssFileRevision;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * @author <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class testVssProvider extends BaseProviderTest {
    private static final Log log = Log.getLog(testVssProvider.class);

    protected void init() throws Exception {
        super.init();
        testsDir = new File(commonConfig.getProperty("vss.tests"));

        svnAdmin = new SvnAdmin();
        svnAdmin.configure(commonConfig);
        if (!svnAdmin.validateConfig()) {
            throw new Exception("SvnAdmin configuration is not valid");
        }

        incrDump = new SvnDump(incrDumpFile.getPath());
        fullDump = new SvnDump(fullDumpFile.getPath());
    }

    protected void initProvider(File testDir) throws Exception {

        VssProvider p = new VssProvider2(testDir);
        Properties provCfg = Util.loadProperties(new File(testDir, "test.properties"));
        provCfg.setProperty("vss.tempdir", tempDir.getPath());
        provCfg.setProperty("vss.project", "$/" + testDir.getName());

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

    private class VssProvider2 extends VssProvider {
        private Vss vss;
        private File testDataDir;

        public VssProvider2(File testDataDir) {
            super();
            this.testDataDir = testDataDir;
        }

        protected Vss getVss() {
            if (vss == null) {
                vss = new Vss();
                vss.init(getConfig(),
                        new File(testDataDir, "history-cache"),
                        new File(testDataDir, "checkout-cache"), true);
            }
            return vss;
        }

        public IContentRetriever createContentRetriever(VssFileRevision revision) {
            return ZeroContentRetriever.INSTANCE;
        }
    }
}

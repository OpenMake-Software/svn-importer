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
package org.polarion.svnimporter.ccprovider.tests;

import org.polarion.svnimporter.ccprovider.CCProvider;
import org.polarion.svnimporter.ccprovider.internal.CCHistoryParser;
import org.polarion.svnimporter.ccprovider.internal.model.CCModel;
import org.polarion.svnimporter.ccprovider.internal.model.CCRevision;
import org.polarion.svnimporter.common.BaseProviderTest;
import org.polarion.svnimporter.common.IContentRetriever;
import org.polarion.svnimporter.common.ISvnModel;
import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.common.Util;
import org.polarion.svnimporter.common.ZeroContentRetriever;
import org.polarion.svnimporter.svnprovider.SvnAdmin;
import org.polarion.svnimporter.svnprovider.SvnDump;
import org.polarion.svnimporter.svnprovider.SvnHistoryHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.text.SimpleDateFormat;

/**
 * @author <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class testCCProvider extends BaseProviderTest {
    private static final Log log = Log.getLog(testCCProvider.class);

    public void init() throws Exception {
        super.init();
        testsDir = new File(commonConfig.getProperty("cc.tests"));

        svnAdmin = new SvnAdmin();
        svnAdmin.configure(commonConfig);
        if (!svnAdmin.validateConfig()) {
            throw new Exception("SvnAdmin configuration is not valid");
        }

        incrDump = new SvnDump(incrDumpFile.getPath());
        fullDump = new SvnDump(fullDumpFile.getPath(), new SimpleDateFormat("yyyyMMdd_HHmmss"), 1);
    }

    protected void initProvider(File testDir) throws Exception {
        CCProvider p = new CCProvider2( new File(testDir, "history.txt"));

        Properties provCfg = Util.loadProperties(new File(testDir, "test.properties"));
        provCfg.setProperty("cc.tempdir", tempDir.getPath());
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


    private class CCProvider2 extends CCProvider {
        private File historyFile;

        public CCProvider2(File historyFile) {
            this.historyFile = historyFile;
        }

        public IContentRetriever createContentRetriever(CCRevision revision) {
            return ZeroContentRetriever.INSTANCE;
        }

        public File checkout(CCRevision revision) {
            return super.checkout(revision);
        }

        protected CCModel buildCCModel() {
            try {
                final CCHistoryParser historyParser = new CCHistoryParser();
                historyParser.setDateFormat(getConfig().getLogDateFormat());
                BufferedReader r = new BufferedReader(new FileReader(historyFile));
                String s;
                while ((s = r.readLine()) != null) {
                    historyParser.parse(s);
                }
                CCModel model = historyParser.getModel();
                model.finishModel();
                return model;
            } catch (IOException e) {
                throw new RuntimeException();
            }
        }
    }
}

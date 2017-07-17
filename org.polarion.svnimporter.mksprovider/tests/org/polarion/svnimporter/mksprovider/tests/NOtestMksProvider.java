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
package org.polarion.svnimporter.mksprovider.tests;

import org.polarion.svnimporter.common.BaseProviderTest;
import org.polarion.svnimporter.common.ISvnModel;
import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.common.Util;
import org.polarion.svnimporter.mksprovider.MksProvider;
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
public class NOtestMksProvider extends BaseProviderTest {
    private static final Log log = Log.getLog(NOtestMksProvider.class);

    private File testDir;
    
    protected void init() throws Exception {
        super.init();
        
        testsDir = new File(commonConfig.getProperty("mks.tests"));

        svnAdmin = new SvnAdmin();
        svnAdmin.configure(commonConfig);
        if (!svnAdmin.validateConfig()) {
            throw new Exception("SvnAdmin configuration is not valid");
        }

        incrDump = new SvnDump(incrDumpFile.getPath());
        fullDump = new SvnDump(fullDumpFile.getPath());
    }

    protected void initProvider(File testDir) throws Exception {

        this.testDir = testDir;
        MksProvider p = new MksProvider();
        Properties provCfg = Util.loadProperties(new File(testDir, "test.properties"));
        provCfg.setProperty("mks.tempdir", tempDir.getPath());
        provCfg.setProperty("record.mode", "playback");
        provCfg.setProperty("record.file", new File(testDir, "record.txt").getPath());

        Util.appendProperties(commonConfig, provCfg);
        p.configure(provCfg);

        log.info("Provider config:");
        p.logEnvironmentInformation();
        if (!p.validateConfig()) {
            throw new Exception("Provider configuration is not valid");
        }

        provider = p;
    }

    /* (non-Javadoc)
     * @see org.polarion.svnimporter.common.BaseProviderTest#getExpectedFullDumpFile()
     */
    protected File getExpectedFullDumpFile(boolean useFileCopy) {
        return new File(testDir, "fulldump." + (useFileCopy ? "copy" : "nocopy"));
    }

    /* At this point, mksProvider does not support incremental dumps */
    protected void buildIncrementalDump(ISvnModel model) throws Exception {
    }

    protected void saveIncrHistory(ISvnModel model) throws Exception {
        SvnHistoryHelper.saveIncrHistory(model, incrHistoryFile.getAbsolutePath());
    }

    protected ISvnModel createIncrementalModel(ISvnModel model) throws IOException, Exception {
        return SvnHistoryHelper.createIncrModel(model, incrHistoryFile.getAbsolutePath());
    }
}

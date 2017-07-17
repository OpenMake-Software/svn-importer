package org.polarion.svnimporter.common;

import junit.framework.TestCase;

import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * BaseProviderTest
 *
 * @author Fedor Zhigaltsov
 * @since 13.02.2006
 */
public abstract class BaseProviderTest extends TestCase {
    private static final Log log = Log.getLog(BaseProviderTest.class);

    protected File tempDir;

    protected File testsDir;
    protected Properties commonConfig;
    protected boolean disableCleanup;
    protected boolean testImport;
    protected IProvider provider;
    protected ISvnAdmin svnAdmin;

    protected ISvnDump incrDump;
    protected ISvnDump fullDump;

    protected File incrHistoryFile;
    protected File fullDumpFile;
    protected File incrDumpFile;
    protected File svnRepo;

    public void testJunit() throws Exception {
        runTests();
    }


    protected boolean isValidTestDir(File file) {
        return !file.getName().equalsIgnoreCase(".svn");
    }

    protected void init() throws Exception {
        commonConfig = Util.loadProperties("tests.properties");

        disableCleanup = "yes".equals(commonConfig.getProperty("disable_cleanup"));
        testImport = "yes".equals(commonConfig.getProperty("alltests.test_import"));

        tempDir = new File(System.getProperty("java.io.tmpdir"));
        String s;
        if ((s = commonConfig.getProperty("alltests.temp-dir")) != null) {
            tempDir = new File(s);
        }

        incrHistoryFile = new File(tempDir, "incrHistory");
        fullDumpFile = new File(tempDir, "fullDump");
        incrDumpFile = new File(tempDir, "incrDump");
        svnRepo = new File(tempDir, "test-svn-repo");

        commonConfig.setProperty("svnadmin.repository_path", svnRepo.getPath());

        log.info("DisableCleanup: " + disableCleanup);
        log.info("TestImport: " + testImport);
        log.info("TempDir: " + tempDir.getAbsolutePath());
        log.info("Incr history: " + incrDumpFile.getAbsolutePath());
        log.info("Full dump: " + fullDumpFile.getAbsolutePath());
        log.info("Incr dump: " + incrDumpFile.getAbsolutePath());
        log.info("Svn repo: " + svnRepo.getAbsolutePath());
    }

    protected void runTests() throws Exception {
        //if(true)return;//TODO: rm
        init();
        Log.configure(commonConfig);

        File[] files = testsDir.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory() && isValidTestDir(files[i])) {
                runTest(files[i], false);
                runTest(files[i], true);
            }
        }
    }

    protected void runTest(File testDir, boolean useFileCopy) throws Exception {
        System.out.println("\n\n\n");
        log.info("--- run test \"" + testDir.getName() + "\" --- useFileCopy=" + useFileCopy);

        commonConfig.setProperty("use_file_copy", useFileCopy?"yes":"no");

        initProvider(testDir);

        try {

//            Don't know why this was here, it doesn't seem to do anything
//            and messes up the record playback logic.  So away it goes!
//            log.info("List files...");
//            provider.listFiles(System.out);

            log.info("Build SVN model...");
            ISvnModel model = provider.buildSvnModel();

            log.info("Check SVN model...");
            checkSvnModel(model);

            if (testImport) {
                log.info("Create SvnAdmin...");
                svnAdmin.configure(commonConfig);
                if (!svnAdmin.validateConfig()) {
                    log.error("SvnAdmin settings is not valid");
                    testImport = false;
                    svnAdmin = null;
                }
            }

            log.info("Build full dump...");
            fullDump.dump(model);
            File checkDumpFile = getExpectedFullDumpFile(useFileCopy);
            if (checkDumpFile != null) {
                assertFilesMatch("Full dump file", checkDumpFile, fullDumpFile);
            }
            if (testImport) {
                initSvnRepo();
                svnAdmin.importDump(fullDump);
            }

            log.info("Build incremental dump...");
            buildIncrementalDump(model);

        } finally {
            if (!disableCleanup) {
                log.info("Cleanup...");
                provider.cleanup();
                incrHistoryFile.delete();
                fullDumpFile.delete();
                incrDumpFile.delete();

                if (svnAdmin != null)
                    svnAdmin.cleanup();

                Util.delete(svnRepo);
            }
        }
    }

    protected abstract void initProvider(File testDir) throws Exception;

    protected void checkSvnModel(ISvnModel model) {
        log.info("nop");
    }
    
    /**
     * Return copy of expected full dump file
     * Must be overridden by subclasses if anything needs to be checked
     * @param useFileCopy - true if file copy was enabled
     */
    protected File getExpectedFullDumpFile(boolean useFileCopy) {
        return null;
    }
    
    /**
     * Assert that two files are identical.  This a convenience method
     * that subclassed checkDumpFile methods can call
     * @param title - assert title
     * @param expect - File containing the expected dump results
     * @param result - File containing the actual dump results
     */
    private void assertFilesMatch(String title, File expect, File result) 
    throws IOException {
        
        BufferedReader expReader = null;
        BufferedReader resReader = null;
        try {
            expReader = new BufferedReader(new FileReader(expect));
            resReader = new BufferedReader(new FileReader(result));
            int lineCount = 0;
            
            String expLine, resLine;
            do {
                lineCount++;
                expLine = expReader.readLine();
                resLine = resReader.readLine();
                assertEquals(title + ":" + lineCount, expLine, resLine);
            } while (expLine != null && resLine != null);
        } finally {
            if (expReader != null) {
                try { expReader.close(); } catch (IOException ex) {}
            }
            if (resReader != null) {
                try {resReader.close(); } catch (IOException ex) {}
            }
        }
        
    }

    protected void buildIncrementalDump(ISvnModel model) throws Exception {
        int revCount = model.getRevisions().size();
        if (revCount < 2) {
            log.info("Too few revision in model (" + revCount + "), cannot test incremental dump");
            return;
        }
        List initRevs = new ArrayList(model.getRevisions().subList(0, revCount / 2));
        List updateRevs = new ArrayList(model.getRevisions());

        // initial dump
        model.getRevisions().clear();
        model.getRevisions().addAll(initRevs);

        log.info("Save incr history...");
        saveIncrHistory(model);

        fullDump.dump(model);
        if (testImport) {
            initSvnRepo();
            log.info("Import initial dump...");
            svnAdmin.importDump(fullDump);
        }

        // incremental dump
        model.getRevisions().clear();
        model.getRevisions().addAll(updateRevs);
        model = createIncrementalModel(model);

        incrDump.dump(model);
        if (testImport) {
            log.info("Import incremental dump...");
            svnAdmin.importDump(incrDump);
        }
    }

    protected abstract void saveIncrHistory(ISvnModel model) throws Exception;

    protected void initSvnRepo() {
        if (svnAdmin.isRepositoryExists()) {
            Util.delete(svnRepo);
        }
        svnAdmin.createRepository();
        svnAdmin.createParentDir();
    }

    protected abstract ISvnModel createIncrementalModel(ISvnModel model) throws IOException, Exception;
}

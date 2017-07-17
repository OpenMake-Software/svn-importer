package org.polarion.svnimporter.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class testExec extends BaseTestCase {
    
    private File execFile;
    private File recordFile = new File("testRecordFile.sav");
    private File outputFile = new File("testOutputFile.sav");

    public testExec(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        recordFile.delete();
        buildExecFile();
    }

    protected void tearDown() throws Exception {
        if (execFile != null) execFile.delete();
        recordFile.delete();
        outputFile.delete();
        super.tearDown();
    }
    
    public void testNormalExec() throws Exception {
        
        // Run process in normal mode and confirm that no record file
        // was generated
        setPlaybackMode("normal");
        ExecResults results = runExec();
        assertFalse(recordFile.exists());
        Playback.getInstance().close();
        
        System.out.println(results);
    }
    
    public void testNormalFileExec() throws Exception {
        
        // Run process in normal mode and confirm that no record file
        // was generated
        setPlaybackMode("normal");
        ExecResults results = runExec(outputFile);
        assertFalse(recordFile.exists());
        Playback.getInstance().close();
        
        System.out.println(results);
    }
    
    public void testNormalRecordPlayback() throws Exception {
        
        // Run process in record mode
        setPlaybackMode("record");
        ExecResults origResults = runExec();
        Playback.getInstance().close();
        
        // Confirm the record file exists
        assertTrue(recordFile.exists());
        
        // Delete the test executable and run the process in playback mode
        execFile.delete();
        setPlaybackMode("playback");
        ExecResults playbackResults = runExec();
        Playback.getInstance().close();
        
        // And confirm that we got the same results
        // even though the original executable does not exist
        assertObjectsEqual(origResults, playbackResults);
    }
    
    public void testParmRecordPlayback() throws Exception {
        
        // Run process in record mode
        setPlaybackMode("record");
        ExecResults origResults = runExec(new String[]{"parm1", "blank parm", "multi\nline\nparm"});
        Playback.getInstance().close();
        
        // Confirm the record file exists
        assertTrue(recordFile.exists());
        
        // Delete the test executable and run the process in playback mode
        execFile.delete();
        setPlaybackMode("playback");
        ExecResults playbackResults = runExec(new String[]{"parm1", "blank parm", "multi\nline\nparm"});
        Playback.getInstance().close();
        
        // And confirm that we got the same results
        // even though the original executable does not exist
        assertObjectsEqual(origResults, playbackResults);
        
    }
    
    public void testNoEnvRecordPlayback() throws Exception {
        
        // Run process in record mode
        setPlaybackMode("record");
        ExecResults origResults = runExec(true);
        Playback.getInstance().close();
        
        // Confirm the record file exists
        assertTrue(recordFile.exists());
        
        // Delete the test executable and run the process in playback mode
        execFile.delete();
        setPlaybackMode("playback");
        ExecResults playbackResults = runExec(true);
        Playback.getInstance().close();
        
        // And confirm that we got the same results
        // even though the original executable does not exist
        assertObjectsEqual(origResults, playbackResults);
    }
    
    public void testNormalRecordFilePlayback() throws Exception {
        
        // Run process in record mode
        setPlaybackMode("record");
        ExecResults origResults = runExec(outputFile);
        Playback.getInstance().close();
        
        // Confirm the record file exists
        assertTrue(recordFile.exists());
        
        // Delete the test executable and run the process in playback mode
        execFile.delete();
        setPlaybackMode("playback");
        ExecResults playbackResults = runExec(outputFile);
        Playback.getInstance().close();
        
        // And confirm that we got the same results
        // even though the original executable does not exist
        assertObjectsEqual(origResults, playbackResults);
    }
    
    public void testStubRecordPlayback() throws Exception {
    	
    	// Run process in normal mode, just to see what results we get
    	setPlaybackMode("normal");
    	ExecResults origResults = runExec();
        
        // Run process in stub record mode
        setPlaybackMode("record-stubs");
        ExecResults recordResults = runExec("This is a dummy stub line");
        Playback.getInstance().close();
        
        // Results should match the original run
        assertObjectsEqual(origResults, recordResults);
        
        // Confirm record file exists
        assertTrue(recordFile.exists());
        
        // Run the process in playback mode
        execFile.delete();
        setPlaybackMode("playback");
        ExecResults playbackResults = runExec();
        Playback.getInstance().close();
        
        // We don't expect the same results in stub mode, 
        // but confirm results were as requested
        ExecResults expResults = new ExecResults(
                new String[]{"This is a dummy stub line"},
                new String[0], 127);
        assertObjectsEqual(expResults, playbackResults);
    }
    
    public void testStubRecordFilePlayback() throws Exception {
    	
    	// Run process in normal mode, just to see what results we get
    	setPlaybackMode("normal");
    	ExecResults origResults = runExec(outputFile);
        
        // Run process in stub record mode
        setPlaybackMode("record-stubs");
        ExecResults recordResults = runExec("This is a dummy stub line", outputFile);
        Playback.getInstance().close();
        
        // Results should match the original run
        assertObjectsEqual(origResults, recordResults);
        
        // Confirm record file exists
        assertTrue(recordFile.exists());
        
        // Run the process in playback mode
        execFile.delete();
        setPlaybackMode("playback");
        ExecResults playbackResults = runExec(outputFile);
        Playback.getInstance().close();
        
        // We don't expect same results, but confirm they are what we expected
        ExecResults expResults = new ExecResults(
                new String[]{"This is a dummy stub line"},
                new String[0], 127);
        assertObjectsEqual(expResults, playbackResults);
    }
    
    public void testConfigErrors() {
        
        // Test some invalid configurations
        
        // Confirm that empty configuration is OK
        Properties config = new Properties();
        assertTrue(Playback.getInstance().config(config));
        
        // Check a couple invalid configurations
        config.setProperty("record.mode", "record");
        assertFalse(Playback.getInstance().config(config));

        config.setProperty("record.mode", "playback");
        assertFalse(Playback.getInstance().config(config));

        config.setProperty("record.mode", "turkey");
        config.setProperty("recortd.file", "testfile");
        assertFalse(Playback.getInstance().config(config));
    }
    
    /**
     * Build a simple test script that generates some output to both stdout
     * and stderr.  Script will be customized to work with current OS
     * @throws Exception if anything goes wrong
     */
    private void buildExecFile() throws Exception {
        String osName = System.getProperties().getProperty("os.name");
        if (osName.toUpperCase().startsWith("WINDOWS")) {
            execFile = new File(".\\testExecFile.bat");
            PrintWriter of = new PrintWriter(new FileWriter(execFile));
            of.println("date /t");
            of.println("time /t");
            of.println("echo %TESTVAR%");
            of.println("nosuchcommand");
            of.close();
        } else {
            execFile = new File("./testExecFile");
            PrintWriter of = new PrintWriter(new FileWriter(execFile));
            of.println("date");
            of.println("echo $TESTVAR");
            of.println("nosuchcommand");
            of.close();
            Runtime.getRuntime().exec("chmod 700 " + execFile.getName()).waitFor();
        }
    }
    
    private void setPlaybackMode(String mode) throws Exception {
        Properties config = new Properties();
        config.setProperty("record.mode", mode);
        config.setProperty("record.file", recordFile.getName());
        if (!Playback.getInstance().config(config))  {
            throw new Exception("Playback configuration error");
        }
    }
    
    /**
     * Create and execute a Exec object and return the accumulated results
     * @return ExecResults object containing the execution results
     */
    private ExecResults runExec() throws Exception {
        return runExec(null, null, false, null);
    }
    
    private ExecResults runExec(String stubResult) throws Exception {
        return runExec(stubResult, null, false, null);
    }
    
    private ExecResults runExec(String stubResult, File outFile) throws Exception {
        return runExec(stubResult, outFile, false, null);
    }
    
    private ExecResults runExec(File outFile) throws Exception {
        return runExec(null, outFile, false, null);
    }
    
    private ExecResults runExec(boolean noEnv) throws Exception {
        return runExec(null, null, noEnv, null);
    }
    
    private ExecResults runExec(String[] parms) throws Exception {
        return runExec(null, null, false, parms);
    }
    
    private ExecResults runExec(String stubResult, File outFile, boolean noEnv, 
                                String[] parms) 
    throws Exception {
        
        String cmd = execFile.getName();
        String cmdList[] = new String[1 + (parms == null ? 0 : parms.length)];
        cmdList[0] = cmd;
        if (parms != null) {
            System.arraycopy(parms, 0, cmdList, 1, parms.length);
        }
        Exec exec = new Exec(cmdList);
        
        if (! noEnv) {
            exec.setEnv(new String[]{"TESTVAR=HAPPY_DAY"});
            exec.setWorkdir(new File("."));
        }
        if (stubResult != null) exec.setResultStub(stubResult);
        
        
        List /* of String */ stdout = new ArrayList();
        if (outFile != null) {
            exec.setStdoutFile(outFile);
        } else {
            exec.setStdoutConsumer(new StreamAccumulator(stdout));
        }
        List /* of String */ stderr = new ArrayList();
        exec.setStderrConsumer(new StreamAccumulator(stderr));
        exec.exec();
        if (exec.getException() != null) {
            throw exec.getException();
        }

        if (outFile != null) {
            BufferedReader in = new BufferedReader(new FileReader(outFile));
            while (true) {
                String line = in.readLine();
                if (line == null) break;
                stdout.add(line);
            }
            in.close();
            outFile.delete();
        }
        
        return new ExecResults((String[])stdout.toArray(new String[stdout.size()]),
                               (String[])stderr.toArray(new String[stderr.size()]),
                               exec.getRc());
    }
    
    private static class StreamAccumulator extends StreamConsumer {
        private List list;
        
        public StreamAccumulator(List list) {
            this.list = list;
        }
        
        public void consumeLine(String line) {
            list.add(line);
        }
    }
    
    /**
     * Private class containing the results of one Exec execution
     */
    private static class ExecResults {
        public String[] stdout;
        public String[] stderr;
        public int rc;
        
        public ExecResults(String[] stdout, String[] stderr, int rc) {
            this.stdout = stdout;
            this.stderr = stderr;
            this.rc = rc;
        }
        
        public String toString() {
            StringWriter results = new StringWriter();
            PrintWriter out = new PrintWriter(results);
            if (stdout.length > 0) {
                out.println("STDOUT:");
                for (int ii = 0; ii < stdout.length; ii++) {
                    out.println("   " + stdout[ii]);
                }
            }
            if (stderr.length > 0) {
                out.println("STDERR:");
                for (int ii = 0; ii < stderr.length; ii++) {
                    out.println("   " + stderr[ii]);
                }
            }
            out.println("STATUS:" + rc);
            return results.toString();
        }
    }
}

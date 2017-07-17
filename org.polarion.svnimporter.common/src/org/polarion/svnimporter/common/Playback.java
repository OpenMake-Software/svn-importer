package org.polarion.svnimporter.common;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

/**
 * This class supports the basic record / playback infratructure
 */
public class Playback {
    
    private static final Log LOG = Log.getLog(Playback.class);
    
    static private Playback instance = new Playback();
    
    private PrintWriter recordWriter = null;
    private BufferedReader playbackReader = null;
    private boolean recordStubs = false;
    
    /**
     * @return singlton Playback instance
     */
    public static Playback getInstance() {
        return instance;
    }
    
    
    // Private constructor to prevent class from being externally created
    private Playback() {}

    /**
     * Configure Playback properties
     * @param config - source Properties object
     * @return true if successful, false if there were any errors
     */
    public boolean config(Properties config) {
        
        // Close any currently open record or playback files
        close();
        
        // Get the record mode and filename.  If not specified the
        // mode defaults to "normal"
        String recordMode = config.getProperty("record.mode");
        String recordFilename = config.getProperty("record.file");
        if (recordMode == null) recordMode = "normal";

        // record and record_stubs mode both require that a record file be
        // specified and that we can open it for writing
        if (recordMode.equals("record") || recordMode.equals("record-stubs")) {
            recordStubs = recordMode.equals("record-stubs");
            if (recordFilename == null) {
                LOG.error("No record file configured in \"" + recordMode + "\" mode");
                return false;
            } 
            try {
                recordWriter = new PrintWriter(
                        new  FileOutputStream(recordFilename));
            } catch (IOException ex) {
                LOG.error("Playback record file " + recordFilename + 
                            " could not be opened for output");
                return false;
            }
        }
        
        // Playback mode requires that file exist and can be opened for input
        else if (recordMode.equals("playback")) {
            if (recordFilename == null) {
                LOG.error("No record file configured in \"playback\" mode");
                return false;
            }
            try {
                playbackReader = new BufferedReader(new FileReader(recordFilename));
            } catch (FileNotFoundException ex) {
                LOG.error("Playback file " + recordFilename + 
                        " could not be opened for input");
                return false;
            }
        }
        
        // The only other legal value is "normal"
        else if (! recordMode.equals("normal")) {
            LOG.error("configuration propertry mks.record.mode value:" + recordMode +
                        " must be \"normal\", \"record\", \"record-stubs\", or \"playback\"");
            return false;
        }
        return true;
    }
    
    /**
     * Return record writer
     * @return record PrintWriter if we are recording, null otherwise
     */
    public PrintWriter getRecordWriter() {
        return recordWriter;
    }

    /**
     * Return playback reader
     * @return BufferedReader if we are in playback mode, null otherwise
     */
    public BufferedReader getPlaybackReader() {
        return playbackReader;
    }
    
    /**
     * Return record stub mode
     * @return true if we are recording file stubs, false if we are not.
     */
    public boolean isRecordStubs() {
        return recordStubs;
    }
    
    /**
     * Close any open record or playback files
     */
    public void close() {
        if (recordWriter != null) {
            recordWriter.close(); 
            recordWriter = null;
        }
        if (playbackReader != null) {
            try { playbackReader.close(); } catch (IOException ex) {}
            playbackReader = null;
        }
    }
}

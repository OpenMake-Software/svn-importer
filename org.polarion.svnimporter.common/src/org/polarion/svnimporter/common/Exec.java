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
package org.polarion.svnimporter.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class Exec {
	private static final Log LOG = Log.getLog(Exec.class);

	public static final int ERROR_EXCEPTION = 1;
    public static final int ERROR_TIMEOUT = 2;
    
    public static final String PLAYBACK_CODES = "+>!";

    /**
	 * Command
	 */
	private String cmd[];
	/**
	 * Process environment
	 */
	private String env[];
	/**
	 * Process working directory
	 */
	private File workdir;
	
	/**
	 * If not null, this is a 
	 * short identifying stub that will be the process stdout
	 * in record-stub mode
	 */
	private String resultStub = null;
	
	/**
	 * If not null, this is the file to which stdout will be copied as a binary
	 * stream.
	 */
	private File stdoutFile = null;

	/**
	 * if true then app will dump process stdout & stderr to log
	 */
	private boolean verboseExec = false;

	/**
	 * Process exit code
	 */
	private int rc;

	private String encoding = null;

	private StreamConsumer stdoutConsumer;
	private StreamConsumer stderrConsumer;

	private Exception exception;
	private int errorCode;
    private long timeout;

    private PrintWriter record = Playback.getInstance().getRecordWriter();
    private PrintWriter recordNormal = (Playback.getInstance().isRecordStubs() ? null : record);
    private BufferedReader playback = Playback.getInstance().getPlaybackReader();

    public Exec(String[] cmd) {
		this.cmd = cmd;
	}

	public void setVerboseExec(boolean verboseExec) {
		this.verboseExec = verboseExec;
	}

	public void setEnv(String[] env) {
		this.env = env;
	}

	public void setWorkdir(File workdir) {
		this.workdir = workdir;
	}
	
	public void setResultStub(String resultStub) {
	    this.resultStub = resultStub;
	}

	public boolean isVerboseExec() {
		return verboseExec;
	}

	public File getWorkdir() {
		return workdir;
	}

	public String[] getEnv() {
		return env;
	}

	public String[] getCmd() {
		return cmd;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEnconding(String charset) {
		this.encoding = charset;
	}

	protected void setRc(int rc) {
		this.rc = rc;
	}

	protected void setException(Exception exception) {
		this.exception = exception;
	}

	protected void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

    /**
     * Set timeout for process.
     * If process is not finished after timeout it will be killed
     *
     * @param timeout (milliseconds)
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    /**
	 * If > 0 then error happen
	 *
	 * @return
	 */
	public int getErrorCode() {
		return errorCode;
	}

	public int getRc() {
		return rc;
	}
	
	public void setStdoutFile(File stdoutFile) {
	    this.stdoutFile = stdoutFile;
	    this.stdoutConsumer = null;
	}

	public void setStdoutConsumer(StreamConsumer stdoutConsumer) {
		this.stdoutConsumer = stdoutConsumer;
		this.stdoutFile = null;
	}

	public void setStderrConsumer(StreamConsumer stderrConsumer) {
		this.stderrConsumer = stderrConsumer;
	}

	/**
	 * if getErrorCode() == ERROR_EXCEPTION then return exception
	 * else - null
	 *
	 * @return
	 */
	public Exception getException() {
		return exception;
	}

	private Thread stdoutThread;
	private Thread stderrThread;
    private Thread timeoutThread;
    private Process process;
	/**
	 * Return true if process finished without errors
	 */
    public void exec() {
		try {
			errorCode = 0;
			exception = null;
			rc = -1;

			if (isVerboseExec())
				LOG.info("exec " + Util.toString(cmd, " "));
			
			// Record or confirm playback command parameters
			recordCmd();
			
			// Playback mode skips the actual process call
			if (playback == null) {
				
				// Run the process
    			process = Runtime.getRuntime().exec(getCmd(), getEnv(), getWorkdir());
    	        
                setupTimeoutThread();
                setupProcessStdin(process);
    			setupProcessStdout(process);
    			setupProcessStderr(process);
    
    			rc = process.waitFor();
    			waitThreads();
			    
			    // And if we are record mode, record the final results
                if (record != null) {
                	
                	// If we are running in result-stub mode, and we have a result
                	// stub, just record that as the output
    			    if (recordNormal == null && resultStub != null) {
                        if (isVerboseExec()) LOG.info(resultStub);
                        record.print('>');
                        record.println(resultStub);
    			    }
                    
                    // If process output was sent directly to a file, copy
                    // that file to the record script.  This is imperfect and
                    // result in badly corrupted binary files, but are more
                    // interested in diagnostic and unit tests than we are in
                    // preserving binary file contents.
    			    else if (stdoutFile != null) {
                        BufferedReader in  = null;
                        try {
                            in = new BufferedReader(new FileReader(stdoutFile));
                            while (true) {
                                String line = in.readLine();
                                if (line == null) break;
                                record.print('>');
                                record.println(line);
                            }
                        } finally {
                            if (in != null) in.close();
                        }
                    }

                    // Write final status to record file
                    record.println("+RESULT:" + rc);
                }
			}
			
			// In playback mode, read lines from the playback file and report
			// as stdout or stderr lines as appropriate.  These are terminated
			// by a result line that records the original exit status
			else {
			    String line;
			    PrintWriter out = null;
			    try {
    			    if (stdoutFile != null) {
    			        out = new PrintWriter(new FileWriter(stdoutFile));
    			    }
    			    while (true) {
    			        line = readPlaybackLine();
    			        char code = line.charAt(0);
    			        line = line.substring(1);
    			        if (code == '>') {
    	                    if (isVerboseExec()) LOG.info(line);
    	                    if (out != null) {
    	                        out.println(line);
    	                    } else if (stdoutConsumer != null) {
    			                stdoutConsumer.consumeLine(line);
    			            }
    			        }
    			        else if (code == '!') {
    	                    LOG.info(line);
    			            if (stderrConsumer != null) {
    			                stderrConsumer.consumeLine(line);
    			            }
    			        }
    			        else break;
    			    }
			    } finally {
			        if (out != null) out.close();
			    }
			    if (! line.substring(0,7).equals("RESULT:")) {
			        throw new RuntimeException("Playback missing RESULT line:" + 
			                                   line.substring(0,7));
			    }
			    rc = Integer.parseInt(line.substring(7));
			}
            LOG.debug("Process exit value: "+rc);
        } catch (Exception e) {
			setErrorCode(ERROR_EXCEPTION);
			setException(e);
		}
	}
    
    /**
     * In record mode, record command parameters
     * In playback mode, confirm that command parameters match recorded values
     */
    private void recordCmd() {
        
        // If not in record or playback mode, there is nothing to do
        if (record == null && playback == null) return;
        
        // record/check each command argument
        for (int ii = 0; ii < cmd.length; ii++) {
            String parm = (ii == 0 ? "EXEC" : "PARM" + ii) + ":" + cmd[ii];
            recordParm(parm);
        }
        
        // Each environment variable
        if (env != null) {
            for (int ii = 0; ii<env.length; ii++) {
                String parm = "ENV" + ii + ":" + env[ii];
                recordParm(parm);
            }
        }
        
        // And the working directory
        if (workdir != null) {
            String parm = "WORKDIR:" + workdir;
            recordParm(parm);
        }
        
        // Add the start marker
        recordParm("START");
    }
    
    /**
     * In record mode, record one specific parameter line
     * In playback mode, confirm that parameter line matches recorded value
     * @param parm Parameter line to be recorded or checked
     */
    private void recordParm(String parm) {
        String line = '+' + parm.replace("\n", "\\n");
        if (record != null) {
            record.println(line);
        }
        if (playback != null) {
            String line2 = readPlaybackLine();
            if (!line.equals(line2)) {
                throw new RuntimeException(
                        "Playback mismatch - Expected:" + line + " was:" + line2);
            }
        }
    }
    
    /**
     * Read non-comment line from playback file
     * @return next line from playback file or null if EOF
     */
    String readPlaybackLine() {
        String line;
        char code;
        try {
            do {
                line = playback.readLine();
                if (line == null) return null;
                code = (line.length() == 0 ?  '\0' : line.charAt(0));
            } while (PLAYBACK_CODES.indexOf(code)<0);
            return line;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void setupTimeoutThread() {
        if (timeout > 0) {
            timeoutThread = new Thread() {
                public void run() {
                    try {
                        Thread.sleep(timeout);
                        if (rc == -1) {
                            LOG.warn("Timeout, destroy process");
                            destroyProcess();
                        }
                    } catch (InterruptedException e) {}
                }
            };
            timeoutThread.start();
        }
    }

    protected void setupProcessStdin(Process process) throws Exception {
		process.getOutputStream().close();
	}

	protected void setupProcessStdout(Process process) throws Exception {
	    
	    if (stdoutFile != null) {
	        stdoutThread = new FilePumper(this, process.getInputStream(), stdoutFile);
	    }
	    
	    else {
    		StreamPumper stdoutPumper = encoding != null ?
    				new StreamPumper(this, process.getInputStream(), encoding) :
    				new StreamPumper(this, process.getInputStream());
    		stdoutPumper.setConsumer(new StreamConsumer() {
    			public void consumeLine(String line) {
    			    synchronized (Exec.this) {
        				if (isVerboseExec()) LOG.info(line);
        				if (recordNormal != null) {
        				    recordNormal.print('>');
        				    recordNormal.println(line);
        				}
        				if (stdoutConsumer != null) stdoutConsumer.consumeLine(line);
    			    }
    			}
    		});
            stdoutThread = stdoutPumper;
	    }
        stdoutThread.setName("stdout");
		stdoutThread.start();
	}

	protected void setupProcessStderr(Process process) throws Exception {
		StreamPumper stderrPumper = encoding != null ?
				new StreamPumper(this, process.getErrorStream(), encoding) :
				new StreamPumper(this, process.getErrorStream());
		stderrPumper.setName("stderr");

		stderrPumper.setConsumer(new StreamConsumer() {
			public void consumeLine(String line) {
			    synchronized (Exec.this) {
                    LOG.info(line);
                    if (recordNormal != null) {
                        recordNormal.print('!');
                        recordNormal.println(line);
                    }
    				if (stderrConsumer != null) {
    				    stderrConsumer.consumeLine(line);
    				}
			    }
            }
		});
		stderrThread = stderrPumper;
		stderrThread.start();
	}

	/**
	 * Wait
	 */
	protected void waitThreads() throws InterruptedException {
        if(timeoutThread!=null) {
            timeoutThread.interrupt();
        }
        stdoutThread.join();
		stderrThread.join();
    }

    private void destroyProcess() {
        process.destroy();
    }
}

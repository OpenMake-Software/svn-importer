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
package org.polarion.svnimporter.vssprovider.internal;

import org.polarion.svnimporter.common.Exec;
import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.common.StreamConsumer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class VssExec extends Exec {
	private static final Log LOG = Log.getLog(VssExec.class);

	public static final int ERROR_AUTH_FAILED = 501;
	public static final int ERROR_CONFIRM_FAILED = 502;

	private Thread autoConfirmThread;

	public VssExec(String[] cmd) {
		super(cmd);
	}

	protected void setupProcessStdin(Process process) throws Exception {
		final OutputStream out = process.getOutputStream();
		autoConfirmThread = new Thread() {
			public void run() {
				while (true) {
					try {
						out.write("y\n".getBytes());
					} catch (IOException e) {
						break;
					}
				}
			}
		};
		autoConfirmThread.start();
	}

	protected void setupProcessStdout(Process process) throws Exception {
		setStdoutConsumer(new StreamConsumer() {
			public void consumeLine(String line) {
				checkErrors(line);
			}
		});
		super.setupProcessStdout(process);
	}

	private void checkErrors(String line) {
		if (line.startsWith("Password: ") || line.startsWith("Username: ")) {
			LOG.error("username or password is invalid");
			setErrorCode(ERROR_AUTH_FAILED);
		}
		if (line.equals("Continue anyway?(Y/N)N")) {
			LOG.error("confirmation failed: " + line);
			setErrorCode(ERROR_CONFIRM_FAILED);
		}
	}

	protected void waitThreads() throws InterruptedException {
		super.waitThreads();
		this.autoConfirmThread.join();
	}
}


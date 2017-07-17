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
package org.polarion.svnimporter.svnprovider.internal.svnadmin;

import org.polarion.svnimporter.common.Exec;
import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.common.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class SvnAdminImportExec extends Exec {
	private static final Log LOG = Log.getLog(SvnAdminImportExec.class);

	private File dumpFile;

	public SvnAdminImportExec(String[] cmd, File dumpFile) {
		super(cmd);
		this.dumpFile = dumpFile;
	}

	private Thread copyStreamThread;

	protected void setupProcessStdin(Process process) throws Exception {
		final OutputStream out = process.getOutputStream();
		final InputStream in = new FileInputStream(dumpFile);
		copyStreamThread = new Thread() {
			public void run() {
				try {
					Util.copy(in, out);
                    out.flush();
                } catch (IOException e) {
					LOG.error("can't copy dump file to svnadmin stdin: ", e);
					setException(e);
				} finally {
                    //System.out.println("*** copy finished ***");
                    Util.close(in);
					Util.close(out);
                }
			}
		};
		copyStreamThread.start();
	}

	protected void waitThreads() throws InterruptedException {
		super.waitThreads();
		this.copyStreamThread.join();
	}
}


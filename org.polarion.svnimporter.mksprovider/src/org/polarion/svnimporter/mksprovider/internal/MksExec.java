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
package org.polarion.svnimporter.mksprovider.internal;

import java.util.List;

import org.polarion.svnimporter.common.Exec;
import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.common.StreamConsumer;

/**
 * 
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class MksExec extends Exec {
	private static final Log LOG = Log.getLog(MksExec.class);

	public static final int ERROR_WRONG_PROJECT_PATH = 301;
	
	public MksExec(List /* of String */ cmdList) {
	    super((String[])cmdList.toArray(new String[cmdList.size()]));
	}

	public MksExec(String[] cmd) {
		super(cmd);
	}

	protected void setupProcessStderr(Process process) throws Exception {
		setStderrConsumer(new StreamConsumer() {
			public void consumeLine(String line) {
				if (line.startsWith("*** The project file")
						&& line.endsWith("is not registered with the current server.")) {
					LOG.error("wrong project path");
					setErrorCode(ERROR_WRONG_PROJECT_PATH);
				}
			}
		});
		super.setupProcessStderr(process);
	}
}


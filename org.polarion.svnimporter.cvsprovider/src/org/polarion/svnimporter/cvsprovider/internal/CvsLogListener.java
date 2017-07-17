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

package org.polarion.svnimporter.cvsprovider.internal;

import org.netbeans.lib.cvsclient.command.FileInfoContainer;
import org.netbeans.lib.cvsclient.command.log.LogInformation;
import org.netbeans.lib.cvsclient.event.BinaryMessageEvent;
import org.netbeans.lib.cvsclient.event.CVSListener;
import org.netbeans.lib.cvsclient.event.FileAddedEvent;
import org.netbeans.lib.cvsclient.event.FileInfoEvent;
import org.netbeans.lib.cvsclient.event.FileRemovedEvent;
import org.netbeans.lib.cvsclient.event.FileToRemoveEvent;
import org.netbeans.lib.cvsclient.event.FileUpdatedEvent;
import org.netbeans.lib.cvsclient.event.MessageEvent;
import org.netbeans.lib.cvsclient.event.ModuleExpansionEvent;
import org.netbeans.lib.cvsclient.event.TerminationEvent;
import org.polarion.svnimporter.common.Log;

/**
 * 
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public abstract class CvsLogListener implements CVSListener {
	private static final Log LOG = Log.getLog(CvsLogListener.class);

	public abstract void addLogInfo(LogInformation logInformation);

	public void fileInfoGenerated(FileInfoEvent e) {
		FileInfoContainer infoContainer = e.getInfoContainer();
		if (infoContainer instanceof LogInformation) {
			addLogInfo((LogInformation) infoContainer);
		} else {
			LOG.warn("unwanted info container: " + infoContainer);
		}
	}

	public void messageSent(MessageEvent e) {
		if(e.isError()) {
			LOG.error("CVS error: "+ e.getMessage());
		}
	}

	public void messageSent(BinaryMessageEvent e) {
	}

	public void fileAdded(FileAddedEvent e) {
	}

	public void fileRemoved(FileRemovedEvent e) {
	}

	public void fileUpdated(FileUpdatedEvent e) {
	}

	public void commandTerminated(TerminationEvent e) {
	}

	public void moduleExpanded(ModuleExpansionEvent e) {
	}

	public void fileToRemove(FileToRemoveEvent e) {
	}
}


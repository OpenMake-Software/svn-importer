/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.lib.cvsclient.command.log;

import org.netbeans.lib.cvsclient.command.BasicCommand;
import org.netbeans.lib.cvsclient.command.Builder;
import org.netbeans.lib.cvsclient.event.EventManager;
import org.netbeans.lib.cvsclient.event.FileInfoEvent;

/**
 * Handles output of "ls" command
 *
 * @author Fedor Gigaltsov
 */
public class LsBuilder implements Builder {

	/**
	 * The event manager to use
	 */
	protected EventManager eventManager;

	public LsBuilder(EventManager eventMan, BasicCommand command) {
		eventManager = eventMan;
	}

	public void outputDone() {
	}

	/*
	Sample command output:
	C:\>cvsnt -d ":pserver:anonymous@gigaltsov2:/var/cvs" ls test4
	cvs ls: Empty password used - try 'cvs login' with a real password

	cvs rls: Listing module: `test4'
	one
	two
	*/

    public void parseLine(String line, boolean isErrorMessage) {
        if (line.length() == 0
                || line.startsWith("cvs ls:")
                || line.startsWith("cvs rls:")
                || line.startsWith("Listing module")) {
            return;
        }
        LogInformation logInfo = new LogInformation();
        logInfo.setRepositoryFilename(line);
        eventManager.fireCVSEvent(new FileInfoEvent(this, logInfo));
    }

	public void parseEnhancedMessage(String key, Object value) {
	}
}

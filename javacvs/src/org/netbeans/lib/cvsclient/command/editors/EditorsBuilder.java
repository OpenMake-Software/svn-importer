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
package org.netbeans.lib.cvsclient.command.editors;

import java.io.*;
import java.text.*;
import java.util.*;

import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.event.*;

/**
 * @author  Thomas Singer
 * @version Nov 11, 2001
 */
public class EditorsBuilder
        implements Builder {
    // Constants ==============================================================

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd hh:mm:ss yyyy");
//  private static final DateFormat DATE_FORMAT = new SimpleDateFormat("EEE MMM dd hh:mm:ss yyyy zzz");

    // Fields =================================================================

    private final EventManager eventManager;

    private String editorsFileName;

    // Setup ==================================================================

    EditorsBuilder(EventManager eventManager) {
	this.editorsFileName=null;
        this.eventManager = eventManager;
    }

    // Implemented ============================================================

    public void parseLine(String line, boolean isErrorMessage) {
        if (!isErrorMessage) {
            parseLine(line);
        }
    }

    public void parseEnhancedMessage(String key, Object value) {
    }

    public void outputDone() {
    }

    // Utils ==================================================================

    private boolean parseLine(String line) {
        StringTokenizer tokenizer = new StringTokenizer(line, "\t");
        if (!tokenizer.hasMoreTokens()) {
            return false;
        }

	//check whether line is the first editors line for this file. 
	//persist for later lines. 
	if(!line.startsWith("\t")) {
	    editorsFileName = tokenizer.nextToken();
        if (!tokenizer.hasMoreTokens()) {
            return false;
        }
	} 
	//must have a filename associated with the line, 
	// either from this line or a previous one
	else if(editorsFileName==null) {
	    return false;
	}

        final String user = tokenizer.nextToken();
        if (!tokenizer.hasMoreTokens()) {
            return false;
        }

        final String dateString = tokenizer.nextToken();
        if (!tokenizer.hasMoreTokens()) {
            return false;
        }

        final String clientName = tokenizer.nextToken();
        if (!tokenizer.hasMoreTokens()) {
            return false;
        }

        final String localDirectory = tokenizer.nextToken();

        try {
            FileInfoContainer fileInfoContainer = parseEntries(localDirectory,
                                                               editorsFileName,
                                                               user,
                                                               dateString,
                                                               clientName);
            final CVSEvent event = new FileInfoEvent(this, fileInfoContainer);
            eventManager.fireCVSEvent(event);
            return true;
        }
        catch (ParseException ex) {
            return false;
        }
    }

    private EditorsFileInfoContainer parseEntries(String localDirectory,
                                                  String fileName,
                                                  String user,
                                                  String dateString,
                                                  String clientName) throws ParseException {
        int lastSlashIndex = fileName.lastIndexOf('/');
        if (lastSlashIndex >= 0) {
            fileName = fileName.substring(lastSlashIndex + 1);
        }

        final Date date = parseDate(dateString);
        final File file = new File(localDirectory, fileName);
        return new EditorsFileInfoContainer(file,
                                            user,
                                            date,
                                            clientName);
    }

    private Date parseDate(String dateString) throws ParseException {
        int firstSpaceIndex = Math.max(dateString.indexOf(' '), 0);
        int lastSpaceIndex = Math.min(dateString.lastIndexOf(' '), dateString.length());

//      dateString = dateString.substring(0, lastSpaceIndex).trim();
        dateString = dateString.substring(firstSpaceIndex, lastSpaceIndex).trim();

        return DATE_FORMAT.parse(dateString);
    }
}

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

import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.vssprovider.VssException;
import org.polarion.svnimporter.vssprovider.internal.model.VssFile;
import org.polarion.svnimporter.vssprovider.internal.model.VssFileActionType;
import org.polarion.svnimporter.vssprovider.internal.model.VssFileRevision;
import org.polarion.svnimporter.vssprovider.internal.model.VssLabel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @author <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class VssHistoryParser {
    private static final Log LOG = Log.getLog(VssHistoryParser.class);

    private static final String VERSION = "Version ";
    private static final String LABEL = "Label: ";
    private static final String CHECKED_IN = "Checked in";
    private static final String CREATED = "Created";
    private static final String LABELED = "Labeled";
    private static final String COMMENT = "Comment: ";
    private static final String USER = "User: ";
    private static final String DATE = "Date: ";
    private static final String TIME = "Time: ";
    private static final String LABEL_COMMENT = "Label comment: ";

    private VssConfig config;

    public VssHistoryParser(VssConfig config) {
        this.config = config;
    }

    private List notBoundLabels = new ArrayList();

    public void parseFileHistory(File historyFile, VssFile vssFile) {
        try {
            InputStreamReader encReader = new InputStreamReader(new FileInputStream(historyFile), config.getLogEncoding());
            BufferedReader in = new BufferedReader(encReader);
            try {
                String line;
                String oldLine = null;
                VssFileRevision lastEntry = null;
                VssFileRevision beforeLastEntry = null;
                while (true) {
                    if (oldLine != null) {
                        line = oldLine;
                        oldLine = null;
                    } else {
                        line = in.readLine();
                        if (line == null) break;
                    }
                    if (line.startsWith("*****")) {
                        ArrayList vssEntry = new ArrayList();
                        vssEntry.add(line);
                        line = in.readLine();
                        while (line != null && !line.startsWith("*****")) {
                            vssEntry.add(line);
                            line = in.readLine();
                        }
                        oldLine = line;
                        beforeLastEntry = lastEntry;
                        lastEntry = parseEntry(vssEntry, vssFile);
                    }
                }
                if (lastEntry != null && beforeLastEntry != null && lastEntry.getDate().getTime() == beforeLastEntry.getDate().getTime()) {
                    long t = lastEntry.getDate().getTime();
                    t -= 1000;
                    lastEntry.setDate(new Date(t));
                    LOG.info("Correct date of first revision: " + lastEntry.getModelFile().getPath() + " " + lastEntry.getNumber());
                }
            } finally {
                in.close();
            }
        } catch (IOException e) {
            throw new VssException(e);
        }
    }

    private VssFileRevision parseEntry(List entry, VssFile vssFile) {

/* "Add/Change" action example:
*****************  Version 1   *****************
Label: "LABEL1"
User: Admin        Date: 28.12.05   Time: 16:11
Created
Comment: add file file1.txt comment
Label comment: label comment
*/

/* "Labeled" action example:
**********************
Label: "0.9.115"
User: Dudarev      Date:  8.07.05   Time: 17:41
Labeled
Label comment:
*/

        boolean recordComment = false;
        Integer version = null;
        String label = null;
        String actionString = null;
        String comment = null;
        String user = null;
        Date date = null;
        String labelComment = null;

        for (Iterator i = entry.iterator(); i.hasNext();) {
            String line = (String) i.next();

            if (!recordComment && version == null && line.startsWith("*")) {
                int v = line.indexOf(VERSION);
                if (v != -1) {
                    int start = v + VERSION.length();
                    int end = line.indexOf("*", start);
                    if (end != -1) {
                        String s = line.substring(start, end).trim();
                        try {
                            version = new Integer(Integer.parseInt(s));
                        } catch (NumberFormatException e) {
                            LOG.error("Wrong version number: " + s);
                        }
                    }
                }
            } else if (!recordComment && label == null && line.startsWith(LABEL)) {
                String s = line.substring(LABEL.length()).trim();
                s = s.replaceFirst("^\"", "").replaceFirst("\"$", ""); //remove '"'
                if (s.length() > 0) {
                    label = s;
                }
            } else if (!recordComment && user == null && line.startsWith(USER)) {
                int userStart = USER.length();
                int userEnd = line.indexOf(DATE);
                if (userEnd != -1) {
                    String s = line.substring(userStart, userEnd).trim();
                    if (s.length() > 0) {
                        user = s;
                    }
                }
                date = parseDate(line);
            } else if (!recordComment && actionString == null && line.startsWith(CHECKED_IN)) {
                actionString = CHECKED_IN;
            } else if (!recordComment && actionString == null && (line.startsWith(CREATED) || line.indexOf( " archived to " )>0)) {
                actionString = CREATED;
            } else if (!recordComment && actionString == null && line.startsWith(LABELED)) {
                actionString = LABELED;
            } else if (!recordComment && comment == null && line.startsWith(COMMENT)) {
                comment = line.substring(COMMENT.length());
                recordComment = true;
            } else if (recordComment) {
                if (labelComment != null) {
                    labelComment += "\n" + line;
                } else {
                    if (line.startsWith(LABEL_COMMENT)) {
                        labelComment = line.substring(LABEL_COMMENT.length());
                    } else {
                        comment += "\n" + line;
                    }
                }
            }
        }

        VssLabel vssLabel = null;
        if (label != null) {
            vssLabel = new VssLabel(label);
            vssLabel.setAuthor(user);
            vssLabel.setComment(labelComment);
            vssLabel.setDate(date);
        }

        if (LABELED.equals(actionString)) {
            // in "Labeled" action label is not bound to any revision explicitly
            // we accumulate all such labels in notBoundLabels collection and
            // add labels from this collection to next parsed revision
            notBoundLabels.add(vssLabel);
            return null;
        } else {
            if (version == null) {
                LOG.warn("Skip entry: version is not set\n" + entryToString(entry));
                return null;
            }
            if (user == null) {
                LOG.warn("Skip entry: user is not set\n" + entryToString(entry));
                return null;
            }
            if (date == null) {
                LOG.warn("Skip entry: date is not set\n" + entryToString(entry));
                return null;
            }
            if (actionString == null) {
                LOG.warn("Wrong entry: action is not set\n" + entryToString(entry));
                return null;
            }

            VssFileRevision revision = new VssFileRevision(version.intValue());
            if (CREATED.equals(actionString)) {
                revision.setType(VssFileActionType.ADD);
            } else if (CHECKED_IN.equals(actionString)) {
                revision.setType(VssFileActionType.CHANGE);
            } else {
                throw new IllegalStateException();
            }

            revision.setAuthor(user);
            revision.setDate(date);
            revision.setMessage(comment != null ? comment : "");

            if (vssLabel != null)
                revision.addLabel(vssLabel);

            for (Iterator i = notBoundLabels.iterator(); i.hasNext();) {
                revision.addLabel((VssLabel) i.next());
                i.remove();
            }

            vssFile.addRevision(revision);
            revision.setModelFile(vssFile);

            return revision;
        }
    }

    private String entryToString(List entry) {
        StringBuffer b = new StringBuffer();
        for (Iterator i = entry.iterator(); i.hasNext();) {
            if (b.length() > 0) b.append("\n");
            b.append(i.next());
        }
        return b.toString();
    }

    private Date parseDate(String line) {
        //User: Admin        Date: 24.01.05   Time: 17:36
        int idxDate = line.indexOf(DATE);
        if (idxDate == -1)
            return null;
        int idxTime = line.indexOf(TIME);
        if (idxTime == -1)
            return null;
        String sdate = line.substring(idxDate + DATE.length(), idxTime).trim();
        String stime = line.substring(idxTime + TIME.length()).trim();
        if(stime.endsWith("a") || stime.endsWith("p")) stime+="m"; //5:03p->5:03pm
        DateFormat df = config.getLogDateFormat();
        String date = sdate + " " + stime;

        try {
            return df.parse(date);
        } catch (ParseException e) {
            LOG.error("Wrong date: " + date);
            return null;
        }
    }
}

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
package org.polarion.svnimporter.ccprovider.internal;

import org.polarion.svnimporter.ccprovider.internal.model.CCBranch;
import org.polarion.svnimporter.ccprovider.internal.model.CCFile;
import org.polarion.svnimporter.ccprovider.internal.model.CCModel;
import org.polarion.svnimporter.ccprovider.internal.model.CCRevision;
import org.polarion.svnimporter.ccprovider.internal.model.CCRevisionState;
import org.polarion.svnimporter.common.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Vector;

/**
 * @author <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class CCHistoryParser {
    private static final Log LOG = Log.getLog(CCHistoryParser.class);

    public static final String HISTORY_FIELD_SEPARATOR = "@@@";

    public static final String HISTORY_FORMAT
            = "%Nd;%En;%Vn;%o;!%l;!%a;%m;%u;%Nc;\\n".replaceAll(";", HISTORY_FIELD_SEPARATOR);

    private CCModel model;
    private DateFormat dateFormat;

    public CCHistoryParser() {
        this.model = new CCModel();
    }

    public void setDateFormat(DateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }

    public CCModel getModel() {
        return model;
    }

    private String prevLine = null;

    /**
     * Split one history line
     *
     * @param s
     * @return
     */
    private String[] splitRecord(String s) {
        Vector fields = new Vector();
        int i=0;
        while(i<s.length()) {
            int nextsep = s.indexOf(HISTORY_FIELD_SEPARATOR, i);
            if(nextsep==-1)
                break;
            fields.add(s.substring(i, nextsep));
            i = nextsep+HISTORY_FIELD_SEPARATOR.length();
        }
        return (String[]) fields.toArray(new String[0]);
    }

    public void parse(String s) {
        if (prevLine != null) {
            s = prevLine + "\n" + s;
        }

        String[] fields = splitRecord(s);
        if (fields.length < 9) {
            prevLine = s;
            return;
        } else if (fields.length > 9) {
            LOG.error("Wrong history line: " + s);
            return;
        }
        prevLine = null;
        Date date = parseDate(fields[0]);
        String ccpath = fields[1];
        String path = ccpath.replaceAll("\\\\", "/");

        String revision = fields[2];
        String operation = fields[3];
        String[] labels = parseLabels(fields[4]);
        String type = fields[6];
        String author = fields[7];
        String comment = fields[8];
        if (comment != null && comment.endsWith(HISTORY_FIELD_SEPARATOR)) // always true?
            comment = comment.substring(0, comment.length() - HISTORY_FIELD_SEPARATOR.length());

        if (!"checkin".equals(operation)) return;

        boolean delete = false;
        String rfe = "Removed file element \"";
        if ("directory version".equals(type)
                && comment != null
                && comment.indexOf(rfe) != -1
                && comment.endsWith("\".")) {
            String deletedFilename = comment.substring(comment.indexOf(rfe) + rfe.length(), comment.length() - 2);
            ccpath = ".".equals(path) ? deletedFilename : path + "\\" + deletedFilename;
            path = ccpath.replaceAll("\\\\", "/");
            comment = comment.substring(0, comment.indexOf(rfe));
            if (comment.endsWith("\n")) comment = comment.substring(0, comment.length() - 1);
            delete = true;
        } else {
            if (!"version".equals(type))
                return;
        }

        String[] parsedRevision = CCRevisionNumber.parseRevision(revision);
        if (parsedRevision == null) return;

        int numberInBranch = CCRevisionNumber.getNumberInBranch(parsedRevision);
        if (numberInBranch == -1) return;

        String[] branchNames = CCRevisionNumber.getBranchNames(parsedRevision);

        CCFile ccfile = (CCFile) model.getFiles().get(path);
        if (ccfile == null) {
            ccfile = new CCFile(path);
            ccfile.setCcpath(ccpath);
            CCBranch trunk = new CCBranch("main");
            trunk.setTrunk(true);
            //trunk.setName("main");
            ccfile.addBranch(trunk);
            ccfile.setTrunk(trunk);
            model.addFile(ccfile);
        }

        CCBranch branch;
        if (branchNames.length == 1) {
            branch = ccfile.getTrunk();
        } else {
            String branchName = branchNames[branchNames.length - 1];
            branch = (CCBranch) ccfile.getBranches().get(branchName);
            if (branch == null) {
                branch = new CCBranch(branchName);
                branch.setName(branchName);
                ccfile.addBranch(branch);
            }
        }

        if (delete) {
            // revision number is not significant now, right revision number will
            // be assigned in CCBranch.handleDeletedRevisions()
            revision = "fixInCCBranch "+revision;
        }

        CCRevision ccrevision = new CCRevision(revision);
        ccrevision.setAuthor(author);
        ccrevision.setMessage(comment);
        ccrevision.setDate(date);
        ccrevision.setNumberInBranch(numberInBranch);

        for (int i = 0; i < labels.length; i++) {
            ccrevision.newLabel(labels[i]);
        }

        if (delete)
            ccrevision.setState(CCRevisionState.DELETE);

        if (delete) {
            branch.addDeletedRevision(ccrevision);
            // revision will be added to ccfile later, in CCBranch.handleDeletedRevisions()
        } else {
            ccrevision.setBranch(branch);
            branch.addRevision(ccrevision);
            ccfile.addRevision(ccrevision);
        }
    }

    private String[] parseLabels(String s) {
        // format: !(label1, label2, label3)
        if (s == null || !s.startsWith("!(") || !s.endsWith(")"))
            return new String[0];
        return s.substring(2, s.length() - 1).split(", ");
    }

    /**
     * Parse date string
     *
     * @param s
     * @return
     */
    private Date parseDate(String s) {
        try {
            return dateFormat.parse(s);
        } catch (ParseException e) {
            LOG.error("wrong date: " + s);
            return null;
        }
    }
}


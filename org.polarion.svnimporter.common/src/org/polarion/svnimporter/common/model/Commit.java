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
package org.polarion.svnimporter.common.model;

import org.polarion.svnimporter.common.RevisionNumber;
import org.polarion.svnimporter.common.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeMap;

/**
 * @author <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class Commit {

    /**
     * Commit's author
     */
    private String author;
    /**
     * Commit's message
     */
    private String message;
    /**
     * Commit's date
     */
    private Date date;

//    /**
//     * ModelFile -> Revisions list
//     */
    private Map revisions = new TreeMap();

    public Commit() {
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * Return list of revisons grouped in this commit
     *
     * @return
     */
    public Collection getRevisions() {
        List revs = new ArrayList();
        for (Iterator i = revisions.keySet().iterator(); i.hasNext();) {
            ModelFile f = (ModelFile) i.next();
            revs.addAll((List) revisions.get(f));
        }
        return revs;
    }

    /**
     * Get list of revisions of certain file
     *
     * @param modelFile
     * @return
     */
    public List getRevisions(ModelFile modelFile) {
        return (List) revisions.get(modelFile);
    }

    /**
     * Add revision to commit
     *
     * @param revision
     */
    public void addRevision(Revision revision) {
        List revs = (List) revisions.get(revision.getModelFile());
        if (revs == null) {
            revs = new ArrayList();
            revisions.put(revision.getModelFile(), revs);
        }
        // revisions list must be ordered from oldest revision to newest (by number)
        int insertIndex = -1;
        for (int i = 0; i < revs.size(); i++) {
            Revision r = (Revision) revs.get(i);
            if (compareRevisionNumbers(revision.getNumber(), r.getNumber()) < 0) {
                insertIndex = i;
                break;
            }
        }
        if (insertIndex != -1) {
            revs.add(insertIndex, revision);
        } else {
            revs.add(revision);
        }
    }

    public String getDebugInfo() {
        StringBuffer b = new StringBuffer();
        b.append("Commit d[" + Util.toString(date) + "] a[" + author + "] c[" + Util.escape(message) + "]\n");

        for (Iterator j = revisions.values().iterator(); j.hasNext();) {
            List revs = (List) j.next();
            for (Iterator i = revs.iterator(); i.hasNext();) {
                Revision revision = (Revision) i.next();
                b.append(revision.getPath() + "\n");
                b.append("\t" + revision.getDebugInfo() + "\n");
            }
        }
        return b.toString();
    }

    protected int compareRevisionNumbers(String number1, String number2) {
        return RevisionNumber.compare(number1, number2);
    }

    public String joinRevisionNumbers() {
        Set numbers = new HashSet();
        StringBuffer b = new StringBuffer();
        for (Iterator i = getRevisions().iterator(); i.hasNext();) {
            Revision revision = (Revision) i.next();
            String n = revision.getNumber();
            if (!numbers.contains(n)) {
                if (b.length() > 0) b.append(",");
                b.append(n);
                numbers.add(n);
            }
        }
        return b.toString();
    }
}

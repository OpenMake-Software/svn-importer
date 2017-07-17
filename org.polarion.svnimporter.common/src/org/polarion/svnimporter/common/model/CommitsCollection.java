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

import org.polarion.svnimporter.common.CommonException;
import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.common.RevisionNumber;
import org.polarion.svnimporter.common.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * @author <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class CommitsCollection {
    private static final Log LOG = Log.getLog(CommitsCollection.class);
    /**
     * Commit id(hash of date, author, log) -> Commit
     */
    private Map commits = new HashMap();

    /**
     * Some class inherited from Commit
     */
    private Class commitClass;

    /**
     * Class to instantinate new commits
     *
     * @param commitClass
     */
    public CommitsCollection(Class commitClass) {
        this.commitClass = commitClass;
    }

    protected Commit createCommit(Revision revision) {
        try {
            Commit commit = (Commit) commitClass.newInstance();
            commit.setAuthor(revision.getAuthor());
            commit.setMessage(revision.getMessage());
            commit.setDate(revision.getDate());
            commit.addRevision(revision);
            return commit;
        } catch (Exception e) {
            throw new CommonException("can't instantinate new Commit object: " + commitClass, e);
        }
    }

    /**
     * Add model files
     *
     * @param modelFiles
     */
    public void addFiles(Collection modelFiles) {
        for (Iterator i = modelFiles.iterator(); i.hasNext();) {
            ModelFile modelFile = (ModelFile) i.next();
            addFile(modelFile);
        }
    }

    /**
     * Add model file
     *
     * @param modelFile
     */
    public void addFile(ModelFile modelFile) {
        for (Iterator i = modelFile.getRevisions().values().iterator(); i.hasNext();) {
            Revision curRevision = (Revision) i.next();
            String commitId = getCommitId(curRevision);
            if (!commits.containsKey(commitId)) {
                Commit commit = createCommit(curRevision);
                commits.put(commitId, commit);
            } else {
                Commit commit = (Commit) commits.get(commitId);
                commit.addRevision(curRevision);
            }
        }
    }

    /**
     * Compare revision numbers
     *
     * @param revisionNumber1
     * @param revisionNumber2
     * @return > 0 if revisionNumber1 is newer than revisionNumber1, 0 if equals, &lt; 0 if older
     */
    protected int compareRevisionNumbers(String revisionNumber1, String revisionNumber2) {
        return RevisionNumber.compare(revisionNumber1, revisionNumber2);
    }

    private static String getCommitId(Revision revision) {
        String s = "" + revision.getDate().getTime() + revision.getAuthor() + revision.getMessage();
        return Util.getHash(s);
    }

    /**
     * Separate commits
     *
     * @return
     */
    public List separateCommits() {
        // sort commits by date
        TreeSet allCommitsSorted = new TreeSet(new Comparator() {
            public int compare(Object o1, Object o2) {
                Commit c1 = (Commit) o1;
                Commit c2 = (Commit) o2;
                int diff = c1.getDate().compareTo(c2.getDate());
                if (diff == 0) {
                    // if commits has equal date - try to determine order by
                    // revision numbers
                    diff = compareEqualDateCommits(c1, c2);
                }
                if (diff == 0) diff = 1;
                return diff;
            }
        });
        allCommitsSorted.addAll(commits.values());
        List l = new ArrayList();
        l.addAll(allCommitsSorted);
        return l;
    }

    protected int compareEqualDateCommits(Commit c1, Commit c2) {
        // if commits has equal date - try to determine order by
        // revision numbers
        boolean problem = false;
        Iterator i = c1.getRevisions().iterator();
        while (i.hasNext()) {
            Revision r1 = (Revision) i.next();
            List revisionsR2 = c2.getRevisions(r1.getModelFile());
            if (revisionsR2==null || revisionsR2.isEmpty())
                continue;
            for (Iterator r2i = revisionsR2.iterator(); r2i.hasNext();) {
                Revision r2 = (Revision) r2i.next();
                int diff = RevisionNumber.compare(r1.getNumber(), r2.getNumber());
                if (diff != 0) {
                    return diff;
                } else {
                    problem = true;
                }
            }
        }
        if (problem)
            LOG.warn("Potential problem: two commits with equal dates and same files"
                    + "\n" + c1.getDebugInfo()
                    + "\n" + c2.getDebugInfo());
        return 0;
    }
}

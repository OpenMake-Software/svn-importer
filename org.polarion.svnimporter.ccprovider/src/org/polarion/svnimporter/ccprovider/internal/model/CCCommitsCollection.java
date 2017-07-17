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
package org.polarion.svnimporter.ccprovider.internal.model;

import org.polarion.svnimporter.common.model.Commit;
import org.polarion.svnimporter.common.model.CommitsCollection;
import org.polarion.svnimporter.common.model.Revision;
import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.ccprovider.internal.CCRevisionNumber;

import java.util.Iterator;
import java.util.List;

/**
 * @author <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class CCCommitsCollection extends CommitsCollection {
    private static final Log LOG = Log.getLog(CCCommitsCollection.class);

    public CCCommitsCollection(Class commitClass) {
        super(commitClass);
    }

    protected int compareEqualDateCommits(Commit c1, Commit c2) {
        // if commits has equal date - try to determine order by
        // revision numbers
        Iterator i = c1.getRevisions().iterator();
        while (i.hasNext()) {
            CCRevision r1 = (CCRevision) i.next();
            List revisionsR2 = c2.getRevisions(r1.getModelFile());
            if (revisionsR2 ==null || revisionsR2.isEmpty())
                continue;
            for (Iterator r2i = revisionsR2.iterator(); r2i.hasNext();) {
                CCRevision r2 = (CCRevision) r2i.next();
                if (r1.getBranch().equals(r2.getBranch())) {
                   return r1.getNumberInBranch() - r2.getNumberInBranch();
                }
            }
        }
        return 0;
    }

    /**
     * Compare revision numbers
     *
     * @param revisionNumber1
     * @param revisionNumber2
     * @return > 0 if revisionNumber1 is newer than revisionNumber1, 0 if equals, &lt; 0 if older
     */
    protected int compareRevisionNumbers(String revisionNumber1, String revisionNumber2) {
        return CCRevisionNumber.compare(revisionNumber1, revisionNumber2);
    }
}


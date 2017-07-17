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

package org.polarion.svnimporter.cvsprovider.internal.model;

import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.common.Util;
import org.polarion.svnimporter.common.model.Revision;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class CvsRevision extends Revision {
    private static final Log LOG = Log.getLog(CvsRevision.class);

    /**
     * Rcs 'raw' state: 'Exp' or 'dead'
     */
    private String rcsState;


    private CvsRevisionState state;

    /**
     * Collection of CvsTags
     */
    private Map tags = new HashMap(); // tag name -> tag

    /**
     * If revision is invalid - do not account it in commits
     */

    public CvsRevision(String number) {
        super(number);
    }

    public Collection getTags() {
        return tags.values();
    }

    public String getRcsState() {
        return rcsState;
    }

    public void setRcsState(String rcsState) {
        this.rcsState = rcsState;
    }

    public CvsRevisionState getState() {
        return state;
    }

    public void setState(CvsRevisionState state) {
        this.state = state;
    }

    /**
     * Add tag
     *
     * @param tag
     */
    public void addTag(CvsTag tag) {
        if ("dead".equalsIgnoreCase(getRcsState())) {
            LOG.warn("Ignore tag '" + tag.getName() + "' on deleted revision " + getNumber());
        } else {
            if (tags.containsKey(tag.getName())) {
                LOG.error(getPath() + ": duplicate tag " + tag.getName());
            } else {
                tags.put(tag.getName(), tag);
            }
        }
    }

    public String getDebugInfo() {
        String branchName;
        branchName = !getBranch().isTrunk() ? getBranch().getName() : "TRUNK";
        String stateName = state != null ? state.getName() : null;
        String childBranchesStr = "None";
        if (getChildBranches().size() > 0) {
            StringBuffer b = new StringBuffer();
            for (Iterator i = getChildBranches().iterator(); i.hasNext();) {
                CvsBranch cvsBranch = (CvsBranch) i.next();
                if (b.length() > 0) b.append(", ");
                b.append("[" + cvsBranch.getName() + " " + cvsBranch.getNumber() + "]");
            }
            childBranchesStr = b.toString();
        }
        String childTagsStr = "None";
        if (tags.size() > 0) {
            StringBuffer b = new StringBuffer();
            for (Iterator i = tags.values().iterator(); i.hasNext();) {
                CvsTag tag = (CvsTag) i.next();
                if (b.length() > 0) b.append(", ");
                b.append("[" + tag.getName() + "]");
            }
            childTagsStr = b.toString();
        }
        return "File: " + getPath() + "\n"
                + "-------------------------------------\n\t"
                + "branch :  " + branchName + " " + ((getBranch() != null) ? getBranch().getNumber() : "?") + "\n\t"
                + "number :  " + getNumber() + "\n\t"
                + "state  :  " + stateName + "\n\t"
                + "author :  " + getAuthor() + "\n\t"
                + "date   :  " + Util.toString(getDate()) + "\n\t"
                + "log    :  " + Util.escape(getMessage()) + "\n\t"
                + "branches : " + childBranchesStr + "\n\t"
                + "tags     : " + childTagsStr + "\n";
    }

    public String toString() {
        return "CvsRevision[" + getModelFile().getPath() + ":" + getNumber() + "]";
    }
}

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
package org.polarion.svnimporter.mksprovider.internal.model;

import org.polarion.svnimporter.common.model.Revision;

import java.util.Date;

/**
 * 
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class MksRevision extends Revision {

	private MksRevisionState state;
	
	// If revision state is COPY, this contains the source of the file copy
	private MksRevision source;

	/**
	 * Constructor
	 * @param number revision number
	 * @param author author
	 * @param date revision date
	 * @param labels array of revision labels, or null if no labels
	 * @param description String containing revision description.  Multi-line
	 * descriptions can be entered by embedding \n characters in the description
	 */
	public MksRevision(String number, String author, Date date, 
	                   String description) {
		super(number);
        setAuthor(author);
        setDate(date);
        setMessage(description);
        
        // Default to change state until proven otherwise
        setState(MksRevisionState.CHANGE);
	}

	public MksRevisionState getState() {
		return state;
	}

	public void setState(MksRevisionState state) {
		this.state = state;
	}
	
	public MksRevision getSource() {
	    return source;
	}
	
	public void setSource(MksRevision revision) {
	    this.source = revision;
	}
    
    /**
     * Determine if revision belongs to an assigned branch.  That is a branch
     * which has been assigned to a development path.
     * @return true if it has been assigned
     */
    public boolean isAssigned() {
        return ((MksBranch)getBranch()).isAssigned();
    }
}


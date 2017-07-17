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
package org.polarion.svnimporter.stprovider.internal.model;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.polarion.svnimporter.common.model.Revision;

/**
 * 
 * 
 * @author <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class STRevision extends Revision implements STChangeRequestLinkTarget {
	/** Revision state ('add', 'change', 'delete') */
	private STRevisionState state;

	/** Ordinal number in branch (first revision has number=1) */
	private int numberInBranch;

	/** the original StarTeam username */
	private String stUserName;

	/** the original StarTeam modification date */
	private String stDate;

	private Set labels = new HashSet();
	private SortedSet linkedCRs = new TreeSet();
	private boolean fetched = false;
	
	
	public STRevision(String number) {
		super(number);
	}

	public int getNumberInBranch() {
		return numberInBranch;
	}

	public void setNumberInBranch(int numberInBranch) {
		this.numberInBranch = numberInBranch;
	}

	public STRevisionState getState() {
		return state;
	}

	public void setState(STRevisionState state) {
		this.state = state;
	}

	/**
	 * @return the stUserName
	 */
	public String getStUserName() {
		return stUserName;
	}

	/**
	 * @param stUserName
	 *            the stUserName to set
	 */
	public void setStUserName(String stUserName) {
		this.stUserName = stUserName;
	}

	/**
	 * @return the stDate
	 */
	public String getStDate() {
		return stDate;
	}

	/**
	 * @param stDate
	 *            the stDate to set
	 */
	public void setStDate(String stDate) {
		this.stDate = stDate;
	}

	public STLabel newLabel(String labelName) {
		STLabel label = new STLabel(this, labelName);
		labels.add(label);
		return label;
	}

	public STLabel[] getLabels() {
		return (STLabel[]) labels.toArray(new STLabel[0]);
	}

	void addLinkedChangeRequest(STChangeRequest changeRequest) {
		linkedCRs.add(changeRequest);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.polarion.svnimporter.stprovider.internal.model.STChangeRequestLinkTarget#getLinkedChangeRequests()
	 */
	public STChangeRequest[] getLinkedChangeRequests() {
		// cr list must be ordered by number
		return (STChangeRequest[]) linkedCRs.toArray(new STChangeRequest[linkedCRs.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "STRevision[" + getPath() + ", " + getNumber() + "]";
	}

	public boolean isFetched() {
		return fetched;
	}

	public void setFetched(boolean fetched) {
		this.fetched = fetched;
	}
}

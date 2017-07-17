/***************************************************************************************************
 * Copyright (c) 2006 Gunnar Wagenknecht and others.
 * All rights reserved. 
 *
 * This program and the accompanying materials are made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Gunnar Wagenknecht - initial API and implementation
 *               Eclipse.org - concepts and code used from Eclipse projects
 **************************************************************************************************/
package org.polarion.svnimporter.stprovider.internal.model;

import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * A StarTeam Change Request
 */
public class STChangeRequest implements Comparable {

	public static final Comparator BY_LAST_CHECKIN_TIME_COMPERATOR = new Comparator() {
		public int compare(Object arg0, Object arg1) {
			STChangeRequest obj1 = (STChangeRequest) arg0;
			STChangeRequest obj2 = (STChangeRequest) arg1;
			return obj1.getTimeOfFirstRevision().compareTo(obj2.getTimeOfFirstRevision());
		}
	};

	private final int number;
	private final Set linkedRevisions = new HashSet();
	private Date timeOfFirstRevision;

	/**
	 * @param number
	 */
	public STChangeRequest(int number) {
		super();
		this.number = number;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return 31 + number;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof STChangeRequest))
			return false;
		final STChangeRequest other = (STChangeRequest) obj;
		if (number != other.number)
			return false;
		return true;
	}

	/**
	 * @return the number
	 */
	public int getNumber() {
		return number;
	}

	public void linkToRevision(STRevision revision) {
		Date date = revision.getDate();
		if (null == date)
			throw new IllegalArgumentException("unable to link revision without date");

		if (null == timeOfFirstRevision || date.before(timeOfFirstRevision))
			timeOfFirstRevision = date;

		linkedRevisions.add(revision);
		revision.addLinkedChangeRequest(this);
	}

	public STRevision[] getLinkedRevisions() {
		return (STRevision[]) linkedRevisions.toArray(new STRevision[linkedRevisions.size()]);
	}

	/**
	 * @return the timeOfFirstRevision
	 */
	public Date getTimeOfFirstRevision() {
		return timeOfFirstRevision;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object object) {
		if (null == object)
			throw new IllegalArgumentException("cannot compare to null");

		STChangeRequest other = (STChangeRequest) object;
		return getNumber() - other.getNumber();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "STChangeRequest[" + getNumber() + "]";
	}
}

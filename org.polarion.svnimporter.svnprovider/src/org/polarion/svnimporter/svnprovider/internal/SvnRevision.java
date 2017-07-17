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

package org.polarion.svnimporter.svnprovider.internal;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class SvnRevision {
	/**
	 * Revision number
	 */
    private int number;

    /**
     * Revision date. This corresponds to the "svn:date" property.
     * It is stored as a Date since the programm uses it in comparisions
     * when dumping revisions up to a given date.
     */
    private Date revisionDate;

	/**
	 * Actions in revision (sorted by path)
	 * WilckensF: this does not make sense. It means that an action on
	 * branches/... comes before actions on tag/.. and these come before
	 * actions on /trunk. This means that svnimporter is not able to support
	 * file copy operations to model version labels, for these presuppose that a
	 * file is first added to trunk or to branches and then copied to tags.
	 *
	 * The actions should be a list. Then it has to be ensured that when transforming
	 * a source model into a SVN model, the actions are added in the right order.
	 */
/*	private final Collection actions = new TreeSet(new Comparator() {
		public int compare(Object o1, Object o2) {
			SvnNodeAction a1 = (SvnNodeAction) o1;
			SvnNodeAction a2 = (SvnNodeAction) o2;
			return a1.getAbsolutePath().compareToIgnoreCase(a2.getAbsolutePath());
		}
	});
*/
	private final Collection actions = new LinkedList();

	private SvnProperties properties = new SvnProperties();

    /**
     * Module for which this revision belongs 
     * useful only for incremental import for cvs with multiple modules (when cvs.modulename=*)
     */
    private String moduleName="";

    public SvnRevision() {
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public void addAction(SvnNodeAction action) {
		actions.add(action);
	}

	public String getDebugInfo() {
		StringWriter w = new StringWriter();
		PrintWriter p = new PrintWriter(w);
		p.println("Revision N " + number+" module "+moduleName);
		p.println("\tProperties: " + properties.getDebugInfo());
		p.println("\tChanges: ");
		for (Iterator i = actions.iterator(); i.hasNext();) {
			SvnNodeAction svnNodeAction = (SvnNodeAction) i.next();
			p.println("\t\t" + svnNodeAction.getDebugInfo());
		}
		return w.toString();
	}

	/**
	 * Write svn revision in dump file format to stream
	 *
	 * @param out
	 */
	public void dump(PrintStream out) {
		out.print("Revision-number: " + number + "\n");
		properties.dump(out);
		for (Iterator i = actions.iterator(); i.hasNext();) {
			SvnNodeAction svnNodeAction = (SvnNodeAction) i.next();
			svnNodeAction.dump(out);
		}
	}

	public String getAuthor() {
		return properties.get("svn:author");
	}

	public void setAuthor(String author) {
		properties.set("svn:author", SvnUtil.toUtf8(author));
	}

	public String getDate() {
		return properties.get("svn:date");
	}

    /**
     * return the revision date. As opposed to
     * getDate, a java.util.Date object is returned.
     */
    public Date getRevisionDate() {
        return revisionDate;
    }

    public void setDate(Date date) {
        revisionDate = date;
        setDate(SvnUtil.formatSvnDate(date));
    }

    // this function is private to ensure that the date is only set by
    // setRevisionDate.
	private void setDate(String date) {
		properties.set("svn:date", date);
	}

	public String getMessage() {
		return properties.get("svn:log");
	}

	public void setMessage(String message) {
		properties.set("svn:log", SvnUtil.toUtf8(message));
	}

	public Collection getActions() {
		return actions;
	}

    public SvnProperties getProperties() {
        return properties;
    }
}


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
package org.polarion.svnimporter.svnprovider;

import org.polarion.svnimporter.common.ISvnModel;
import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.common.Util;
import org.polarion.svnimporter.svnprovider.internal.SvnNodeAction;
import org.polarion.svnimporter.svnprovider.internal.SvnRevision;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Date;

/**
 * @author <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class SvnHistoryHelper {
	private static final Log LOG = Log.getLog(SvnHistoryHelper.class);

	/**
	 * Field separator in history file
	 */
	private static final String FIELD_SEPARATOR = ";";
	private static final String FIELD_SEPARATOR_REPL = "####SvnImpFieldSepRepl###";

	private static final int REVISION_FIELDS_COUNT = 5;
	private static final int ACTION_FIELDS_COUNT = 3;

//	private static final String SEPARATOR = "####";
	//private static final String SEPARATOR = FIELD_SEPARATOR;

	/**
	 * Save model's history up to a given date
	 *
	 * @param model
	 * @param file
     * @param lastDate
	 * @throws FileNotFoundException
	 */
	public static void saveIncrHistory(ISvnModel model, String file, Date lastDate) throws FileNotFoundException {
		PrintStream out = new PrintStream(new FileOutputStream(file), false);
		try {
			List revisions = model.getRevisions();
			for (int i = 0; i < revisions.size(); i++) {
				SvnRevision revision = (SvnRevision) revisions.get(i);
				if (lastDate != null && lastDate.before(revision.getRevisionDate()))
					break;
				out.println("revision"
						+ FIELD_SEPARATOR
                        + escape(revision.getModuleName())
                        + FIELD_SEPARATOR
                        + escape(revision.getAuthor())
						+ FIELD_SEPARATOR
						+ escape(revision.getDate())
						+ FIELD_SEPARATOR
						+ escape(revision.getMessage()));
				for (Iterator j = revision.getActions().iterator(); j.hasNext();) {
					SvnNodeAction action = (SvnNodeAction) j.next();
					out.println("action"
							+ FIELD_SEPARATOR
							+ escape(action.getNodeAction())
							+ FIELD_SEPARATOR
							+ escape(action.getAbsolutePath()));
				}
			}
		} finally {
			out.close();
		}
	}

    /**
     * Save model's history
     *
     * @param model
     * @param file
     * @throws FileNotFoundException
     */
    public static void saveIncrHistory(ISvnModel model, String file) throws FileNotFoundException {
        saveIncrHistory(model, file, null);
    }

	/**
	 * Load history
	 *
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private static List load(String file) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(file));
		try {
			List revisions = new ArrayList();
			lRevision rev = null;
			String line;
			int maxFieldCount = Math.max(REVISION_FIELDS_COUNT, ACTION_FIELDS_COUNT);

			while ((line = in.readLine()) != null) {

				//----- unescape special symbols ('\n', '\r', FIELD_SEPARATOR)
				String[] ss = line.split(FIELD_SEPARATOR, maxFieldCount);
				for (int i = 0; i < ss.length; i++)
					ss[i] = unescape(ss[i]);

				if (line.startsWith("revision")) {
					if (ss.length != REVISION_FIELDS_COUNT)
						throw new SvnException("wrong history data(revision line): " + line);
					rev = new lRevision();
					rev.moduleName = ss[1];
                    rev.author = ss[2];
					rev.date = ss[3];
					rev.message = ss[4];
					revisions.add(rev);
				} else if (line.startsWith("action")) {
					if (ss.length != ACTION_FIELDS_COUNT || rev == null)
						throw new SvnException("wrong history data(action line): " + line);
					String type = ss[1];
					String path = ss[2];
					rev.actions.put(path, type);
				} else {
					throw new SvnException("wrong history data(unrecognized): " + line);
				}
			}
			return revisions;
		} finally {
			in.close();
		}
	}

	/**
	 * Create incremental svn model
	 *
	 * @param fullModel   - full svn model
	 * @param historyFile - file with previous model history
	 * @return
	 * @throws IOException
	 */
	public static SvnModel createIncrModel(ISvnModel fullModel, String historyFile, Date lastDate) throws IOException {
		LOG.debug("********************* creating incremental dump ********************");
		List oldRevisions = load(historyFile);
		List newRevisions = fullModel.getRevisions();

		Map oldIds = new HashMap();
		Map newIds = new HashMap();

		// build up oldIds and newIds
		for (Iterator i = oldRevisions.iterator(); i.hasNext();) {
			lRevision lRevision = (lRevision) i.next();
			String id = getRevisionId(lRevision);
			if (oldIds.containsKey(id))
				throw new SvnException("duplicated id: " + id);
			oldIds.put(id, lRevision);
		}
		for (Iterator i = newRevisions.iterator(); i.hasNext();) {
			SvnRevision revision = (SvnRevision) i.next();
			String id = getRevisionId(revision);
			if (newIds.containsKey(id))
				throw new SvnException("duplicated id: " + id);
			newIds.put(id, revision);
		}

		SvnIncrModel incrementalModel = new SvnIncrModel(oldRevisions.size() + 1);
		int oldIndex = 0;
		int newIndex = 0;

		while (true) {
			if (oldIndex >= oldRevisions.size() && newIndex >= newRevisions.size()) {
				break;
			}
			if (oldIndex >= oldRevisions.size()) {
				// record all remaining new revisions in incrModel
				for (int i = newIndex; i < newRevisions.size(); i++) {
					SvnRevision revision = (SvnRevision) newRevisions.get(i);
					if (lastDate != null && lastDate.before(revision.getRevisionDate()))
						break;
					LOG.debug("new revision found in tail: " + revision.getDebugInfo());
					incrementalModel.addRevision(revision);
				}
				break;
			}
			if (newIndex >= newRevisions.size()) {
				LOG.warn("revisions at end of history, not present in new full model:");
				for (int i = oldIndex; i < oldRevisions.size(); i++) {
					lRevision r = (lRevision) oldRevisions.get(i);
					LOG.warn("date=" + r.date + " author=" + r.author + " message=" + r.message);
				}
				break;
			}
			//------------------------------------------------------------------------------------
			lRevision oldRevision = (lRevision) oldRevisions.get(oldIndex);
			SvnRevision newRevision = (SvnRevision) newRevisions.get(newIndex);
			String oldId = getRevisionId(oldRevision);
			String newId = getRevisionId(newRevision);

			if (oldId.equals(newId)) {
				// ---------- search new changes in same revisions --------------
				// if we find new actions, we collect them in a newly generated Revision
				// that has the same date the original one, but a new revision number.
				// This new revision will be appended to the incremental model.
				// Problem: the dates in the incremental model will not be increasing.
				//
				// When can this happen anyway? I believe only if a tag is set on a
				// file revision. Now in the source VM the tag operation is a timeless
				// operation, not attached to any date.
				// This code is intended to capture tag settings later than the previous
				// dump, but it is not able to handle the later removal of a tag.
                //
                // Tag operation is attached to revision (maybe from previous dump) which tagged (R1).
                // Since we cannot change revision from previous dump - we must create new revision (R2)
                // and add operations which not present in previous version of R1 to revision R2.
                // We don't support "removal tag" operation.
                Collection oldPaths = oldRevision.actions.keySet();
				Iterator i = newRevision.getActions().iterator();
				SvnRevision incrRevision = null;
				while (i.hasNext()) {
					SvnNodeAction newAction = (SvnNodeAction) i.next();
					String actionPath = newAction.getAbsolutePath();
					if (!oldRevision.actions.containsKey(actionPath)) {
						LOG.debug("new action found: " + newAction.getDebugInfo());
						if (incrRevision == null)
							incrRevision = incrementalModel.createRevisionClone(newRevision);
						incrRevision.addAction(newAction);
					} else {
						String oldActionType = (String) oldRevision.actions.get(actionPath);
						if (!oldActionType.equals(newAction.getNodeAction()))
							LOG.error("path '" + actionPath + "' has different action types in history and new model: "
									+ oldActionType + "!=" + newAction.getNodeAction());
						oldPaths.remove(actionPath);
					}
				}
				if (oldPaths.size() != 0) {
					LOG.error("old paths not present in new model: " + Util.toString(oldPaths));
                }
                oldIds.remove(oldId);
				newIds.remove(newId);
				oldIndex++;
				newIndex++;
			} else {
				if (oldIds.containsKey(newId)) {
					LOG.warn("skip old revision: " + oldRevision.getDebugInfo());
					oldIndex++;
				} else {
					LOG.info("new revision found: " + newRevision.getDebugInfo());
					// is it still guaranteed that the revision dates increase with
					// the revisions?
					incrementalModel.addRevision(newRevision);
					newIndex++;
				}
			}
		}
		return incrementalModel;
	}

	/** create incremental SvnModel, ignoring lastDate.
	 */
	public static SvnModel createIncrModel(ISvnModel fullModel, String historyFile) throws IOException {
		return createIncrModel(fullModel, historyFile, null);
	}

	private static class lRevision {
		String author;
		String date;
		String message;
        String moduleName;
        Map actions = new HashMap();

		String getDebugInfo() {
            StringBuffer b = new StringBuffer();
            b.append("revision: a[" + author + "] "
                    + "d[" + date + "] "
                    + "m[" + message + "] "
                    + "mod[" + moduleName + "]");
			return b.toString();
		}
	}

	private static String getRevisionId(SvnRevision revision) {
        return revision.getModuleName() + FIELD_SEPARATOR +
                revision.getDate() + FIELD_SEPARATOR +
                revision.getAuthor() + FIELD_SEPARATOR +
                revision.getMessage();
	}

    private static String getRevisionId(lRevision revision) {
        return revision.moduleName + FIELD_SEPARATOR
                + revision.date + FIELD_SEPARATOR
                + revision.author + FIELD_SEPARATOR
                + revision.message;
	}

	private static String escape(String s) {
		return escapeFieldSeparator(escapeNewline(s));
	}

	private static String escapeNewline(String s) {
		if (s == null) return null;
		s = s.replaceAll("\n", "\\\\n");
		s = s.replaceAll("\r", "\\\\r");
		return s;
	}

	private static String escapeFieldSeparator(String s) {
		if (s == null) return null;
		return s.replaceAll(FIELD_SEPARATOR, FIELD_SEPARATOR_REPL);
	}

	private static String unescape(String s) {
		return unescapeFieldSeparator(unescapeNewline(s));
	}

	private static String unescapeNewline(String s) {
		if (s == null) return null;
		s = s.replaceAll("\\\\n", "\n");
		s = s.replaceAll("\\\\r", "\r");
		return s;
	}

	private static String unescapeFieldSeparator(String s) {
		if (s == null) return null;
		return s.replaceAll(FIELD_SEPARATOR_REPL, FIELD_SEPARATOR);
	}
}

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
package org.polarion.svnimporter.pvcsprovider;

import org.polarion.svnimporter.common.IContentRetriever;
import org.polarion.svnimporter.common.IProvider;
import org.polarion.svnimporter.common.ISvnModel;
import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.common.ProviderConfig;
import org.polarion.svnimporter.common.Util;
import org.polarion.svnimporter.common.ZeroContentRetriever;
import org.polarion.svnimporter.pvcsprovider.internal.PvcsConfig;
import org.polarion.svnimporter.pvcsprovider.internal.PvcsContentRetriever;
import org.polarion.svnimporter.pvcsprovider.internal.PvcsExec;
import org.polarion.svnimporter.pvcsprovider.internal.PvcsTransform;
import org.polarion.svnimporter.pvcsprovider.internal.VlogParser;
import org.polarion.svnimporter.pvcsprovider.internal.model.PvcsFile;
import org.polarion.svnimporter.pvcsprovider.internal.model.PvcsModel;
import org.polarion.svnimporter.pvcsprovider.internal.model.PvcsRevision;
import org.polarion.svnimporter.svnprovider.SvnModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * @author <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class PvcsProvider implements IProvider {
	private static final Log LOG = Log.getLog(PvcsProvider.class);
	/**
	 * Provider's config
	 */
	private PvcsConfig config;
	/**
	 * 
	 */
	private File m_InstructionsFile; // File for saving "pcli" instructions
	private File m_InstructionsFileChecksum; // File for saving "pcli"
	// instructions checksum

	private Map m_File2rev2path; // Map PvcsFile -> (Map revision number ->
	// localFile)
	private Map m_File2rev2pathChecksum; // Map PvcsFile -> (Map revision
	// number -> localFileChecksum)

	private boolean testMode = false;

	public void setTestMode(boolean testMode) {
		this.testMode = testMode;
	}

	/**
	 * Configure provider
	 * 
	 * @param properties
	 */
	public void configure(Properties properties) {
		config = new PvcsConfig(properties);
		m_InstructionsFile = new File(config.getTempDir(), "instr.tmp");
		m_InstructionsFileChecksum = new File(config.getTempDir(),
				"instrChecksum.tmp");
	}

	/**
	 * Validate configuration
	 * 
	 * @return
	 */
	public boolean validateConfig() {
		return config.validate();
	}

	/**
	 * Get provider's configuration
	 * 
	 * @return
	 */
	public ProviderConfig getConfig() {
		return config;
	}

	/**
	 * Log environment information
	 */
	public void logEnvironmentInformation() {
		config.logEnvironmentInformation();
	}

	/**
	 * Build pvcs model
	 * 
	 * @return
	 */
	private PvcsModel buildPvcsModel() {
		File vlogFile = getVlogFile();
		File filesFile = getFilesFile();

		if (!(config.keepVlogFile() && vlogFile.exists() && filesFile.exists()))
			getLogInformation(filesFile, vlogFile);
		VlogParser parser = new VlogParser(config);
		parser.parse(filesFile, vlogFile);

		PvcsModel pvcsModel = new PvcsModel();
		for (PvcsFile pvcsFile : parser.getFiles().values()) {;
			pvcsModel.addFile(pvcsFile);
		}

		pvcsModel.finishModel();

		LOG.info("PVCS model has been created.");
		pvcsModel.printSummary();

		return pvcsModel;
	}

	protected File getFilesFile() {
		return new File(config.getTempDir(), "files.tmp");
	}

	protected File getVlogFile() {
		return new File(config.getTempDir(), "vlog.tmp");
	}

	public void listFiles(PrintStream out) {
		Collection files = buildPvcsModel().getFiles().keySet();
		for (Iterator i = files.iterator(); i.hasNext();)
			out.println(i.next());
	}

	/**
	 * Transform pvcs model to svn model
	 * 
	 * @return
	 */
	public ISvnModel buildSvnModel() {
		PvcsModel pvcsModel = buildPvcsModel();
		PvcsTransform transform = new PvcsTransform(this);
		SvnModel svnModel = transform.transform(pvcsModel);

		LOG.info("Svn model has been created");
		LOG.info("total number of revisions in svn model: "
				+ svnModel.getRevisions().size());

		//XXX Hack to fix tests
		if (!testMode) {
			// the content with one call to pcli
			m_File2rev2path = getAllContents(pvcsModel, config.getTempDir(),
					m_InstructionsFile);

			// if files should be checked get the content twice
			if (config.isValidateCheckouts()) {
				m_File2rev2pathChecksum = getAllContents(pvcsModel, config
						.getCheckoutTempDir(), m_InstructionsFileChecksum);
			}
		}

		return svnModel;
	}

	/**
	 * Save version information (generated by pcli command) to targetVlogFile
	 * and targetFilesFile. The targetFilesFile will contain the list of file
	 * names belonging to the subproject to import. The file names are as
	 * defined by the PVCS project structure. The VlogFile, on the other hand,
	 * uses the physical archive locations. The nth line in the filesFile
	 * corresponds to the nth archive described in the VlogFile. What we want to
	 * import is the directory structure as defined by the PVCS project; the
	 * physical archive locations are implementation details that will not be
	 * migrated.
	 * 
	 * @param targetVlogFile
	 */
	protected void getLogInformation(File targetFilesFile, File targetVlogFile) {

		String subproject = config.getSubproject();
		if (subproject == null)
			subproject = "";

		if (!subproject.startsWith("/"))
			subproject = "/" + subproject;

		// write the targetFilesFile
		executeCommand(new String[] { config.getExecutable(), "run",
				"->" + getPvcsPath(targetFilesFile.getAbsolutePath()), "-q",
				"listversionedfiles",
				"-pr" + getPvcsPath(config.getProjectPath()),
				getLogonString(false), "-l", "-z", getPvcsPath(subproject) });

		// write the targetVlogFile
		executeCommand(new String[] { config.getExecutable(), "run",
				"->" + getPvcsPath(targetVlogFile.getAbsolutePath()), "-q",
				"vlog", "-pr" + getPvcsPath(config.getProjectPath()),
				getLogonString(false), "-z", getPvcsPath(subproject) });
	}

	/**
	 * Create content retriever for revision
	 * 
	 * @param revision
	 * @return
	 */
	public IContentRetriever createContentRetriever(PvcsRevision revision) {
		if (config.isUseOnlyLastRevisionContent() && !revision.isLastRevision()) {
			return ZeroContentRetriever.INSTANCE;
		} else {
			return new PvcsContentRetriever(this, revision);
		}
	}

	private String getLogonString(boolean useQuotes) {
		String userId = config.getUserName();
		if (userId == null)
			return "";
		if (config.getPassword() != null) {
			userId += ":" + config.getPassword();
		}
		String quotes = (useQuotes ? "\"" : "");
		return ("-id" + quotes + userId + quotes);
	}

	/**
	 * Retrieve all content of all revisions from model in temp dir
	 * 
	 * @param model
	 */
	private Map getAllContents(PvcsModel model, File tempDir,
			File instructionsFile) {
		LOG.debug("get all contents for " + tempDir.getAbsolutePath());
		Map f2r2p = new HashMap();

		try {
			PrintWriter out = new PrintWriter(new FileOutputStream(
					instructionsFile));

			// set variable DOLLAR so that it can be used by getPvcsPath(..
			// ,true) for
			// escaping $ characters in pathnames; in pcli skripts it is not
			// sufficient
			// to replace $ characters by '$'.
			out.println("set -vDOLLAR '$'");

			try {
				model.getFiles(); // TODO: check if this really nessecary

				for (Iterator i = model.getFiles().values().iterator(); i
						.hasNext();) {
					PvcsFile file = (PvcsFile) i.next();

					Map rev2path = new HashMap();
					f2r2p.put(file, rev2path);

					for (Iterator j = file.getRevisions().values().iterator(); j
							.hasNext();) {
						PvcsRevision revision = (PvcsRevision) j.next();
						String revNum = revision.getNumber();

						File localFile = getLocalFile(revision, tempDir);
						rev2path.put(revision.getNumber(), localFile);

						String pvcsPath = ((PvcsFile) revision.getModelFile())
								.getPvcsPath();
						if (!pvcsPath.startsWith("/")) {
							pvcsPath = "/" + pvcsPath;
						}

						out.print("run get ");
						out.print("-pr\"'"
								+ getPvcsPath(config.getProjectPath(), true)
								+ "'\" ");
						out
								.print("-a\""
										+ getPvcsPath(localFile
												.getAbsolutePath(), true)
										+ "\" ");
						out.print("-r\"" + revNum + "\" ");
						out.print(getLogonString(true) + " ");
						out.print("\"" + getPvcsPath(pvcsPath, true) + "\"");
						out.println();
						localFile.getParentFile().mkdirs();
					}
				}
			} finally {
				out.close();
			}

			executeCommand(new String[] { config.getExecutable(), "run",
					"-s" + instructionsFile.getAbsolutePath() });

			return f2r2p;
		} catch (FileNotFoundException e) {
			throw new PvcsException(e);
		}
	}

	/**
	 * Checkout file revision into temp dir
	 * 
	 * @param revision
	 * @return
	 */
	public File checkout(PvcsRevision revision) throws IOException {
		File alreadyReceivedFile = getFileFromMap(revision, m_File2rev2path,
				"PvcsProvider.checkout():");

		if (alreadyReceivedFile == null) {
			return null;
		}

		LOG.info("  PvcsProvider.checkout() => : " + alreadyReceivedFile);

		if (config.isValidateCheckouts()) {
			LOG.info("Validating checkout...");

			// Determine the checksum from the file list
			String firstChecksum = Util.md5checksum(alreadyReceivedFile);

			// Determine the checksum from the checksum file list
			File checksumFile = getFileFromMap(revision,
					m_File2rev2pathChecksum, "PvcsProvider.checkoutChecksum():");

			if (checksumFile == null) {
				return null;
			}

			String secondChecksum = Util.md5checksum(checksumFile);

			// check if the checksums match
			if (secondChecksum.equals(firstChecksum) == false) {
				LOG.error("The checksums of file the " + alreadyReceivedFile
						+ " revision " + revision.getNumber()
						+ " doesn't match !!! [" + firstChecksum + "] != ["
						+ secondChecksum + "]");

				return null;
			}

			LOG.info("chechkSum validation successfully completed");
		}

		return alreadyReceivedFile;
	}

	private File getFileFromMap(PvcsRevision revision, Map map,
			String logMessage) {
		Map rev2path = (Map) map.get(revision.getModelFile());

		if (rev2path == null) {
			LOG
					.error(logMessage
							+ "rev2path == null => getContent - Problem !");
			return null;
		}

		File mapFile = (File) rev2path.get(revision.getNumber());

		if (mapFile == null) {
			LOG.error(logMessage + "File not found");
			return null;
		}
		if (mapFile.exists() == false) {
			LOG.error(logMessage + "File " + mapFile.getAbsolutePath()
					+ " doesn't exist.");
			return null;
		}
		if (mapFile.isFile() == false) {
			LOG.error(logMessage + "File " + mapFile.getAbsolutePath()
					+ " is not a file.");
			return null;
		}

		return mapFile;
	}

	/**
	 * @param file
	 * @param revisionNumber
	 * @param pvcsPath
	 * @return true - if checkout was successful
	 */
	private boolean checkout(File file, String revisionNumber, String pvcsPath) {
		// do NOT use quotes to surround arguments here. Each String in the
		// array will
		// already be treated as a single argument, even if it contains blanks.
		executeCommand(new String[] { config.getExecutable(), "get",
				"-pr" + getPvcsPath(config.getProjectPath()),
				"-a" + getPvcsPath(file.getAbsolutePath()),
				"-r" + revisionNumber, getLogonString(false),
				getPvcsPath(pvcsPath) });
		return file.exists();
	}

	private String getPvcsPath(String path) {
		return getPvcsPath(path, false);
	}

	private String getPvcsPath(String path, boolean replaceDollarByVariable) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < path.length(); ++i) {
			char c = path.charAt(i);
			switch (c) {
			// escape single quote
			case '\'':
				buf.append("\\'");
				break;
			// escpape double quotes. They cannot be part of a Windows file
			// name, but
			// of a Unix file name.
			case '"':
				buf.append("\\\"");
				break;
			// surround '$' with single quotes or replace it by '$DOLLAR' since
			// $ has a special
			// meaning to the pcli interpreter; if '$DOLLAR' is used the
			// variable must be set before
			case '$':
				if (replaceDollarByVariable == true) {
					buf.append("'$DOLLAR'");
				} else {
					buf.append("'$'");
				}
				break;
			// replace '\' by '/'. This works even on Windows. The advantage is
			// that
			// a single quote after backslash (as path separator) will not be
			// interpreted as
			// an escaped single quote. Thus "c:\path\$1.txt" will be
			// interpreted correctly
			// (transformed into "c:/path/'$'1.txt" and not into
			// "c:\path\'$'.txt").
			case '\\':
				buf.append('/');
				break;
			default:
				buf.append(c);
			}
		}
		return buf.toString();
	}

	/**
	 * Return path to local copy
	 * 
	 * @param pvcsRevision
	 * @return
	 */
	private File getLocalFile(PvcsRevision pvcsRevision, File tempDir) {
		File localFilePr = new File(tempDir, pvcsRevision.getPath());
		String revNum = pvcsRevision.getNumber();
		return new File(localFilePr.getAbsolutePath() + "_rev"
				+ revNum.replaceAll("[^0-9]", "_"));
	}

	/**
	 * Exec pcli command
	 * 
	 * @param cmd
	 */
	private void executeCommand(String[] cmd) {
		PvcsExec exec = new PvcsExec(cmd);
		exec.setWorkdir(config.getTempDir());
		exec.setVerboseExec(config.isVerboseExec());
		exec.exec();
		if (exec.getErrorCode() != 0) {
			if (exec.getErrorCode() == PvcsExec.ERROR_EXCEPTION)
				throw new PvcsException("error during execution command "
						+ Util.toString(exec.getCmd(), " ")
						+ ", exception caught", exec.getException());
			else if (exec.getErrorCode() == PvcsExec.ERROR_WRONG_PROJECT_PATH)
				throw new PvcsException("error during execution command "
						+ Util.toString(exec.getCmd(), " ")
						+ ": wrong project path \"" + config.getProjectPath()
						+ "\"");
			else
				throw new PvcsException("error during execution command "
						+ Util.toString(exec.getCmd(), " "));
		}
		if (exec.getRc() != 0) {
      throw new PvcsException("Process exit code: " + exec.getRc());
    }
	}

	/**
	 * Cleanup
	 */
	public void cleanup() {
		LOG.debug("cleanup");
		File tempDir = config.getTempDir();
		if (!Util.delete(tempDir)) {
			LOG.error("can't delete temp dir: " + tempDir.getAbsolutePath());
		}

		if (config.isValidateCheckouts() == true) {
			File tempDirCheckOut = config.getCheckoutTempDir();

			if (!Util.delete(tempDirCheckOut)) {
				LOG.error("can't delete temp dir: "
						+ tempDirCheckOut.getAbsolutePath());
			}
		}
	}
}

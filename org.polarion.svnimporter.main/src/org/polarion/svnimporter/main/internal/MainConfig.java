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

package org.polarion.svnimporter.main.internal;

import org.polarion.svnimporter.common.ConfigUtil;
import org.polarion.svnimporter.common.IProvider;
import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.common.Util;
import org.polarion.svnimporter.svnprovider.SvnAdmin;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParsePosition;
import java.util.Date;
import java.util.Properties;

/**
 *
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class MainConfig {
	private static final Log LOG = Log.getLog(MainConfig.class);

	private static final String SVNIMPORTER_VERSION = "svnimporter 1.1-SHAPSHOT (after M8)";
	/**
	 * Command line options
	 */
	private static final String ONLY_LIST_MODE = "list";
	private static final String FULL_DUMP_MODE = "full";
	private static final String INCREMENTAL_DUMP_MODE = "incremental";

	private static final String HELP_MODE = "help";
	private static final String VERSION_MODE = "version";

	/**
	 * Config file keys
	 */
	private static final String FULL_DUMP_FILE_KEY = "full.dump.file";
	private static final String INCR_DUMP_FILE_KEY = "incr.dump.file";
	private static final String INCR_HISTORY_FILE_KEY = "incr.history.file";
	private static final String LIST_FILES_TO_KEY = "list.files.to";
	private static final String SRC_PROVIDER_KEY = "srcprovider";
	private static final String DUMPFILE_SIZE_LIMIT = "dump.file.sizelimit.mb";
	private static final String USAGE = "usage: command config_file [lastdate]"
			+ "\n\nCommands:"
			+ "\n\t" + ONLY_LIST_MODE + " - dont create dump, only list files"
			+ "\n\t" + FULL_DUMP_MODE + " - create full dump [revisions up to lastdate]"
			+ "\n\t" + INCREMENTAL_DUMP_MODE + " - create incremental dump [revisions up to lastdate]"
			+ "\n\t" + HELP_MODE + " - show help"
			+ "\n\t" + VERSION_MODE + " - show version";

	private static final String AUTOPROPS_FILE_KEY = "config.autoprops";
	/**
	 * Path to full dump file. Can be a file pattern containing "%date%".
	 */
	private String fullDumpFilePattern;

	/**
	 * Path to incremental dump file. Can be a file pattern containing "%date%".
	 */
	private String incrlDumpFilePattern;

	/**
	 * Path to (incremental) history file
	 */
	private String incrHistoryFile;

	/**
	 * Path to autoprops file
	 */
	private String autopropsFile;
	/**
	 * Enable full dump mode
	 */
	private boolean fullDump;

	/**
	 * Enable incemental dump mode
	 */
	private boolean incrementalDump;

	/**
	 * Enable "only list files" mode
	 */
	private boolean onlyListFiles;

	/**
	 * Target file for saving scm's files list
	 */
	private String listFilesTo;

	/**
	 * Enable "import dump into svn" mode
	 */
	private boolean importDump;
	private boolean existingSvnrepos;

	/**
	 * If true then do not cleanup provider's temp files
	 */
	private boolean disableCleanup;

	/**
	 * Source scm provider
	 */
	private IProvider srcProvider;

	private boolean clearSvnParentDir;

	/**
	 * SvnAdmin
	 */
	private SvnAdmin svnAdmin;

	private Log historyLogger;

	private Properties srcProperties;

	/**
	 * consider only revision up to this date.
	 */
	private Date lastRevDate;

	/**
	 * size limit for dump files in Megabyte.
	 */
	private int dumpFileSizeLimit;


	public MainConfig() {
	}

	/**
	 * Parse command line arguments
	 *
	 * @param args
	 */
	public void parseArgs(String[] args) {
		if (args.length < 1) {
			usage("You must specify command");
			return;
		}
		String firstArg = args[0];
		if (ONLY_LIST_MODE.equals(firstArg)) {
			onlyListFiles = true;
		} else if (FULL_DUMP_MODE.equals(firstArg)) {
			fullDump = true;
		} else if (INCREMENTAL_DUMP_MODE.equals(firstArg)) {
			incrementalDump = true;
		} else if (HELP_MODE.equals(firstArg)) {
			usage(null);
			return;
		} else if (VERSION_MODE.equals(firstArg)) {
			showVersion();
			return;
		} else {
			usage("Unknown command \"" + firstArg + "\"");
			return;
		}

		if (args.length < 2) {
			usage("You must specify config file as second parameter");
			return;
		}

		if (args.length > 3) {
			StringBuffer b = new StringBuffer();
			b.append("unknown command line parameters: ");
			for (int i = 2; i < args.length; i++) {
				b.append(args[i] + " ");
			}
			usage(b.toString());
			return;
		}

		String configFile = args[1];
		try {
			srcProperties = Util.loadProperties(configFile);
			Log.configure(srcProperties);
			historyLogger = Log.getLog("historyLogger");
			parseConfig();
		} catch (IOException e) {
			error("can't open config file \"" + configFile + "\"", e);
		}

		if (args.length == 3) {
			SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss");
			lastRevDate = fmt.parse(args[2], new ParsePosition(0)); // expected Day in ISO format
			if (lastRevDate == null) {
				usage("ERROR: Cannot convert " + args[2] + " to date. Expected format: \"yyyy-MM-dd'T'kk:mm:ss\"");
				return;
			}
		}
	}

	/**
	 * if true - then configuration has errors
	 */
	private boolean hasErrors = false;

	/**
	 * Validate configuration
	 *
	 * @return list of errors
	 */
	public boolean validate() {
		return !hasErrors;
	}

	/**
	 * Print error
	 *
	 * @param message
	 * @param e
	 */
	private void error(String message, Exception e) {
		LOG.error(message, e);
		hasErrors = true;
	}

	/**
	 * Print error
	 *
	 * @param message
	 */
	private void error(String message) {
		LOG.error(message);
		hasErrors = true;
	}

	/**
	 * Print usage instruction
	 */
	private void usage(String error) {
		if (error != null)
			System.err.println(error + "\n");
		System.err.println(USAGE);
		hasErrors = true;
	}

	private void showVersion() {
		System.out.println(SVNIMPORTER_VERSION);
		hasErrors = true;
	}

	/**
	 * 'Nice suffix' date format
	 */
	private final DateFormat dateStringFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
	private String dateString;

	/**
	 * Return date string for insert as 'nice suffix' in filenames
	 *
	 * @return
	 */
	private String getDateString() {
		if (dateString == null)
			dateString = dateStringFormat.format(new Date());
		return dateString;
	}

	public DateFormat getDateStringFormat() {
		return dateStringFormat;
	}
	/**
	 * Set incremental history file
	 */
	private void setIncrHistoryFile() {
		String s = getStringProperty(INCR_HISTORY_FILE_KEY);
		if (s == null) return;
		incrHistoryFile = s;
	}

	/**
	 * Set target file for saving scm's files list
	 */
	private void setListFilesTo() {
		String s = getStringProperty(LIST_FILES_TO_KEY);
		if (s == null) return;
		listFilesTo = insertDate(s);
	}

	/**
	 * Configure source scm provider
	 */
	private void setSrcProvider() {
		String providerName = getStringProperty(SRC_PROVIDER_KEY);
		if (providerName == null) return;
		String providerClass = getStringProperty(providerName + ".class");
		if (providerClass == null) return;

		try {
			Class c = Class.forName(providerClass);
			Object o = c.newInstance();
			if (!(o instanceof IProvider)) {
				error("class \"" + providerClass + "\" does not implement IProvider interface");
				return;
			}
			srcProvider = (IProvider) o;

			if (!isFullDump())
				ConfigUtil.setBooleanProperty(srcProperties, "useOnlyLastRevisionContent", false);//small hack, see ProviderConfig.configure()

			srcProvider.configure(srcProperties);
			if (!srcProvider.validateConfig()) {
				hasErrors = true;
			}
		} catch (Exception e) {
			error("can't create new instance of provider class \"" + providerClass + "\"", e);
		}
	}

	/**
	 * Configure dump file, src provider, ...
	 */
	private void parseConfig() {

		fullDumpFilePattern = getStringProperty(FULL_DUMP_FILE_KEY);
		incrlDumpFilePattern = getStringProperty(INCR_DUMP_FILE_KEY);
		autopropsFile = getStringProperty(AUTOPROPS_FILE_KEY, false);
		setIncrHistoryFile();
		setSrcProvider();
		setListFilesTo();

		disableCleanup = getBooleanProperty("disable_cleanup");

		importDump = getBooleanProperty("import_dump_into_svn");
		existingSvnrepos = getBooleanProperty("existing_svnrepos");
		clearSvnParentDir = getBooleanProperty("clear_svn_parent_dir");

		dumpFileSizeLimit = getIntProperty(DUMPFILE_SIZE_LIMIT);
		if (dumpFileSizeLimit < 0)
			error("configuration property value \"" + DUMPFILE_SIZE_LIMIT + "\" may not be negative.");

		if (importDump && (isFullDump() || isIncrementalDump())) {
			svnAdmin = new SvnAdmin();
			svnAdmin.configure(srcProperties);
			if (!svnAdmin.validateConfig())
				hasErrors = true;
		}
	}

	public String getFullDumpFilePattern() {
		return fullDumpFilePattern;
	}

	public String getIncrDumpFilePattern() {
		return incrlDumpFilePattern;
	}

	public String getIncrHistoryFile() {
		return incrHistoryFile;
	}

	public String getAutopropsFile() {
		return autopropsFile;
	}

	public boolean isFullDump() {
		return fullDump;
	}

	public boolean isIncrementalDump() {
		return incrementalDump;
	}

	public boolean isOnlyListFiles() {
		return onlyListFiles;
	}

	public String getListFilesTo() {
		return listFilesTo;
	}

	public boolean isImportDump() {
		return importDump;
	}

	public boolean isExistingSvnrepos() {
		return existingSvnrepos;
	}

	public boolean isDisableCleanup() {
		return disableCleanup;
	}

	public boolean isClearSvnParentDir() {
		return clearSvnParentDir;
	}

	public Log getHistoryLogger() {
		return historyLogger;
	}

	public IProvider getSrcProvider() {
		return srcProvider;
	}

	public SvnAdmin getSvnAdmin() {
		return svnAdmin;
	}

	public Date getLastRevisionDate() {
		return lastRevDate;
	}

	public int getDumpFileSizeLimit() {
		return dumpFileSizeLimit;
	}

	/**
	 * Log environment information
	 */
	public void logEnvironmentInformation() {
		LOG.info("****************************************************************************");
		LOG.info("*** Global options ***");
		LOG.info("Mode = " + getMode());
		LOG.info("Import dump into svn = \"" + importDump + "\"");
		LOG.info("Import dump only if svn repository exist = \"" + existingSvnrepos + "\"");
		LOG.info("Full dump path = \"" + fullDumpFilePattern + "\"");
		LOG.info("Incremental dump path = \"" + incrlDumpFilePattern + "\"");
		LOG.info("Incremental history path = \"" + incrHistoryFile + "\"");
		LOG.info("Save files list to = \"" + listFilesTo + "\"");
		LOG.info("Source provider's class = \"" + srcProvider.getClass().getName() + "\"");
		if (lastRevDate != null)
			LOG.info("Date of last revision to dump = \"" + lastRevDate.toString() + "\"");
		LOG.info("Size limit for dump files (in MB): "
			+ (dumpFileSizeLimit > 0 ? Integer.toString(dumpFileSizeLimit) : "none"));
		srcProvider.logEnvironmentInformation();
		if (svnAdmin != null) {
			svnAdmin.logEnvironmentInformation();
		}
		LOG.info("****************************************************************************");
	}

	public String getMode() {
		String mode = "";
		if (incrementalDump) mode = "create incremental dump";
		if (fullDump) mode = "create full dump";
		if (onlyListFiles) mode = "list files";
		return mode;
	}

	private String getStringProperty(String key, boolean mandatory) {
		String value = srcProperties.getProperty(key);
		if (value == null || value.length() < 1) {
			if (mandatory)
				error("configuration property \"" + key + "\" is not set");
			value = null;
		} else {
			value = value.trim();
		}
		return value;
	}

	private String getStringProperty(String key) {
		return getStringProperty(key, true);
	}

	protected boolean getBooleanProperty(String key) {
		return ConfigUtil.getBooleanProperty(srcProperties, key);
	}

	private int getIntProperty(String key) {
		String val = getStringProperty(key);
		if (val == null) return 0;
		try {
			return Integer.parseInt(val);
		} catch (NumberFormatException e) {
			error("configuration property value \"" + key + "\" is not a valid integer.");
			return 0;
		}
	}

	private String insertDate(String value) {
		int indexOfDate = value.indexOf("%date%");
		if (indexOfDate != -1) {
			return value.replaceAll("%date%", getDateString());
		} else {
			return value;
		}
	}
}


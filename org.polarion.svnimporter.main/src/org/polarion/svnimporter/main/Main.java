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
package org.polarion.svnimporter.main;

import org.polarion.svnimporter.common.ISvnModel;
import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.common.Playback;
import org.polarion.svnimporter.common.Timer;
import org.polarion.svnimporter.common.Util;
import org.polarion.svnimporter.main.internal.MainConfig;
import org.polarion.svnimporter.svnprovider.SvnAdmin;
import org.polarion.svnimporter.svnprovider.SvnDump;
import org.polarion.svnimporter.svnprovider.SvnHistoryHelper;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;

/**
 *
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class Main {
	private static final Log LOG = Log.getLog(Main.class);

	public static void main(String[] args) {
	    int status = 1;
		Main main = new Main();
		if (main.configure(args)) {
			status = main.run();
		}
		System.exit(status);
	}

	/**
	 * Configuration
	 */
	private MainConfig config;

	/**
	 * Configure
	 *
	 * @return false if config is invalid
	 */
	private boolean configure(String[] args) {
		config = new MainConfig();
		config.parseArgs(args);
		return config.validate();
	}

	/**
	 * Run
	 */
	private int run() {
	    int status = 0;
	    
		config.logEnvironmentInformation();

		startHistory();
		Timer timer = new Timer();
		timer.start();

		try {
			if (config.isOnlyListFiles()) {
				listFiles();
			} else if (config.isFullDump()) {
				ISvnModel fullModel = buildFullSvnModel();
				if (fullModel.isEmpty()) {
					recordHistory("empty svn model - nothing to import");
				} else {
					recordHistory("saving svn model to file...");
					SvnDump fullDump = new SvnDump(config.getFullDumpFilePattern(),
														//config.getIncrDumpFilePattern(),
														config.getDateStringFormat(),
														config.getDumpFileSizeLimit());
					saveDump(fullModel, fullDump);

					if (config.isImportDump()) {
						importDump(fullDump);
					}

					SvnHistoryHelper.saveIncrHistory(fullModel, config.getIncrHistoryFile(), config.getLastRevisionDate());
				}
			} else if (config.isIncrementalDump()) {
				ISvnModel fullModel = buildFullSvnModel();
				if (fullModel.isEmpty()) {
					recordHistory("empty svn model - nothing to import");
				} else {
					recordHistory("creating incremental svn model...");
                    ISvnModel incrementalModel = SvnHistoryHelper.createIncrModel(fullModel,
                            config.getIncrHistoryFile(),
                            config.getLastRevisionDate());
					if (incrementalModel.isEmpty()) {
						recordHistory("no changes detected for incremental dump");
					} else {
						SvnDump incrDump = new SvnDump(config.getIncrDumpFilePattern(),
																config.getDateStringFormat(),
																config.getDumpFileSizeLimit());
						recordHistory("saving incremental dump to file...");
						saveDump(incrementalModel, incrDump);

						if (config.isImportDump()) {
							importDump(incrDump);
						}

						recordHistory("saving incremental history to file...");
						SvnHistoryHelper.saveIncrHistory(fullModel, config.getIncrHistoryFile(), config.getLastRevisionDate());
					}
				}
			}
			recordHistory("successfully finished");
		} catch (Throwable t) {
			LOG.error("EXCEPTION CAUGHT: " + Util.getStackTrace(t));
			status = 1;
		}
		if (!config.isDisableCleanup()) {
			config.getSrcProvider().cleanup();
			if (config.getSvnAdmin() != null)
				config.getSvnAdmin().cleanup();
		}
		
		// Close playback files
		Playback.getInstance().close();

		timer.stop();
		recordHistory("duration: " + timer.getDuration() + " seconds");
		return status;
	}

	/**
	 * Build svn model
	 *
	 * @return
	 */
	private ISvnModel buildFullSvnModel() {
		recordHistory("creating full svn model...");
		return config.getSrcProvider().buildSvnModel();
	}

	/**
	 * List files
	 */
	private void listFiles() {
		LOG.info("List files to " + config.getListFilesTo());
		PrintStream out = Util.openPrintStream(config.getListFilesTo());
		if (out == null) return;
		try {
			config.getSrcProvider().listFiles(out);
		} finally {
			out.close();
		}
	}

	/**
	 * Import dump into svn repository by svnadmin
	 *
	 * @param dump
	 */
	private void importDump(SvnDump dump) {
		if (checkSvnRepository()) {
			recordHistory("import dump into svn...");
			config.getSvnAdmin().importDump(dump);
			recordHistory("svnadmin import dump finished");
		} else {
			recordHistory("import aborted on repository check");
		}
	}

	/**
	 * Create svn repository if need, ...
	 *
	 * @return
	 */
	private boolean checkSvnRepository() {
		SvnAdmin svnadmin = config.getSvnAdmin();
		recordHistory("check repository...");
		if (!svnadmin.isRepositoryExists()) {
			recordHistory("repository is not exist");
			if (config.isExistingSvnrepos()) {
				recordHistory("aborting import");
				return false;
			}
			recordHistory("creating new svn repository...");
			svnadmin.createRepository();
		} else {
			recordHistory("repository is exist");
		}
		recordHistory("check parent dir in repository...");
		if (!svnadmin.isParentDirExists()) {
			recordHistory("parent dir is not exist, creating...");
			svnadmin.createParentDir();
		} else {
			recordHistory("parent dir is exist");
			if (config.isFullDump() && config.isClearSvnParentDir()) {
				recordHistory("clear svn parent dir...");
				svnadmin.clearParentDir();
			}
		}
		return true;
	}


	/**
	 * Save dump to file
	 *
	 * @param svnModel
   * @param svnDump
	 * @throws IOException
	 */
	private void saveDump(ISvnModel svnModel, SvnDump svnDump) {
			svnDump.dump(svnModel, config.getLastRevisionDate());
	}

	/**
	 * Write line to history file
	 *
	 * @param line
	 */
	private void recordHistory(String line) {
		config.getHistoryLogger().info(line);
	}

	/**
	 * Write history file header
	 */
	private void startHistory() {
		recordHistory("**********************************************************************");
		recordHistory("date: " + Util.toString(new Date()));
		recordHistory("mode: " + config.getMode());
		recordHistory("src provider: " + config.getSrcProvider().getClass());
	}
}


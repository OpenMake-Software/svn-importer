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

import org.polarion.svnimporter.common.BooleanWrapper;
import org.polarion.svnimporter.common.Exec;
import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.common.StreamConsumer;
import org.polarion.svnimporter.common.Util;
import org.polarion.svnimporter.common.ISvnAdmin;
import org.polarion.svnimporter.common.ISvnDump;
import org.polarion.svnimporter.svnprovider.internal.svnadmin.SvnAdminConfig;
import org.polarion.svnimporter.svnprovider.internal.svnadmin.SvnAdminImportExec;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Properties;

/**
 *
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class SvnAdmin implements ISvnAdmin {
	private static final Log LOG = Log.getLog(SvnAdmin.class);

	private SvnAdminConfig config;

	public void configure(Properties props) {
		config = new SvnAdminConfig(props);
	}

	public boolean validateConfig() {
		return config.validate();
	}

	/**
	 * Log environment information
	 */
	public void logEnvironmentInformation() {
		config.logEnvironmentInformation();
	}

	/**
	 * Execute "svnadmin import" command
	 *
	 * @param dumpfile
	 */
	public void importDump(ISvnDump dump) {
		String parentDir = config.getParentDir();
		if ("".equals(parentDir)) parentDir = "/";
        String[] cmd = new String[]{config.getAdminExecutable(),
                "load",
                //"-q",/*quiet*/
                "--parent-dir", parentDir,
                config.getRepositoryPath().getAbsolutePath()};
		for (Iterator i = dump.getFileNames().iterator(); i.hasNext(); ) {
			String dumpFile = (String)i.next();
			LOG.info("importing dump file \"" + dumpFile + "\" ...");
            SvnAdminImportExec exec = new SvnAdminImportExec(cmd, new File(dumpFile));
            exec.setTimeout(config.getImportTimeout());
            File workDir = new File(".");
            executeCommand(exec, workDir, true);
		}
	}

	/**
	 * Create repository url
	 *
	 * @return
	 */
	private String getRepositoryUrl() {
		String path = config.getRepositoryPath().getAbsolutePath();
		String url = "file:///" + path.replaceAll("\\\\", "/");
		if (url.endsWith("/"))
			url = url.substring(0, url.length() - 1);
		return url;
	}

	/**
	 * IsRepositoryExists?
	 *
	 * @return
	 */
	public boolean isRepositoryExists() {
		File repositoryLocation = config.getRepositoryPath();
		if (!repositoryLocation.exists()) return false;
		if (!repositoryLocation.isDirectory()) {
			throw new SvnAdminException("svn repository location is not a directory: " + repositoryLocation.getAbsolutePath());
		}
		File files[] = repositoryLocation.listFiles();
		if (files == null || files.length == 0) {
			//empty directory
			return false;
		}

		File workdir = config.getTempDir();
		String repositoryUrl = getRepositoryUrl();
		String module = repositoryUrl.substring(repositoryUrl.lastIndexOf("/"));
		File checkoutDir = new File(workdir, module);
		if (checkoutDir.exists() && !Util.delete(checkoutDir)) {
			throw new SvnException("can't delete directory: " + checkoutDir.getAbsolutePath());
		}

		String[] cmd = new String[]{config.getClientExecutable(),
									"co",
									"--non-recursive",
									"--non-interactive",
									repositoryUrl};
		executeCommand(new Exec(cmd), workdir, false);

		if (!checkoutDir.exists() || !checkoutDir.isDirectory()) {
			throw new SvnAdminException("svn repository is not valid: " + repositoryUrl);
		}
		return true;
	}

	/**
	 * Execute "svnadmin create PATH_TO_REPOSITORY" command
	 */
	public void createRepository() {
		File repository = config.getRepositoryPath();

		Exec exec = new Exec(new String[]{
			config.getAdminExecutable(),
			"create",
			repository.getAbsolutePath()
		});

		executeCommand(exec, new File("."), true);

		if (!repository.exists() || !repository.isDirectory()) {
			throw new SvnAdminException("repository creation failed: "
					+ repository.getAbsolutePath());
		}
	}

	/**
	 * Check that parent dir (for import) is exists in svn repository
	 *
	 * @return
	 */
	public boolean isParentDirExists() {
		return isRepositoryPathExists(config.getParentDir());
	}

	/**
	 * Create parent dir (for import) in repository
	 */
	public void createParentDir() {
		String parentDir = config.getParentDir();
		if ("".equals(parentDir)) {
			throw new SvnAdminException("can't create svn root directory");
		}
		String dirs[] = parentDir.split("/");

		String curPath = "";
		for (int i = 0; i < dirs.length; i++) {
			if (curPath.length() > 0) curPath += "/";
			curPath += dirs[i];
			if (!isRepositoryPathExists(curPath)) {
				createRepositoryPath(curPath);
			}
		}
	}

	/**
	 * Clear parent dir
	 */
	public void clearParentDir() {
		clearDir(config.getParentDir());
	}

	/**
	 * Clear dir
	 */
	private void clearDir(String path) {
		List children = getChildList(path);
		for (int i = 0; i < children.size(); i++) {
			String childPath = (String) children.get(i);
			if (".polarion".equals(childPath))
				continue;
			deleteRepositoryPath(path + "/" + childPath);
		}
	}

	/**
	 * Return list of path's child entires
	 *
	 * @return
	 */
	private List getChildList(String path) {
		String url = getRepositoryUrl() + "/" + path;
		Exec exec = new Exec(new String[]{
			config.getClientExecutable(),
			"ls",
			"--non-interactive",
			url
		});
		final List children = new ArrayList();
		exec.setStdoutConsumer(new StreamConsumer() {
			public void consumeLine(String line) {
				String child = line;
				if (child.endsWith("/")) child = child.substring(0, child.length() - 1);
				if (child.startsWith("/")) child = child.substring(1, child.length());
				children.add(child);
			}
		});
		executeCommand(exec, config.getTempDir(), true);
		return children;
	}

	/**
	 * Delete repository path
	 *
	 * @param path
	 */
	private void deleteRepositoryPath(String path) {
		LOG.info("deleting repository path: " + path);
		String url = getRepositoryUrl() + "/" + path;
		Exec exec = new Exec(new String[]{
			config.getClientExecutable(),
			"delete",
			"-m", "deleting " + path,
			"--non-interactive",
			url
		});
		executeCommand(exec, config.getTempDir(), true);
	}

	/**
	 * Check that the svn repository path is exist
	 *
	 * @param path
	 * @return
	 */
	private boolean isRepositoryPathExists(String path) {
		String url = getRepositoryUrl() + "/" + path;
		String[] cmd = new String[]{
			config.getClientExecutable(),
			"ls",
			"--non-interactive",
			url
		};
		final BooleanWrapper exists = new BooleanWrapper(true);
		final BooleanWrapper error = new BooleanWrapper(false);
		final StringBuffer b = new StringBuffer();
		Exec exec = new Exec(cmd);
		exec.setStderrConsumer(new StreamConsumer() {
			public void consumeLine(String line) {
				if (line.startsWith("svn: URL")
                        && line.endsWith(config.getPathNotExistSignature())) {
                        //&& line.endsWith("non-existent in that revision")) {
					exists.setValue(false);
				} else {
					if (b.length() > 0) b.append("\n");
					b.append(line);
					error.setValue(true);
				}
			}
		});
		executeCommand(exec, config.getTempDir(), false);
		if (error.getValue()) {
			throw new SvnAdminException("error during execution 'svn ls' command: " + b);
		}
		return exists.getValue();
	}

	/**
	 * Create directory in svn repository (using "svn mkdir" command)
	 *
	 * @param path
	 */
	private void createRepositoryPath(String path) {
		String url = getRepositoryUrl() + "/" + path;
		LOG.info("creating repository path: " + path);
		String[] cmd = new String[]{
			config.getClientExecutable(),
			"mkdir",
			"--non-interactive",
			"-m", "creating path '" + path + "'",
			url
		};
		final BooleanWrapper error = new BooleanWrapper(false);
		final StringBuffer b = new StringBuffer();
		Exec exec = new Exec(cmd);
		exec.setStderrConsumer(new StreamConsumer() {
			public void consumeLine(String line) {
				if (b.length() > 0) b.append("\n");
				b.append(line);
				error.setValue(true);
			}
		});
		executeCommand(exec, config.getTempDir(), true);
		if (error.getValue()) {
			throw new SvnAdminException("can't create svn parent directory: " + b);
		}
	}

	/**
	 * Execute command
	 *
	 * @param exec
	 */
	private void executeCommand(Exec exec, File workDir, boolean checkRC) {
		exec.setVerboseExec(config.isVerboseExec());
		exec.setWorkdir(workDir);
		exec.exec();
		if (exec.getErrorCode() != 0) {
			if (exec.getErrorCode() == Exec.ERROR_EXCEPTION)
				throw new SvnAdminException("Error during execution command "
						+ Util.toString(exec.getCmd(), " ") + ", exception caught", exec.getException());
			else
				throw new SvnAdminException("Error during execution command "
						+ Util.toString(exec.getCmd(), " "));
		} else if(checkRC && exec.getRc()!=0) {
            throw new SvnAdminException("Error during execution command "
						+ Util.toString(exec.getCmd(), " ")+": process exit code "+exec.getRc());
        }
    }

	/**
	 * Delete temp directory
	 */
	public void cleanup() {
		LOG.debug("cleanup");
		File tempDir = config.getTempDir();
		if (!Util.delete(tempDir)) {
			LOG.error("can't delete temp dir: " + tempDir.getAbsolutePath());
		}
	}
}

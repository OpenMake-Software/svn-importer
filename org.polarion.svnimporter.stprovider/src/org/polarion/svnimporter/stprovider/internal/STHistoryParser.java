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
package org.polarion.svnimporter.stprovider.internal;

import com.starbase.starteam.Audit;
import com.starbase.starteam.ChangeRequest;
import com.starbase.starteam.File;
import com.starbase.starteam.Folder;
import com.starbase.starteam.Item;
import com.starbase.starteam.Label;
import com.starbase.starteam.Link;
import com.starbase.starteam.LinkEndpoint;
import com.starbase.starteam.Project;
import com.starbase.starteam.Server;
import com.starbase.starteam.StarTeamFinder;
import com.starbase.starteam.User;
import com.starbase.starteam.View;
import com.starbase.starteam.ViewConfiguration;
import com.starbase.util.OLEDate;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.stprovider.internal.model.STBranch;
import org.polarion.svnimporter.stprovider.internal.model.STFile;
import org.polarion.svnimporter.stprovider.internal.model.STModel;
import org.polarion.svnimporter.stprovider.internal.model.STRevision;
import org.polarion.svnimporter.stprovider.internal.model.STRevisionState;

/**
 * @author <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 * @author Gunnar Wagenknecht
 */
public class STHistoryParser {
	private static final Log LOG = Log.getLog(STHistoryParser.class);
	
	private static final DateFormat RFC_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");

	/**
	 * returns the relativ path
	 * 
	 * @param file
	 * @return
	 */
	public static String getRelativePath(File file) {
		String name = file.getName();
		String parentFolderHierarchy = file.getParentFolderHierarchy().replace('\\', '/');
		if (!parentFolderHierarchy.endsWith("/"))
			parentFolderHierarchy += "/";
		return file.getView().getName() + "/" + parentFolderHierarchy + name;
	}

	/**
	 * returns the relativ path
	 *
	 * @param folder
	 * @return
	 */
	public static String getRelativePath(Folder folder) {
		String name = folder.getName();
		if(null == folder.getParentFolder())
			return name;

		String parentFolderHierarchy = folder.getParentFolderHierarchy().replace('\\', '/');
		if (!parentFolderHierarchy.endsWith("/"))
			parentFolderHierarchy += "/";
		return parentFolderHierarchy + name;
	}

	private STModel model;

	private STConfig config;

	private Matcher includesMatcher;

	private Matcher excludesMatcher;

	public STHistoryParser() {
		this.model = new STModel();
	}
//
//	public void buildHistory() {
//		buildHistory(config.getUrl());
//	}

	public void buildHistory(Folder folder) {
		processFolder(folder);
  }
//
//	public void buildHistory(String url) {
//		Folder folder = StarTeamFinder.openFolder(url);
//		View view = null != folder ? folder.getView() : StarTeamFinder.openView(url);
//		Project project = null != view ? view.getProject() : StarTeamFinder.openProject(url);
//
//		if (null == project)
//			throw new STException("No project found for URL: " + url);
//
//		if (null == view)
//			view = project.getDefaultView();
//
//		if (null == folder)
//			folder = view.getRootFolder();
//
//		processFolder(project.getServer(), project, view, folder);
//
//		project.getServer().disconnect();
//	}

  private void processFolder(Folder folder) {
    processFolder(folder.getServer(), folder.getView().getProject(), folder.getView(), folder);

    if (config.isImportDerivedViews()) {
      View[] views = folder.getView().getDerivedViews();
      for (int i = 0; i < views.length; i++) {
        View view = views[i];
        if (!config.getDerivedViewsList().contains(view.getName()))
        	continue;
        processFolder(view.getRootFolder());
      }
    }
  }
  /**
	 * Checks if the specified file is included in the current configuration.
	 *
	 * @param file
	 * @return <code>true</code> if the file is allowed, <code>false</code>
	 *         otherwise
	 */
	boolean checkIncluded(File file) {
		if (null == includesMatcher && null == excludesMatcher)
			return true;

		return checkIncluded(getRelativePath(file));
	}

	/**
	 * Checks if the specified folder is included in the current configuration.
	 *
	 * @param folder
	 * @return <code>true</code> if the folder is allowed, <code>false</code>
	 *         otherwise
	 */
	boolean checkIncluded(Folder folder) {
		if (null == includesMatcher && null == excludesMatcher)
			return true;

		return checkIncluded(getRelativePath(folder));
	}

	/**
	 * Checks if the specified path is included in the current confifuration.
	 *
	 * @param relativePath
	 * @return <code>true</code> if the path is allowed, <code>false</code>
	 *         otherwise
	 */
	private boolean checkIncluded(String relativePath) {
		if (null != includesMatcher && !includesMatcher.reset(relativePath).matches())
			return false;

		if (null != excludesMatcher && excludesMatcher.reset(relativePath).matches())
			return false;

		return true;
	}

	/**
	 * Returns the model.
	 *
	 * @return the model
	 */
	public STModel getModel() {
		return model;
	}

	/**
	 * Finds deleted files.
	 * <p>
	 * The process of checking out deleted files from StarTeam requires you to
	 * know then the file was deleted. To discover deleted files automatically
	 * audit logging has to be used on the server for a long time. The following
	 * is an outline of the process.
	 * <nl>
	 * <li>Go through the audit logs and discover deletion events</li>
	 * <li>Setup timebased view right before deletion time</li>
	 * <li>Process file history like normal</li>
	 * </nl>
	 * You should expect this to be very time consuming.
	 * </p>
	 *
	 * @param server
	 * @param project
	 * @param view
	 * @param folder
	 */
	void processDeleted(Server server, Project project, View view, Folder folder) {

		/*
		 * TODO: although this implementation follows the specified steps it is
		 * problematic; I never got it working properly. Because the view has to
		 * be setup twice (once for parsing the history and once for checking
		 * out the file) and this is where things break. The problem is that I
		 * never found a way to uniquly locate the element within a view accross
		 * StarTeam sessions.
		 */

		// find deleted items
		Item[] audits = folder.getItems(server.getTypeNames().AUDIT);
		for (int i = 0; i < audits.length; i++) {
			Audit audit = (Audit) (audits[i]);
			if (server.getPropertyEnums().AUDIT_EVENT_ID_DELETED == audit.getInt(server.getPropertyNames().AUDIT_EVENT_ID)) {
				String deletedItemTypeName = audit.getItemClassName();
				if (server.getTypeNames().FOLDER.equals(deletedItemTypeName) || server.getTypeNames().FILE.equals(deletedItemTypeName)) {
					Date deletionDate = audit.getCreatedTime().createDate();
					User deletedBy = server.getUser(audit.getCreatedBy());
					String deletedItemName = audit.getItemDescriptor();
					String comment = audit.getComment();
					// String deletedRevision =
					// audit.getString(server.getPropertyNames().AUDIT_ITEM_1_INFO);

					View viewAtDeletionTime = new View(view, ViewConfiguration.createFromTime(new OLEDate(new Date(deletionDate.getTime()))));
					Folder folderAtDeletionTime = viewAtDeletionTime.findFolder(folder.getID());
					if (server.getTypeNames().FILE.equals(deletedItemTypeName)) {
						File deletedFile = StarTeamFinder.findFile(folderAtDeletionTime, deletedItemName, true);
						if (null != deletedFile) {
							// get the file
							STFile stfile = recordFile(project, deletedFile);

							// get the branch
							STBranch branch = recordBranch(stfile, view);

							// recorde deletion
							recordRevision(stfile, branch, deletedFile, true, deletedBy, deletionDate, comment);

							// process file history
							processFileHistory(server, deletedFile, stfile, branch);
						}
					} else if (server.getTypeNames().FOLDER.equals(deletedItemTypeName)) {
						Folder deletedFolder = StarTeamFinder.findSubFolder(folderAtDeletionTime, deletedItemName);
						if (null != deletedFolder) {
							processDeleted(server, project, viewAtDeletionTime, deletedFolder);
						}
					}
					viewAtDeletionTime.discard();
				}
			}
		}
	}

	/**
	 * @param server
	 * @param project
	 * @param view
	 * @param folder
	 * @param file
	 */
	void processFile(Server server, Project project, View view, Folder folder, File file) {
		if (!checkIncluded(file)) {
			LOG.debug(MessageFormat.format("Skipping file {0} due to regular expression match.", new Object[] { getRelativePath(file) }));
			return;
		}

		// get the file
		STFile stfile = recordFile(project, file);

		// get the branch
		STBranch branch = recordBranch(stfile, view);

		// process file history
		processFileHistory(server, file, stfile, branch);
	}

	/**
	 * @param server
	 * @param file
	 * @param stfile
	 * @param branch
	 */
	void processFileHistory(Server server, File file, STFile stfile, STBranch branch) {
		// proccess revisions
		Item[] itemRevisions = file.getHistory();
		for (int i = 0; i < itemRevisions.length; i++) {
			File revision = (File) itemRevisions[i];

			User user = server.getUser(revision.getModifiedBy());
			Date modificationDate = revision.getModifiedTime().createDate();
			String comment = revision.getComment();

			recordRevision(stfile, branch, revision, false, user, modificationDate, comment);
		}
	}

	// Pre 6.0 versions of starteam lack populate methods that we usually call
	// for efficiency reasons.  If calls to these methods throw NoSuchMethod
	// exceptions we will just quit calling them.
	private static boolean noPopulate = false;

	/**
	 * @param server
	 * @param project
	 * @param view
	 * @param folder
	 */
	void processFolder(Server server, Project project, View view, Folder folder) {

		if (!checkIncluded(folder)) {
			LOG.debug(MessageFormat.format("Skipping folder {0} due to regular expression match.", new Object[] { getRelativePath(folder) }));
			return;
		}

		// process files
		Item[] files = folder.getItems(server.getTypeNames().FILE);
		for (int i = 0; i < files.length; i++) {
			File file = (File) files[i];
			if (! noPopulate) {
			    try {
	        		int retries = config.getRetries();  //the number of retries per folder
	        		int n = retries;
	        		boolean good = false;
	        		while (!good && retries > 0) {
	        			
	        			retries--;
	        			try {
	        				file.populate();
	        			} catch (NoSuchMethodError ex) {
	        				noPopulate = true;
	        			}
	        			catch (Exception e){
	        				LOG.error("error while processing file \"" + file.toString() + ": " + e.toString());
	        				if (retries - 1 >= 0){
	        					LOG.info("Retrying to process file \"" + file.toString());
	        					try {
	        						Thread.sleep(1000);
	        					} catch (InterruptedException e1) {
	        						Thread.currentThread().interrupt();
	        					}
	        					continue;
	        				}
	        				else
	        				{
	        					LOG.error("Could not process file \"" + file.toString() + " after " + n 
	        							+ " attempts, cancelling ...");
	        					return;
	        				}
	        			}
	        			good = true;
	        		}
			    } catch (NoSuchMethodError ex) {
			        noPopulate = true;
			    }
			}
			processFile(server, project, view, folder, file);
			file.discard();
		}

		// recurse into sub folders
		Item[] folders = folder.getItems(server.getTypeNames().FOLDER);
		for (int i = 0; i < folders.length; i++) {
			Folder subFolder = (Folder) folders[i];
            if (! noPopulate) {
        		int retries = config.getRetries();  //the number of retries per folder
        		int n = retries;
        		boolean good = false;
        		while (!good && retries > 0) {
        			
        			retries--;
        			try {
        				subFolder.populate();
        			} catch (NoSuchMethodError ex) {
        				noPopulate = true;
        			}
        			catch (Exception e){
        				LOG.error("error while populating folder \"" + subFolder.toString() + ": " + e.toString());
        				if (retries - 1 >= 0){
        					LOG.info("Retrying populate of folder \"" + subFolder.toString());
        					try {
        						Thread.sleep(1000);
        					} catch (InterruptedException e1) {
        						Thread.currentThread().interrupt();
        					}
        					continue;
        				}
        				else
        				{
        					LOG.error("Could not populate the folder \"" + subFolder.toString() + " after " + n 
        							+ " attempts, cancelling ...");
        					return;
        				}
        			}
        			good = true;
        		}
            }
			processFolder(server, project, view, subFolder);
			subFolder.discard();
		}

		// find deleted files
		// currently disabled: see processDeleted why
		// processDeleted(server, project, view, folder);
	}

	/**
	 * @param file
	 * @param view
	 * @return
	 */
	STBranch recordBranch(STFile file, View view) {
		STBranch branch;
		if (config.isOnlyTrunk() || !view.isBranchOnShare() ) {
			branch = file.getTrunk();
		} else {
			String branchName = view.getName();
			branch = (STBranch) file.getBranches().get(branchName);
			if (branch == null) {
				branch = new STBranch(branchName);
				branch.setName(branchName);
				file.addBranch(branch);
			}
		}
		return branch;
	}

	/**
	 * @param project
	 * @param file
	 * @return
	 */
	STFile recordFile(Project project, File file) {
	    // get path
	    String relativePath = getRelativePath(file);
	    STFile stfile = (STFile) model.getFiles().get(relativePath);
	    if (stfile == null) {
	        stfile = new STFile(relativePath);
	        STBranch trunk = new STBranch(file.getView().getName());
	        trunk.setName(file.getView().getName());
	        if (trunk.getName().equals(project.getDefaultView().getName())) {
	            trunk.setTrunk(true);
	        } else {
	            trunk.setTrunk(false);
	        }
	        stfile.addBranch(trunk);
	        stfile.setTrunk(trunk);
	        stfile.setObjectId(file.getItemID());
	        stfile.setFolder(file.getView().getRootFolder());
	        model.addFile(stfile);
	    }
	    return stfile;
	}

	/**
	 * @param stfile
	 * @param branch
	 * @param revision
	 * @param deleted
	 * @param user
	 * @param modificationDate
	 * @param comment
	 */
	private void recordRevision(STFile stfile, STBranch branch, File revision, boolean deleted, User user, Date modificationDate, String comment) {
		// revision
		STRevision strevision = new STRevision(revision.getDotNotation());
		
		// revision author
		String username = (null != user ? user.getName() : String.valueOf(revision.getModifiedBy()));
		strevision.setStUserName(username);
		strevision.setAuthor(config.translateUserName(username));

		// revision version
		strevision.setDate(modificationDate);
		strevision.setStDate(RFC_TIME_FORMAT.format(modificationDate));
		strevision.setNumberInBranch(revision.getViewVersion());

		// comment
		if(null == comment || comment.trim().length() == 0)
			comment = "(empty log message)";
		if(config.isTranslateUserNames())
			comment += MessageFormat.format(" ({0})", new Object[]{username}); 
		strevision.setMessage(comment);

		// labels
		Label[] labels = revision.getAttachedLabels();
		for (int j = 0; j < labels.length; j++) {
			strevision.newLabel(labels[j].getName());
		}
		
		// linked CRs
		recordLinkedCRs(revision, strevision);
			
		boolean recorded = false;
		if (deleted) {
			// mark as deleted
			strevision.setState(STRevisionState.DELETE);

			// only add to branch
			branch.addDeletedRevision(strevision);

			// revision will be added to file later, in
			// STBranch.handleDeletedRevisions()
			recorded = true;
		} else {
			// add to branch
			strevision.setBranch(branch);
			branch.addRevision(strevision);

			// add to file
			recorded = stfile.addRevision(strevision);
		}
		if (recorded)
			LOG.debug(MessageFormat.format("Recorded revision: \t{0} \t{1} \t{2}", new Object[] { stfile.getPath(), strevision.getNumber(), deleted ? "DELETED" : "" }));

	}

	/**
	 * Records linked CRs.
	 * @param revision
	 * @param strevision
	 */
	private void recordLinkedCRs(File revision, STRevision strevision) {
		Link[] links = revision.getServer().findLinks(revision);
		for (int i = 0; i < links.length; i++) {
			if(links[i].getParentEndpoint().isPinned() && links[i].getParentEndpoint().getRevisionNumber() == revision.getRevisionNumber()) {
				LinkEndpoint endpoint = links[i].getChildEndpoint();
				if(endpoint.getType().getName().equals(revision.getServer().getTypeNames().CHANGEREQUEST)) {
					// record CR in model
					ChangeRequest referencedCR = (ChangeRequest) revision.getView().findItem(endpoint.getType(), endpoint.getItemID());
					if(null != referencedCR)
						model.newChangeRequest(referencedCR.getNumber()).linkToRevision(strevision);
				}
			}
		}
	}

	/**
	 * @param config
	 *            the config to set
	 */
	public void setConfig(STConfig config) {
		this.config = config;
		this.model.setConfig(config);

		if (null != config) {
			if (null != config.getIncludesRegEx() && config.getIncludesRegEx().trim().length() > 0)
				includesMatcher = Pattern.compile(config.getIncludesRegEx()).matcher("");
			if (null != config.getExcludesRegEx() && config.getExcludesRegEx().trim().length() > 0)
				excludesMatcher = Pattern.compile(config.getExcludesRegEx()).matcher("");
		}
	}
}

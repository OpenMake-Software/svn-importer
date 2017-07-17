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
package org.polarion.svnimporter.mksprovider;

import org.polarion.svnimporter.common.IContentRetriever;
import org.polarion.svnimporter.common.IProvider;
import org.polarion.svnimporter.common.ISvnModel;
import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.common.RevisionNumber;
import org.polarion.svnimporter.common.Util;
import org.polarion.svnimporter.common.ZeroContentRetriever;
import org.polarion.svnimporter.mksprovider.internal.MksBranchProjectParser;
import org.polarion.svnimporter.mksprovider.internal.MksConfig;
import org.polarion.svnimporter.mksprovider.internal.MksContentRetriever;
import org.polarion.svnimporter.mksprovider.internal.MksExec;
import org.polarion.svnimporter.mksprovider.internal.MksRlogParser;
import org.polarion.svnimporter.mksprovider.internal.MksTransform;
import org.polarion.svnimporter.mksprovider.internal.MksUtil;
import org.polarion.svnimporter.mksprovider.internal.model.MksCheckpoint;
import org.polarion.svnimporter.mksprovider.internal.model.MksCheckpointBranch;
import org.polarion.svnimporter.mksprovider.internal.model.MksCheckpointHistory;
import org.polarion.svnimporter.mksprovider.internal.model.MksModel;
import org.polarion.svnimporter.mksprovider.internal.model.MksRevision;
import org.polarion.svnimporter.svnprovider.SvnModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class MksProvider implements IProvider {
    private static final Log LOG = Log.getLog(MksProvider.class);

    private MksConfig config;
    
    private int mksExecCount = 0;
    private int mksExecLimit = 0;

    public MksProvider() {
    }

    /**
     * Configure provider
     *
     * @param properties
     */
    public void configure(Properties properties) {
        config = new MksConfig(properties);
        mksExecLimit = config.getMksExecLimit();
    }

    /**
     * Validate configuration
     *
     * @return false if configuration has errors
     */
    public boolean validateConfig() {
        return config.validate();
    }

    /**
     * Get provider configuration
     *
     * @return
     */
    public MksConfig getConfig() {
        return config;
    }

    /**
     * Build mks model
     *
     * @return
     */
    private MksModel buildMksModel() {
        
        // Shut down any current MKS client
        shutdownClient();

        // Build a new MksModel
        MksModel model = new MksModel();
        
        // Get a Map of the development paths and root checkpoints
        Map<String,String> branchRoots = getBranchRoots();
        
        // Construct and add checkpoint history model
        model.setCheckpointHistory(buildCheckpointHistory());
        
        // Load all of the information we get from a generic rlog command.
        // This may not include revisions for files that no longer exist on
        // the main path, those will be added later
        new MksRlogParser(config).parse(this, model);
        
        // Process the main development path
        processDevPath(model, null, null);
        
        // process all alternate development paths
        for (Map.Entry<String, String> entry : branchRoots.entrySet()) {
          processDevPath(model, entry.getKey(), entry.getValue());
        }
        
        // Assign orphan branch names if so configured
        if (config.isGenerateOrphanBranches()) {
            model.assignOrphanBranches(config.getOrphanBranchPrefix());
        }

        // consolidate all MKS changes into SVN commits
        model.finishModel();

        LOG.info("MKS model has been created.");
        model.printSummary();

        return model;
    }
    
    private Map<String,String> getBranchRoots() {
      
        MksExec exec = new MksExec(new String[]{
            config.getExecutable(),
            "projectinfo",
            "-P", config.getProjectPath()
        });
        
        Map<String,String> result = new LinkedHashMap<String,String>();
        File file = new File(config.getTempDir(), "projectinfo.txt");
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(
                    executeCommand(exec, file), config.getLogEncoding()));
    
            String line;
            do {
                line  = reader.readLine();
                if (line == null) {
                    throw new MksException("Could not parse development paths from projectinfo text");
                }
            } while (! line.startsWith("Development Paths:"));
            while (true) {
                line = reader.readLine();
                if (line == null) break;
                if (line.startsWith("Associated Issues:")) break;
                String devPath = null;
                String rootRevNumber = null;
                int ibrk2 = line.lastIndexOf(')');
                int ibrk1 = line.lastIndexOf('(', ibrk2-1);
                if (ibrk1 > 0) {
                    rootRevNumber = line.substring(ibrk1+1, ibrk2).trim();
                    devPath = line.substring(0, ibrk1).trim();
                }
                if (rootRevNumber == null || rootRevNumber.length() == 0 ||
                    devPath == null || devPath.length() == 0) {
                    throw new MksException("Could not parse development path from:" + line);
                }
                result.put(devPath, rootRevNumber);
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new MksException(ex.getMessage(), ex);
        } finally {
            if (reader != null) {
                try { reader.close(); } catch (IOException ex) {}
            }
            file.delete();
        }
        return result;
    }
    
    /**
     * Construct checkpoint history model
     * @return checkpoint history model
     */
    private MksCheckpointHistory buildCheckpointHistory() {
        
        MksCheckpointHistory history = new MksCheckpointHistory();
        
        // Execute a viewprojecthhistory command and run through the results
        MksExec exec = new MksExec(new String[]{
            config.getExecutable(),
            "viewprojecthistory",
            "-P", config.getProjectPath(),
            "--fields=revision,author,date,labels,description"
        });
        File file = new File(config.getTempDir(), "projecthistory.txt");
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(
                    executeCommand(exec, file), config.getLogEncoding()));
            
            // First line is a header containing the project name
            // which we are not interested in
            reader.readLine();
            
            // Read first line to prime the parsing algorithm
            String line = reader.readLine();
            
            while (true) {
                if (line == null) break;
                
                // Parse out a checkpoint revision
                String[] fields = line.split("\t");
                if (fields.length < 3) {
                    throw new MksException("Error parsing viewprojecthistory line:" + line);
                }
                String projRevNumber = fields[0];
                // Just check for valid revision number
                RevisionNumber.parse(projRevNumber);
                String author = fields[1];
                Date date = config.getLogDateFormat().parse(fields[2]);
                String[] labels = null;
                if (fields.length > 3) labels = parseLabels(fields[3]);
                StringBuffer description = new StringBuffer();
                for (int ndx = 4; ndx < fields.length; ndx++) {
                	if (ndx > 4) description.append('\t');
                	description.append(fields[ndx].trim());
                }
                
                boolean blank = false;
                while (true) {
                    line = reader.readLine();

                    // EOF definitely means the end of description lines
                    if (line == null) break;

                    // Otherwise a valid revision line marks the end of the description
                    // lines.  Unfortunately, some users have been creating
                    // description lines that look a lot like revision lines,
                    // so we have to be pretty picky about what we let through
                    fields = line.split("\t");
                    if (fields.length >= 3) {
                        
                        // Try parsing the line as though it were a revision line
                        // If this doesn't throw any exception, break out of the
                        // description line loop.  If it does, ignore the 
                        // exception and process the line as a description line
                        try {
                           RevisionNumber.parse(fields[0]);
                           config.getLogDateFormat().parse(fields[2]);
                           if (fields.length > 3) parseLabels(fields[3]);
                           break;
                        } catch (Exception ex) {}
                    }
                    line = line.trim();
                    if (line.length() == 0) {
                        if (description.length() > 0) blank = true;
                    } else {
                        if (description.length() > 0) description.append('\n');
                        if (blank) description.append('\n');
                        description.append(line.trim());
                    }
                }
                
                // Description lines that start with '+' were generated by
                // MKS and contain no useful information
                if (description.length() > 0 && 
                    description.charAt(0) == '+') description.setLength(0);
                
                // Construct the new checkpoint
                MksCheckpoint checkpoint = 
                    new MksCheckpoint(projRevNumber, author, date, labels, description.toString());
                
                // And add it to the project history
                history.addRevision(checkpoint);
                
                // Compute the history branch for this checkpoint
                String projBranchNumber = MksUtil.revNum2BranchNum(projRevNumber);
                
                // If the branch does not already exist, create a new one
                MksCheckpointBranch branch = history.getBranch(projBranchNumber);
                if (branch == null) {
                    branch = new MksCheckpointBranch(projBranchNumber);
                    branch.setName(null);  // Branch names will be added later
                    history.addBranch(branch);
                }
                
                // And assign this checkpoint to this branch
                checkpoint.setBranch(branch);
                branch.addRevision(checkpoint);
           }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new MksException(ex.getMessage(), ex);
        } finally {
            if (reader != null) {
                try { reader.close(); } catch (IOException ex) {}
            }
            file.delete();
        }
        return history;
    }

    /**
     * Parse labels projecthistory fields
     * @param labelStr label value from viewprojecthistory command
     * @return array of labels to be attached to this revision
     * or null if there are none.
     */
    private String[] parseLabels(String labelStr) {
        
        if (labelStr.trim().length() == 0) return null;
        
        String[] labels =  labelStr.split(",");
        for (int ii = 0; ii < labels.length; ii++) {
            labels[ii] = Util.cleanLabel(labels[ii]);
        }
        return labels;
    }
    
    /**
     * Process an MKS development path
     * @param model MKS model being constructed
     * @param devPath name of alternate development path or null if main path
     * @param rootRevNumber Project revision number from which this alternate
     * dev path is rooted or null if main path 
     * @param Map mapping alternate dev paths to their root checkpoint
     */
    private void processDevPath(MksModel model, String devPath, String rootRevNumber) {
        
        // If this is an alternate devpath, lookup the root project checkpoint
        // and assign this development path to it.  In theory we should be able
        // to allow multiple dev paths to share the same root checkpoint, but
        // the CI interface does not appear to have any way to distinguish
        // which history branches go with which development path.  So for now
        // we will just throw up our hands and punt.
        MksCheckpointHistory history = model.getCheckpointHistory();
        MksCheckpoint rootCheckpoint = null;
        String branchNum = "1";
        if (rootRevNumber != null) {
            rootCheckpoint = history.getRevision(rootRevNumber);
            if (rootCheckpoint == null) {
                throw new MksException("Unknown root checkpoint:" + rootRevNumber +
                                       " for devpath:" + devPath);
            }
            rootCheckpoint.addDevPath(devPath);
            
            branchNum = getDevPathBranch(devPath, rootRevNumber);
        }
        
        // Process all of the files that are current members of this development path
        MksBranchProjectParser parser = new MksBranchProjectParser(config);
        parser.parse(this, model, devPath, rootCheckpoint);
            
        // Next find the project history branch associated with this path
        MksCheckpointBranch branch = history.getBranch(branchNum);
        if (branch != null) {

            // Associate branch with this development path
            branch.setTrunk(devPath == null);
            branch.setName(devPath);
            
            // Loop through all of the checkpoints on this branch.  The checkpoint
            // branch has been set up to store checkpoints in reverse chronological
            // order, so we will be process them from last to first.
            Date nextCheckpointDate = parser.getLastCommitDate();
            for (MksCheckpoint checkpoint : branch.getRevisions()) {
                parser.parse(this, model, devPath, rootCheckpoint, 
                             checkpoint, nextCheckpointDate);
                nextCheckpointDate = checkpoint.getDate();
                
                // Bad things will happen if the checkpoint contains any
                // revisions with commit dates that are not before the
                // checkpoint date.  If we find that situation, generate a  
                // warning message and correct the checkpoint date
                
                // It isn't that uncommon for a user to check something in and
                // then checkpoint the project within the 1 minute resolution
                // that MKS keeps, so if the dates are equal we will fix it
                // without generating a warning.
                Date lastCommitDate = parser.getLastCommitDate();
                if (lastCommitDate != null && ! nextCheckpointDate.after(lastCommitDate)) {
                    if (! lastCommitDate.equals(lastCommitDate)) {
                        LOG.warn("Checkpoint " + checkpoint.getNumber() +
                                 " Date " + checkpoint.getDate() +
                                 " out of sequence with commit on " + parser.getLastCommitDate() +
                                 " for " + parser.getLastCommitRevision().getModelFile().getPath() +
                                 ":" + parser.getLastCommitRevision().getNumber());
                    }
                    checkpoint.setDate(new Date(parser.getLastCommitDate().getTime()+1));
                }
            }
        }
    }
    
    /**
     * Get the project history branch associated with a development path
     * @param devPath development path name
     * @param rootRevNumber development path root revision
     * @return project history branch associated with development path
     */
    private String getDevPathBranch(String devPath, String rootRevNumber) {
        
        // Get the last revision for this devpath 
        MksExec exec = new MksExec(new String[]{
                config.getExecutable(),
                "projectinfo",
                "-P", config.getProjectPath(),
                "--devpath="+devPath
        });
        File file = new File(config.getTempDir(), "devpathinfo.txt");
        BufferedReader reader = null;
        String devPathRev = null;
        try {
            reader = new BufferedReader(new InputStreamReader(
                    executeCommand(exec, file), config.getLogEncoding()));
    
            String line;
            do {
                line  = reader.readLine();
                if (line == null) {
                    throw new MksException("Could not parse development path revision from projectinfo text");
                }
            } while (! line.startsWith("Revision:"));
            String[] fields = line.trim().split(" +");
            devPathRev = fields[1]; 
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new MksException(ex.getMessage(), ex);
        } finally {
            if (reader != null) {
                try { reader.close(); } catch (IOException ex) {}
            }
            file.delete();
        }
        
        // If last revision matches the root revision, then there have not
        // been any checkpoints on this branch, so there will be no 
        // corresponding branch history
        if (devPathRev.equals(rootRevNumber)) return null;
        
        // Otherwise return the last revision number stripped of it's
        // last component
        String result = null;
        String rootDevPathRev = null;
        try {
        	result = RevisionNumber.stripLastComponent(devPathRev);
            rootDevPathRev = RevisionNumber.stripLastComponent(result);
        } catch (Exception ex) {}
        if (! rootRevNumber.equals(rootDevPathRev)) {
            throw new MksException("Devpath:" + devPath + 
                                   " last revision:" + devPathRev +
                                   " is not on subranch from root revision " +
                                   rootRevNumber);
        }
        return result;
    }

    /**
     * Print list of repository files to stream
     *
     * @param out
     */
    public void listFiles(PrintStream out) {
        Collection<?> files = buildMksModel().getFiles().keySet();
        for (Object file : files) out.println(file);
    }

    /**
     * Build svn model by mks model
     *
     * @return
     */
    public ISvnModel buildSvnModel() {
        MksModel mksModel = buildMksModel();
        MksTransform transform = new MksTransform(this);
        SvnModel svnModel = transform.transform(mksModel);
        LOG.info("Svn model has been created");
        LOG.info("total number of revisions in svn model: " + svnModel.getRevisions().size());
        return svnModel;
    }

    /**
     * Create content retriever for revision
     *
     * @param revision
     * @return
     */
    public IContentRetriever createContentRetriever(MksRevision revision) {
        if (config.isUseOnlyLastRevisionContent() && !revision.isLastRevision()) {
            return ZeroContentRetriever.INSTANCE;
        } else {
            return new MksContentRetriever(this, revision);
        }
    }

    /**
     * Log environment information
     */
    public void logEnvironmentInformation() {
        config.logEnvironmentInformation();
    }

    /**
     * Execute command directing output to file, then return a Reader
     * on that file
     *
     * @param exec Exec object to be executed
     * @param outFile Output file to be build and opened
     * @return outFile input stream
     */
    public InputStream executeCommand(MksExec exec, File outFile) {
        
        // The MKS client has some memory leak issues which we deal with by
        // shutting it down after a specific number of calls
        LOG.debug("MKS Exec count:" + mksExecCount + "  Limit:" + mksExecLimit);
        if (mksExecLimit > 0 && mksExecCount++ > mksExecLimit) {
            shutdownClient();
            mksExecCount = 1;
        }

        try {
            exec.setVerboseExec(config.isVerboseExec());
            exec.setEnconding(config.getLogEncoding());
        
            exec.setStdoutFile(outFile);
            exec.exec();
        
            if (exec.getException() != null) {
                throw exec.getException();
            }
            if (exec.getErrorCode() != 0) {
                if (exec.getErrorCode() == MksExec.ERROR_EXCEPTION) {
                    throw new MksException("error during execution command "
                            + Util.toString(exec.getCmd(), " ") + ", exception caught", exec.getException());
                } else if (exec.getErrorCode() == MksExec.ERROR_WRONG_PROJECT_PATH) {
                    String error = "error during execution command "
                                + Util.toString(exec.getCmd(), " ")
                                + ": wrong project path \"" + config.getProjectPath() + "\"";
                    if (config.isMksExecContinue()) {
                        LOG.error(error);
                    } else {
                        throw new MksException(error);
                    }
                } else {
                    throw new MksException("error during execution command "
                            + Util.toString(exec.getCmd(), " "));
                }
            } else if (exec.getRc() != 0) {
                if (config.isMksExecContinue()) {
                    LOG.error("Process exit code: " + exec.getRc());
                } else {
                    throw new MksException("Process exit code: " + exec.getRc());
                }
            }
            return new FileInputStream(outFile);

        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception e) {
            throw new MksException(e.getMessage(), e);
        }
    }
    
    // Shut down MKS client
    private void shutdownClient() {
        LOG.info("Shutting down MKS client");
        MksExec exec = 
            new MksExec(new String[]{config.getExecutable(), "exit", "--shutdown"});
        exec.exec();
        Exception ex = exec.getException();
        if (ex != null) {
            if (RuntimeException.class.isInstance(ex)) {
                throw (RuntimeException)ex;
            } else {
                throw new MksException(ex.getMessage(), ex);
            }
        }
        
        // Seems to take some time to actually shut down.  If we try to start
        // another client to quickly we get a shutdown in progress error.
        // So we will add a short pause before we try anything else
        try { Thread.sleep(2000); } catch (InterruptedException ex2) {}
    }

    /**
     * Cleanup
     */
    public void cleanup() {
        LOG.debug("cleanup");
        try {
        } finally {
            File tempDir = config.getTempDir();
            if (!Util.delete(tempDir)) {
                LOG.error("can't delete temp dir: " + tempDir.getAbsolutePath());
            }
        }
    }
}

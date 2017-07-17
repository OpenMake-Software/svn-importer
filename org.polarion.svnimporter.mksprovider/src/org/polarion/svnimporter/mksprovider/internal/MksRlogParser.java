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
package org.polarion.svnimporter.mksprovider.internal;

import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.common.RevisionNumber;
import org.polarion.svnimporter.mksprovider.MksException;
import org.polarion.svnimporter.mksprovider.MksProvider;
import org.polarion.svnimporter.mksprovider.internal.model.MksBranch;
import org.polarion.svnimporter.mksprovider.internal.model.MksFile;
import org.polarion.svnimporter.mksprovider.internal.model.MksModel;
import org.polarion.svnimporter.mksprovider.internal.model.MksRevision;
import org.polarion.svnimporter.mksprovider.internal.model.MksSubproject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * 
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class MksRlogParser {
	private static final Log LOG = Log.getLog(MksRlogParser.class);
    private MksConfig config;

    public MksRlogParser(MksConfig config) {
        this.config = config;
    }
    

    /**
     * Create file entries for all files found in main project development path and
     * add them to an MksModel object
     * @param provider The parent MksProvider object
     * @param model MksModel object to be updated
     */
    public void parse(MksProvider provider, MksModel model) {
        parse(provider, model, 
              new MksSubproject(config.getProjectDir(), config.getProjectFilename()),
              null);
    }

    /**
     * Run an rlog command and parse the resulting output file, adding the
     * results to an MksModel object 
     * @param provider The parent MksProvider object
     * @param model MksModel object to be updated
     * @param project project to be processed
     * @param filename filename to be processed, null to process all files
     * in this project
     * @returns last file object added to model
     */
    public MksFile parse(MksProvider provider, MksModel model,
                         MksSubproject project, String filename) {
        String projectDir = project.getProjectDir();
    	    
        List<String> /* of String */ list = new ArrayList<String>();
        list.add(config.getExecutable());
        list.add("rlog");
        list.add("--batch");
        list.add("-R");
        project.addCmdList(list);
        list.add("--headerformat=MEMBERNAME:{membername}\nRELMEMBERNAME:{relativemembername}\nATTR:{attributes}\nFORMAT:{format}\n");
        list.add("--format=REVISION:{revision}\nAUTHOR:{author}\nDATE:{date}\nDESCRIPTION:\n{description}\nEND_DESCRIPTION\n");
        list.add("--trailerformat=**********\n");
        if (filename != null) {
            if (! filename.startsWith(projectDir)) {
                throw new MksException(filename + " does not in a subdirectory of " + project);
            }
            list.add(filename.substring(projectDir.length()));
        }
        
        String mksRootDir = provider.getConfig().getMksRootDir();
	    
        File rlogFile = new File(config.getTempDir(), "rlog.txt");
        BufferedReader reader = null;
        MksFile lastFile = null;
        try {
            MksExec exec = new MksExec(list);
            reader = new BufferedReader(new InputStreamReader(
                    provider.executeCommand(exec, rlogFile), config.getLogEncoding()));

            while (true) {
                MksFile file = parseFile(projectDir, reader);
                if (file == null) break;
            	    
                // If a MKS root directory was specified, exclude any file
                // that isn't in or under that directory
                // We have ensured that it ends with a / so a simple
                // startWith check should be sufficient
                if (mksRootDir != null && ! file.getPath().startsWith(mksRootDir)) {
                    LOG.info("File:" + file.getPath() + " not in MKS root directory - skipped");
                } else {
                    model.addFile(file);
                    lastFile = file;
                }
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new MksException(ex.getMessage(), ex);
        } finally {
            if (reader != null) {
                try { reader.close(); } catch (IOException ex) {
                    throw new MksException(ex.getMessage(), ex);
                }
            }
            rlogFile.delete();
        }
        return lastFile;
    }
  
  	/**
  	 * Parse file definition form rlog output file
  	 * @param projectDir The relative directory of the project being processed
  	 * within the main MKS project
  	 * @param reader rlog output reader
  	 * @return File object parsed from output file or null if no more files are present
  	 * @throws IOException if file cannot be parsed successfully
  	 */
  	private MksFile parseFile(String projectDir, BufferedReader reader) 
  	throws IOException {
  	    
  	    // Read next line from rlog file
  	    // if EOF, return null
  	    String line = reader.readLine();
  	    if (line == null) return null;
  	    
  	    // Parse the global file information
  	    String memberName = projectDir + parseItem("MEMBERNAME", line);
  	    String relMemberName = parseItem("RELMEMBERNAME", reader.readLine());
  	    String attributes = parseItem("ATTR", reader.readLine());
  	    String format = parseItem("FORMAT", reader.readLine());
  	    
  	    // Old code checks for and replaces '\\' with '/'.  Not sure why this
  	    // is necessary, but we will play it safe and keep that functionality
          memberName = memberName.replaceAll("\\\\", "/");
          relMemberName = relMemberName.replaceAll("\\\\", "/");
  	    
  	    // And use all of this to build a file object
          LOG.info("Building file:" + memberName);
  	    MksFile file = new MksFile(memberName, relMemberName, 
  	                               attributes, format.equals("0"));
  	    
  	    // Now loop parsing revision information for this file
  	    while (true) {
  	        
  	        // Parse the next revision for this file
  	        // If there aren't any more, break out of loop
  	        MksRevision revision = parseRevision(reader);
  	        if (revision == null) break;
  	        
  	        // If we got one, extract a branch number from the revision number
  	        // Add that branch to the file branches if it isn't already there
  	        // and add this revision to that branch
  	        
  	        // Not that these are local branch numbers specific to each particular
  	        // member.  They will get converted to development branch names 
  	        // later on.
  	        LOG.info("Adding Revision:" + revision.getNumber());
            String branchNum = MksUtil.revNum2BranchNum(revision.getNumber());
            MksBranch branch = file.getBranch(branchNum);
            if (branch == null) {
                branch = new MksBranch(branchNum);
                branch.setName(null);  // Branch names will be added later
                file.addBranch(branch);
            }
            revision.setBranch(branch);
            branch.addRevision(revision);
          
            // Finally add the new revision to this file
  	        file.addRevision(revision);
  	    }
  	    
  	    // Now that we have processed all of the revisions
  	    // and organized them into branches
  	    // it is time to do some last minute branch organization
  	    
  	    // Loop through all of the created branches
        // This has to be done in branch order, which means the values have to
        // be sorted first
        
  	    MksBranch[] branches = (MksBranch[])file.getBranches().values().toArray(new MksBranch[file.getBranches().size()]);
  	    Arrays.sort(branches, new Comparator<MksBranch>(){
          public int compare(MksBranch b1, MksBranch b2) {
            return RevisionNumber.compare(b1.getNumber(), b2.getNumber());
          }});
        for (MksBranch branch : branches) {
            
            // If this isn't the trunk branch, we have to do some work tying
            // this branch to it's originating revision in another branch
            LOG.info("Processing branch:" + branch.getNumber());
            if (! branch.getNumber().equals("1")) {
                
                // First step is to identify the revision from which this 
                // branch originated.  There has to be one!
                // Unless it was deleted as a phantom lock copy, which is why
                // we have to keep looking for the original
                String srn = branchNum2SproutRevNum(branch.getNumber());
                MksRevision parentRevision = file.getSourceRevision(srn);
                if (parentRevision == null) {
                    throw new MksException("Could not find parrent revisions for branch " +
                                           file.getPath() + ":" + branch.getNumber());
                }
                
                // Next get the first revision on the branch and see if it has
                // the same revision date as the originating revision.  If it 
                // does, we will assume that it is a lock copy of the original
                // version and delete it.
                
                // It seems that some old version of MKS could generate multiple
                // lock copies on the same branch, so we will have to keep 
                // checking for them :(
                MksRevision firstRevision = (MksRevision)branch.getRevisions().first();
                while (firstRevision != null &&
                        firstRevision.getDate().equals(parentRevision.getDate())) {
                    LOG.info("Deleting branch lock copy revision:" + firstRevision.getNumber());
                    
                    // OK, it's not quite that simple, any child branches this
                    // revision has accumulated have to be transfered to the
                    // parent revision
                    for (Iterator itr = firstRevision.getChildBranches().iterator();
                         itr.hasNext(); ) {
                        MksBranch childBranch = (MksBranch)itr.next();
                        childBranch.setSproutRevision(parentRevision);
                        parentRevision.getChildBranches().add(childBranch);
                    }
                    
                    // And it has to be deleted from both the file and branch
                    file.deleteRevision(firstRevision);
                    branch.deleteRevision(firstRevision);
                    
                    // And we have to get the next revision in line (if there
                    // is one for the rest of first revision processing
                    firstRevision = null;
                    if (! branch.getRevisions().isEmpty()) {
                        firstRevision = (MksRevision)branch.getRevisions().first();
                    }
                }
                
                // If that resulted in an empty branch, delete the branch
                if (firstRevision == null) {
                    LOG.info("Deleting empty branch:" + branch.getNumber());
                    file.getBranches().remove(branch.getNumber());
                }
                
                // Otherwise add this branch as a child branch for the
                // parent revisions
                else {
                    branch.setSproutRevision(parentRevision);
                    parentRevision.addChildBranch(branch);
                }
            }
          }
          file.checkSequenceDates();
          LOG.info("File build complete:" + memberName);
          return file;
      }

    /**
     * Parse file revision from rlog output file
     * @param reader rlog output file reader
     * @return null if there are no more revisions for this file,
     * otherwise returns new revision object
     * @throws IOException if anything goes wrong
     */
    private MksRevision parseRevision(BufferedReader reader) throws IOException {
    	    // Read next line from rlog file
        // If it is the end of file seperater, return null
        String line = reader.readLine();
        if (line.equals("**********")) return null;
        
        // Parse revision information from file
        String revisionNumber = parseItem("REVISION", line);
        String author = parseItem("AUTHOR", reader.readLine());
        Date date = parseDate(reader.readLine());
        String description = parseDescription(reader);
        
        // And use it to build a new revision
        MksRevision revision = 
            new MksRevision(revisionNumber, author, date, description);
        
        return revision;
    }
    
    /**
     * Parse date from rlog output file
     * @param line from rlog file
     * @return the parsed date
     * @throws MksException if date cannot be parsed
     */
    private Date parseDate(String line) {
        
        String dateString = parseItem("DATE", line);
        try {
            return config.getLogDateFormat().parse(dateString);
        } catch (ParseException ex) {
            throw new MksException("cannot parse date: " + dateString, ex);
        }
    }
    
    private String parseDescription(BufferedReader reader) throws IOException {
        
        parseItem("DESCRIPTION", reader.readLine());
        
        StringBuffer result = new StringBuffer();
        boolean blank = false;
        while(true) {
            String line = reader.readLine();
            if (line == null) {
                throw new MksException(
                        "RlogParser did not find end revision description marker");
            }
            line = line.trim();
            if (line.equals("END_DESCRIPTION")) break;
            
            if (line.length() == 0) {
                blank = true;
            } else {
                if (result.length() > 0) {
                    result.append('\n');
                    if (blank) result.append('\n');
                }
                result.append(line);
                blank = false;
            }
        }
        return result.toString();
    }
	
    /**
     * Parse Rlog token time
     * @param title item title
     * @param line line from which value should be parsed
     * @return item value parsed from line
     * @throws MksException if line does not contain properly formated token
     */
    private String parseItem(String title, String line) {
        if (line == null || ! line.startsWith(title + ":")) {
            throw new MksException("MksRlogParser parse error - expected " + title +": line but found " + line);
        }
        return line.substring(title.length()+1).trim();
    }
    
    /**
     * Extract revision number from which branch number originated
     * @param branchNum branch number
     * @return originating revision number
     */
    private String branchNum2SproutRevNum(String branchNum) {
        return RevisionNumber.stripLastComponent(branchNum);
    }
}

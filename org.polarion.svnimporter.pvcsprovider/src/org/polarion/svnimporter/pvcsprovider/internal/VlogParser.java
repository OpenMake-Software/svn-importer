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
package org.polarion.svnimporter.pvcsprovider.internal;

import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.common.Util;
import org.polarion.svnimporter.pvcsprovider.PvcsException;
import org.polarion.svnimporter.pvcsprovider.internal.model.PvcsBranch;
import org.polarion.svnimporter.pvcsprovider.internal.model.PvcsFile;
import org.polarion.svnimporter.pvcsprovider.internal.model.PvcsRevision;
import org.polarion.svnimporter.pvcsprovider.internal.model.PvcsTag;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class VlogParser {
	private static final Log LOG = Log.getLog(VlogParser.class);

	private PvcsConfig config;

	/**
	 * Repository filename -> PvcsFile
	 */
	private Map<String, PvcsFile> files;

	public VlogParser(PvcsConfig config) {
		this.config = config;
	}
//
//	private PvcsFile curFile;
//	private PvcsRevision curRevision;
//
//	private int parserState;
//	private final int STATE_NORMAL = 0;
//	private final int WAIT_FOR_COMMENT = 1;
//	private final int WAIT_FOR_LABELS = 2;
//	private final int WAIT_FOR_DESC = 3;
//	private final int WAIT_FOR_ATTR = 4;
//
//	private Map versionLabels; // fixed labels. Map label -> revision number.
//	// (Each tag label is assigned to exactly one revision number, but one revision number
//	// can have more than one tag.)
//
//	private String fileDesc; // file description

	private BufferedReader filesReader; // reader for files.tmp containing the names of the verioned files
	// (as opposed to the physical archive locations on disk).
	
	private BufferedReader vlogReader; // The Vlog file reader

	/**
	 * Parse log file
	 *
	 * @param vlogfile the file generated by the vlog command
	 * @param filesFile the file generated by the listrevisions command.
	 * We rely on the fact that there is a one-to-one correspondence between the
	 * lines of filesFile and the sections in the vlog file that describe an archive.
	 * (with some robustness - see below)
	 */
	public void parse(File filesFile, File vlogfile) {
		files = new HashMap<String, PvcsFile>();

		try {
			InputStreamReader encReader = new InputStreamReader(new FileInputStream(vlogfile), config.getLogEncoding());
			vlogReader = new BufferedReader(encReader);
			filesReader = new BufferedReader(new InputStreamReader(new FileInputStream(filesFile), config.getLogEncoding()));
			try {
			  PvcsFile file;
			  while ((file = parseFile()) != null) {
			    files.put(file.getPath(), file);
			  }
			} finally {
				vlogReader.close();
				filesReader.close();
			}
		} catch (IOException e) {
			throw new PvcsException(e);
		}
	}
	
	private PvcsFile parseFile() throws IOException {
	  String line;
	  
	  // Look for Archive: line
	  do {
	    line = vlogReader.readLine();
	    if (line == null) return null;
	  } while (line.trim().length() == 0);
	  
	  if (!line.startsWith("Archive:")) {
	    throw new PvcsException("VlogParser expected Archive: but found " + line);
	  }
	  line = line.substring(8).trim();
    
    // Read the Workfile
    String workfile = vlogReader.readLine();
    if(workfile == null || !workfile.startsWith("Workfile:")) {
      throw new PvcsException("Workfile: line should follow Archive: line in vlog (Archive: "+line+")");
    }
    workfile = workfile.substring(9).trim();

    // Sometimes it occurs that the file is listed in files
    // but is missing in vlog (with error message at the beginning of vlog instead.
    // We handle such a situation by skipping such file.
    String pvcsPath = null;
    do {
      pvcsPath = filesReader.readLine();
      if (pvcsPath == null) {
        throw new PvcsException("no PVCS path in \"files.tmp\" for archive \"" + line + "\"");
      }
      if(!pvcsPath.toLowerCase().endsWith(workfile.toLowerCase())) {
        LOG.error("Vlog workfile does not correspond to the files entry. Skipping the files entry "+pvcsPath
            +"\nSearch vlog for error messages!");
        pvcsPath = null;
      }
    } while(pvcsPath == null);

    String path = getPath(pvcsPath); // the actual archive name will be ignored.
    PvcsFile curFile = new PvcsFile(path);
    curFile.setPvcsPath(pvcsPath);
    
    /* It is assumed here that the trunk revisions start with "1.".
      This is not generally true. Trunk revisions can start with any number, and it is
      even possible that the first digit changes on the trunk, so "4.0" can be the successor
      revision to "3.52". See below for a workaround
    */
    PvcsBranch trunk = new PvcsBranch("1");
    trunk.setTrunk(true);
    curFile.addBranch(trunk);
    Map<String, String> versionLabels = new HashMap<String, String>();
    
    // Start processing file components
    line = vlogReader.readLine();
    if (line == null) throw new PvcsException("Premature end of vlogfile");
    boolean done = false;
    boolean revDone = false;
    do {
      
      // Process file attributes
      if (line.startsWith("Attributes:")) {
        while (true) {
          line = vlogReader.readLine();
          if (line == null) throw new PvcsException("Premature end of vlogfile");
          if (!line.startsWith(" ")) break;
          if (config.importAttributes()) {
            line = line.trim();
            String key, value = null;
            int sep = line.indexOf('=');
            if (sep < 0) { // attribute without value
              key = line.trim();
            } else {
              key = line.substring(0, sep).trim();
              // the values are enclosed by '"'
              int start = next(line, sep, '"') + 1;
              int end = next(line, start, '"');
              value = line.substring(start, end);
            }
            curFile.addProperty(key, value);
          }
        }
      }
      
      // Process version labels
      else if (line.startsWith("Version labels:")) {
        while (true) {
          line = vlogReader.readLine();
          if (line == null) throw new PvcsException("Premature end of vlogfile");
          if (!line.startsWith(" ")) break;
          line = line.trim();
          int start = 1;
          int end = next(line, start, '"');
          String label = Util.cleanLabel(line.substring(start, end));
                        start = end + 4;
          String version = line.substring(start);
          if(version.endsWith("*")) {
              ;// floating labels are not supported
          } else {
              versionLabels.put(label, version);
          }
        }
      }
      
      // Process description
      else if (line.startsWith("Description:")) {
        StringBuilder sb = new StringBuilder();
        while (true) {
          line = vlogReader.readLine();
          if (line == null) throw new PvcsException("Premature end of vlogfile");
          if (line.startsWith("===================================")) revDone = true;
          if (revDone || line.startsWith("-----------------------------------"))  {
            // remove last empty line from description
            if (sb.length() > 0 && sb.charAt(sb.length()-1)=='\n') sb.setLength(sb.length()-1);
            // we treat the description as a special property
            curFile.addProperty("description", sb.toString());
            done = true;
            break;
          } else {
            if (sb.length() > 0) sb.append('\n');
            sb.append(line);
          }
        }
      }
      
      // Anything else should be ignored
      else {
        line = vlogReader.readLine();
        if (line == null) throw new PvcsException("Premature end of vlogfile");
      }
    } while (!done); 
    
    // Done with global file descriptions, now start processing revisions
    done = false;
    while (!revDone) {

      // process revision record
      // The entire revision block may be indented by some amount that we need
      // to undo
      line = vlogReader.readLine();
      if (line == null) throw new PvcsException("Premature end of vlogfile");
      int st = 0;
      while (st < line.length() && Character.isWhitespace(line.charAt(st))) st++;
      line = line.substring(st);
      if (!line.startsWith("Rev ")) {
        throw new PvcsException("Expected revision but found: " + line);
      }
      String revNumber = line.substring(4).trim();
      String branchNumber = PvcsUtil.getBranchNumber(revNumber);
      // workaround for the naive assumption that the trunk revisions start with "1":
      // if the branchNumber does not contain '.', it is assumed on
      // the trunk. The trunk is always assigned branch number 1.
      if (branchNumber.indexOf(".") == -1) {
        branchNumber = "1";
      }

      PvcsRevision curRevision = new PvcsRevision(revNumber);

      PvcsBranch branch = curFile.getBranch(branchNumber);
      if (branch == null) {
        branch = new PvcsBranch(branchNumber);
        branch.setName(branchNumber); // there is no "branch name" in PVCS
        curFile.addBranch(branch);
      }
      curRevision.setBranch(branch);

      branch.addRevision(curRevision);
      curFile.addRevision(curRevision);
      
      // Process revision info
      while (true) {
        line = vlogReader.readLine();
        if (line == null) throw new PvcsException("Premature end of vlogfile");
        line = line.substring(st);
        
        if (line.startsWith("Checked in:")) {
          line = line.substring(11).trim();
          Date date = parseDate(line);
          if (date != null) curRevision.setDate(date);
        } else if (line.startsWith("Author id:")) {
          line = line.substring(10).trim();
          int pt = line.indexOf(' ');
          if (pt >= 0) line = line.substring(0,pt);
          curRevision.setAuthor(line);
          break;
        }
      }
      
      // Process revision comments
      StringBuilder sb = new StringBuilder();
      while (true) {
        line = vlogReader.readLine();
        if (line == null) throw new PvcsException("Premature end of vlogfile");
        if (line.startsWith("===================================")) revDone = true;
        if (revDone || line.startsWith("-----------------------------------"))  {
          curRevision.setMessage(sb.toString());
          break;
        } else {
          line = line.substring(st);
          if (line.startsWith("Branches: ")) {
                        ;//skip line
          } else {
            if(sb.length() >0) sb.append("\n");
            sb.append(line);
          }
        }
      }
    };
    
    // OK, everything has been parsed out.  But we still have some cleanup functions
    // First go back through all of the branches and assign their sprout revisions.
    // We couldn't do this earlier because revision are listed in reverse chronological
    // order and the sprout revisions didn't exist when the branch was first processed.
    for (PvcsBranch branch : (Collection<PvcsBranch>)curFile.getBranches().values()) {
      if (! branch.isTrunk()) {
        String branchNumber = branch.getNumber();
        String sproutRevNumber = PvcsUtil.getSproutRevisionNumber(branchNumber);
        PvcsRevision sproutRevision = curFile.getRevision(sproutRevNumber);
        if (sproutRevision == null) {
          LOG.error(curFile.getPath() + ": unknown sprout revision for branch " + branchNumber);
        } else {
          branch.setSproutRevision(sproutRevision);
          sproutRevision.addChildBranch(branch);
        }
      }
    }
    
    // Next we have to to through the list of version labels parsed earlier
    // and use them to assign labels to the correct revisions
    for (String label : versionLabels.keySet()) {
      String revisionNumber = versionLabels.get(label);
      PvcsRevision revision = curFile.getRevision(revisionNumber);
      if (revision == null) {
                LOG.warn("Labeled revision " + revisionNumber + " not found, label=" + label + ")");
      } else {
        revision.addTag(new PvcsTag(label));
      }
    }
    
    curFile.checkSequenceDates();
	  return curFile;
	}

	private String getPath(String line) {
		String filename = line;

		if (filename.startsWith("/"))
			filename = filename.substring(1);

		if (config.getSubproject() != null) {
			if (!filename.toLowerCase().startsWith(config.getSubproject().toLowerCase()))
				throw new PvcsException("filename " + filename + " must start with subproject name: " + config.getSubproject());
			filename = filename.substring(config.getSubproject().length());//+'/'
		}
		if (filename.startsWith("/"))
			filename = filename.substring(1);

		return filename;
	}


	private Date parseDate(String sdate) {
		DateFormat df = config.getLogDateFormat();
		try {
			return df.parse(sdate);
		} catch (ParseException e) {
			LOG.error("wrong date: " + sdate + "(" + "sample format: " + df.format(new Date()) + ")");
			return null;
		}
	}

	private int next(String s, int startIndex, char c) {
		int i = startIndex;
		while (i < s.length() && (s.charAt(i) != c))
			i++;
		return i;
	}

	public Map<String, PvcsFile> getFiles() {
		return files;
	}
}

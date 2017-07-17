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
package org.polarion.svnimporter.mksprovider.internal.model;

import java.util.List;

/**
 * Class reprenting an MKS project, either the main project or a subproject.
 * In the greater scheme of things, subprojects are auxilary structures attached
 * to File objects rather than the other way around.  Their primary purpose is
 * to supply the project parameters (--project, --projrevno, and --devpath)
 * that need to be passed to most si client commands to retrieve information
 * about that file.<p>
 * 
 * In real life, Files contain many revisions which can be attached to subprojects
 * with different development path and checkpoint numbers.  The current structure
 * only keeps one subproject (the first one found) for each file.  So far we 
 * have been able to retrieve the content of all file revisions with the 
 * project parameters that were kept for the first revision processed.
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class MksSubproject {
    
    // real project path/dir is the absolute path of this project in the
    // MKS repository, after adjusting for any shared subprojects in the
    // original project path
    private String realProjectPath;
    private String realProjectDir;
    
    // project Path/dir is the path of this subproject relative to the main
    // project directory, unadjusted by any shared subproject references
    private String projectPath;
    private String projectDir;
    
    // These are the development path and revision number associated with the project
    private String devPath;
    private String projRevNumber;
    
    /**
     * Construct the main project
     * @param realProjectDir absolute directory containing main project
     * @param realProjectFilename main project name
     */
    public MksSubproject(String realProjectDir, String realProjectFilename) {
        this(realProjectDir, realProjectFilename, null, null);
    }
    
    /**
     * Construct the main project
     * @param realProjectDir absolute directory containing main project
     * @param realProjectFilename main project name
     * @param devPath development path name or null if this is the main 
     * development path
     * @param projRevNumber project revision number if this is a checkpoint
     * subproject, otherwise null
     */
    public MksSubproject(String realProjectDir, String realProjectFilename,
                         String devPath, String projRevNumber) {
        this.realProjectDir = realProjectDir;
        this.realProjectPath = realProjectDir + realProjectFilename;
        this.projectDir = "";
        this.projectPath = realProjectFilename;
        this.devPath = devPath;
        this.projRevNumber = projRevNumber;
    }

    /**
     * Construct subproject
     * @param parent this subprojects parent project
     * @param projectPath subproject path relative to main project
     * @param sharedProjectPath if this is a shared subproject, this is the
     * absolute path name of the real project that is being shared, otherwise
     * it is null 
     * @param devPath development path name or null if this is the main 
     * development path
     * @param projRevNumber project revision number if this is a checkpoint
     * subproject, otherwise null
     */
    public MksSubproject(MksSubproject parent, String projectPath,
                         String devPath, String projRevNumber) {
        this.projectPath = parent.projectDir + projectPath;
        this.projectDir = stripProjectDir(this.projectPath);
        this.realProjectPath = parent.realProjectDir + projectPath;
        this.realProjectDir = stripProjectDir(this.realProjectPath);
        
        this.devPath = devPath;
        this.projRevNumber = projRevNumber;
    }

    /**
     * Set the real project path when this project has been identified as
     * a shared subproject
     * @param realProjectPath absolute path to real project
     */
    public void setRealPath(String realProjectPath) {
        this.realProjectPath = realProjectPath;
        this.realProjectDir = stripProjectDir(this.realProjectPath);
    }

    /**
     * Strip project name and return the path with a terminating / 
     * @param path path name
     * @return directory portion of the path name
     */
    private static String stripProjectDir(String path) {
        int index = path.lastIndexOf('/');
        return (index < 0 ? "" : path.substring(0,index+1));
    }

    public String getProjectPath() {
        return projectPath;
    }

    public String getProjectDir() {
        return projectDir;
    }
    
    public String getRealProjecPath() {
        return realProjectPath;
    }
    
    public String getRealProjectDir() {
        return realProjectDir;
    }

    public String getDevPath() {
        return devPath;
    }
    
    public String getProjRevNumber() {
        return projRevNumber;
    }
    
    public void addCmdList(List<String> cmdList) {
        cmdList.add("--project=" + realProjectPath);
        if (projRevNumber != null) {
            cmdList.add("--projectRevision=" + projRevNumber);
        }
        else if (devPath != null) {
            cmdList.add("--devpath=" + devPath);
        }
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("Project path:" + projectPath);
        if (devPath != null) buf.append("  DevPath:" + devPath);
        if (projRevNumber != null) buf.append("  ProjRevNumber:" + projRevNumber);
        return buf.toString();
    }
}

/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.cvsclient.command.checkout;

import java.io.*;

import org.netbeans.lib.cvsclient.command.*;

/**
 * Object containing information about various modules defined in the repository.
 * Is parsed from the output of cvs checkout -c and cvs checkout -s.

 * @author   Milos Kleint
 */
public class ModuleListInformation extends FileInfoContainer {

    private String moduleName;

    private String moduleStatus;

    private final StringBuffer paths = new StringBuffer();

    private String type;

    public ModuleListInformation() {
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getModuleStatus() {
        return moduleStatus;
    }

    public void setModuleStatus(String moduleStatus) {
        this.moduleStatus = moduleStatus;
    }

    public String getPaths() {
        return paths.toString();
    }

    public void addPath(String path) {
        if (paths.length() > 0) {
            paths.append(' ');
        }
        paths.append(path);
    }

    public File getFile() {
        return null;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

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
package org.polarion.svnimporter.vssprovider.internal;

import org.polarion.svnimporter.vssprovider.VssException;

import java.util.Iterator;
import java.util.Properties;
import java.io.File;

/**
 * @author <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class VssUtil {
    public static String[] loadVSSEnvironment(String serverPath, String user, String password) {
        Properties systemProps = System.getProperties();
        if (serverPath != null) {
            systemProps.put("SSDIR", serverPath);
        }

        if (user != null)
            systemProps.put("SSUSER", user);

        if (password != null)
            systemProps.put("SSPWD", password);

        String[] env = new String[systemProps.size()];
        int index = 0;
        Iterator systemPropIterator = systemProps.keySet().iterator();
        while (systemPropIterator.hasNext()) {
            String propName = (String) systemPropIterator.next();
            env[index] = propName + "=" + systemProps.get(propName);
            index++;
        }

        return env;
    }

    /**
     * Convert VSS path to (temporary) filename
     * For example: $/Project/File1 -> __project_file1
     *
     * @param vssPath
     */
    public static String convertVssPathToFilename(String vssPath) {
        return vssPath.replaceAll("\\/", "___").replaceAll("\\$", "_");
    }

    /**
     * Delete temp. file if exists and throw exception if cannot delete
     *
     * @param f
     */
    public static void deleteTempFile(File f) {
        if (f.exists())
            if (!f.delete())
                throw new VssException("Cannot delete temp. file: " + f.getAbsolutePath());
    }
}


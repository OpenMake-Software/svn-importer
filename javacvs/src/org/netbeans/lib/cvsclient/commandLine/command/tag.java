/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.cvsclient.commandLine.command;

import java.io.File;

import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.command.tag.*;
import org.netbeans.lib.cvsclient.commandLine.*;

/**
 * Tag checked out files.
 *
 * @author  Martin Entlicher
 */
public class tag extends AbstractCommandProvider {
    
    public String getName() {
        return "tag"; // NOI18N
    }
    
    public String[] getSynonyms() {
        return new String[] { "ta" }; // NOI18N
    }
    
    public Command createCommand(String[] args, int index, GlobalOptions gopt, String workDir) {
        TagCommand command = new TagCommand();
        command.setBuilder(null);
        final String getOptString = command.getOptString();
        GetOpt go = new GetOpt(args, getOptString);
        int ch = -1;
        go.optIndexSet(index);
        boolean usagePrint = false;
        String arg;
        while ((ch = go.getopt()) != go.optEOF) {
            boolean ok = command.setCVSCommand((char)ch, go.optArgGet());
            if (!ok) {
                usagePrint = true;
            }
        }
        if (usagePrint) {
            throw new IllegalArgumentException(getUsage());
        }
        int argIndex = go.optIndexGet();
        // test if we have been passed the tag name
        if (argIndex < args.length) {
            command.setTag(args[argIndex]);
        } else {
            throw new IllegalArgumentException(getUsage());
        }
        int fileArgsIndex = argIndex + 1;
        // test if we have been passed any file arguments
        if (fileArgsIndex < args.length) {
            File[] fileArgs = new File[args.length - fileArgsIndex];
            // send the arguments as absolute paths
            if (workDir == null) {
                workDir = System.getProperty("user.dir");
            }
            File workingDir = new File(workDir);
            for (int i = fileArgsIndex; i < args.length; i++) {
                fileArgs[i - fileArgsIndex] = new File(workingDir, args[i]);
            }
            command.setFiles(fileArgs);
        }
        return command;
        
    }
    
}

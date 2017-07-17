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
import org.netbeans.lib.cvsclient.command.importcmd.*;
import org.netbeans.lib.cvsclient.commandLine.*;

/**
 * Imports a file or directory to the repository.
 *
 * @author  Martin Entlicher
 */
public class Import extends AbstractCommandProvider {
    
    public String getName() {
        return "import"; // NOI18N
    }
    
    public String[] getSynonyms() {
        return new String[] { "im", "imp" }; // NOI18N
    }
    
    public Command createCommand(String[] args, int index, GlobalOptions gopt, String workDir) {
        ImportCommand command = new ImportCommand();
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
            throwUsage();
        }
        int argIndex = go.optIndexGet();
        // test if we have been passed the repository, vendor-tag and release-tag argument
        if (argIndex < (args.length - 2)) {
            command.setModule(args[argIndex]);
            command.setVendorTag(args[++argIndex]);
            command.setReleaseTag(args[++argIndex]);
        } else {
            throwUsage();
        }
        return command;
        
    }
    
    private void throwUsage() {
        throw new IllegalArgumentException(getUsage());
    }
    
}

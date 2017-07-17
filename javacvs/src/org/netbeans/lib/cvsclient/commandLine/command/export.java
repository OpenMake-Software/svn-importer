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
import java.util.ResourceBundle;

import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.command.export.*;
import org.netbeans.lib.cvsclient.commandLine.*;

/**
 * Export sources from CVS repository.
 *
 * @author  Martin Entlicher
 */
public class export extends AbstractCommandProvider {
    
    public String getName() {
        return "export"; // NOI18N
    }
    
    public String[] getSynonyms() {
        return new String[] { "ex", "exp" }; // NOI18N
    }
    
    public Command createCommand(String[] args, int index, GlobalOptions gopt, String workDir) {
        ExportCommand command = new ExportCommand();
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
        if (command.getExportByDate() == null && command.getExportByRevision() == null) {
            throw new IllegalArgumentException("cvs [export]: "+ResourceBundle.getBundle(export.class.getPackage().getName()+".Bundle").getString("export.Msg_NeedTagOrDate")); // NOI18N)
        }
        int modulesArgsIndex = go.optIndexGet();
        // test if we have been passed any module arguments
        if (modulesArgsIndex < args.length) {
            String[] modulesArgs = new String[args.length - modulesArgsIndex];
            // send the arguments as absolute paths
            for (int i = modulesArgsIndex; i < args.length; i++) {
                modulesArgs[i - modulesArgsIndex] = args[i];
            }
            command.setModules(modulesArgs);
        }
        return command;
        
    }
    
}

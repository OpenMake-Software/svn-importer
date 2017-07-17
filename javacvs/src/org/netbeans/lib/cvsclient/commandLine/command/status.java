/*****************************************************************************
 * Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is available at http://www.sun.com/

 * The Original Code is the CVS Client Library.
 * The Initial Developer of the Original Code is Robert Greig.
 * Portions created by Robert Greig are Copyright (C) 2000.
 * All Rights Reserved.

 * Contributor(s): Robert Greig.
 *****************************************************************************/
package org.netbeans.lib.cvsclient.commandLine.command;

import java.io.*;

import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.command.status.*;
import org.netbeans.lib.cvsclient.commandLine.*;

/**
 * The status command
 * @author  Robert Greig
 */
public class status extends AbstractCommandProvider {
    
    public String[] getSynonyms() {
        return new String[] { "st", "stat" }; // NOI18N
    }
    
    public Command createCommand(String[] args, int index, GlobalOptions gopt, String workDir) {
        StatusCommand command = new StatusCommand();
        final String getOptString = command.getOptString();
        GetOpt go = new GetOpt(args, getOptString);
        int ch = -1;
        go.optIndexSet(index);
        boolean usagePrint = false;
        while ((ch = go.getopt()) != go.optEOF) {
            boolean ok = command.setCVSCommand((char)ch, go.optArgGet());
/*            if ((char)ch == 'R')
                command.setRecursive(true);
            else if ((char)ch == 'l')
                command.setRecursive(false);
            else if ((char)ch == 'v')
                command.setIncludeTags(true);

            else
 */
            if (!ok) {
                usagePrint = true;
            }
        }
        if (usagePrint) {
            throw new IllegalArgumentException(getUsage());
        }
        int fileArgsIndex = go.optIndexGet();
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
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

import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.command.checkout.*;
import org.netbeans.lib.cvsclient.commandLine.*;

/**
 * A factory class for creating and configuring a checkout command
 * @author  Robert Greig
 */
public class checkout extends AbstractCommandProvider {
    
    public String[] getSynonyms() {
        return new String[] { "co", "get" };
    }
    
    public Command createCommand(String[] args, int index, GlobalOptions gopt, String workDir) {
        CheckoutCommand command = new CheckoutCommand();
        command.setBuilder(null);
        final String getOptString = command.getOptString();
        GetOpt go = new GetOpt(args, getOptString);
        int ch = -1;
        go.optIndexSet(index);
        boolean usagePrint = false;
        while ((ch = go.getopt()) != go.optEOF) {
            boolean ok = command.setCVSCommand((char)ch, go.optArgGet());
            if (!ok) {
                usagePrint = true;
            }
        }
        if (usagePrint) {
            throw new IllegalArgumentException(getUsage());
        }
        int modulesArgsIndex = go.optIndexGet();
        // test if we have been passed any file arguments
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
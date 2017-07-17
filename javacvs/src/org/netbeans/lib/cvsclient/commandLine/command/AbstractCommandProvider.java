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

import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.netbeans.lib.cvsclient.command.Command;

/**
 * The provider of CVS commands.
 * The implementation of this interface knows how to create a CVS command
 * from an array of arguments.
 *
 * @author  Martin Entlicher
 */
abstract class AbstractCommandProvider implements CommandProvider {
    
    /**
     * Get the name of this command.
     * The default implementation returns the name of the implementing class.
     */
    public String getName() {
        String className = getClass().getName();
        int dot = className.lastIndexOf('.');
        if (dot > 0) {
            return className.substring(dot + 1);
        } else {
            return className;
        }
    }
    
    public String getUsage() {
        return ResourceBundle.getBundle(CommandProvider.class.getPackage().getName()+".Bundle").getString(getName()+".usage"); // NOI18N
    }
    
    public void printShortDescription(PrintStream out) {
        String msg = ResourceBundle.getBundle(CommandProvider.class.getPackage().getName()+".Bundle").getString(getName()+".shortDescription"); // NOI18N
        out.print(msg);
    }
    
    public void printLongDescription(PrintStream out) {
        String msg = ResourceBundle.getBundle(CommandProvider.class.getPackage().getName()+".Bundle").getString(getName()+".longDescription"); // NOI18N
        out.println(MessageFormat.format(msg, new Object[] { getUsage() }));
    }
    
}

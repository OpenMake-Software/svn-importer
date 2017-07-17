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
import java.util.ResourceBundle;

import org.netbeans.lib.cvsclient.command.Command;
import org.netbeans.lib.cvsclient.command.GlobalOptions;

/**
 * The provider of CVS commands.
 * The implementation of this interface knows how to create a CVS command
 * from an array of arguments.
 *
 * @author  Martin Entlicher
 */
public interface CommandProvider {
    
    /**
     * Get the name of this command.
     * The default implementation returns the name of the implementing class.
     */
    public String getName();
    
    /**
     * Get the list of synonyms of names of this command.
     */
    public abstract String[] getSynonyms();
    
    /**
     * Create the CVS command from an array of arguments.
     * @param args The array of arguments passed to the command.
     * @param index The index in the array where the command's arguments start.
     * @param workDir The working directory.
     * @return The implementation of the {@link org.netbeans.lib.cvsclient.command.Command}
     *         class, which have set the passed arguments.
     */
    public abstract Command createCommand(String[] args, int index, GlobalOptions gopt, String workDir);
    
    /**
     * Get a short string describibg the usage of the command.
     */
    public String getUsage();
    
    /**
     * Print a short help description (one-line only) for this command to the
     * provided print stream.
     * @param out The print stream.
     */
    public void printShortDescription(PrintStream out);
    
    /**
     * Print a long help description (multi-line with all supported switches
     * and their description) of this command to the provided print stream.
     * @param out The print stream.
     */
    public void printLongDescription(PrintStream out);
    
}

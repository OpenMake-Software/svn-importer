/*****************************************************************************
 * Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is available at http://www.sun.com/

 * The Original Code is the CVS Client Library.
 * The Initial Developer of the Original Code is Milos Kleint.
 * Portions created by Milos Kleint are Copyright (C) 2003.
 * All Rights Reserved.

 * Contributor(s): Milos Kleint.
 *****************************************************************************/

package org.netbeans.lib.cvsclient.commandLine;

import java.io.PrintStream;
import org.netbeans.lib.cvsclient.event.CVSListener;

/**
 * for commands created in commandLine.command, that don't want to have the BasicListener 
 * attached to the created command, but rather a custom one.
 * @author  milos
 */
public interface ListenerProvider
{
    /**
     * 
     */
    CVSListener createCVSListener(PrintStream stdout, PrintStream stderr);
}

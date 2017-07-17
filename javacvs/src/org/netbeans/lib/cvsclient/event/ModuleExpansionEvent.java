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

package org.netbeans.lib.cvsclient.event;

/**
 * This event is really intended only for the use in the Checkout command.
 * During a checkout command, the client must ask the server to expand modules
 * to determine whether there are aliases defined for a particular module.
 * The client must then use the expansion to determine if a local directory
 * exists and if so, send appropriate Modified requests etc.
 * @author Robert Greig
 */
public class ModuleExpansionEvent extends CVSEvent {
    /**
     * The expanded module name
     */
    private String module;

    /**
     * Creates new ModuleExpansionEvent.
     * @param source the source of the event
     * @param theModule the module name that the original request has been
     * "expanded" to.
     */
    public ModuleExpansionEvent(Object source, String module) {
        super(source);
        this.module = module;
    }

    /**
     * Get the module name that the original module name has been expanded to.
     * @return the expanded name
     */
    public String getModule() {
        return module;
    }

    /**
     * Fire the event to the event listener. Subclasses should call the
     * appropriate method on the listener to dispatch this event.
     * @param listener the event listener
     */
    protected void fireEvent(CVSListener listener) {
        listener.moduleExpanded(this);
    }
}

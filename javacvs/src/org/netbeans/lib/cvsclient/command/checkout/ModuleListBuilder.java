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

package org.netbeans.lib.cvsclient.command.checkout;

import java.util.*;

import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.event.*;

/**
 * Handles the building of module list information object and the firing of
 * events when complete objects are built.
 *
 * @author  Milos Kleint
 */
public class ModuleListBuilder implements Builder {
    /**
     * The module object that is currently being built.
     */
    private ModuleListInformation moduleInformation;

    /**
     * The event manager to use.
     */
    private final EventManager eventManager;

    private final CheckoutCommand checkoutCommand;

    public ModuleListBuilder(EventManager eventMan, CheckoutCommand comm) {
        eventManager = eventMan;
        checkoutCommand = comm;
    }

    public void outputDone() {
        if (moduleInformation != null) {
            eventManager.fireCVSEvent(new FileInfoEvent(this, moduleInformation));
            moduleInformation = null;
        }
    }

    public void parseLine(String line, boolean isErrorMessage) {
        line = line.replace('\t', ' ');
        if (!line.startsWith(" ")) { //NOI18N
            processModule(line, true);
        }
        else {
            processModule(line, false);
        }
    }

    protected void processModule(String line, boolean firstLine) {
        StringTokenizer tok = new StringTokenizer(line, " ", false); //NOI18N
        if (firstLine) {
            outputDone();
            moduleInformation = new ModuleListInformation();
            String modName = tok.nextToken();
            moduleInformation.setModuleName(modName);
            if (checkoutCommand.isShowModulesWithStatus()) {
                String stat = tok.nextToken();
                moduleInformation.setModuleStatus(stat);
            }
        }
        while (tok.hasMoreTokens()) {
            String nextTok = tok.nextToken();
            if (nextTok.startsWith("-")) { //NOI18N
                moduleInformation.setType(nextTok);
                continue;
            }
            moduleInformation.addPath(nextTok);
        }
    }

    public void parseEnhancedMessage(String key, Object value) {
    }
}

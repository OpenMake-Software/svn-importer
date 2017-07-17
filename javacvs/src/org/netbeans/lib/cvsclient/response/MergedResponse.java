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
package org.netbeans.lib.cvsclient.response;

import java.util.*;

import org.netbeans.lib.cvsclient.admin.*;
import org.netbeans.lib.cvsclient.event.*;
import org.netbeans.lib.cvsclient.util.*;

/**
 * This response is very similar to an UpdatedResponse except that it backs
 * up the file being merged, and the file in question will still not be
 * up-to-date after the merge.
 * @author  Robert Greig
 * @see org.netbeans.lib.cvsclient.response.UpdatedResponse
 */
class MergedResponse extends UpdatedResponse {

    /**
     * Process the data for the response.
     * @param r the buffered reader allowing the client to read the server's
     * response. Note that the actual response name has already been read
     * and the reader is positioned just before the first argument, if any.
     * @param services various services that are useful to response handlers
     * @throws ResponseException if something goes wrong handling this response
     */
    public void process(LoggedDataInputStream dis, ResponseServices services)
            throws ResponseException {
        super.process(dis, services);
        EventManager manager = services.getEventManager();
        if (manager.isFireEnhancedEventSet()) {
            manager.fireCVSEvent(new EnhancedMessageEvent(this, EnhancedMessageEvent.MERGED_PATH, localFile));
        }
    }

    /**
     * Returns the Conflict field for the file's entry.
     * Can be overriden by subclasses.
     * (For example the MergedResponse that sets the "result of merge" there.)
     * @param date the date to put in
     * @param hadConflicts if there were conflicts (e.g after merge)
     * @return the conflict field
     */
    protected String getEntryConflict(Date date, boolean hadConflicts) {
        if (!hadConflicts) {
            return "Result of merge"; //NOI18N
        }
        else {
            return "Result of merge+" + //NOI18N
                    getDateFormatter().format(date);
        }
    }

}

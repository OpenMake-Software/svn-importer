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

package org.netbeans.lib.cvsclient.request;

/**
 * Implements the Sticky request
 * @author  Milos Kleint
 */
public final class StickyRequest extends Request {
    /**
     * The sticky tag/date to send
     */
    private String sticky;

    /**
     * Construct a new Sticky request
     * @param theStickyTag the sticky tag to use as an argument in the request
     */
    public StickyRequest(String theStickyTag) {
        sticky = theStickyTag;
    }

    /**
     * Get the request String that will be passed to the server
     * @return the request String
     */
    public String getRequestString() throws UnconfiguredRequestException {
        if (sticky == null) {
            throw new UnconfiguredRequestException(
                    "Sticky tag has not been set");
        }

        return "Sticky " + sticky + "\n"; //NOI18N
    }

    public void setStickyTag(String tag) {
        sticky = tag;
    }

    /**
     * Is a response expected from the server?
     * @return true if a response is expected, false if no response if
     * expected
     */
    public boolean isResponseExpected() {
        return false;
    }
}
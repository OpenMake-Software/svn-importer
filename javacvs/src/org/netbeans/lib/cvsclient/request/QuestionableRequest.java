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
package org.netbeans.lib.cvsclient.request;

/**
 * The questionable request. Tell the server which directory to use.
 * @author  Milos Kleint
 */
public final class QuestionableRequest extends Request {

    /**
     * name of file that is questionable
     */
    private String questionFile;

    /**
     * Create a new QuestionableRequest
     * @param fileName name of the file that is questionable.
     */
    public QuestionableRequest(String questionFile) {
        this.questionFile = questionFile;
    }

    /**
     * Get the request String that will be passed to the server
     * @return the request String
     */
    public String getRequestString() throws UnconfiguredRequestException {
        if (questionFile == null) {
            throw new UnconfiguredRequestException(
                    "Questionable request has not been configured");
        }

        return "Questionable " + questionFile + "\n"; //NOI18N
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

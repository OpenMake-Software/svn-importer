/*****************************************************************************
 * Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is available at http://www.sun.com/
 *
 * The Original Code is the CVS Client Library.
 * The Initial Developer of the Original Code is Robert Greig.
 * Portions created by Robert Greig are Copyright (C) 2000.
 * All Rights Reserved.
 *
 * Contributor(s): Robert Greig.
 *****************************************************************************/
package org.netbeans.lib.cvsclient.request;

/**
 * The directory request. Tell the server which directory to use.
 * @author  Robert Greig
 */
public final class DirectoryRequest extends Request {

    /**
     * The local directory. Used as the first argument
     */
    private final String localDirectory;

    /**
     * The repository. The second argument
     */
    private final String repository;

    /**
     * Create a new DirectoryRequest
     * @param theLocalDirectory the local directory argument
     * @param theRepository the repository argument
     */
    public DirectoryRequest(String localDirectory, String repository) {
        if (localDirectory == null || repository == null) {
            throw new IllegalArgumentException("Both, directory and repository, must not be null!");
        }

        this.localDirectory = localDirectory;
        this.repository = repository;
    }

    /**
     * Returns the value of used local directory.
     */
    public String getLocalDirectory() {
        return localDirectory;
    }

    /**
     * Returns the value of used repository directory.
     */
    public String getRepository() {
        return repository;
    }

    /**
     * Get the request String that will be passed to the server
     * @return the request String
     */
    public String getRequestString() {
        return "Directory " + localDirectory + "\n" + repository + "\n"; //NOI18N
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

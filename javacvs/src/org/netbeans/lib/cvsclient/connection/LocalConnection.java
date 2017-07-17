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
package org.netbeans.lib.cvsclient.connection;

import java.io.*;

import org.netbeans.lib.cvsclient.util.*;
import java.util.Map;
import java.util.HashMap;

/**
 * Implements a connection to a local server. See the cvs documents for more
 * information about different connection methods. Local is popular where
 * the CVS repository exists on the machine where the client library is
 * running.<p>
 * Because this library implements just the client part, it can not operate
 * directly on the repository. It needs a server to talk to. Therefore
 * it needs to execute the server process on the local machine.
 *
 * @author  Robert Greig
 */
public class LocalConnection extends AbstractConnection {


    /**
     * The CVS process that is being run.
     */
    protected Process process;

    /**
     * Creates a instance of ServerConnection.
     */
    public LocalConnection() {
        reset();
    }

    /**
     * Authenticate a connection with the server.
     *
     * @throws AuthenticationException if an error occurred
     */
    private void openConnection()
            throws AuthenticationException {
        try {
            process = Runtime.getRuntime().exec("cvs server"); //NOI18N
            setOutputStream(new LoggedDataOutputStream(process.
                                                      getOutputStream()));
            setInputStream(new LoggedDataInputStream(process.getInputStream()));
        }
        catch (IOException t) {
            reset();
            String locMessage = AuthenticationException.getBundleString(
                    "AuthenticationException.ServerConnection"); //NOI18N
            throw new AuthenticationException("Connection error", t, locMessage); //NOI18N
        }
    }

    private void reset() {
        process = null;
        setInputStream(null);
        setOutputStream(null);
    }

    /**
     * Authenticate with the server. Closes the connection immediately.
     * Clients can use this method to ensure that they are capable of
     * authenticating with the server. If no exception is thrown, you can
     * assume that authentication was successful
     *
     * @throws AutenticationException if the connection with the server
     *                                cannot be established
     */
    public void verify() throws AuthenticationException {
        try {
            process = Runtime.getRuntime().exec("cvs server"); //NOI18N
            process.destroy();
        }
        catch (Exception e) {
            String locMessage = AuthenticationException.getBundleString(
                    "AuthenticationException.ServerVerification"); //NOI18N
            throw new AuthenticationException("Verification error", e, locMessage); //NOI18N
        }
        finally {
            reset();
        }
    }

    /**
     * Authenticate with the server and open a channel of communication
     * with the server. This Client will
     * call this method before interacting with the server. It is up to
     * implementing classes to ensure that they are configured to
     * talk to the server (e.g. port number etc.)
     * @throws AutenticationException if the connection with the server
     * cannot be established
     */
    public void open() throws AuthenticationException {
        openConnection();
    }

    /**
     * Returns true to indicate that the connection was successfully established.
     */
    public boolean isOpen() {
        return process != null;
    }

    /**
     * Close the connection with the server.
     */
    public void close() throws IOException {
        try {
            if (process != null) {
                process.destroy();
            }
        }
        finally {
            reset();
        }
    }
    
    /**
     * @return 0, no port is used by the local connection.
     */
    public int getPort() {
        return 0; // No port
    }
    
    /**
     * Modify the underlying inputstream.
     * @param modifier the connection modifier that performs the modifications
     * @throws IOException if an error occurs modifying the streams
     */
    public void modifyInputStream(ConnectionModifier modifier)
            throws IOException {
        modifier.modifyInputStream(getInputStream());
    }

    /**
     * Modify the underlying outputstream.
     * @param modifier the connection modifier that performs the modifications
     * @throws IOException if an error occurs modifying the streams
     */
    public void modifyOutputStream(ConnectionModifier modifier)
            throws IOException {
        modifier.modifyOutputStream(getOutputStream());
    }
    
}

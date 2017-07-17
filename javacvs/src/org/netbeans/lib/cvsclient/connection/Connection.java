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
package org.netbeans.lib.cvsclient.connection;

import java.io.*;

import org.netbeans.lib.cvsclient.command.CommandAbortedException;
import org.netbeans.lib.cvsclient.util.*;

/**
 * Provides a method for accessing a connection, in order to be able to
 * communicate using the CVS Protocol. Instances of this interface are used
 * by the Client class to communicate with the server without being too
 * concerned with how the communication is taking place or how it was
 * set up.
 * @see org.netbeans.lib.cvsclient.Client
 * @author  Robert Greig
 */
public interface Connection {
    /**
     * Get a data inputstream for reading data
     * @return an input stream
     **/
    LoggedDataInputStream getInputStream();

    /**
     * Get an output stream for sending data to the server
     * @return an output stream
     **/
    LoggedDataOutputStream getOutputStream();

    /**
     * Open a connection with the server. Until this method is called, no
     * communication with the server can take place. This Client will
     * call this method before interacting with the server. It is up to
     * implementing classes to ensure that they are configured to
     * talk to the server (e.g. port number etc.)
     * @throws AutenticationException if the connection with the server
     * cannot be established
     **/
    void open() throws AuthenticationException, CommandAbortedException;

    /**
     * Verify a cnnection with the server. Simply verifies that a connection
     * could be made, for example that the user name and password are both
     * acceptable. Does not create input and output stream. For that, use
     * the open() method.
     */
    void verify() throws AuthenticationException;

    /**
     * Close the connection with the server
     */
    void close() throws IOException;

    /**
     * Returns true to indicate that the connection was successfully established.
     */
    boolean isOpen();

    /**
     * Get the repository
     */
    String getRepository();
    
    /**
     * Get the port number, which this connection is actually using.
     * @return The port number or zero, when the port number does not have sense.
     */
    int getPort();

    /**
     * Modify the underlying inputstream
     * @param modifier the connection modifier that performs the modifications
     * @throws IOException if an error occurs modifying the streams
     */
    void modifyInputStream(ConnectionModifier modifier) throws IOException;

    /**
     * Modify the underlying outputstream
     * @param modifier the connection modifier that performs the modifications
     * @throws IOException if an error occurs modifying the streams
     */
    void modifyOutputStream(ConnectionModifier modifier) throws IOException;
}

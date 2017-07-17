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

package org.netbeans.lib.cvsclient.connection;

import java.io.IOException;
import org.netbeans.lib.cvsclient.connection.Connection;
import org.netbeans.lib.cvsclient.util.*;


/**
 * This class abstracts the common features and functionality that all connection protocols to CVS
 * share
 *
 * @author Sriram Seshan
 */
public abstract class AbstractConnection implements Connection {
    
 
    /**
     * The name of the repository this connection is made to
     */
    private String repository = null;

    /**
     * The socket's input stream.
     */
    private LoggedDataInputStream inputStream;

    /**
     * The socket's output stream.
     */
    private LoggedDataOutputStream outputStream;

    /** Creates a new instance of AbstractConnection */
    public AbstractConnection() {
    }
    
    /**
     * Get an input stream for receiving data from the server.
     * @return a data input stream
     */
    public LoggedDataInputStream getInputStream() {
        return inputStream;
    }
    
    /**
     * Set an input stream for receiving data from the server.
     * The old stream (if any) is closed.
     * @param inputStream The data input stream
     */
    protected final void setInputStream(LoggedDataInputStream inputStream) {
        if (this.inputStream == inputStream) return ;
        if (this.inputStream != null) {
            try {
                this.inputStream.close();
            } catch (IOException ioex) {/*Ignore*/}
        }
        this.inputStream = inputStream;
    }

    /**
     * Get an output stream for sending data to the server.
     * @return an output stream
     */
    public LoggedDataOutputStream getOutputStream() {
        return outputStream;
    }    
 
    /**
     * Set an output stream for sending data to the server.
     * The old stream (if any) is closed.
     * @param outputStream The data output stream
     */
    protected final void setOutputStream(LoggedDataOutputStream outputStream) {
        if (this.outputStream == outputStream) return ;
        if (this.outputStream != null) {
            try {
                this.outputStream.close();
            } catch (IOException ioex) {/*Ignore*/}
        }
        this.outputStream = outputStream;
    }

    /**
     * Get the repository path.
     * @return the repository path, e.g. /home/banana/foo/cvs
     */
    public String getRepository() {
        return repository;
    }

    /**
     * Set the repository path.
     * @param root the repository
     */
    public void setRepository(String repository) {
        this.repository = repository;
    }    
    
}

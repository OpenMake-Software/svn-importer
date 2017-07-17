/*****************************************************************************
 * Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is available at http://www.sun.com/

 * The Original Code is the CVS Client Library.
 * The Initial Developer of the Original Code is Gerrit Riessen.
 * Portions created by Gerrit Riessen are Copyright (C) 2000.
 * All Rights Reserved.

 * Contributor(s): Gerrit Riessen.
 *****************************************************************************/

package org.netbeans.lib.cvsclient.connection;

import org.netbeans.lib.cvsclient.CVSRoot;

/**
 Simple class for managing the mapping from CVSROOT specifications to
 Connection classes.
 @author <a href="mailto:gerrit.riessen@wiwi.hu-berlin.de">Gerrit Riessen</a>, OAR Development AG
 @author <a href="mailto:rami.ojares@elisa.fi">Rami Ojares</a>, Elisa Internet Oy
 */
public class ConnectionFactory {
    
    /**
     <b>Protected Constructor</b>
     */
    protected ConnectionFactory() {}
    
    /**
     * Returns a Connection object to handle the specific CVSRoot
     * specification. This returns null if not suitable connection
     * was found.
     * 
     * If the return value is an instance of the PServerConnection class,
     * then the encoded password needs to be set if not defined in the CVSRoot.
     * This is left up to the client to set.
     */
    public static Connection getConnection(String cvsRoot) throws IllegalArgumentException {
        
        CVSRoot root = CVSRoot.parse(cvsRoot);
        return getConnection(root);
        
    }
    
    /**
     * Returns a Connection object to handle the specific CVSRoot
     * specification. This returns null if not suitable connection
     * was found.
     * 
     * If the return value is an instance of the PServerConnection class,
     * then the encoded password needs to be set if not defined in the CVSRoot.
     * This is left up to the client to set.
     */
    public static Connection getConnection(CVSRoot root) throws IllegalArgumentException {
        
        // LOCAL CONNECTIONS (no-method, local & fork)
        if (root.isLocal()) {
            LocalConnection con = new LocalConnection();
            con.setRepository(root.getRepository());
            return con;
        }
        
        String method = root.getMethod();
        // SSH2Connection (server, ext)
        /* SSH2Connection is TBD
        if (
            method == null || CVSRoot.METHOD_SERVER == method || CVSRoot.METHOD_EXT == method
        ) {
            // NOTE: If you want to implement your own authenticator you have to construct SSH2Connection yourself
            SSH2Connection con = new SSH2Connection(
                root,
                new ConsoleAuthenticator()
            );
            return con;
        }
         */
        
        // PServerConnection (pserver)
        if (CVSRoot.METHOD_PSERVER == method) {
            PServerConnection con = new PServerConnection(root);
            return con;
        }
        
        throw new IllegalArgumentException("Unrecognized CVS Root: " + root);
        
    }

}

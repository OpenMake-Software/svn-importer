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

import org.netbeans.lib.cvsclient.util.*;

/**
 * Provides a way to modify the communication channel between client and
 * server.
 * @author  Robert Greig
 */
public interface ConnectionModifier {

    void modifyInputStream(LoggedDataInputStream ldis) throws IOException;

    void modifyOutputStream(LoggedDataOutputStream ldos) throws IOException;

}


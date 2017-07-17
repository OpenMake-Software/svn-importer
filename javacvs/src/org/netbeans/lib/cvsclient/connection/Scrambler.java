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

/**
 * Provides methods used to scramble text. A concrete implementation will
 * use a particular encoding scheme to scramble the text.
 * @author  Robert Greig
 */
public interface Scrambler {
    /**
     * Scramble text, turning it into a String of scrambled data
     * @return a String containing the scrambled data
     */
    String scramble(String text);
}
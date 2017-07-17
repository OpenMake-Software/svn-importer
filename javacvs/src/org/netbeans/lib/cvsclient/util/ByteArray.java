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

package org.netbeans.lib.cvsclient.util;

/**
 * The growable array of bytes.
 * This class in not thread safe.
 *
 * @author  Martin Entlicher
 */
public class ByteArray extends Object {
    
    private byte[] bytesBuffer;
    private int length;
    
    /** Creates a new instance of ByteArray */
    public ByteArray() {
        bytesBuffer = new byte[50];
        length = 0;
    }
    
    /**
     * Add a byte to the byte array.
     */
    public void add(byte b) {
        if (bytesBuffer.length <= length) {
            byte[] newBytesBuffer = new byte[length + length/2];
            System.arraycopy(bytesBuffer, 0, newBytesBuffer, 0, bytesBuffer.length);
            bytesBuffer = newBytesBuffer;
        }
        bytesBuffer[length++] = b;
    }
    
    /**
     * Get the array of bytes.
     */
    public byte[] getBytes() {
        byte[] bytes = new byte[length];
        System.arraycopy(bytesBuffer, 0, bytes, 0, length);
        return bytes;
    }
    
    /**
     * Get the String representation of bytes in this array. The bytes are
     * decoded using the platform's default charset.
     */
    public String getStringFromBytes() {
        return new String(bytesBuffer, 0, length);
    }
    
    /**
     * Reset the byte array to zero length.
     */
    public void reset() {
        length = 0;
    }
}

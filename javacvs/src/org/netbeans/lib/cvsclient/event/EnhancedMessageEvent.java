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
package org.netbeans.lib.cvsclient.event;

/**
 * An event sent from the server to indicate that a message should be
 * displayed to the user
 * @author  Milos Kleint
 */
public class EnhancedMessageEvent extends MessageEvent {

    /**
     * Sent by MergedResponse when 2 files were merged.
     * The value is a String instance that tells the full path to the file.
     */
    public static final String MERGED_PATH = "Merged_Response_File_Path"; // NOI18N

    /**
     * Sent when a file was successfully sent to server.
     */
    public static final String FILE_SENDING = "File_Sent_To_Server"; // NOI18N

    /**
     * Sent when a all requests were sent to server.
     * Value is a String with value of "ok".
     */
    public static final String REQUESTS_SENT = "All_Requests_Were_Sent"; // NOI18N

    /**
     * Sent before all request are processed.
     * Value is an Integer object.
     */
    public static final String REQUESTS_COUNT = "Requests_Count"; // NOI18N

    private String key;
    private Object value;

    /**
     * Construct a MessageEvent
     * @param source the source of the event
     * @param key identifier. Specifies what the value object is.
     * @param value. Some value passed to the listeners. The key parameter helps
     *   the listeners to identify the value and handle it correctly.
     * for stderr rather than stdout), false otherwise
     */

    public EnhancedMessageEvent(Object source, String key, Object value) {
        super(source, "", false); // NOI18N
        this.key = key;
        this.value = value;
    }

    /** Getter for property key.
     * @return Value of property key.
     */
    public String getKey() {
        return key;
    }

    /** Setter for property key.
     * @param key New value of property key.
     */
    public void setKey(String key) {
        this.key = key;
    }

    /** Getter for property value.
     * @return Value of property value.
     */
    public Object getValue() {
        return value;
    }

    /** Setter for property value.
     * @param value New value of property value.
     */
    public void setValue(Object value) {
        this.value = value;
    }
}

/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.lib.cvsclient.command;

/**
 * @author  Thomas Singer
 */
public final class KeywordSubstitutionOptions {

    public static final KeywordSubstitutionOptions DEFAULT = new KeywordSubstitutionOptions("kv"); //NOI18N
    public static final KeywordSubstitutionOptions DEFAULT_LOCKER = new KeywordSubstitutionOptions("kvl"); //NOI18N
    public static final KeywordSubstitutionOptions ONLY_KEYWORDS = new KeywordSubstitutionOptions("k"); //NOI18N
    public static final KeywordSubstitutionOptions ONLY_VALUES = new KeywordSubstitutionOptions("v"); //NOI18N
    public static final KeywordSubstitutionOptions OLD_VALUES = new KeywordSubstitutionOptions("o"); //NOI18N
    public static final KeywordSubstitutionOptions BINARY = new KeywordSubstitutionOptions("b"); //NOI18N

    public static KeywordSubstitutionOptions findKeywordSubstOption(String keyword) {
        if (BINARY.toString().equals(keyword)) {
            return BINARY;
        }
        if (DEFAULT.toString().equals(keyword)) {
            return DEFAULT;
        }
        if (DEFAULT_LOCKER.toString().equals(keyword)) {
            return DEFAULT_LOCKER;
        }
        if (OLD_VALUES.toString().equals(keyword)) {
            return OLD_VALUES;
        }
        if (ONLY_KEYWORDS.toString().equals(keyword)) {
            return ONLY_KEYWORDS;
        }
        if (ONLY_VALUES.toString().equals(keyword)) {
            return ONLY_VALUES;
        }
        return null;
    }

    private String value;

    private KeywordSubstitutionOptions(String value) {
        this.value = value;
    }

    public String toString() {
        return value;
    }
}
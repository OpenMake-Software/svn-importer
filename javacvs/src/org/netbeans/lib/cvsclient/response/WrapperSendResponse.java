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

package org.netbeans.lib.cvsclient.response;

import java.io.*;
import java.util.*;

import org.netbeans.lib.cvsclient.util.*;
import org.netbeans.lib.cvsclient.util.SimpleStringPattern;
import org.netbeans.lib.cvsclient.command.KeywordSubstitutionOptions;

/**
 * This class handles the response from the server to a wrapper-sendme-rcsOptions
 * request
 * @author  Sriram Seshan
 */
public class WrapperSendResponse implements Response {
    
    public static Map parseWrappers(String line) {
        StringTokenizer tokenizer = new StringTokenizer(line);

        // the first token is the pattern
        SimpleStringPattern pattern = new SimpleStringPattern(tokenizer.nextToken());

        // it is followed by option value pairs
        String option, value;
        
        Map wrappersMap = null;

        while (tokenizer.hasMoreTokens()) {
            option = tokenizer.nextToken();
            value = tokenizer.nextToken();

            // do not bother with the -m Options now
            if (option.equals("-k")) { //NOI18N

                // This is a keyword substitution option
                // strip the quotes
                int first = value.indexOf('\'');
                int last = value.lastIndexOf('\'');
                if (first >=0 && last >= 0) {
                    value = value.substring(first+1, last);
                }

                KeywordSubstitutionOptions keywordOption = KeywordSubstitutionOptions.findKeywordSubstOption(value);
                if (wrappersMap == null) {
                    if (!tokenizer.hasMoreTokens()) {
                        wrappersMap = Collections.singletonMap(pattern, keywordOption);
                    } else {
                        wrappersMap = new LinkedHashMap();
                        wrappersMap.put(pattern, keywordOption);
                    }
                } else {
                    wrappersMap.put(pattern, keywordOption);
                }
            }
        }
        return wrappersMap;
    }
    
    /**
     * Process the data for the response.
     * @param dis the data inputstream allowing the client to read the server's
     * response. Note that the actual response name has already been read
     * and the input stream is positioned just before the first argument, if
     * any.
     */
    public void process(LoggedDataInputStream dis, ResponseServices services)
            throws ResponseException {
        try {
            
            String wrapperSettings = dis.readLine();
            Map wrappers = parseWrappers(wrapperSettings);
            for (Iterator it = wrappers.keySet().iterator(); it.hasNext(); ) {
                StringPattern pattern = (StringPattern) it.next();
                KeywordSubstitutionOptions keywordOption = (KeywordSubstitutionOptions) wrappers.get(pattern);
                services.addWrapper(pattern, keywordOption);
            }
        }
        catch (EOFException ex) {
            throw new ResponseException(ex, ResponseException.getLocalMessage("CommandException.EndOfFile", null)); //NOI18N
        }
        catch (IOException ex) {
            throw new ResponseException(ex);
        }
        catch (NoSuchElementException nse) {
            throw new ResponseException(nse);            
        }
    }

    /**
     * Is this a terminal response, i.e. should reading of responses stop
     * after this response. This is true for responses such as OK or
     * an error response
     */
    public boolean isTerminalResponse() {
        return false;
    }
    
}

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
package org.netbeans.lib.cvsclient.response;

import java.util.*;

/**
 * Create response objects appropriate for handling different types of response
 * @author  Robert Greig
 */
public class ResponseFactory {
    
    private final Map responseInstancesMap;
    private String previousResponse = null;
    
    public ResponseFactory() {
        responseInstancesMap = new HashMap();
        responseInstancesMap.put("E", new ErrorMessageResponse()); //NOI18N
        responseInstancesMap.put("M", new MessageResponse()); //NOI18N
        responseInstancesMap.put("Mbinary", new MessageBinaryResponse()); //NOI18N
        responseInstancesMap.put("MT", new MessageTaggedResponse()); //NOI18N
        responseInstancesMap.put("Updated", new UpdatedResponse()); //NOI18N
        responseInstancesMap.put("Rcs-diff", new RcsDiffResponse()); //NOI18N
        responseInstancesMap.put("Checked-in", new CheckedInResponse()); //NOI18N
        responseInstancesMap.put("New-entry", new NewEntryResponse()); //NOI18N
        responseInstancesMap.put("ok", new OKResponse()); //NOI18N
        responseInstancesMap.put("error", new ErrorResponse()); //NOI18N
        responseInstancesMap.put("Set-static-directory", new SetStaticDirectoryResponse()); //NOI18N
        responseInstancesMap.put("Clear-static-directory", new ClearStaticDirectoryResponse()); //NOI18N
        responseInstancesMap.put("Set-sticky", new SetStickyResponse()); //NOI18N
        responseInstancesMap.put("Clear-sticky", new ClearStickyResponse()); //NOI18N
        responseInstancesMap.put("Valid-requests", new ValidRequestsResponse()); //NOI18N
        responseInstancesMap.put("Merged", new MergedResponse()); //NOI18N
        responseInstancesMap.put("Notified", new NotifiedResponse()); //NOI18N
        responseInstancesMap.put("Removed", new RemovedResponse()); //NOI18N
        responseInstancesMap.put("Remove-entry", new RemoveEntryResponse()); //NOI18N
        responseInstancesMap.put("Copy-file", new CopyFileResponse()); //NOI18N
        responseInstancesMap.put("Mod-time", new ModTimeResponse()); //NOI18N
        responseInstancesMap.put("Template", new TemplateResponse()); //NOI18N
        responseInstancesMap.put("Module-expansion", new ModuleExpansionResponse()); //NOI18N
        responseInstancesMap.put("Wrapper-rcsOption", new WrapperSendResponse()); //NOI18N
        
    }
    
    public Response createResponse(String responseName) {
        Response response = (Response)responseInstancesMap.get(responseName);
        if (response != null) {
            previousResponse = responseName;
            return response;
        }
        if (previousResponse != null && previousResponse.equals("M")) { //NOI18N
            return new MessageResponse(responseName);
        }
        previousResponse = null;
        throw new IllegalArgumentException("Unhandled response: " + //NOI18N
                                              responseName + "."); //NOI18N
    }
}

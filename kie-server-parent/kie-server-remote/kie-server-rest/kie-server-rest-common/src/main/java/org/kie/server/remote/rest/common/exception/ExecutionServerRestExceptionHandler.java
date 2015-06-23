/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.remote.rest.common.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class makes sure that exceptions thrown as a result of REST requests
 * are translated into a user-friendly output (with the correct REST error code)
 * for the client.
 */
@Provider
public class ExecutionServerRestExceptionHandler implements ExceptionMapper<ExecutionServerRestOperationException> {

    protected static Logger logger = LoggerFactory.getLogger(ExecutionServerRestExceptionHandler.class);

    // TODO: add info to response about e.getThrowable()
    // TODO: return an object, instead a simple string, depending on the HTTP error code
    
    @Override
    public Response toResponse(ExecutionServerRestOperationException e) {
        
        // Translate exception to status
        ResponseBuilder responseBuilder = null;
        int status = -1;
        status = e.getStatus();

        // Convert status to correct information in response
        if (status > 0) {
            responseBuilder = Response.status(status);
        } else {
            responseBuilder = Response.serverError();
        }
       
        // Build and send response
        responseBuilder.entity(e.getMessage());
       
        Variant variant = e.getVariant();
        return responseBuilder.variant(variant).build();
    }

    public static String getRelativePath(UriInfo uriInfo) { 
        return uriInfo.getRequestUri().toString().replaceAll( ".*/rest", "");
    }
    
}

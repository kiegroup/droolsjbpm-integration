/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.kie.remote.services.rest.exception;

import static org.kie.remote.common.rest.RestEasy960Util.getVariant;
import static org.kie.remote.common.rest.RestEasy960Util.jsonVariant;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import javax.xml.bind.JAXBException;

import org.kie.services.client.serialization.jaxb.impl.JaxbRestRequestException;
import org.kie.services.client.serialization.jaxb.impl.JaxbRequestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class makes sure that exceptions thrown as a result of REST requests
 * are translated into a user-friendly output (with the correct REST error code)
 * for the client.
 */
@Provider
@RequestScoped
public class DescriptiveExceptionHandler implements ExceptionMapper<KieRemoteRestOperationException> {

    protected static Logger logger = LoggerFactory.getLogger(DescriptiveExceptionHandler.class);

    @Context
    protected UriInfo uriInfo;

    @Context
    protected HttpHeaders headers;   
    
    @Override
    public Response toResponse(KieRemoteRestOperationException e) {
        
        // Translate exception to status
        ResponseBuilder responseBuilder = null;
        int status = -1;
        status = e.getStatus();

        logger.warn("Exception thrown when processing request [" + getRelativePath(uriInfo) + "]; responding with status " + status, e);

        // Convert status to correct information in response
        if (status > 0) {
            responseBuilder = Response.status(status);
        } else {
            responseBuilder = Response.serverError();
        }
       
        boolean addException = true;
        JaxbRequestStatus requestStatus;
        switch(status) { 
        case 400:
            requestStatus = JaxbRequestStatus.BAD_REQUEST;
            addException = false;
            break;
        case 403: 
            requestStatus = JaxbRequestStatus.FORBIDDEN;
            break;
        case 404: 
            requestStatus = JaxbRequestStatus.NOT_FOUND;
            addException = false;
            break;
        case 409:
            requestStatus = JaxbRequestStatus.PERMISSIONS_CONFLICT;
            break;
        case 500:
        case -1: 
        default:
            requestStatus = JaxbRequestStatus.FAILURE;
        }
       
        // Build and send response
        Exception responseException = addException ? e : null;
        JaxbRestRequestException response = new JaxbRestRequestException(uriInfo.getRequestUri().toString(), responseException, requestStatus);
        try {
            responseBuilder.entity(response.prettyPrint());
        } catch (JAXBException jaxbe) {
            responseBuilder.entity(JaxbRestRequestException.convertStackTraceToString(jaxbe));
        }
       
        Variant variant = getVariant(headers); 
        return responseBuilder.variant(variant).build();
    }

    public static String getRelativePath(UriInfo uriInfo) { 
        return uriInfo.getRequestUri().toString().replaceAll( ".*/rest", "");
    }
    
}

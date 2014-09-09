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
    
    @Context 
    private Providers providers;
    

    private static final String [] kieServicesRemotePaths = { 
       "deployment",
       "history",
       "runtime",
       "task" 
    };
    
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
        
        JaxbRequestStatus requestStatus;
        switch(status) { 
        case 400:
            requestStatus = JaxbRequestStatus.BAD_REQUEST;
            break;
        case 403: 
            requestStatus = JaxbRequestStatus.FORBIDDEN;
            break;
        case 404: 
            requestStatus = JaxbRequestStatus.NOT_FOUND;
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
        JaxbRestRequestException response = new JaxbRestRequestException(uriInfo.getRequestUri().toString(), e, requestStatus);
        try {
            responseBuilder.entity(response.prettyPrint());
        } catch (JAXBException jaxbe) {
            responseBuilder.entity(JaxbRestRequestException.convertStackTraceToString(jaxbe));
        }
       
        // Determine if the exception came from kie-services-remote or guvnor
        // - if guvnor use JSON
        boolean knowledgeStoreUrl = true;
        String path = uriInfo.getRequestUri().toString().replaceAll( ".*/rest/", "");
        for( String resourcePath : kieServicesRemotePaths ) { 
            if( path.startsWith(resourcePath) ) { 
                knowledgeStoreUrl = false;
                break;
            }
        }
        Variant variant;
        if( knowledgeStoreUrl ) { 
           variant = jsonVariant;
        } else { 
           variant = getVariant(headers); 
        }
        
        return responseBuilder.variant(variant).build();
    }

    public static String getRelativePath(UriInfo uriInfo) { 
        return uriInfo.getRequestUri().toString().replaceAll( ".*/rest", "");
    }
    
}

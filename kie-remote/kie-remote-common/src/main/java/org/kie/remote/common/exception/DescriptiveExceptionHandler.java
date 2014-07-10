package org.kie.remote.common.exception;

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
import javax.xml.bind.JAXBException;

import org.kie.remote.common.jaxb.JaxbException;
import org.kie.remote.common.jaxb.JaxbRequestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class makes sure that exceptions thrown as a result of REST requests
 * are translated into a user-friendly output (with the correct REST error code)
 * for the client.
 */
@Provider
@RequestScoped
public class DescriptiveExceptionHandler implements ExceptionMapper<Exception> {

    protected static Logger logger = LoggerFactory.getLogger(DescriptiveExceptionHandler.class);

    @Context
    protected UriInfo uriInfo;

    @Context
    protected HttpHeaders headers;


    private static final String [] kieServicesRemotePaths = { 
       "deployment",
       "history",
       "runtime",
       "task" 
    };
    
    @Override
    public Response toResponse(Exception e) {
        
        // Translate exception to status
        ResponseBuilder responseBuilder = null;
        int status = -1;
        if (e instanceof RestOperationException) {
            status = ((RestOperationException) e).getStatus();
        } else if (e.getClass().getPackage().getName().equals("org.jboss.resteasy.spi")) {
            String simpleClassName = e.getClass().getSimpleName();
            if ("BadRequestException".equals(simpleClassName)) {
                status = 400; // 400: Bad request (due to syntax)
            } else if ("ReaderException".equals(simpleClassName)) {
                status = 400;
            } else if ("UnauthorizedException".equals(simpleClassName)) {
                status = 401; // 401: Unauthorized
            } else if ("NotFoundException".equals(simpleClassName)) {
                status = 404; // 404: Not found
            } else if ("MethodNotAllowedException".equals(simpleClassName)) {
                status = 405; // 405: (POST, GET, etc.) Method not allowed
            } else if ("NotAcceptableException".equals(simpleClassName)) {
                status = 406; // 406: Not acceptable (form of request)
            } else if ("WriterException".equals(simpleClassName)) {
                status = 500; // 500: internal server error
            } else if ("InternalServerErrorException".equals(simpleClassName)) {
                status = 500;
            }
        }
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
        JaxbException response = new JaxbException(uriInfo.getRequestUri().toString(), e, requestStatus);
        try {
            responseBuilder.entity(response.prettyPrint());
        } catch (JAXBException jaxbe) {
            responseBuilder.entity(JaxbException.convertStackTraceToString(jaxbe));
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

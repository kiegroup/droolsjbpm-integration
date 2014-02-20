package org.kie.services.remote.rest.exception;

import static org.kie.services.remote.rest.ResourceBase.getRelativePath;
import static org.kie.services.remote.rest.ResourceBase.getVariant;

import javax.enterprise.context.RequestScoped;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBException;

import org.kie.services.client.serialization.jaxb.rest.JaxbExceptionResponse;
import org.kie.services.client.serialization.jaxb.rest.JaxbGenericResponse;
import org.kie.services.client.serialization.jaxb.rest.JaxbRequestStatus;
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

    private static final Logger logger = LoggerFactory.getLogger(DescriptiveExceptionHandler.class);

    @Context
    HttpServletRequest request;

    @Context
    HttpHeaders headers;

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
        logger.warn("Exception thrown when processing request [" + getRelativePath(request) + "]; responding with status " + status, e);

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
        JaxbExceptionResponse response = new JaxbExceptionResponse(request, e, requestStatus);
        try {
            responseBuilder.entity(response.prettyPrint());
        } catch (JAXBException jaxbe) {
            responseBuilder.entity(JaxbExceptionResponse.convertStackTraceToString(jaxbe));
        }
        return responseBuilder.variant(getVariant(headers)).build();
    }

}

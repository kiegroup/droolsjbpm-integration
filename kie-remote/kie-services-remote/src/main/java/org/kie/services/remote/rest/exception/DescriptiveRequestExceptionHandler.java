package org.kie.services.remote.rest.exception;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class DescriptiveRequestExceptionHandler implements ExceptionMapper<IncorrectRequestException> {

    @Context
    HttpServletRequest request;
    
    @Override
    public Response toResponse(IncorrectRequestException ire) {
        ResponseBuilder responseBuilder = Response.status(400);
        String url = request.getRequestURI();
        if( request.getQueryString() != null ) { 
            url += "?" + request.getQueryString();
        }
        String msg = "<errorResponse>\n"
                + "<link>" + url + "</link>\n"
                + "<error>" + ire.getMessage() + "</error>\n"
                + "</errorResponse>";
        responseBuilder.entity(msg);
        // TODO: jaxb object for ^^
        return responseBuilder.build();
    }

}

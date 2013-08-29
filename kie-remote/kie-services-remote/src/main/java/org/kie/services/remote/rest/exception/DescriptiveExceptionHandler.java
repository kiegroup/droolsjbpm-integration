package org.kie.services.remote.rest.exception;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBException;

import org.jboss.resteasy.spi.BadRequestException;
import org.jboss.resteasy.spi.InternalServerErrorException;
import org.jboss.resteasy.spi.MethodNotAllowedException;
import org.jboss.resteasy.spi.NotAcceptableException;
import org.jboss.resteasy.spi.NotFoundException;
import org.jboss.resteasy.spi.ReaderException;
import org.jboss.resteasy.spi.UnauthorizedException;
import org.jboss.resteasy.spi.WriterException;
import org.kie.services.client.serialization.jaxb.rest.JaxbGenericResponse;
import org.kie.services.remote.exception.KieRemoteServicesPreConditionException;

@Provider
public class DescriptiveExceptionHandler implements ExceptionMapper<Exception> {

    @Context
    HttpServletRequest request; 
    
    @Override
    public Response toResponse(Exception e) {
        ResponseBuilder responseBuilder = null;
        if( e instanceof KieRemoteServicesPreConditionException ) { 
            responseBuilder = Response.status(412); // 412: precondition failed
        } else if( e instanceof BadRequestException ) { 
            responseBuilder = Response.status(400); // 400: Bad request (due to syntax)
        } else if( e instanceof ReaderException ) { 
            responseBuilder = Response.status(400);
        } else if( e instanceof UnauthorizedException ) { 
            responseBuilder = Response.status(401); // 401: Unauthorized
        } else if( e instanceof NotFoundException ) { 
            responseBuilder = Response.status(404); // 404: Not found
        } else if( e instanceof MethodNotAllowedException ) { 
            responseBuilder = Response.status(405); // 405: (POST, GET, etc.) Method not allowed
        } else if( e instanceof NotAcceptableException ) { 
            responseBuilder = Response.status(406); // 406: Not acceptable (form of request)
        } else if( e instanceof WriterException ) { 
            responseBuilder = Response.status(500); // 500: internal server error
        } else if( e instanceof InternalServerErrorException ) { 
            responseBuilder = Response.status(500);
        } else {
            responseBuilder = Response.serverError();
        }
        
        JaxbGenericResponse response = new JaxbGenericResponse(request, e);
        
        try {
            responseBuilder.entity(response.prettyPrint());
        } catch (JAXBException jaxb) {
            responseBuilder.entity(JaxbGenericResponse.convertStackTraceToString(jaxb));
        }
        return responseBuilder.build();
    }

}

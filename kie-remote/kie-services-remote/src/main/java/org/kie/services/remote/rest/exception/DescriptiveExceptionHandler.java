package org.kie.services.remote.rest.exception;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBException;

import org.kie.services.remote.rest.jaxb.general.JaxbGenericResponse;

@Provider
public class DescriptiveExceptionHandler implements ExceptionMapper<Exception> {

    @Context
    HttpServletRequest request; 
    
    @Override
    public Response toResponse(Exception e) {
        ResponseBuilder responseBuilder = Response.serverError();
        JaxbGenericResponse response = new JaxbGenericResponse(request, e);
        try {
            responseBuilder.entity(response.prettyPrint());
        } catch (JAXBException jaxb) {
            responseBuilder.entity(JaxbGenericResponse.convertStackTraceToString(jaxb));
        }
        return responseBuilder.build();
    }

}

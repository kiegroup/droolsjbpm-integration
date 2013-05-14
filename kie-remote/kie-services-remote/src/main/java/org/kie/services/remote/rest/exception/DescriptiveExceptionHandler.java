package org.kie.services.remote.rest.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class DescriptiveExceptionHandler implements ExceptionMapper<IncorrectRequestException> {

    @Override
    public Response toResponse(IncorrectRequestException ire) {
        Response.serverError();
        return null;
    }

}

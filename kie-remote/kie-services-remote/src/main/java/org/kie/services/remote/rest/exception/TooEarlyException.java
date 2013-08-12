package org.kie.services.remote.rest.exception;

import org.jboss.resteasy.spi.BadRequestException;

public class TooEarlyException extends BadRequestException {

    /** generated serial version UID */
    private static final long serialVersionUID = 1L;

    public TooEarlyException(String msg) { 
        super(msg);
    }
}

package org.kie.services.remote.exception;

import org.jboss.resteasy.spi.BadRequestException;

public class DomainNotFoundBadRequestException extends BadRequestException {

    /** Generated serial version UID */
    private static final long serialVersionUID = 6533087530265037387L;

    public DomainNotFoundBadRequestException(String s) {
        super(s);
    }

    public DomainNotFoundBadRequestException(String s, Throwable throwable) {
        super(s, throwable);
    }

}

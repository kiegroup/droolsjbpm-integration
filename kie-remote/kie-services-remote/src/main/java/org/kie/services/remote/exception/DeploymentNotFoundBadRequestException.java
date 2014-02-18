package org.kie.services.remote.exception;

import org.jboss.resteasy.spi.BadRequestException;

/**
 * This exception should be thrown when a REST operation references a deployment unit
 * that does not exist or can not be found.
 */
public class DeploymentNotFoundBadRequestException extends BadRequestException {

    /** Generated serial version UID */
    private static final long serialVersionUID = 6533087530265037387L;

    public DeploymentNotFoundBadRequestException(String s) {
        super(s);
    }

    public DeploymentNotFoundBadRequestException(String s, Throwable throwable) {
        super(s, throwable);
    }

}

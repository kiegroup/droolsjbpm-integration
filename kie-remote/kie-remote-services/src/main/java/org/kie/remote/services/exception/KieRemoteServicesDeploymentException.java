package org.kie.remote.services.exception;

import org.kie.remote.services.rest.exception.KieRemoteRestOperationException;



/**
 * This exception should be thrown when operation's relating to a deployment fail.
 */
public class KieRemoteServicesDeploymentException extends KieRemoteRestOperationException {

    /** generated serial version UID */
    private static final long serialVersionUID = -8714630316007772520L;

    public KieRemoteServicesDeploymentException(String s) {
        super(s, Status.INTERNAL_SERVER_ERROR);
    }

    public KieRemoteServicesDeploymentException(String s, Throwable throwable) {
        super(s, throwable, Status.INTERNAL_SERVER_ERROR);
    }
}

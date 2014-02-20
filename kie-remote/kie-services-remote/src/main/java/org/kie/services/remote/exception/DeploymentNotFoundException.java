package org.kie.services.remote.exception;

import org.kie.services.remote.rest.exception.RestOperationException;


/**
 * This exception should be thrown when a REST or JMS operation references a deployment unit
 * that does not exist or can not be found.
 */
public class DeploymentNotFoundException extends RestOperationException {

    /** Generated serial version UID */
    private static final long serialVersionUID = 6533087530265037387L;

    public DeploymentNotFoundException(String s) {
        super(s, Status.NOT_FOUND);
    }

    public DeploymentNotFoundException(String s, Throwable throwable) {
        super(s, throwable, Status.NOT_FOUND);
    }

}

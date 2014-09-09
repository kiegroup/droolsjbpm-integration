package org.kie.remote.services.exception;

import org.kie.remote.services.rest.exception.KieRemoteRestOperationException;


/**
 * This exception should be thrown when a REST or JMS operation references a deployment unit
 * that does not exist or can not be found.
 */
public class DeploymentNotFoundException extends KieRemoteRestOperationException {

    /** Generated serial version UID */
    private static final long serialVersionUID = 6533087530265037387L;

    public DeploymentNotFoundException(String s) {
        super(s, Status.NOT_FOUND);
    }

    public DeploymentNotFoundException(String s, Throwable throwable) {
        super(s, throwable, Status.NOT_FOUND);
    }

}

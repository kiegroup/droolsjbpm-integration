package org.kie.remote.services.exception;



/**
 * This exception should be thrown when an operation by infrastructure code, such as JMS or REST code, that we expect to succeed has failed. 
 */
public class KieRemoteServicesRuntimeException extends RuntimeException {

    /** generated serial version UID */
    private static final long serialVersionUID = -5411787229668474892L;

    public KieRemoteServicesRuntimeException(String msg) { 
        super(msg);
    }

    public KieRemoteServicesRuntimeException(String msg, Throwable cause) { 
        super(msg, cause);
    }
}

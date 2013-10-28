package org.kie.services.remote.exception;


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

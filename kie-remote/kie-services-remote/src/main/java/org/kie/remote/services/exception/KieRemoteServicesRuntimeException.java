package org.kie.remote.services.exception;

import javax.jms.Connection;
import javax.jms.Session;


/**
 * This exception is primarily used by the JMS code to indicate that an operation
 * that we expect to succeed has failed. In most of the cases, when this exception
 * is thrown, it is due to a failure by the underlying JMS framework ({@link Session}, 
 * {@link Connection}).
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

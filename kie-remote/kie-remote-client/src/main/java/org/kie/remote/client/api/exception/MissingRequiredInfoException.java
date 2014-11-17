package org.kie.remote.client.api.exception;

import org.kie.remote.client.api.RemoteRuntimeEngineFactory;


/**
 * This exception is thrown by the *RuntimeFactory classes
 * when a method is called on instances created by the 
 * {@link RemoteRuntimeEngineFactory}
 * </p>
 * It indicates that the client instance can not execute
 * the method called because required information is missing.
 */
public class MissingRequiredInfoException extends org.kie.services.client.api.command.exception.MissingRequiredInfoException {

    /** generated serial version UID */
    private static final long serialVersionUID = 7415935205523780077L;

    public MissingRequiredInfoException() {
        super();
    }
    
    public MissingRequiredInfoException(String msg) { 
        super(msg);
    }
}

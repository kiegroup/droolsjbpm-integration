package org.kie.services.client.api.command;

/**
 * This exception is thrown when unexpected exceptions are returned 
 * during interaction with a remote REST or JMS service 
 * in the client RemoteRuntime* classes. 
 */
public class RemoteRuntimeException extends RuntimeException {

    /** generated serial version UID **/
    private static final long serialVersionUID = 7230681758239352495L;

    public RemoteRuntimeException(String msg, Throwable cause) { 
        super(msg, cause);
    }
    
    public RemoteRuntimeException(String msg) { 
        super(msg);
    }
    
}

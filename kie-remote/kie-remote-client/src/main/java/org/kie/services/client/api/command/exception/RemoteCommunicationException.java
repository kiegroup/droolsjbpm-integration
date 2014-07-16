package org.kie.services.client.api.command.exception;

/**
 * This exception is thrown when communications with a remote REST or JMS service 
 * fail in the client RemoteRuntime* classes. 
 * </p>
 * In other words, this exception indicates<ul>
 * <li>That the communication has failed</li>
 * </ul>
 */
public class RemoteCommunicationException extends RuntimeException {

    /** generated serial version UID **/
    private static final long serialVersionUID = 7230681758239352495L;

    public RemoteCommunicationException(String msg, Throwable cause) { 
        super(msg, cause);
    }
    
    public RemoteCommunicationException(String msg) { 
        super(msg);
    }
    
}

package org.kie.services.client.api.command.exception;

/**
 * This class will be deleted as of 7.x
 * 
 * @see org.kie.remote.client.api.exception.RemoteCommunicationException
 */
@Deprecated
public class RemoteCommunicationException extends RuntimeException {

    public RemoteCommunicationException(String msg, Throwable cause) { 
        super(msg, cause);
    }
    
    public RemoteCommunicationException(String msg) { 
        super(msg);
    }
    
}

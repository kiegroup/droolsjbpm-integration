package org.kie.services.client.api.builder.exception;


/**
 * @see org.kie.remote.client.api.exception.InsufficientInfoToBuildException
 */
@Deprecated
public class InsufficientInfoToBuildException extends IllegalStateException {

    public InsufficientInfoToBuildException() {
        super();
    }
    
    public InsufficientInfoToBuildException(String msg) { 
        super(msg);
    }
}

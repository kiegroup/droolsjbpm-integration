package org.kie.services.client.api.builder.exception;


/**
 * This class will be deleted as of 7.x
 * 
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

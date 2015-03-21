package org.kie.services.client.api.command.exception;

import org.kie.services.client.api.RemoteRuntimeEngineFactory;

/**
 * This class will be deleted as of 7.x
 * 
 * @see org.kie.remote.client.api.exception.MissingRequiredInfoException
 */
@Deprecated
public class MissingRequiredInfoException extends IllegalStateException {

    public MissingRequiredInfoException() {
        super();
    }
    
    public MissingRequiredInfoException(String msg) { 
        super(msg);
    }
    
}

package org.kie.services.client.api.command.exception;

import org.kie.services.client.api.RemoteRuntimeEngineFactory;

/**
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

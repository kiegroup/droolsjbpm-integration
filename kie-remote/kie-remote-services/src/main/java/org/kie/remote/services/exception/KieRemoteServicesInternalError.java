package org.kie.remote.services.exception;

/**
 * This class is meant to be thrown in situations that should never happen. 
 * </p> 
 * If this exception *is* thrown, then it's almost certain that it's caused by a the code, 
 * and not be the circumstances or data presented to this component. 
 */
public class KieRemoteServicesInternalError extends Error {

    /** generated serial version UID */
    private static final long serialVersionUID = -6741972907562227891L;
   
    public KieRemoteServicesInternalError(String msg) { 
        super(msg);
    }

    public KieRemoteServicesInternalError(String msg, Throwable cause) { 
        super(msg, cause);
    }

}

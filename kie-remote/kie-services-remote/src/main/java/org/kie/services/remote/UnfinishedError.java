package org.kie.services.remote;

public class UnfinishedError extends Error {

    /** generated serial version UID */
    private static final long serialVersionUID = -6741972907562227891L;
   
    public UnfinishedError(String msg) { 
        super(msg);
    }

    public UnfinishedError(String msg, Throwable cause) { 
        super(msg, cause);
    }

}

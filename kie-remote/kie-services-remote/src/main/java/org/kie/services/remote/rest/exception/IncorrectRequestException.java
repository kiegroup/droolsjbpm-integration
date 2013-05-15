package org.kie.services.remote.rest.exception;

public class IncorrectRequestException extends RuntimeException {

    /** generated serial version UID */
    private static final long serialVersionUID = 5259958024823520830L;


    public IncorrectRequestException(String msg) { 
        super(msg);
    }
    

    public IncorrectRequestException(String msg, Throwable cause) { 
        super(msg, cause);
    }
}    
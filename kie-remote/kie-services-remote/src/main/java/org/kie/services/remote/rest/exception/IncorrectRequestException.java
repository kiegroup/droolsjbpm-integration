package org.kie.services.remote.rest.exception;

public class IncorrectRequestException extends RuntimeException {

    public IncorrectRequestException(String msg) { 
        super(msg);
    }
    

    public IncorrectRequestException(String msg, Throwable cause) { 
        super(msg, cause);
    }
}    
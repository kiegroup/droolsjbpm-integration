package org.kie.services.remote.rest.exception;

import java.net.URL;

public class IncorrectRequestException extends RuntimeException {

    /** generated serial version UID */
    private static final long serialVersionUID = 5259958024823520830L;
    private URL url;

    public IncorrectRequestException(String msg) { 
        super(msg);
        this.url = url;
    }
    

    public IncorrectRequestException(String msg, Throwable cause) { 
        super(msg, cause);
        this.url = url;
    }
}    
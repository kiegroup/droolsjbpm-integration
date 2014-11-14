package org.kie.server.client;

public class SerializationException extends RuntimeException {

    /** Generated serial version UID */
    private static final long serialVersionUID = -4588806446951069542L;

    public SerializationException(String msg, Throwable cause) { 
        super(msg, cause);
    }
    
    public SerializationException(String msg) { 
        super(msg);
    }
}

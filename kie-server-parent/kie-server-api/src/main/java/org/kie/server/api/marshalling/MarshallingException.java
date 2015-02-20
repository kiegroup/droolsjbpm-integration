package org.kie.server.api.marshalling;

public class MarshallingException
        extends RuntimeException {

    /** Generated serial version UID */
    private static final long serialVersionUID = -4588806446951069542L;

    public MarshallingException(String msg, Throwable cause) {
        super(msg, cause);
    }
    
    public MarshallingException(String msg) {
        super(msg);
    }
}

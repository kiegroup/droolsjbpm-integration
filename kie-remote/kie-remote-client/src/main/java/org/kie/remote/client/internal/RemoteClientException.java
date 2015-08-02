package org.kie.remote.client.internal;

public abstract class RemoteClientException extends RuntimeException {

    /** generated serial version UID */
    private static final long serialVersionUID = -5838803377930362293L;

    private String shortMessage;
   
    public RemoteClientException(String shortMsg, String msg, Throwable throwable) { 
        super(msg, throwable);
        this.shortMessage = shortMsg;
    }
    
    public RemoteClientException(String shortMsg, String msg) { 
        super(msg);
        this.shortMessage = shortMsg;
    }
    
    public RemoteClientException(String msg, Throwable throwable) { 
        super(msg, throwable);
    }
    
    public RemoteClientException(String msg) { 
        super(msg);
    }
    
    public RemoteClientException() { 
        super();
    }
    
    public String getShortMessage() { 
        return shortMessage;
    }
    
}

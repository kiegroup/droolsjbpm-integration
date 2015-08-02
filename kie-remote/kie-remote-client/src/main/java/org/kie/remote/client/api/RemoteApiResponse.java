package org.kie.remote.client.api;

import javax.xml.bind.annotation.XmlType;


public class RemoteApiResponse<R> {
 
    @XmlType
    public static enum RemoteOperationStatus { 
        SUCCESS,
        PERMISSIONS_FAILURE,
        COMMUNICATION_FAILURE,
        CLIENT_FAILURE,
        SERVER_FAILURE,
        UNKNOWN_FAILURE;
    }
   
    private RemoteOperationStatus status = RemoteOperationStatus.SUCCESS;
    
    private String statusDetails = null;
    
    private Throwable cause = null;
    
    private R result = null;
    
    public RemoteApiResponse() { 
        // default constructor
    }
    
    public RemoteApiResponse(RemoteOperationStatus status, String statusDetails) { 
       this.status = status;
       this.statusDetails = statusDetails;
    }
    
    public RemoteApiResponse(RemoteOperationStatus status, Exception exception) { 
        this(status, exception.getMessage());
        this.result = null;
    }
    
    public RemoteApiResponse(RemoteOperationStatus status, String message, Throwable cause) { 
        this(status, message);
        this.cause = cause;
    }
    
    public RemoteApiResponse(R result) { 
        this.status = RemoteOperationStatus.SUCCESS;
        this.result = result;
    }
    
    public RemoteOperationStatus getStatus() {
        return status;
    }

    public String getStatusDetails() {
        return statusDetails;
    }

    public Throwable getCause() {
        return cause;
    }

    public R getResult() { 
       return this.result;
    }

}

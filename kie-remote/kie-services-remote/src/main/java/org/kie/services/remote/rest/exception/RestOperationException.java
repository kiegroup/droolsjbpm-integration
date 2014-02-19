package org.kie.services.remote.rest.exception;


public class RestOperationException extends RuntimeException {

    /** Generated serial version UID */
    private static final long serialVersionUID = 3065096836333886139L;

    protected Integer status = null;

    public RestOperationException(String s) {
        super(s);
    }

    protected RestOperationException(String s, Status status) {
        super(s);
        this.status = status.getValue();
    }

    public RestOperationException(String s, Throwable throwable) {
        super(s, throwable);
    }

    protected RestOperationException(String s, Throwable throwable, Status status) {
        super(s, throwable);
        this.status = status.getValue();
    }

    public int getStatus() {
        return status;
    }

    public enum Status { 
        BAD_REQUEST(400), UNAUTHORIZED(401),
        NOT_FOUND(404), METHOD_NOT_ALLOWED(405),
        NOT_ACCEPTABLE(406),
        PRE_CONDITION_FAILED(412),
        INTERNAL_SERVER_ERROR(500);
       
        private final int status;
        
        Status(int s) { 
           this.status = s; 
        }
        
        public int getValue() { 
            return this.status;
        }
    }
    
    public static RestOperationException badRequest(String msg) { 
        return new RestOperationException(msg, Status.BAD_REQUEST);
    }
    
    public static RestOperationException notAcceptable(String msg) { 
        return new RestOperationException(msg, Status.NOT_ACCEPTABLE);
    }
    
    public static RestOperationException preConditionFailed(String msg) { 
        return new RestOperationException(msg, Status.PRE_CONDITION_FAILED);
    }
    
    public static RestOperationException notFound(String msg) { 
        return new RestOperationException(msg, Status.NOT_FOUND);
    }
    
    public static RestOperationException internalServerError(String msg, Exception e) { 
        return new RestOperationException(msg, e, Status.INTERNAL_SERVER_ERROR);
    }
   
    public static RestOperationException unauthorized(String msg, Exception e) { 
        return new RestOperationException(msg, e, Status.UNAUTHORIZED);
    }
}

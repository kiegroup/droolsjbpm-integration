package org.kie.remote.services.ws.command;

import javax.xml.ws.WebServiceException;

public class KieRemoteWebServiceException extends WebServiceException {

    /** generated serial version UID */
    private static final long serialVersionUID = -5846490699830607523L;
    

    /** Constructs a new exception with the specified detail 
     *  message.  The cause is not initialized.
     *  @param message The detail message which is later 
     *                 retrieved using the getMessage method
    **/
    public KieRemoteWebServiceException(String message) {
      super(message);
    }

    /** Constructs a new exception with the specified detail 
     *  message and cause.
     *
     *  @param message The detail message which is later retrieved
     *                 using the getMessage method
     *  @param cause   The cause which is saved for the later
     *                 retrieval throw by the getCause method 
    **/ 
    public KieRemoteWebServiceException(String message, Throwable cause) {
      super(message,cause);
    }
}

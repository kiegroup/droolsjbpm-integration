package org.kie.server.client;

public class KieServicesClientException extends RuntimeException {

    public KieServicesClientException() {
    }

    public KieServicesClientException(String message) {
        super(message);
    }

    public KieServicesClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public KieServicesClientException(Throwable cause) {
        super(cause);
    }

}


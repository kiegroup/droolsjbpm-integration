package org.kie.server.client;

public class KieServicesException
        extends RuntimeException {

    public KieServicesException() {
    }

    public KieServicesException(String message) {
        super(message);
    }

    public KieServicesException(String message, Throwable cause) {
        super(message, cause);
    }

    public KieServicesException(Throwable cause) {
        super(cause);
    }

}


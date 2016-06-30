package org.kie.server.integrationtests.controller.client.exception;

public class UnexpectedResponseCodeException extends RuntimeException{

    private static final long serialVersionUID = 1L;

    private int responseCode;

    public UnexpectedResponseCodeException(int responseCode) {
        super();
        this.responseCode = responseCode;
    }

    public UnexpectedResponseCodeException(int responseCode, String message) {
        super(message);
        this.responseCode = responseCode;
    }

    public UnexpectedResponseCodeException(int responseCode, Throwable cause) {
        super(cause);
        this.responseCode = responseCode;
    }

    public UnexpectedResponseCodeException(int responseCode, String message, Throwable cause) {
        super(message, cause);
        this.responseCode = responseCode;
    }

    public int getResponseCode() {
        return responseCode;
    }
}

package org.kie.services.client.api.rest;

import java.io.IOException;

public class KieRemoteHttpRequestException extends RuntimeException {

    /** generated serial version UID */
    private static final long serialVersionUID = 8703710246151287879L;

    /**
     * Create a new {@link KieRemoteHttpRequestException} with the given cause
     *
     * @param msg
     * @param cause
     */
    public KieRemoteHttpRequestException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    /**
     * Create a new {@link KieRemoteHttpRequestException} with the given cause
     *
     * @param msg
     * @param cause
     */
    public KieRemoteHttpRequestException(final String msg) {
        super(msg);
    }
    
    /**
     * Get {@link IOException} that triggered this request exception
     *
     * @return {@link IOException} cause
     */
    @Override
    public IOException getCause() {
        return (IOException) super.getCause();
    }
}

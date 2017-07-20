package org.kie.server.common.rest;

import java.util.List;
import java.util.ArrayList;

/**
 * Provides static lists of exceptions which map to a particular HTTP status codes. This is internal API that should be
 * used when REST requests made to Kie Server throw exceptions.
 */
public class HttpStatusCodeException {

    // Exceptions which return HTTP Status code 400
    public static final List<Class> BAD_REQUEST = new ArrayList<>();

    static {
        BAD_REQUEST.add( NumberFormatException.class );
        BAD_REQUEST.add( IllegalArgumentException.class );
    }

}

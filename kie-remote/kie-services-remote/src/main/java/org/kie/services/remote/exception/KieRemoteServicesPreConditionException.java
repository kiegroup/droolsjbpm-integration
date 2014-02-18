package org.kie.services.remote.exception;

import org.jboss.resteasy.spi.BadRequestException;

/**
 * This exception should be thrown when a REST operation can not be executed
 * because of a failed precondition.
 * </p>
 * For example:<ul>
 * <li>The process instance that the REST operation references does not exist anymore</li>
 * <li>The variable instance that the REST operation references does not exist anymore</li>
 * </ul>
 */
public class KieRemoteServicesPreConditionException extends BadRequestException {

    /** generated serial version UID */
    private static final long serialVersionUID = 51449186920300698L;

    public KieRemoteServicesPreConditionException(String msg) { 
        super(msg);
    }
}

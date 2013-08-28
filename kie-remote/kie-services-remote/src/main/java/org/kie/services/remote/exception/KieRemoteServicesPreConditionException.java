package org.kie.services.remote.exception;

import org.jboss.resteasy.spi.BadRequestException;

public class KieRemoteServicesPreConditionException extends BadRequestException {

    /** generated serial version UID */
    private static final long serialVersionUID = 51449186920300698L;

    public KieRemoteServicesPreConditionException(String msg) { 
        super(msg);
    }
}

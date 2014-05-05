package org.kie.remote.services.ws.sei.command;

import org.kie.remote.services.ws.common.KieRemoteWebServiceException;
import org.kie.remote.services.ws.common.WebServiceFaultInfo;

public class CommandWebServiceException extends KieRemoteWebServiceException {

    /** default serial version UID */
    private static final long serialVersionUID = 2301L;

    public CommandWebServiceException(String message, WebServiceFaultInfo faultInfo) {
        super(message, faultInfo);
    }

}

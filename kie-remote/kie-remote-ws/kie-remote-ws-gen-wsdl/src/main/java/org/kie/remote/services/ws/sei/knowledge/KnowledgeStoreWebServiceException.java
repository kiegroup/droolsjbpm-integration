package org.kie.remote.services.ws.sei.knowledge;

import org.kie.remote.services.ws.common.KieRemoteWebServiceException;
import org.kie.remote.services.ws.common.WebServiceFaultInfo;

public class KnowledgeStoreWebServiceException extends KieRemoteWebServiceException {

    /** default serial version UID */
    private static final long serialVersionUID = 2301L;

    public KnowledgeStoreWebServiceException(String message, WebServiceFaultInfo faultInfo) {
        super(message, faultInfo);
    }

}

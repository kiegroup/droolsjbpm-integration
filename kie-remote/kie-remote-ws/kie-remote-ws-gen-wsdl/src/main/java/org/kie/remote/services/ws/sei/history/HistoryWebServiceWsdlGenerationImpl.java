package org.kie.remote.services.ws.sei.history;

import javax.jws.WebService;

import org.kie.remote.services.ws.common.KieRemoteWebServiceException;

/**
 * Only used for WSDL generation
 */
@WebService(
        serviceName = "HistoryService", 
        portName = "HistoryServicePort", 
        name = "HistoryService", 
        targetNamespace = HistoryWebService.NAMESPACE)
public class HistoryWebServiceWsdlGenerationImpl implements HistoryWebService {

    @Override
    public ProcessInstanceLogResponse findProcessInstanceLogs(HistoryInstanceLogRequest historyInstLogRequest)
            throws KieRemoteWebServiceException {
        return null;
    }

    @Override
    public NodeInstanceLogResponse findNodeInstanceLogs(HistoryInstanceLogRequest historyInstLogRequest)
            throws KieRemoteWebServiceException {
        return null;
    }

    @Override
    public VariableInstanceLogResponse findVariableInstanceLogs(HistoryInstanceLogRequest historyInstLogRequest)
            throws KieRemoteWebServiceException {
        return null;
    }

}
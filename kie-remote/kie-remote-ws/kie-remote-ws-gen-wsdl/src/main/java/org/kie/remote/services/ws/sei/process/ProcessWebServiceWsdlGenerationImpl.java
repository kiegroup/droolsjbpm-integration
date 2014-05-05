package org.kie.remote.services.ws.sei.process;

import javax.jws.WebService;

import org.kie.remote.services.ws.common.KieRemoteWebServiceException;

/**
 * Only used for WSDL generation
 */
@WebService(
        serviceName = "ProcessService", 
        portName = "ProcessServicePort", 
        name = "ProcessService", 
        targetNamespace = ProcessWebService.NAMESPACE)
public class ProcessWebServiceWsdlGenerationImpl implements ProcessWebService {

    @Override
    public ProcessInstanceResponse startProcess(ProcessDefIdAndParametersRequest procDefIdAndParams) throws KieRemoteWebServiceException {
        return null;
    }

    @Override
    public void abortProcess(ProcessInstanceIdAndSignalRequest procInstIdRequest) throws KieRemoteWebServiceException { }

    @Override
    public void signalProcess(ProcessInstanceIdAndSignalRequest procInstIdAndSignalRequest) throws KieRemoteWebServiceException { }

    @Override
    public ProcessInstanceResponse getProcessInstanceInfo(ProcessInstanceInfoRequest getProcessInstanceInfo) throws KieRemoteWebServiceException {
        return null;
    }

    @Override
    public void completeWorkItem(WorkItemIdRequest workItemId) throws KieRemoteWebServiceException { }

    @Override
    public void abortWorkItem(WorkItemIdRequest workItemId) throws KieRemoteWebServiceException { }

}

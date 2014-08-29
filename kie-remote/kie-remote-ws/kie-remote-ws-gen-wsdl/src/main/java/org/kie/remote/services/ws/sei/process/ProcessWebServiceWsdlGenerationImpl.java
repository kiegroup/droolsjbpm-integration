package org.kie.remote.services.ws.sei.process;

import javax.jws.WebService;

import org.kie.remote.services.ws.common.KieRemoteWebServiceException;

/**
 * Only used for initial WSDL generation
 */
@WebService(
        serviceName = "ProcessService", 
        portName = "ProcessServicePort", 
        name = "ProcessService", 
        targetNamespace = ProcessWebService.NAMESPACE)
public class ProcessWebServiceWsdlGenerationImpl implements ProcessWebService {

    @Override
    public ProcessInstanceResponse manageProcess( ManageProcessInstanceRequest procDefIdAndParams )
            throws KieRemoteWebServiceException {
        return null;
    }

    @Override
    public void manageWorkItem( ManageWorkItemRequest workItemRequest ) throws KieRemoteWebServiceException {
    }

}
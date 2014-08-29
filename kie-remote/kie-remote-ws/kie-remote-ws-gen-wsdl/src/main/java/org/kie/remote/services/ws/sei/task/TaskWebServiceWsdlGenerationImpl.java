package org.kie.remote.services.ws.sei.task;

import javax.jws.WebService;

import org.kie.remote.client.jaxb.JaxbTaskSummaryListResponse;

/**
 * Only used for WSDL generation
 */
@WebService(
        serviceName = "TaskService", 
        portName = "TaskServicePort", 
        name = "TaskService", 
        targetNamespace = TaskWebService.NAMESPACE)
public class TaskWebServiceWsdlGenerationImpl implements TaskWebService {

    @Override
    public void taskOperation( TaskOperationRequest arg0 ) throws TaskWebServiceException {
    }

    @Override
    public JaxbTaskSummaryListResponse query( TaskQueryRequest arg0 ) throws TaskWebServiceException {
        return null;
    }


}
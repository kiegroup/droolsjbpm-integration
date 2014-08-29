
package org.kie.remote.services.ws.sei.task;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

import org.kie.remote.client.jaxb.JaxbTaskSummaryListResponse;
import org.kie.remote.services.ws.sei.ServicesVersion;

/**
 * Only used for initial WSDL generation
 */
@WebService(name = "ProcessService", targetNamespace = TaskWebService.NAMESPACE)
public interface TaskWebService {

    final static String NAMESPACE = "http://services.remote.kie.org/" + ServicesVersion.VERSION + "/task";
    
    @WebMethod(action = "urn:TaskOperation")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "taskOperation", targetNamespace = NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperTaskOperation")
    @ResponseWrapper(localName = "taskOperationResponse", targetNamespace = "http://services.remote.kie.org/process", className = "org.kie.remote.services.ws.wsdl.generated.WrapperTaskOperationResponse")
    public void taskOperation(@WebParam(name = "arg0", targetNamespace = "") TaskOperationRequest arg0) throws TaskWebServiceException;

    @WebMethod(action = "urn:Query")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "queryTasks", targetNamespace = NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperTaskQueryRequest")
    @ResponseWrapper(localName = "queryTasksResponse", targetNamespace = NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperTaskQueryResponse")
    public JaxbTaskSummaryListResponse query(@WebParam(name = "arg0", targetNamespace = "") TaskQueryRequest arg0) throws TaskWebServiceException;

}

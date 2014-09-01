
package org.kie.remote.services.ws.sei.process;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

import org.kie.remote.services.ws.common.KieRemoteWebServiceException;
import org.kie.remote.services.ws.sei.ServicesVersion;

/**
 * Only used for initial WSDL generation
 */
@WebService(name = "ProcessService", targetNamespace = ProcessWebService.NAMESPACE)
public interface ProcessWebService {

    static final String NAMESPACE = "http://services.remote.kie.org/" + ServicesVersion.VERSION + "/process";
    
    @WebMethod(action = "urn:ManageProcess")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "manageProcess", targetNamespace = ProcessWebService.NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperManageProcessInstanceRequest")
    @ResponseWrapper(localName = "manageProcessResponse", targetNamespace = ProcessWebService.NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperProcessInstanceResponse")
    public ProcessInstanceResponse manageProcess(@WebParam(name = "request", targetNamespace = "") ManageProcessInstanceRequest processInstanceRequest) throws KieRemoteWebServiceException;

    @WebMethod(action = "urn:ManageWorkItem")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "manageWorkItem", targetNamespace = ProcessWebService.NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperManageWorkItem")
    @ResponseWrapper(localName = "manageWorkItemResponse", targetNamespace = ProcessWebService.NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperVoidResponse")
    public void manageWorkItem(@WebParam(name = "request", targetNamespace = "") ManageWorkItemRequest workItemRequest) throws KieRemoteWebServiceException;

    @WebMethod(action = "urn:QueryProcess")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "queryProcess", targetNamespace = ProcessWebService.NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperQueryProcessRequest")
    @ResponseWrapper(localName = "queryProcessResponse", targetNamespace = ProcessWebService.NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperProcessInstanceResponse")
    public Jaxb query(@WebParam(name = "request", targetNamespace = "") QueryProcessRequest processQueryRequest) throws KieRemoteWebServiceException;

}

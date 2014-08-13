
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
 * This service is responsible for managing process instances, including associated aspects like work item management. 
 * </p> 
 * All service interactions will require a deployment id. 
 */
@WebService(name = "ProcessService", targetNamespace = ProcessWebService.NAMESPACE)
public interface ProcessWebService {

    static final String NAMESPACE = "http://services.remote.kie.org/" + ServicesVersion.VERSION + "/process";
    
    @WebMethod(action = "urn:StartProcess")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "startProcess", targetNamespace = ProcessWebService.NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperProcessDefIdAndParameters")
    @ResponseWrapper(localName = "startProcessResponse", targetNamespace = ProcessWebService.NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperProcessInstanceInfo")
    public ProcessInstanceResponse startProcess(@WebParam(name = "procDefIdAndParams", targetNamespace = "") ProcessDefIdAndParametersRequest procDefIdAndParams) throws KieRemoteWebServiceException;

    @WebMethod(action = "urn:AbortProcess")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "abortProcess", targetNamespace = ProcessWebService.NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperProcessInstanceId")
    @ResponseWrapper(localName = "abortProcessResponse", targetNamespace = ProcessWebService.NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperVoidResponse")
    public void abortProcess(@WebParam(name = "procInstId", targetNamespace = "") ProcessInstanceIdAndSignalRequest procInstIdRequest) throws KieRemoteWebServiceException;

    @WebMethod(action = "urn:SignalProcess")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "signalProcess", targetNamespace = ProcessWebService.NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperProcessInstanceIdAndSignal")
    @ResponseWrapper(localName = "signalProcessResponse", targetNamespace = ProcessWebService.NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperVoidResponse")
    public void signalProcess(@WebParam(name = "procInstIdAndSignal", targetNamespace = "") ProcessInstanceIdAndSignalRequest procInstIdAndSignal) throws KieRemoteWebServiceException;

    @WebMethod(action = "urn:GetProcessInstanceInfo")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getProcessInstanceInfo", targetNamespace = ProcessWebService.NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperProcessInstanceInfoRequest")
    @ResponseWrapper(localName = "getProcessInstanceInfoResponse", targetNamespace = ProcessWebService.NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperProcessInstanceInfo")
    public ProcessInstanceResponse getProcessInstanceInfo(@WebParam(name = "getProcessInstanceInfo", targetNamespace = "") ProcessInstanceInfoRequest getProcessInstanceInfo) throws KieRemoteWebServiceException;

    @WebMethod(action = "urn:CompleteWorkItem")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "completeWorkItem", targetNamespace = ProcessWebService.NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperWorkItemId")
    @ResponseWrapper(localName = "completeWorkItemResponse", targetNamespace = ProcessWebService.NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperVoidResponse")
    public void completeWorkItem(@WebParam(name = "workItemId", targetNamespace = "") WorkItemIdRequest workItemId) throws KieRemoteWebServiceException;

    @WebMethod(action = "urn:AbortWorkItem")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "abortWorkItem", targetNamespace = ProcessWebService.NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperWorkItemId")
    @ResponseWrapper(localName = "abortWorkItemResponse", targetNamespace = ProcessWebService.NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperVoidResponse")
    public void abortWorkItem(@WebParam(name = "workItemId", targetNamespace = "") WorkItemIdRequest workItemId) throws KieRemoteWebServiceException;

}

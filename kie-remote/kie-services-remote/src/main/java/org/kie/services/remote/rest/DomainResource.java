package org.kie.services.remote.rest;

import static org.kie.services.client.message.ServiceMessageMapper.*;
import static org.kie.services.client.message.ServiceMessage.*;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.kie.services.client.message.OperationMessage;
import org.kie.services.client.message.ServiceMessage;
import org.kie.services.client.message.ServiceMessageMapper;
import org.kie.services.client.message.serialization.impl.JaxbSerializationProvider;
import org.kie.services.client.message.serialization.impl.jaxb.JaxbServiceMessage;
import org.kie.services.remote.UnfinishedError;
import org.kie.services.remote.ejb.ProcessRequestBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO:
 * current uRL structure
 * 
 * |{domain}|session|{oper} or |{domain}|task|{id}|oper
 * 
 * but a better idea is probablyt the following, given that ServiceMessage already includes the domain info
 * 
 * |domain|{domain}|session|{oper} etc.
 * |all|session|{oper}
 * 
 * TODO:
 * 
 * Add execption mappers for thrown exceptions
 */

@Path("/{domain : [a-zA-z0-9-]+}")
@RequestScoped
public class DomainResource {

    private Logger logger = LoggerFactory.getLogger(DomainResource.class);

    @PathParam("domain")
    private String domainName;

    @EJB
    protected ProcessRequestBean processRequestBean;

    // Helper data --------------------------------------------------------------------------------------------------------------
    
    private static Map<Method, Map<String, Integer>> [] serviceMethodArgsMap = new Map[3]; 
    static { 
       serviceMethodArgsMap[KIE_SESSION_REQUEST] = RestQueryParamDataMapper.mapKieSessionQueryParameters();
       serviceMethodArgsMap[WORK_ITEM_MANAGER_REQUEST] = RestQueryParamDataMapper.mapWorkItemManagerQueryParameters();
       serviceMethodArgsMap[TASK_SERVICE_REQUEST] = RestQueryParamDataMapper.mapTaskServiceQueryParameters();
    }

    // Helper methods ------------------------------------------------------------------------------------------------------------
    
    private JaxbServiceMessage handleOperationRequestWithParams(UriInfo uriInfo, int serviceType, String operName, Long objectId) { 
        ServiceMessage serviceRequest = convertQueryParamsToServiceMsg(domainName, operName, uriInfo.getQueryParameters(), serviceType, objectId, serviceMethodArgsMap[serviceType]);
        // TODO: limit client api when sending REST request -- or otherwise expand this to do batch operations
        OperationMessage responseOper = this.processRequestBean.doOperation(serviceRequest, serviceRequest.getOperations().get(0));

        ServiceMessage responseMsg = new ServiceMessage(serviceRequest.getDomainName());
        responseMsg.addOperation(responseOper);
        return new JaxbServiceMessage(responseMsg);
    }
    
    private JaxbServiceMessage handleOperationRequestWithXml(JaxbServiceMessage xmlRequest, String operName, Long objectId) 
            throws Exception { 
        ServiceMessage serviceRequest = JaxbSerializationProvider.convertJaxbRequesMessageToServiceMessage(xmlRequest);
        // TODO: objectId: trust the URL or the xml?  (==> batch xml or not? )
        // TODO: limit client api when sending REST request -- or otherwise expand this to do batch operations
        OperationMessage responseOper = this.processRequestBean.doOperation(serviceRequest, serviceRequest.getOperations().get(0));

        ServiceMessage responseMsg = new ServiceMessage(serviceRequest.getDomainName());
        responseMsg.addOperation(responseOper);
        return new JaxbServiceMessage(responseMsg);
    }


    // KieSession methods -------------------------------------------------------------------------------------------------------

    @POST
    @Path("session/{oper: [a-zA-Z]+}")
    @Produces(MediaType.APPLICATION_XML)
    public JaxbServiceMessage kieSessionOperationWithParams(@Context UriInfo uriInfo, @PathParam("oper") String operName) {
        return handleOperationRequestWithParams(uriInfo, ServiceMessage.KIE_SESSION_REQUEST, operName, null);
    }

    @POST
    @Path("session/{oper: [a-zA-Z]+}")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public JaxbServiceMessage kieSessionOperationWithXml(@PathParam("oper") String operName, JaxbServiceMessage xmlRequest)
            throws Exception {
        return handleOperationRequestWithXml(xmlRequest, operName, null);
    }

    @GET
    @Path("session/procInst/{id: [0-9]+}")
    @Produces(MediaType.APPLICATION_XML)
    public void processInstanceStatus(@PathParam("id") Long procesInstanceId) {
        // TODO
        if (false) {
            String operName = null;
            // ...
        }
        throw new UnfinishedError("Process instance status retrievel has not yet been implemented.");
    }

    @POST
    @Path("session/procInst/{procInstId: [0-9]+}/{oper: [a-zA-Z]*}")
    @Produces(MediaType.APPLICATION_XML)
    public JaxbServiceMessage processInstanceOperationWithParams(@Context UriInfo uriInfo,
            @PathParam("procInstId") Long processInstanceId, @PathParam("oper") String operName) {
        // TODO: objectId not meant for processInstanceId?
        return handleOperationRequestWithParams(uriInfo, ServiceMessage.KIE_SESSION_REQUEST, operName, processInstanceId);
    }

    @POST
    @Path("session/procInst/{procInstId: [0-9]+}/{oper: [a-zA-Z]*}")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public JaxbServiceMessage processInstanceOperationWithXml(@PathParam("procInstId") Long processInstanceId,
            @PathParam("oper") String operName, JaxbServiceMessage xmlRequest) throws Exception {
        // TODO: objectId not meant for processInstanceId?
        return handleOperationRequestWithXml(xmlRequest, operName, processInstanceId);
    }

    // TaskService methods ------------------------------------------------------------------------------------------------------

    @POST
    @Path("task/{id : \\d+}/{oper : [a-zA-Z]+}")
    @Consumes(MediaType.APPLICATION_XML)
    public JaxbServiceMessage taskOperationWithParams(@Context UriInfo uriInfo, @PathParam("id") Long taskId,
            @PathParam("oper") String operName) {
        return handleOperationRequestWithParams(uriInfo, ServiceMessage.TASK_SERVICE_REQUEST, operName, taskId);
    }

    @POST
    @Path("task/{id : \\d+}/{oper : [a-zA-Z]+}")
    @Consumes(MediaType.APPLICATION_XML)
    public JaxbServiceMessage taskOperationWithXml(@PathParam("id") Long taskId, @PathParam("oper") String operName, 
            JaxbServiceMessage xmlRequest) throws Exception {
        return handleOperationRequestWithXml(xmlRequest, operName, taskId);
    }

}

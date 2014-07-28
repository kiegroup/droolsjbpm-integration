package org.kie.remote.services.rest;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

import org.drools.core.command.runtime.process.*;
import org.drools.core.process.instance.WorkItem;
import org.drools.core.util.StringUtils;
import org.jbpm.kie.services.api.RuntimeDataService;
import org.jbpm.kie.services.api.bpmn2.BPMN2DataService;
import org.jbpm.kie.services.impl.model.ProcessAssetDesc;
import org.jbpm.process.audit.VariableInstanceLog;
import org.jbpm.process.audit.command.FindVariableInstancesCommand;
import org.kie.api.command.Command;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandsRequest;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandsResponse;
import org.kie.services.client.serialization.jaxb.impl.process.*;
import org.kie.services.client.serialization.jaxb.rest.JaxbGenericResponse;
import org.kie.remote.services.util.FormURLGenerator;
import org.kie.workbench.common.services.rest.RestOperationException;

/**
 * If a method in this class is annotated by a @Path annotation, 
 * then the name of the method should match the URL specified in the @Path, 
 * where "_" characters should be used for all "/" characters in the path. 
 * <p>
 * For example: 
 * <pre>
 * @Path("/begin/{varOne: [_a-zA-Z0-9-:\\.]+}/midddle/{varTwo: [a-z]+}")
 * public void begin_varOne_middle_varTwo() { 
 * </pre>
 * 
 * If the method is annotated by the @Path anno, but is the "root", then
 * give it a name that explains it's function.
 */
@Path("/runtime/{deploymentId: [\\w\\.-]+(:[\\w\\.-]+){2,2}(:[\\w\\.-]*){0,2}}")
@RequestScoped
@SuppressWarnings("unchecked")
public class RuntimeResource extends ResourceBase {

    /* REST information */
    @Context
    protected HttpHeaders headers;
    
    @Context
    protected UriInfo uriInfo;
    
    @Context
    private Request restRequest;
   
    @Inject
    private RuntimeDataService runtimeDataService;
   
    @Inject
    private BPMN2DataService bpmn2DataService;

    @Inject
    private FormURLGenerator formURLGenerator;

    /* KIE information and processing */
    
    @PathParam("deploymentId")
    protected String deploymentId;

    // Rest methods --------------------------------------------------------------------------------------------------------------

    /**
     * The "/execute" method, primarily for the classes in the kie-services-client jar. 
     * </p>
     * A pain to support.. 
     * 
     * @param cmdsRequest The {@link JaxbCommandsRequest} containing the {@link Command} and other necessary info.
     * @return A {@link JaxbCommandsResponse} with the result from the {@link Command}
     */
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("/execute")
    public JaxbCommandsResponse execute(JaxbCommandsRequest cmdsRequest) {
        return restProcessJaxbCommandsRequest(cmdsRequest);
    } 
 
    @GET
    @Path("/process/{processDefId: [_a-zA-Z0-9-:\\.]+}/")
    public Response process_defId(@PathParam("processDefId") String processId) {
        ProcessAssetDesc processAssetDescList = runtimeDataService.getProcessesByDeploymentIdProcessId(deploymentId, processId); 
        JaxbProcessDefinition jaxbProcDef = convertProcAssetDescToJaxbProcDef(processAssetDescList);
        Map<String, String> variables = bpmn2DataService.getProcessData(processId);
        jaxbProcDef.setVariables(variables);
        return createCorrectVariant(jaxbProcDef, headers);
    }
    
    @POST
    @Path("/process/{processDefId: [_a-zA-Z0-9-:\\.]+}/start")
    public Response process_defId_start(@PathParam("processDefId") String processId) {
        Map<String, List<String>> requestParams = getRequestParams(uriInfo);
        String oper = getRelativePath(uriInfo);
        Map<String, Object> params = extractMapFromParams(requestParams, oper);

        ProcessInstance result = startProcessInstance(processId, params);

        JaxbProcessInstanceResponse responseObj = new JaxbProcessInstanceResponse(result, uriInfo.getRequestUri().toString());
        return createCorrectVariant(responseObj, headers);
    }

    @POST
    @Path("/process/{processDefId: [_a-zA-Z0-9-:\\.]+}/startform")
    public Response process_defId_startform(@PathParam("processDefId") String processId) {
        List<String> result = (List<String>) processRequestBean.doKieSessionOperation(new GetProcessIdsCommand(), deploymentId, null);

        if (result != null && result.contains(processId)) {
            Map<String, List<String>> requestParams = getRequestParams(uriInfo);

            String opener = "";

            List<String> openers = headers.getRequestHeader("host");
            if (openers.size() == 1) {
                opener = openers.get(0);
            }

            String formUrl = formURLGenerator.generateFormProcessURL(uriInfo.getBaseUri().toString(), processId, deploymentId, opener, requestParams);
            if (!StringUtils.isEmpty(formUrl)) {
                JaxbProcessInstanceFormResponse response = new JaxbProcessInstanceFormResponse(formUrl, uriInfo.getRequestUri().toString());
                return createCorrectVariant(response, headers);
            }
        }
        throw RestOperationException.notFound("Process " + processId + " is not available.");
    }

    @GET
    @Path("/process/instance/{procInstId: [0-9]+}")
    public Response process_instance_procInstId(@PathParam("procInstId") Long procInstId) {
        ProcessInstance result = getProcessInstance(procInstId);
        return createCorrectVariant(new JaxbProcessInstanceResponse(result), headers);
    }

    @POST
    @Path("/process/instance/{procInstId: [0-9]+}/abort")
    public Response process_instance_procInstId_abort(@PathParam("procInstId") Long procInstId) {
        Command<?> cmd = new AbortProcessInstanceCommand();
        ((AbortProcessInstanceCommand) cmd).setProcessInstanceId(procInstId);
       
        try { 
            processRequestBean.doKieSessionOperation(
                cmd, 
                deploymentId, 
                procInstId);
        } catch( IllegalArgumentException iae ) { 
            if( iae.getMessage().startsWith("Could not find process instance") ) {
                throw RestOperationException.notFound("Process instance " + procInstId + " is not available.");
            }
            throw iae;
        }
                
        return createCorrectVariant(new JaxbGenericResponse(uriInfo.getRequestUri().toString()), headers);
    }

    @POST
    @Path("/process/instance/{procInstId: [0-9]+}/signal")
    public Response process_instance_procInstId_signal(@PathParam("procInstId") Long procInstId) {
        String oper = getRelativePath(uriInfo);
        Map<String, List<String>> params = getRequestParams(uriInfo);
        String eventType = getStringParam("signal", true, params, oper);
        Object event = getObjectParam("event", false, params, oper);
        Command<?> cmd = new SignalEventCommand(procInstId, eventType, event);
        
        processRequestBean.doKieSessionOperation(cmd, deploymentId, procInstId);
        
        return createCorrectVariant(new JaxbGenericResponse(uriInfo.getRequestUri().toString()), headers);

    }

    @GET
    @Path("/process/instance/{procInstId: [0-9]+}/variable/{varName: [\\w\\.-]+}")
    public Response process_instance_procInstId_variable_varName(@PathParam("procInstId") Long procInstId,
            @PathParam("varName") String varName) {
        Object procVar =  processRequestBean.getVariableObjectInstanceFromRuntime(deploymentId, procInstId, varName);
     
        // serialize
        QName rootElementName = getRootElementName(procVar); 
        @SuppressWarnings("rawtypes") // unknown at compile time, dynamic from deployment
        JAXBElement<?> jaxbElem = new JAXBElement(rootElementName, procVar.getClass(), procVar) ;
        
        // return
        return createCorrectVariant(jaxbElem, headers);
    }
  
    @POST
    @Path("/signal")
    public Response signal() {
        String oper = getRelativePath(uriInfo);
        Map<String, List<String>> requestParams = getRequestParams(uriInfo);
        String eventType = getStringParam("signal", true, requestParams, oper);
        Object event = getObjectParam("event", false, requestParams, oper);

        processRequestBean.doKieSessionOperation(
                new SignalEventCommand(eventType, event),
                deploymentId, 
                (Long) getNumberParam(PROC_INST_ID_PARAM_NAME, false, requestParams, oper, true));
        
        return createCorrectVariant(new JaxbGenericResponse(uriInfo.getRequestUri().toString()), headers);
    }

    @GET
    @Path("/workitem/{workItemId: [0-9-]+}")
    public Response workitem_workItemId(@PathParam("workItemId") Long workItemId) { 
        String oper = getRelativePath(uriInfo);
        WorkItem workItem = (WorkItem) processRequestBean.doKieSessionOperation(
                new GetWorkItemCommand(workItemId),
                deploymentId, 
                (Long) getNumberParam(PROC_INST_ID_PARAM_NAME, false, getRequestParams(uriInfo), oper, true));
               
        if( workItem == null ) { 
            throw RestOperationException.notFound("WorkItem " + workItemId + " does not exist.");
        }
        
        return createCorrectVariant(new JaxbWorkItem(workItem), headers);
    }
    
    @POST
    @Path("/workitem/{workItemId: [0-9-]+}/{oper: [a-zA-Z]+}")
    public Response worktiem_workItemId_oper(@PathParam("workItemId") Long workItemId, @PathParam("oper") String operation) {
        String oper = getRelativePath(uriInfo);
        Map<String, List<String>> params = getRequestParams(uriInfo);
        Command<?> cmd = null;
        if ("complete".equalsIgnoreCase((operation.trim()))) {
            Map<String, Object> results = extractMapFromParams(params, operation);
            cmd = new CompleteWorkItemCommand(workItemId, results);
        } else if ("abort".equalsIgnoreCase(operation.toLowerCase())) {
            cmd = new AbortWorkItemCommand(workItemId);
        } else {
            throw RestOperationException.badRequest("Unsupported operation: " + oper);
        }
      
        // Will NOT throw an exception if the work item does not exist!!
        processRequestBean.doKieSessionOperation(
                cmd, 
                deploymentId, 
                (Long) getNumberParam(PROC_INST_ID_PARAM_NAME, false, params, oper, true));
                
        return createCorrectVariant(new JaxbGenericResponse(uriInfo.getRequestUri().toString()), headers);
    }

    /**
     * WithVars methods
     */
    
    @POST
    @Path("/withvars/process/{processDefId: [_a-zA-Z0-9-:\\.]+}/start")
    public Response withvars_process_processDefId_start(@PathParam("processDefId") String processId) {
        Map<String, List<String>> requestParams = getRequestParams(uriInfo);
        String oper = getRelativePath(uriInfo);
        Map<String, Object> params = extractMapFromParams(requestParams, oper );

        ProcessInstance procInst = startProcessInstance(processId, params);
        
        Map<String, String> vars = getVariables(procInst.getId());
        JaxbProcessInstanceWithVariablesResponse resp = new JaxbProcessInstanceWithVariablesResponse(procInst, vars, uriInfo.getRequestUri().toString());
        
        return createCorrectVariant(resp, headers);
    }

    @GET
    @Path("/withvars/process/instance/{procInstId: [0-9]+}")
    public Response withvars_process_instance_procInstId(@PathParam("procInstId") Long procInstId) {
        
        ProcessInstance procInst = getProcessInstance(procInstId);
        Map<String, String> vars = getVariables(procInstId);
        JaxbProcessInstanceWithVariablesResponse responseObj = new JaxbProcessInstanceWithVariablesResponse(procInst, vars, uriInfo.getRequestUri().toString());
        
        return createCorrectVariant(responseObj, headers);
    }

    @POST
    @Path("/withvars/process/instance/{procInstId: [0-9]+}/signal")
    public Response withvars_process_instance_procInstid_signal(@PathParam("procInstId") Long procInstId) {
        String oper = getRelativePath(uriInfo);
        Map<String, List<String>> params = getRequestParams(uriInfo);
        String eventType = getStringParam("signal", true, params, oper);
        Object event = getObjectParam("event", false, params, oper);

        processRequestBean.doKieSessionOperation(
                new SignalEventCommand(procInstId, eventType, event),
                deploymentId, 
                procInstId);
        
        ProcessInstance processInstance = getProcessInstance(procInstId);
        Map<String, String> vars = getVariables(processInstance.getId());
        
        return createCorrectVariant(new JaxbProcessInstanceWithVariablesResponse(processInstance, vars), headers);
    }

    // Helper methods --------------------------------------------------------------------------------------------------------------
 
    private ProcessInstance getProcessInstance(long procInstId) { 
        Command<?> cmd = new GetProcessInstanceCommand(procInstId);
        ((GetProcessInstanceCommand) cmd).setReadOnly(true);
        Object procInstResult = processRequestBean.doKieSessionOperation(
                cmd,
                deploymentId, 
                procInstId);
        
        if (procInstResult != null) {
            return (ProcessInstance) procInstResult;
        } else {
            throw RestOperationException.notFound("Unable to retrieve process instance " + procInstId
                    + " which may have been completed. Please see the history operations.");
        }
        
    }
    
    private Map<String, String> getVariables(long processInstanceId) {
        Object result = processRequestBean.doKieSessionOperation(
                new FindVariableInstancesCommand(processInstanceId),
                deploymentId, 
                processInstanceId);
        List<VariableInstanceLog> varInstLogList = (List<VariableInstanceLog>) result;
        
        Map<String, String> vars = new HashMap<String, String>();
        if( varInstLogList.isEmpty() ) { 
            return vars;
        }
        
        Map<String, VariableInstanceLog> varLogMap = new HashMap<String, VariableInstanceLog>();
        for( VariableInstanceLog varLog: varInstLogList ) {
            String varId = varLog.getVariableId();
            VariableInstanceLog prevVarLog = varLogMap.put(varId, varLog);
            if( prevVarLog != null ) { 
                if( prevVarLog.getDate().after(varLog.getDate()) ) { 
                  varLogMap.put(varId, prevVarLog);
                } 
            }
        }
        
        for( Entry<String, VariableInstanceLog> varEntry : varLogMap.entrySet() ) { 
            vars.put(varEntry.getKey(), varEntry.getValue().getValue());
        }
            
        return vars;
    }
    
    private ProcessInstance startProcessInstance(String processId, Map<String, Object> params) { 
        Object result = null;
        try { 
            result = processRequestBean.doKieSessionOperation(
                new StartProcessCommand(processId, params),
                deploymentId, 
                null);
        } catch( IllegalArgumentException iae ) { 
            if( iae.getMessage().startsWith("Unknown process ID")) { 
                throw RestOperationException.notFound("Process '" + processId + "' is not known to this deployment.");
            }
            throw iae;
        }
        return (ProcessInstance) result;
    }

    protected QName getRootElementName(Object object) { 
        boolean xmlRootElemAnnoFound = false;
        Class<?> objClass = object.getClass();
        
        // This usually doesn't work in the kie-wb/bpms environment, see comment below
        XmlRootElement xmlRootElemAnno = objClass.getAnnotation(XmlRootElement.class);
        logger.debug("Getting XML root element annotation for " + object.getClass().getName());
        if( xmlRootElemAnno != null ) { 
            xmlRootElemAnnoFound = true;
            return new QName(xmlRootElemAnno.name());
        } else { 
            /**
             * There seem to be weird classpath issues going on here, probably related
             * to the fact that kjar's have their own classloader..
             * (The XmlRootElement class can't be found in the same classpath as the
             * class from the Kjar)
             */
            for( Annotation anno : objClass.getAnnotations() ) { 
                Class<?> annoClass = anno.annotationType();
                // we deliberately compare *names* and not classes because it's on a different classpath!
                if( XmlRootElement.class.getName().equals(annoClass.getName()) ) { 
                    xmlRootElemAnnoFound = true;
                    try {
                        Method nameMethod = annoClass.getMethod("name");
                        Object nameVal = nameMethod.invoke(anno);
                        if( nameVal instanceof String ) { 
                            return new QName((String) nameVal); 
                        }
                    } catch (Exception e) {
                        throw RestOperationException.internalServerError("Unable to retrieve XmlRootElement info via reflection", e);
                    } 
                }
            }
            if( ! xmlRootElemAnnoFound ) { 
                String errorMsg = "Unable to serialize " + object.getClass().getName() + " instance "
                        + "because it is missing a " + XmlRootElement.class.getName() + " annotation with a name value.";
                throw RestOperationException.internalServerError(errorMsg);
            }
            return null;
        }
    }

}

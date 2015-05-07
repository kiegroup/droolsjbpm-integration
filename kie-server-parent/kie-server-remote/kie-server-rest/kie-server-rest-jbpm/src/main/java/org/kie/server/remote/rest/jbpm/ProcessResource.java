package org.kie.server.remote.rest.jbpm;

import static org.kie.server.remote.rest.common.util.RestUtils.createCorrectVariant;
import static org.kie.server.remote.rest.common.util.RestUtils.createResponse;
import static org.kie.server.remote.rest.common.util.RestUtils.getVariant;
import static org.kie.server.remote.rest.jbpm.resources.Messages.*;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import org.jbpm.services.api.DefinitionService;
import org.jbpm.services.api.DeploymentNotFoundException;
import org.jbpm.services.api.ProcessInstanceNotFoundException;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.model.ProcessDefinition;
import org.kie.api.command.Command;
import org.kie.api.runtime.manager.Context;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItem;
import org.kie.server.api.model.type.JaxbLong;
import org.kie.server.remote.rest.common.exception.ExecutionServerRestOperationException;
import org.kie.server.remote.rest.common.util.QueryParameterUtil;

@Path("/server")
public class ProcessResource  {

    private ProcessService processService;
    private DefinitionService definitionService;
    private RuntimeDataService runtimeDataService;

    public ProcessResource(ProcessService processService, DefinitionService definitionService, RuntimeDataService runtimeDataService) {
        this.processService = processService;
        this.definitionService = definitionService;
        this.runtimeDataService = runtimeDataService;
    }

    protected static String getRelativePath(HttpServletRequest httpRequest) { 
        String url =  httpRequest.getRequestURI();
        url.replaceAll( ".*/rest", "");
        return url;
    }
    
    @PUT
    @Path("containers/{id}/process/{pId}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response startProcess(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("pId") String processId, @javax.ws.rs.core.Context HttpServletRequest request) {

        Variant v = getVariant(headers);
        // Check for presence of process id
        try { 
            ProcessDefinition procDef = definitionService.getProcessDefinition(containerId, processId);
            if( procDef == null ) { 
                throw ExecutionServerRestOperationException.notFound(MessageFormat.format(PROCESS_DEFINITION_NOT_FOUND, processId, containerId), v);
            }
        } catch( Exception e ) { 
                throw ExecutionServerRestOperationException.internalServerError(
                        MessageFormat.format(PROCESS_DEFINITION_FETCH_ERROR, processId, containerId, e.getMessage()), v);
        }

        Map<String, Object> params = QueryParameterUtil.extractMapFromParams(request.getParameterMap(), getRelativePath(request), v);
        Long processInstanceId = processService.startProcess(containerId, processId, params);

        // return response
        try {
            return createResponse(new JaxbLong(processInstanceId), v, Response.Status.CREATED);
        } catch (Exception e) {
            throw ExecutionServerRestOperationException.internalServerError(
                    MessageFormat.format(CREATE_RESPONSE_ERROR, e.getMessage()), v);
        }
    }


    public Long startProcess(String containerId, String processId, Map<String, Object> parameters) {
        return processService.startProcess(containerId, processId, parameters);
    }

    @DELETE
    @Path("containers/{id}/process/instance/{pInstanceId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response abortProcessInstance(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("pInstanceId") Long processInstanceId) {
        Variant v = getVariant(headers);
        try {
            processService.abortProcessInstance(processInstanceId);
            // return null to produce 204 NO_CONTENT response code
            return null;
        } catch (ProcessInstanceNotFoundException e) {
            throw ExecutionServerRestOperationException.notFound(
                    MessageFormat.format(PROCESS_INSTANCE_NOT_FOUND, processInstanceId), v);
        } catch (DeploymentNotFoundException e) {
            throw ExecutionServerRestOperationException.notFound(
                    MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v);
        } catch (Exception e) {
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }


    @DELETE
    @Path("containers/{id}/process/instance")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void abortProcessInstances(@javax.ws.rs.core.Context HttpHeaders headers, @javax.ws.rs.core.Context HttpServletRequest request) {

    }


    public void signalProcessInstance(Long aLong, String s, Object o) {

    }


    public void signalProcessInstances(List<Long> list, String s, Object o) {

    }


    public ProcessInstance getProcessInstance(Long aLong) {
        return null;
    }


    public void setProcessVariable(Long aLong, String s, Object o) {

    }


    public void setProcessVariables(Long aLong, Map<String, Object> map) {

    }


    public Object getProcessInstanceVariable(Long aLong, String s) {
        return null;
    }


    public Map<String, Object> getProcessInstanceVariables(Long aLong) {
        return null;
    }


    public Collection<String> getAvailableSignals(Long aLong) {
        return null;
    }


    public void completeWorkItem(Long aLong, Map<String, Object> map) {

    }


    public void abortWorkItem(Long aLong) {

    }


    public WorkItem getWorkItem(Long aLong) {
        return null;
    }


    public List<WorkItem> getWorkItemByProcessInstance(Long aLong) {
        return null;
    }


    public <T> T execute(String s, Command<T> command) {
        return null;
    }


    public <T> T execute(String s, Context<?> context, Command<T> command) {
        return null;
    }


}

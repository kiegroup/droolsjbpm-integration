package org.kie.server.remote.rest.jbpm;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
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
import org.kie.server.api.marshalling.ModelWrapper;
import org.kie.server.api.model.type.JaxbLong;
import org.kie.server.api.model.type.JaxbMap;
import org.kie.server.remote.rest.common.exception.ExecutionServerRestOperationException;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.marshal.MarshallerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.remote.rest.common.util.RestUtils.*;
import static org.kie.server.remote.rest.jbpm.resources.Messages.*;

@Path("/server")
public class ProcessResource  {

    public static final Logger logger = LoggerFactory.getLogger(ProcessResource.class);

    private ProcessService processService;
    private DefinitionService definitionService;
    private RuntimeDataService runtimeDataService;
    private MarshallerHelper marshallerHelper;

    public ProcessResource(ProcessService processService, DefinitionService definitionService, RuntimeDataService runtimeDataService, KieServerRegistry context) {
        this.processService = processService;
        this.definitionService = definitionService;
        this.runtimeDataService = runtimeDataService;
        this.marshallerHelper = new MarshallerHelper(context);
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
    public Response startProcess(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("pId") String processId, @DefaultValue("") String payload) {
        Variant v = getVariant(headers);
        String type = v.getMediaType().getSubtype();
        // Check for presence of process id
        try {
            ProcessDefinition procDef = definitionService.getProcessDefinition(containerId, processId);
            if( procDef == null ) {
                throw ExecutionServerRestOperationException.notFound(MessageFormat.format(PROCESS_DEFINITION_NOT_FOUND, processId, containerId), v);
            }
        } catch( Exception e ) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(
                        MessageFormat.format(PROCESS_DEFINITION_FETCH_ERROR, processId, containerId, e.getMessage()), v);
        }
        logger.debug("About to unmarshal parameters from payload: '{}'", payload);
        Map<String, Object> parameters = marshallerHelper.unmarshal(containerId, payload, type, JaxbMap.class, Map.class);

        logger.debug("Calling start process with id {} on container {} and parameters {}", processId, containerId, parameters);
        Long processInstanceId = processService.startProcess(containerId, processId, parameters);

        // return response
        try {
            String response = marshallerHelper.marshal(containerId, type, ModelWrapper.wrap(processInstanceId));
            logger.debug("Returning OK response with content '{}'", response);
            return createResponse(response, v, Response.Status.CREATED);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
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
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
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

    @GET
    @Path("containers/{id}/process/instance/{pInstanceId}/variable/{varName}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Object getProcessInstanceVariable(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId,
                                             @PathParam("pInstanceId") Long processInstanceId, @PathParam("varName") String varName) {
        Variant v = getVariant(headers);
        String type = v.getMediaType().getSubtype();
        try {

            Object variable = processService.getProcessInstanceVariable(processInstanceId, varName);

            if (variable == null) {
                throw ExecutionServerRestOperationException.notFound(
                        MessageFormat.format(VARIABLE_INSTANCE_NOT_FOUND, varName, processInstanceId), v);
            }

            logger.debug("About to marshal process variable with name '{}' {}", varName, variable);
            String response = marshallerHelper.marshal(containerId, type, ModelWrapper.wrap(variable));

            logger.debug("Returning OK response with content '{}'", response);
            return createResponse(response, v, Response.Status.OK);

        } catch (ProcessInstanceNotFoundException e) {
            throw ExecutionServerRestOperationException.notFound(
                    MessageFormat.format(PROCESS_INSTANCE_NOT_FOUND, processInstanceId), v);
        } catch (DeploymentNotFoundException e) {
            throw ExecutionServerRestOperationException.notFound(
                    MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }

    @GET
    @Path("containers/{id}/process/instance/{pInstanceId}/variables")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessInstanceVariables(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("pInstanceId") Long processInstanceId) {
        Variant v = getVariant(headers);
        String type = v.getMediaType().getSubtype();
        try {
            Map<String, Object> variables = processService.getProcessInstanceVariables(processInstanceId);

            logger.debug("About to marshal process variables {}", variables);
            String response = marshallerHelper.marshal(containerId, type, ModelWrapper.wrap(variables));

            logger.debug("Returning OK response with content '{}'", response);
            return createResponse(response, v, Response.Status.OK);

        } catch (ProcessInstanceNotFoundException e) {
            throw ExecutionServerRestOperationException.notFound(
                    MessageFormat.format(PROCESS_INSTANCE_NOT_FOUND, processInstanceId), v);
        } catch (DeploymentNotFoundException e) {
            throw ExecutionServerRestOperationException.notFound(
                    MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }

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

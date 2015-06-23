/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.remote.rest.jbpm;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.kie.api.runtime.process.WorkItem;
import org.kie.internal.KieInternalServices;
import org.kie.internal.process.CorrelationKey;
import org.kie.internal.process.CorrelationKeyFactory;
import org.kie.server.api.model.instance.WorkItemInstance;
import org.kie.server.api.model.instance.WorkItemInstanceList;
import org.kie.server.remote.rest.common.exception.ExecutionServerRestOperationException;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.marshal.MarshallerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.api.rest.RestURI.*;
import static org.kie.server.remote.rest.common.util.RestUtils.*;
import static org.kie.server.remote.rest.jbpm.resources.Messages.*;

@Path("/server")
public class ProcessResource  {

    public static final Logger logger = LoggerFactory.getLogger(ProcessResource.class);

    private ProcessService processService;
    private DefinitionService definitionService;
    private RuntimeDataService runtimeDataService;
    private MarshallerHelper marshallerHelper;

    private CorrelationKeyFactory correlationKeyFactory = KieInternalServices.Factory.get().newCorrelationKeyFactory();

    public ProcessResource(ProcessService processService, DefinitionService definitionService, RuntimeDataService runtimeDataService, KieServerRegistry context) {
        this.processService = processService;
        this.definitionService = definitionService;
        this.runtimeDataService = runtimeDataService;
        this.marshallerHelper = new MarshallerHelper(context);
    }

    protected static String getRelativePath(HttpServletRequest httpRequest) {
        String url =  httpRequest.getRequestURI();
        url = url.replaceAll( ".*/rest", "");
        return url;
    }

    @POST
    @Path(START_PROCESS_POST_URI)
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response startProcess(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("pId") String processId, @DefaultValue("") String payload) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
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
        Map<String, Object> parameters = marshallerHelper.unmarshal(containerId, payload, type, Map.class);

        logger.debug("Calling start process with id {} on container {} and parameters {}", processId, containerId, parameters);
        Long processInstanceId = processService.startProcess(containerId, processId, parameters);

        // return response
        try {
            String response = marshallerHelper.marshal(containerId, type, processInstanceId);
            logger.debug("Returning CREATED response with content '{}'", response);
            return createResponse(response, v, Response.Status.CREATED);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(
                    MessageFormat.format(CREATE_RESPONSE_ERROR, e.getMessage()), v);
        }
    }

    @POST
    @Path(START_PROCESS_WITH_CORRELATION_KEY_POST_URI)
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response startProcessWithCorrelation(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("pId") String processId,
            @PathParam("correlationKey") String correlationKey, @DefaultValue("") String payload) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
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
        Map<String, Object> parameters = marshallerHelper.unmarshal(containerId, payload, type, Map.class);

        String[] correlationProperties = correlationKey.split(":");

        CorrelationKey actualCorrelationKey = correlationKeyFactory.newCorrelationKey(Arrays.asList(correlationProperties));

        logger.debug("Calling start process with id {} on container {} and parameters {}", processId, containerId, parameters);
        Long processInstanceId = processService.startProcess(containerId, processId, actualCorrelationKey, parameters);

        // return response
        try {
            String response = marshallerHelper.marshal(containerId, type, processInstanceId);
            logger.debug("Returning CREATED response with content '{}'", response);
            return createResponse(response, v, Response.Status.CREATED);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(
                    MessageFormat.format(CREATE_RESPONSE_ERROR, e.getMessage()), v);
        }
    }


    @DELETE
    @Path(ABORT_PROCESS_INST_DEL_URI)
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
    @Path(ABORT_PROCESS_INSTANCES_DEL_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response abortProcessInstances(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId, @QueryParam("instanceId") List<Long> processInstanceIds) {
        Variant v = getVariant(headers);
        try {
            processService.abortProcessInstances(processInstanceIds);
            // return null to produce 204 NO_CONTENT response code
            return null;
        } catch (ProcessInstanceNotFoundException e) {
            throw ExecutionServerRestOperationException.notFound(
                    MessageFormat.format(PROCESS_INSTANCE_NOT_FOUND, processInstanceIds), v);
        } catch (DeploymentNotFoundException e) {
            throw ExecutionServerRestOperationException.notFound(
                    MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }

    @POST
    @Path(SIGNAL_PROCESS_INST_POST_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response signalProcessInstance(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("pInstanceId") Long processInstanceId, @PathParam("sName") String signalName, String eventPayload) {

        Variant v = getVariant(headers);
        String type = getContentType(headers);
        try {
            logger.debug("About to unmarshal event from payload: '{}'", eventPayload);
            Object event = marshallerHelper.unmarshal(containerId, eventPayload, type, Object.class);

            logger.debug("Calling signal '{}' process instance with id {} on container {} and event {}", signalName, processInstanceId, containerId, event);
            processService.signalProcessInstance(processInstanceId, signalName, event);

            return createResponse(null, v, Response.Status.OK);

        } catch (ProcessInstanceNotFoundException e) {
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(PROCESS_INSTANCE_NOT_FOUND, processInstanceId), v);
        } catch (DeploymentNotFoundException e) {
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }


    @POST
    @Path(SIGNAL_PROCESS_INSTANCES_PORT_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response signalProcessInstances(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId,
            @QueryParam("instanceId") List<Long> processInstanceIds, @PathParam("sName") String signalName, String eventPayload) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
        try {

            logger.debug("About to unmarshal event from payload: '{}'", eventPayload);
            Object event = marshallerHelper.unmarshal(containerId, eventPayload, type, Object.class);

            logger.debug("Calling signal '{}' process instances with id {} on container {} and event {}", signalName, processInstanceIds, containerId, event);
            processService.signalProcessInstances(processInstanceIds, signalName, event);

            return createResponse("", v, Response.Status.OK);

        } catch (ProcessInstanceNotFoundException e) {
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(PROCESS_INSTANCE_NOT_FOUND, processInstanceIds), v);
        } catch (DeploymentNotFoundException e) {
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }

    @GET
    @Path(PROCESS_INSTANCE_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessInstance(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("pInstanceId") Long processInstanceId, @QueryParam("withVars") boolean withVars) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
        try {

            ProcessInstanceDesc instanceDesc = runtimeDataService.getProcessInstanceById(processInstanceId);
            if (instanceDesc == null) {
                throw ExecutionServerRestOperationException.notFound(MessageFormat.format(PROCESS_INSTANCE_NOT_FOUND, processInstanceId), v);
            }

            org.kie.server.api.model.instance.ProcessInstance processInstance = org.kie.server.api.model.instance.ProcessInstance.builder()
                    .id(instanceDesc.getId())
                    .processId(instanceDesc.getProcessId())
                    .processName(instanceDesc.getProcessName())
                    .processVersion(instanceDesc.getProcessVersion())
                    .state(instanceDesc.getState())
                    .containerId(instanceDesc.getDeploymentId())
                    .date(instanceDesc.getDataTimeStamp())
                    .initiator(instanceDesc.getInitiator())
                    .processInstanceDescription(instanceDesc.getProcessInstanceDescription())
                    .parentInstanceId(instanceDesc.getParentId())
                    .build();

            if (Boolean.TRUE.equals(withVars)) {
                Map<String, Object> variables = processService.getProcessInstanceVariables(processInstanceId);
                processInstance.setVariables(variables);
            }

            logger.debug("About to marshal process instance with id '{}' {}", processInstanceId, processInstance);
            String response = marshallerHelper.marshal(containerId, type, processInstance);

            logger.debug("Returning OK response with content '{}'", response);
            return createResponse(response, v, Response.Status.OK);

        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }


    @PUT
    @Path(PROCESS_INSTANCE_VAR_PUT_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response setProcessVariable(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("pInstanceId") Long processInstanceId, @PathParam("varName") String varName, String variablePayload) {

        Variant v = getVariant(headers);
        String type = getContentType(headers);
        try {

            logger.debug("About to unmarshal variable from payload: '{}'", variablePayload);
            Object variable = marshallerHelper.unmarshal(containerId, variablePayload, type, Object.class);

            logger.debug("Setting variable '{}' on process instance with id {} with value {}", varName, processInstanceId, variable);
            processService.setProcessVariable(processInstanceId, varName, variable);

            return createResponse("", v, Response.Status.CREATED);

        } catch (ProcessInstanceNotFoundException e) {
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(PROCESS_INSTANCE_NOT_FOUND, processInstanceId), v);
        } catch (DeploymentNotFoundException e) {
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }

    @POST
    @Path(PROCESS_INSTANCE_VARS_POST_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response setProcessVariables(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("pInstanceId") Long processInstanceId, String variablePayload) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
        try {

            logger.debug("About to unmarshal variables from payload: '{}'", variablePayload);
            Map<String, Object> variables = marshallerHelper.unmarshal(containerId, variablePayload, type, Map.class);

            logger.debug("Setting variables '{}' on process instance with id {} with value {}", variables.keySet(), processInstanceId, variables.values());
            processService.setProcessVariables(processInstanceId, variables);

            return createResponse("", v, Response.Status.OK);

        } catch (ProcessInstanceNotFoundException e) {
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(PROCESS_INSTANCE_NOT_FOUND, processInstanceId), v);
        } catch (DeploymentNotFoundException e) {
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }

    @GET
    @Path(PROCESS_INSTANCE_VAR_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Object getProcessInstanceVariable(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId,
                                             @PathParam("pInstanceId") Long processInstanceId, @PathParam("varName") String varName) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
        try {

            Object variable = processService.getProcessInstanceVariable(processInstanceId, varName);

            if (variable == null) {
                throw ExecutionServerRestOperationException.notFound(
                        MessageFormat.format(VARIABLE_INSTANCE_NOT_FOUND, varName, processInstanceId), v);
            }

            logger.debug("About to marshal process variable with name '{}' {}", varName, variable);
            String response = marshallerHelper.marshal(containerId, type, variable);

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
    @Path(PROCESS_INSTANCE_VARS_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessInstanceVariables(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("pInstanceId") Long processInstanceId) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
        try {
            Map<String, Object> variables = processService.getProcessInstanceVariables(processInstanceId);

            logger.debug("About to marshal process variables {}", variables);
            String response = marshallerHelper.marshal(containerId, type, variables);

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
    @Path(PROCESS_INSTANCE_SIGNALS_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getAvailableSignals(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("pInstanceId") Long processInstanceId) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
        try {
            Collection<String> signals = processService.getAvailableSignals(processInstanceId);

            logger.debug("About to marshal available signals {}", signals);
            String response = marshallerHelper.marshal(containerId, type, signals);

            logger.debug("Returning OK response with content '{}'", response);
            return createResponse(response, v, Response.Status.OK);
        }  catch (ProcessInstanceNotFoundException e) {
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(PROCESS_INSTANCE_NOT_FOUND, processInstanceId), v);
        } catch (DeploymentNotFoundException e) {
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }

    }


    @PUT
    @Path(PROCESS_INSTANCE_WORK_ITEM_COMPLETE_PUT_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response completeWorkItem(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("pInstanceId") Long processInstanceId, @PathParam("workItemId") Long workItemId, String resultPayload) {

        Variant v = getVariant(headers);
        String type = getContentType(headers);
        try {
            logger.debug("About to unmarshal work item result from payload: '{}'", resultPayload);
            Map<String, Object> results = marshallerHelper.unmarshal(containerId, resultPayload, type, Map.class);

            logger.debug("Completing work item '{}' on process instance id {} with value {}", workItemId, processInstanceId, results);
            processService.completeWorkItem(workItemId, results);

            return createResponse("", v, Response.Status.CREATED);

        } catch (ProcessInstanceNotFoundException e) {
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(PROCESS_INSTANCE_NOT_FOUND, processInstanceId), v);
        } catch (DeploymentNotFoundException e) {
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }


    @PUT
    @Path(PROCESS_INSTANCE_WORK_ITEM_ABORT_PUT_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response abortWorkItem(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("pInstanceId") Long processInstanceId, @PathParam("workItemId") Long workItemId) {
        Variant v = getVariant(headers);

        try {
            logger.debug("Aborting work item '{}' on process instance id {}", workItemId, processInstanceId);
            processService.abortWorkItem(workItemId);

            return createResponse("", v, Response.Status.CREATED);

        } catch (ProcessInstanceNotFoundException e) {
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(PROCESS_INSTANCE_NOT_FOUND, processInstanceId), v);
        } catch (DeploymentNotFoundException e) {
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }


    @GET
    @Path(PROCESS_INSTANCE_WORK_ITEM_BY_ID_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getWorkItem(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("pInstanceId") Long processInstanceId, @PathParam("workItemId") Long workItemId) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
        try {
            WorkItem workItem = processService.getWorkItem(workItemId);

            if (workItem == null) {
                throw ExecutionServerRestOperationException.notFound(MessageFormat.format(WORK_ITEM_NOT_FOUND, workItemId), v);
            }

            WorkItemInstance workItemInstance = WorkItemInstance.builder()
                    .id(workItem.getId())
                    .nodeInstanceId(((org.drools.core.process.instance.WorkItem)workItem).getNodeInstanceId())
                    .processInstanceId(workItem.getProcessInstanceId())
                    .containerId(((org.drools.core.process.instance.WorkItem)workItem).getDeploymentId())
                    .name(workItem.getName())
                    .nodeId(((org.drools.core.process.instance.WorkItem)workItem).getNodeId())
                    .parameters(workItem.getParameters())
                    .state(workItem.getState())
                    .build();

            logger.debug("About to marshal work item {}", workItemInstance);
            String response = marshallerHelper.marshal(containerId, type, workItemInstance);

            logger.debug("Returning OK response with content '{}'", response);
            return createResponse(response, v, Response.Status.OK);
        }  catch (ProcessInstanceNotFoundException e) {
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(PROCESS_INSTANCE_NOT_FOUND, processInstanceId), v);
        } catch (DeploymentNotFoundException e) {
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }

    }


    @GET
    @Path(PROCESS_INSTANCE_WORK_ITEMS_BY_PROC_INST_ID_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getWorkItemByProcessInstance(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId,
            @PathParam("pInstanceId") Long processInstanceId) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
        try {
            List<WorkItem> workItems = processService.getWorkItemByProcessInstance(processInstanceId);

            WorkItemInstance[] instances = new WorkItemInstance[workItems.size()];
            int counter = 0;
            for (WorkItem workItem : workItems) {
                WorkItemInstance workItemInstance = WorkItemInstance.builder()
                        .id(workItem.getId())
                        .nodeInstanceId(((org.drools.core.process.instance.WorkItem) workItem).getNodeInstanceId())
                        .processInstanceId(workItem.getProcessInstanceId())
                        .containerId(((org.drools.core.process.instance.WorkItem) workItem).getDeploymentId())
                        .name(workItem.getName())
                        .nodeId(((org.drools.core.process.instance.WorkItem) workItem).getNodeId())
                        .parameters(workItem.getParameters())
                        .state(workItem.getState())
                        .build();
                instances[counter] = workItemInstance;
                counter++;
            }
            WorkItemInstanceList result = new WorkItemInstanceList(instances);
            logger.debug("About to marshal work items {}", result);
            String response = marshallerHelper.marshal(containerId, type, result);

            logger.debug("Returning OK response with content '{}'", response);
            return createResponse(response, v, Response.Status.OK);
        }  catch (ProcessInstanceNotFoundException e) {
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(PROCESS_INSTANCE_NOT_FOUND, processInstanceId), v);
        } catch (DeploymentNotFoundException e) {
            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }


}

package org.kie.server.remote.rest.jbpm;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import org.jbpm.services.api.DefinitionService;
import org.jbpm.services.api.model.ProcessDefinition;
import org.jbpm.services.api.model.UserTaskDefinition;
import org.kie.server.api.model.definition.AssociatedEntitiesDefinition;
import org.kie.server.api.model.definition.ServiceTasksDefinition;
import org.kie.server.api.model.definition.SubProcessesDefinition;
import org.kie.server.api.model.definition.TaskInputsDefinition;
import org.kie.server.api.model.definition.TaskOutputsDefinition;
import org.kie.server.api.model.definition.UserTaskDefinitionList;
import org.kie.server.api.model.definition.VariablesDefinition;
import org.kie.server.remote.rest.common.exception.ExecutionServerRestOperationException;

import static org.kie.server.remote.rest.common.util.RestUtils.*;
import static org.kie.server.remote.rest.jbpm.resources.Messages.*;
import static org.kie.server.api.rest.RestURI.*;

@Path("/server")
public class DefinitionServiceResource {

    private DefinitionService definitionService;

    public DefinitionServiceResource(DefinitionService definitionService) {
        this.definitionService = definitionService;
    }


    @GET
    @Path(PROCESS_DEF_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessDefinition(@Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("pId") String processId) {
        Variant v = getVariant(headers);
        try {
            ProcessDefinition procDef = findProcessDefinition(containerId, processId, v);

            Object responseObject = org.kie.server.api.model.definition.ProcessDefinition.builder()
                    .id(procDef.getId())
                    .name(procDef.getName())
                    .version(procDef.getVersion())
                    .packageName(procDef.getPackageName())
                    .containerId(procDef.getDeploymentId())
                    .entitiesAsCollection(procDef.getAssociatedEntities())
                    .serviceTasks(procDef.getServiceTasks())
                    .subprocesses(procDef.getReusableSubProcesses())
                    .variables(procDef.getProcessVariables())
                    .build();
            return createCorrectVariant(responseObject, headers, Response.Status.OK);
        } catch( Exception e ) {
            throw ExecutionServerRestOperationException.internalServerError(
                    MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }

    @GET
    @Path(PROCESS_DEF_SUBPROCESS_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getReusableSubProcesses(@Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("pId") String processId) {
        Variant v = getVariant(headers);
        try {
            findProcessDefinition(containerId, processId, v);

            Collection<String> reusableSubProcesses = definitionService.getReusableSubProcesses(containerId, processId);

            return createCorrectVariant(new SubProcessesDefinition(reusableSubProcesses), headers, Response.Status.OK);
        } catch( Exception e ) {
            throw ExecutionServerRestOperationException.internalServerError(
                    MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }

    @GET
    @Path(PROCESS_DEF_VARIABLES_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessVariables(@Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("pId") String processId) {
        Variant v = getVariant(headers);
        try {
            findProcessDefinition(containerId, processId, v);

            Map<String, String> processVariables = definitionService.getProcessVariables(containerId, processId);

            return createCorrectVariant(new VariablesDefinition(processVariables), headers, Response.Status.OK);
        } catch( Exception e ) {
            throw ExecutionServerRestOperationException.internalServerError(
                    MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }

    @GET
    @Path(PROCESS_DEF_SERVICE_TASKS_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getServiceTasks(@Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("pId") String processId) {
        Variant v = getVariant(headers);
        try {
            findProcessDefinition(containerId, processId, v);

            Map<String, String> serviceTasks = definitionService.getServiceTasks(containerId, processId);

            return createCorrectVariant(new ServiceTasksDefinition(serviceTasks), headers, Response.Status.OK);
        } catch( Exception e ) {
            throw ExecutionServerRestOperationException.internalServerError(
                    MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }

    @GET
    @Path(PROCESS_DEF_ASSOCIATED_ENTITIES_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getAssociatedEntities(@Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("pId") String processId) {
        Variant v = getVariant(headers);
        try {
            findProcessDefinition(containerId, processId, v);

            Map<String, Collection<String>> entities = definitionService.getAssociatedEntities(containerId, processId);

            return createCorrectVariant(AssociatedEntitiesDefinition.from(entities), headers, Response.Status.OK);
        } catch( Exception e ) {
            throw ExecutionServerRestOperationException.internalServerError(
                    MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }

    @GET
    @Path(PROCESS_DEF_USER_TASKS_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getTasksDefinitions(@Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("pId") String processId) {
        Variant v = getVariant(headers);
        try {
            findProcessDefinition(containerId, processId, v);

            Collection<UserTaskDefinition> userTaskDefinitions = definitionService.getTasksDefinitions(containerId, processId);

            return createCorrectVariant(convert(userTaskDefinitions), headers, Response.Status.OK);
        } catch( Exception e ) {
            throw ExecutionServerRestOperationException.internalServerError(
                    MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }

    @GET
    @Path(PROCESS_DEF_USER_TASK_INPUT_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getTaskInputMappings(@Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("pId") String processId, @PathParam("taskName") String taskName) {
        Variant v = getVariant(headers);
        try {
            findProcessDefinition(containerId, processId, v);

            Map<String, String> taskInputs = definitionService.getTaskInputMappings(containerId, processId, taskName);

            return createCorrectVariant(new TaskInputsDefinition(taskInputs), headers, Response.Status.OK);
        } catch( Exception e ) {
            throw ExecutionServerRestOperationException.internalServerError(
                    MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }

    @GET
    @Path(PROCESS_DEF_USER_TASK_OUTPUT_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getTaskOutputMappings(@Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("pId") String processId, @PathParam("taskName") String taskName) {
        Variant v = getVariant(headers);
        try {
            findProcessDefinition(containerId, processId, v);

            Map<String, String> taskOutputs = definitionService.getTaskOutputMappings(containerId, processId, taskName);

            return createCorrectVariant(new TaskOutputsDefinition(taskOutputs), headers, Response.Status.OK);
        } catch( Exception e ) {
            throw ExecutionServerRestOperationException.internalServerError(
                    MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }
    }

    protected ProcessDefinition findProcessDefinition(String containerId, String processId, Variant v) {
        try {

            ProcessDefinition procDef = definitionService.getProcessDefinition(containerId, processId);
            if (procDef == null) {
                throw ExecutionServerRestOperationException.notFound(
                        MessageFormat.format(PROCESS_DEFINITION_NOT_FOUND, processId, containerId), v);
            }

            return procDef;
        } catch (IllegalStateException e) {
            throw ExecutionServerRestOperationException.notFound(
                    MessageFormat.format(PROCESS_DEFINITION_NOT_FOUND, processId, containerId), v);
        }

    }

    protected UserTaskDefinitionList convert(Collection<UserTaskDefinition> taskDefinitions) {
        org.kie.server.api.model.definition.UserTaskDefinition[] userTaskDefinitions = new org.kie.server.api.model.definition.UserTaskDefinition[taskDefinitions.size()];

        int i = 0;
        for (UserTaskDefinition orig : taskDefinitions) {
            org.kie.server.api.model.definition.UserTaskDefinition definition = org.kie.server.api.model.definition.UserTaskDefinition.builder()
                    .name(orig.getName())
                    .comment(orig.getComment())
                    .createdBy(orig.getCreatedBy())
                    .priority(orig.getPriority())
                    .skippable(orig.isSkippable())
                    .entities(orig.getAssociatedEntities().toArray(new String[orig.getAssociatedEntities().size()]))
                    .taskInputs(orig.getTaskInputMappings())
                    .taskOutputs(orig.getTaskOutputMappings())
                    .build();
            userTaskDefinitions[i] = definition;
            i++;
        }

        return new UserTaskDefinitionList(userTaskDefinitions);
    }
}

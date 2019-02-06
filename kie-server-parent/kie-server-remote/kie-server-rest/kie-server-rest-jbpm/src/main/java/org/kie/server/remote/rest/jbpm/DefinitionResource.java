/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

import static org.kie.server.api.rest.RestURI.CONTAINER_ID;
import static org.kie.server.api.rest.RestURI.PROCESS_ID;
import static org.kie.server.api.rest.RestURI.PROCESS_DEF_ASSOCIATED_ENTITIES_GET_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_DEF_GET_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_DEF_SERVICE_TASKS_GET_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_DEF_SUBPROCESS_GET_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_DEF_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_DEF_USER_TASKS_GET_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_DEF_USER_TASK_INPUT_GET_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_DEF_USER_TASK_OUTPUT_GET_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_DEF_VARIABLES_GET_URI;
import static org.kie.server.remote.rest.common.util.RestUtils.buildConversationIdHeader;
import static org.kie.server.remote.rest.common.util.RestUtils.createCorrectVariant;
import static org.kie.server.remote.rest.common.util.RestUtils.getVariant;
import static org.kie.server.remote.rest.common.util.RestUtils.internalServerError;
import static org.kie.server.remote.rest.common.util.RestUtils.notFound;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.GET_PROCESS_DEF_RESPONSE_JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.GET_PROCESS_ENTITIES_RESPONSE_JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.GET_PROCESS_SERVICE_TASKS_RESPONSE_JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.GET_PROCESS_SUBP_RESPONSE_JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.GET_PROCESS_VARS_RESPONSE_JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.GET_TASKS_RESPONSE_JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.GET_TASK_INPUTS_RESPONSE_JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.GET_TASK_OUTPUTS_RESPONSE_JSON;
import static org.kie.server.remote.rest.jbpm.docs.ParameterSamples.JSON;
import static org.kie.server.remote.rest.jbpm.resources.Messages.PROCESS_DEFINITION_NOT_FOUND;
import static org.kie.server.remote.rest.jbpm.resources.Messages.UNEXPECTED_ERROR;

import java.net.URLDecoder;
import java.text.MessageFormat;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import org.kie.server.api.model.definition.AssociatedEntitiesDefinition;
import org.kie.server.api.model.definition.ProcessDefinition;
import org.kie.server.api.model.definition.ServiceTasksDefinition;
import org.kie.server.api.model.definition.SubProcessesDefinition;
import org.kie.server.api.model.definition.TaskInputsDefinition;
import org.kie.server.api.model.definition.TaskOutputsDefinition;
import org.kie.server.api.model.definition.UserTaskDefinitionList;
import org.kie.server.api.model.definition.VariablesDefinition;
import org.kie.server.remote.rest.common.Header;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.jbpm.DefinitionServiceBase;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Example;
import io.swagger.annotations.ExampleProperty;

@Api(value="Process and task definitions")
@Path("server/" + PROCESS_DEF_URI)
public class DefinitionResource {

    private DefinitionServiceBase definitionServiceBase;
    private KieServerRegistry context;

    public DefinitionResource() {

    }

    public DefinitionResource(DefinitionServiceBase definitionServiceBase, KieServerRegistry context) {
        this.definitionServiceBase = definitionServiceBase;
        this.context = context;
    }


    @ApiOperation(value="Returns entity and task information for a specified process.",
            response=ProcessDefinition.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Process or Container Id not found"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=GET_PROCESS_DEF_RESPONSE_JSON)})) })
    @GET
    @Path(PROCESS_DEF_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getProcessDefinition(@Context HttpHeaders headers, 
            @ApiParam(value = "container id where the process definition resides", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId,
            @ApiParam(value = "process id that the definition should be retrieved for", required = true, example = "evaluation") @PathParam(PROCESS_ID) String processId) {
        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            Object responseObject = definitionServiceBase.getProcessDefinition(containerId, processId);

            return createCorrectVariant(responseObject, headers, Response.Status.OK, conversationIdHeader);
        } catch (IllegalStateException e) {
            return notFound(
                    MessageFormat.format(PROCESS_DEFINITION_NOT_FOUND, processId, containerId), v, conversationIdHeader);
        } catch( Exception e ) {
            return internalServerError(
                    MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Retrieves sub process definitions that are defined in given process within given container",
            response=SubProcessesDefinition.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Process or Container Id not found"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=GET_PROCESS_SUBP_RESPONSE_JSON)})) })
    @GET
    @Path(PROCESS_DEF_SUBPROCESS_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getReusableSubProcesses(@Context HttpHeaders headers, 
            @ApiParam(value = "container id where the process definition resides", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId, 
            @ApiParam(value = "process id that subprocesses should be retrieved from", required = true, example = "evaluation") @PathParam(PROCESS_ID) String processId) {
        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            SubProcessesDefinition definition = definitionServiceBase.getReusableSubProcesses(containerId, processId);

            return createCorrectVariant(definition, headers, Response.Status.OK, conversationIdHeader);
        } catch (IllegalStateException e) {
            return notFound(
                    MessageFormat.format(PROCESS_DEFINITION_NOT_FOUND, processId, containerId), v, conversationIdHeader);
        } catch( Exception e ) {
            return internalServerError(
                    MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Retrieves process variables definitions that are present in given process and container",
            response=VariablesDefinition.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Process or Container Id not found"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=GET_PROCESS_VARS_RESPONSE_JSON)})) })
    @GET
    @Path(PROCESS_DEF_VARIABLES_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getProcessVariables(@Context HttpHeaders headers, 
            @ApiParam(value = "container id where the process definition resides", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId, 
            @ApiParam(value = "process id that the variable definitions should be retrieved from", required = true, example = "evaluation") @PathParam(PROCESS_ID) String processId) {
        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            VariablesDefinition variablesDefinition = definitionServiceBase.getProcessVariables(containerId, processId);

            return createCorrectVariant(variablesDefinition, headers, Response.Status.OK, conversationIdHeader);
        } catch (IllegalStateException e) {
            return notFound(
                    MessageFormat.format(PROCESS_DEFINITION_NOT_FOUND, processId, containerId), v, conversationIdHeader);
        } catch( Exception e ) {
            return internalServerError(
                    MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Retrieves service tasks definitions that are present in given process and container",
            response=ServiceTasksDefinition.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Process or Container Id not found"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=GET_PROCESS_SERVICE_TASKS_RESPONSE_JSON)})) })
    @GET
    @Path(PROCESS_DEF_SERVICE_TASKS_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getServiceTasks(@Context HttpHeaders headers, 
            @ApiParam(value = "container id where the process definition resides", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId, 
            @ApiParam(value = "process id that the service task definitions should be retrieved from", required = true, example = "evaluation") @PathParam(PROCESS_ID) String processId) {
        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            ServiceTasksDefinition serviceTasksDefinition = definitionServiceBase.getServiceTasks(containerId, processId);

            return createCorrectVariant(serviceTasksDefinition, headers, Response.Status.OK, conversationIdHeader);
        } catch (IllegalStateException e) {
            return notFound(
                    MessageFormat.format(PROCESS_DEFINITION_NOT_FOUND, processId, containerId), v, conversationIdHeader);
        } catch( Exception e ) {
            return internalServerError(
                    MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Retrieves actors and groups that are involved in given process and container",
            response=AssociatedEntitiesDefinition.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Process or Container Id not found"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=GET_PROCESS_ENTITIES_RESPONSE_JSON)})) })    
    @GET
    @Path(PROCESS_DEF_ASSOCIATED_ENTITIES_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getAssociatedEntities(@Context HttpHeaders headers, 
            @ApiParam(value = "container id where the process definition resides", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId, 
            @ApiParam(value = "process id that the involved actors and groups should be retrieved from", required = true, example = "evaluation") @PathParam(PROCESS_ID) String processId) {
        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            AssociatedEntitiesDefinition associatedEntitiesDefinition = definitionServiceBase.getAssociatedEntities(containerId, processId);

            return createCorrectVariant(associatedEntitiesDefinition, headers, Response.Status.OK, conversationIdHeader);
        } catch (IllegalStateException e) {
            return notFound(
                    MessageFormat.format(PROCESS_DEFINITION_NOT_FOUND, processId, containerId), v, conversationIdHeader);
        } catch( Exception e ) {
            return internalServerError(
                    MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Retrieves user tasks definitions that are present in given process and container",
            response=UserTaskDefinitionList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Process or Container Id not found"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=GET_TASKS_RESPONSE_JSON)})) })
    @GET
    @Path(PROCESS_DEF_USER_TASKS_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getTasksDefinitions(@Context HttpHeaders headers, 
            @ApiParam(value = "container id where the process definition resides", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId, 
            @ApiParam(value = "process id that the user task definitions should be retrieved from", required = true, example = "evaluation") @PathParam(PROCESS_ID) String processId) {
        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            UserTaskDefinitionList userTaskDefinitions = definitionServiceBase.getTasksDefinitions(containerId, processId);

            return createCorrectVariant(userTaskDefinitions, headers, Response.Status.OK, conversationIdHeader);
        } catch (IllegalStateException e) {
            return notFound(
                    MessageFormat.format(PROCESS_DEFINITION_NOT_FOUND, processId, containerId), v, conversationIdHeader);
        } catch( Exception e ) {
            return internalServerError(
                    MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Retrieves input variables defined on a given user task",
            response=TaskInputsDefinition.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Process or Container Id not found"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=GET_TASK_INPUTS_RESPONSE_JSON)})) })
    @GET
    @Path(PROCESS_DEF_USER_TASK_INPUT_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getTaskInputMappings(@Context HttpHeaders headers, 
            @ApiParam(value = "container id where the process definition resides", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId, 
            @ApiParam(value = "process id that given task belongs to", required = true, example = "evaluation") @PathParam(PROCESS_ID) String processId, 
            @ApiParam(value = "task name that input variable definitions should be retrieved for", required = true, example = "Review") @PathParam("taskName") String taskName) {
        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {

            TaskInputsDefinition taskInputsDefinition = definitionServiceBase.getTaskInputMappings(containerId, processId, URLDecoder.decode(taskName, "UTF-8"));

            return createCorrectVariant(taskInputsDefinition, headers, Response.Status.OK, conversationIdHeader);
        } catch (IllegalStateException e) {
            return notFound(
                    MessageFormat.format(PROCESS_DEFINITION_NOT_FOUND, processId, containerId), v, conversationIdHeader);
        } catch( Exception e ) {
            return internalServerError(
                    MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Retrieves output variables defined on a given user task",
            response=TaskOutputsDefinition.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "Process or Container Id not found"), 
            @ApiResponse(code = 200, message = "Successfull response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=GET_TASK_OUTPUTS_RESPONSE_JSON)})) })
    @GET
    @Path(PROCESS_DEF_USER_TASK_OUTPUT_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getTaskOutputMappings(@Context HttpHeaders headers, 
            @ApiParam(value = "container id where the process definition resides", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId, 
            @ApiParam(value = "process id that given task belongs to", required = true, example = "evaluation") @PathParam(PROCESS_ID) String processId, 
            @ApiParam(value = "task name that output variable definitions should be retrieved for", required = true, example = "Review") @PathParam("taskName") String taskName) {
        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            TaskOutputsDefinition taskOutputsDefinition = definitionServiceBase.getTaskOutputMappings(containerId, processId, URLDecoder.decode(taskName, "UTF-8"));

            return createCorrectVariant(taskOutputsDefinition, headers, Response.Status.OK, conversationIdHeader);
        } catch (IllegalStateException e) {
            return notFound(
                    MessageFormat.format(PROCESS_DEFINITION_NOT_FOUND, processId, containerId), v, conversationIdHeader);
        } catch( Exception e ) {
            return internalServerError(
                    MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }


}

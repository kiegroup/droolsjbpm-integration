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
import org.kie.server.api.model.definition.ServiceTasksDefinition;
import org.kie.server.api.model.definition.SubProcessesDefinition;
import org.kie.server.api.model.definition.TaskInputsDefinition;
import org.kie.server.api.model.definition.TaskOutputsDefinition;
import org.kie.server.api.model.definition.UserTaskDefinitionList;
import org.kie.server.api.model.definition.VariablesDefinition;
import org.kie.server.remote.rest.common.Header;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.jbpm.DefinitionServiceBase;

import static org.kie.server.api.rest.RestURI.*;
import static org.kie.server.remote.rest.common.util.RestUtils.*;
import static org.kie.server.remote.rest.jbpm.resources.Messages.*;

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


    @GET
    @Path(PROCESS_DEF_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessDefinition(@Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("pId") String processId) {
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

    @GET
    @Path(PROCESS_DEF_SUBPROCESS_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getReusableSubProcesses(@Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("pId") String processId) {
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

    @GET
    @Path(PROCESS_DEF_VARIABLES_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessVariables(@Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("pId") String processId) {
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

    @GET
    @Path(PROCESS_DEF_SERVICE_TASKS_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getServiceTasks(@Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("pId") String processId) {
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

    @GET
    @Path(PROCESS_DEF_ASSOCIATED_ENTITIES_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getAssociatedEntities(@Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("pId") String processId) {
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

    @GET
    @Path(PROCESS_DEF_USER_TASKS_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getTasksDefinitions(@Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("pId") String processId) {
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

    @GET
    @Path(PROCESS_DEF_USER_TASK_INPUT_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getTaskInputMappings(@Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("pId") String processId, @PathParam("taskName") String taskName) {
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

    @GET
    @Path(PROCESS_DEF_USER_TASK_OUTPUT_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getTaskOutputMappings(@Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("pId") String processId, @PathParam("taskName") String taskName) {
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

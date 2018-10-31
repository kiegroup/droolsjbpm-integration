/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.controller.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.kie.server.controller.api.KieServerControllerException;
import org.kie.server.controller.api.KieServerControllerIllegalArgumentException;
import org.kie.server.controller.api.model.runtime.ContainerList;
import org.kie.server.controller.api.model.runtime.ServerInstanceKeyList;
import org.kie.server.controller.impl.service.RuntimeManagementServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.controller.rest.ControllerUtils.*;

@Api(value = "Controller :: Runtime")
@Path("/controller/runtime")
public class RestRuntimeManagementServiceImpl {

    private static final Logger logger = LoggerFactory.getLogger(RestRuntimeManagementServiceImpl.class);
    private static final String REQUEST_FAILED_TOBE_PROCESSED = "Request failed to be processed due to: ";

    private RuntimeManagementServiceImpl runtimeManagementService;

    @ApiOperation(value = "Retrieve all Kie Server instances connected to the controller for a given Server Template", response = ServerInstanceKeyList.class)
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Server Template not found"),
            @ApiResponse(code = 400, message = "Controller exception"),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @GET
    @Path("servers/{id}/instances")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getServerInstances(@Context HttpHeaders headers,
                                       @ApiParam(name = "Kie Server Template identifier", required = true) @PathParam("id") String serverTemplateId) {
        String contentType = getContentType(headers);
        try {
            logger.debug("Received get server template with id {}", serverTemplateId);
            final ServerInstanceKeyList instances = runtimeManagementService.getServerInstances(serverTemplateId);
            String response = marshal(contentType, instances);
            logger.debug("Returning response for get server instance with server template id '{}': {}", serverTemplateId, response);

            return createCorrectVariant(response, headers, Response.Status.OK);
        } catch (KieServerControllerIllegalArgumentException e) {
            return createCorrectVariant(e.getMessage(), headers, Response.Status.NOT_FOUND);
        } catch (KieServerControllerException e){
            return createCorrectVariant(REQUEST_FAILED_TOBE_PROCESSED + e.getMessage(), headers, Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Get server instances using server template id {} failed due to {}", serverTemplateId, e.getMessage(), e);
            return createCorrectVariant("Unknown error " + e.getMessage(), headers, Response.Status.INTERNAL_SERVER_ERROR);
        }

    }

    @ApiOperation(value = "Retrieve all Containers for a given Kie Server instance", response = ContainerList.class)
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Server Template or Kie Server instance not found"),
            @ApiResponse(code = 400, message = "Controller exception"),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @GET
    @Path("servers/{id}/instances/{instanceId}/containers")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getServerInstanceContainers(@Context HttpHeaders headers,
                                                @ApiParam(name = "Kie Server Template identifier", required = true) @PathParam("id") String serverTemplateId,
                                                @ApiParam(name = "Kie Server instance identifier", required = true) @PathParam("instanceId") String instanceId) {
        String contentType = getContentType(headers);
        try {
            logger.debug("Received get containers for server template with id {} and instance id {}", serverTemplateId, instanceId);

            ContainerList containers = runtimeManagementService.getServerInstanceContainers(serverTemplateId, instanceId);
            String response = marshal(contentType, containers);
            logger.debug("Returning response for get containers for server template with id {} and instance id {}: {}", serverTemplateId, instanceId, response);

            return createCorrectVariant(response, headers, Response.Status.OK);
        } catch (KieServerControllerIllegalArgumentException e) {
            return createCorrectVariant(e.getMessage(), headers, Response.Status.NOT_FOUND);
        } catch (KieServerControllerException e){
            return createCorrectVariant(REQUEST_FAILED_TOBE_PROCESSED + e.getMessage(), headers, Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Get containers using server template id {} and instance id {} failed due to {}", serverTemplateId, instanceId, e.getMessage(), e);
            return createCorrectVariant("Unknown error " + e.getMessage(), headers, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Retrieve all Containers with a given id in Kie Server instances from a specific Server Template", response = ContainerList.class)
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Server Template or Kie Server instance not found"),
            @ApiResponse(code = 400, message = "Controller exception"),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @GET
    @Path("servers/{id}/containers/{containerId}/instances")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getServerTemplateContainers(@Context HttpHeaders headers,
                                                @ApiParam(name = "Kie Server Template identifier", required = true) @PathParam("id") String serverTemplateId,
                                                @ApiParam(name = "Container identifier", required = true) @PathParam("containerId") String containerId) {
        String contentType = getContentType(headers);
        try {
            logger.debug("Received get container {} for server template with id {}", containerId, serverTemplateId);

            ContainerList containers = runtimeManagementService.getServerTemplateContainers(serverTemplateId, containerId);
            String response = marshal(contentType, containers);
            logger.debug("Returning response for get containers for server template with id {} and container id {}: {}", serverTemplateId, containerId, response);

            return createCorrectVariant(response, headers, Response.Status.OK);
        } catch (KieServerControllerIllegalArgumentException e) {
            return createCorrectVariant(e.getMessage(), headers, Response.Status.NOT_FOUND);
        } catch (KieServerControllerException e){
            return createCorrectVariant(REQUEST_FAILED_TOBE_PROCESSED + e.getMessage(), headers, Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Get containers using server template id {} and container id {} failed due to {}", serverTemplateId, containerId, e.getMessage(), e);
            return createCorrectVariant("Unknown error " + e.getMessage(), headers, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    public void setRuntimeManagementService(final RuntimeManagementServiceImpl runtimeManagementService) {
        this.runtimeManagementService = runtimeManagementService;
    }
}

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

import io.swagger.annotations.*;
import org.kie.server.controller.api.KieServerControllerException;
import org.kie.server.controller.api.KieServerControllerIllegalArgumentException;
import org.kie.server.controller.api.model.runtime.ContainerList;
import org.kie.server.controller.api.model.runtime.ServerInstanceKeyList;
import org.kie.server.controller.impl.service.RuntimeManagementServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.controller.rest.ControllerUtils.*;
import static org.kie.server.controller.rest.docs.ParameterSamples.*;

@Api(value = "Controller :: KIE Server instances and KIE containers")
@Path("/controller/runtime")
public class RestRuntimeManagementServiceImpl {

    private static final Logger logger = LoggerFactory.getLogger(RestRuntimeManagementServiceImpl.class);
    private static final String REQUEST_FAILED_TOBE_PROCESSED = "Request failed to be processed due to: ";

    private RuntimeManagementServiceImpl runtimeManagementService;

    @ApiOperation(value = "Returns all KIE Server instances configured with the controller for a specified KIE Server template", response = ServerInstanceKeyList.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "KIE Server instances", examples = @Example({
                @ExampleProperty(mediaType = JSON, value = SERVER_INSTANCE_GET_JSON),
                @ExampleProperty(mediaType = XML, value = SERVER_INSTANCE_GET_XML)
            })),
            @ApiResponse(code = 404, message = "KIE Server template not found"),
            @ApiResponse(code = 400, message = "Controller exception"),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @GET
    @Path("servers/{serverTemplateId}/instances")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getServerInstances(@Context HttpHeaders headers,
                                       @ApiParam(name = "serverTemplateId", value = "ID of the KIE Server template for which you are retrieving KIE Server instances", required = true, example = "test-kie-server") @PathParam("serverTemplateId") String serverTemplateId) {
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

    @ApiOperation(value = "Returns all KIE containers for a specified KIE Server template and a specified KIE Server instance", response = ContainerList.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "KIE container instances", examples = @Example({
                    @ExampleProperty(mediaType = JSON, value = CONTAINER_INSTANCE_LIST_JSON),
                    @ExampleProperty(mediaType = XML, value = CONTAINER_INSTANCE_LIST_XML)
            })),
            @ApiResponse(code = 404, message = "Kie Server template or Kie Server instance not found"),
            @ApiResponse(code = 400, message = "Controller exception"),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @GET
    @Path("servers/{serverTemplateId}/instances/{serverInstanceId}/containers")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getServerInstanceContainers(@Context HttpHeaders headers,
                                                @ApiParam(name = "serverTemplateId", value = "ID of the KIE Server template associated with the KIE Server instance", required = true, example = "test-kie-server") @PathParam("serverTemplateId") String serverTemplateId,
                                                @ApiParam(name = "serverInstanceId", value = "ID of the KIE Server instance for which you are retrieving KIE containers (example: default-kieserver-instance@localhost:8080)", required = true, example = "test-kie-server@localhost:8080") @PathParam("serverInstanceId") String instanceId) {
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

    @ApiOperation(value = "Returns all instances of a specified KIE container in a specified KIE Server template", response = ContainerList.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "KIE container instances", examples = @Example({
                    @ExampleProperty(mediaType = JSON, value = CONTAINER_INSTANCE_LIST_JSON),
                    @ExampleProperty(mediaType = XML, value = CONTAINER_INSTANCE_LIST_XML)
            })),
            @ApiResponse(code = 404, message = "KIE Server template or KIE container not found"),
            @ApiResponse(code = 400, message = "Controller exception"),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @GET
    @Path("servers/{serverTemplateId}/containers/{containerId}/instances")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getServerTemplateContainers(@Context HttpHeaders headers,
                                                @ApiParam(name = "serverTemplateId", value = "ID of the KIE Server template for which you are retrieving KIE containers", required = true, example = "test-kie-server") @PathParam("serverTemplateId") String serverTemplateId,
                                                @ApiParam(name = "containerId", value = "ID of the KIE container to be retrieved", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam("containerId") String containerId) {
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

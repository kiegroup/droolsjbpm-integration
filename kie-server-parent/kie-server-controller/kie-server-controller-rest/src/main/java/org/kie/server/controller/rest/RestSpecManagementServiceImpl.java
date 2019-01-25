/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.controller.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import io.swagger.annotations.*;
import org.kie.server.controller.api.KieServerControllerException;
import org.kie.server.controller.api.KieServerControllerIllegalArgumentException;
import org.kie.server.controller.api.model.spec.*;
import org.kie.server.controller.impl.service.SpecManagementServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.controller.rest.ControllerUtils.*;
import static org.kie.server.controller.rest.docs.ParameterSamples.*;

@Api(value = "Controller :: KIE Server templates and KIE containers")
@Path("/controller/management")
public class RestSpecManagementServiceImpl {

    private static final Logger logger = LoggerFactory.getLogger(RestSpecManagementServiceImpl.class);
    private static final String REQUEST_FAILED_TOBE_PROCESSED = "Request failed to be processed due to: ";

    private SpecManagementServiceImpl specManagementService;

    @ApiOperation(value = "Creates a KIE container in a specified KIE Server template", notes = "You set the KIE container configurations in the request body", code = 201)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "KIE container successfully deployed"),
            @ApiResponse(code = 404, message = "KIE Server template not found"),
            @ApiResponse(code = 400, message = "Controller exception"),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @PUT
    @Path("servers/{serverTemplateId}/containers/{containerId}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response saveContainerSpec(@Context HttpHeaders headers,
                                      @ApiParam(name = "serverTemplateId", value = "ID of the KIE Server template associated with the new KIE container", required = true, example = "test-kie-server") @PathParam("serverTemplateId") String serverTemplateId,
                                      @ApiParam(name = "containerId", value = "ID of the new KIE container", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam("containerId") String containerId,
                                      @ApiParam(name = "body", value = "A map containing the container-name, relevant release-id (group ID, artifact ID, and version), configuration specifications (rule, process, planning), and other components of the new KIE container",
                                              required = true, examples =
                                                    @Example(value = {
                                                          @ExampleProperty(mediaType = JSON, value = CONTAINER_SPEC_JSON),
                                                          @ExampleProperty(mediaType = XML, value = CONTAINER_SPEC_XML)
                                      })) String containerSpecPayload,
                                      @Context UriInfo uriInfo) {

        String contentType = getContentType(headers);
        try {
            logger.debug("Received save container spec request for server template with id {}", serverTemplateId);
            ContainerSpec containerSpec = unmarshal(containerSpecPayload, contentType, ContainerSpec.class);
            logger.debug("Container spec is {}", containerSpec);

            specManagementService.saveContainerSpec(serverTemplateId, containerSpec);
            logger.debug("Returning response for save container spec request for server template with id '{}': CREATED", serverTemplateId);
            return createCreatedVariant("", headers, uriInfo.getAbsolutePathBuilder());
        } catch (KieServerControllerIllegalArgumentException e) {
            return createCorrectVariant(e.getMessage(), headers, Response.Status.NOT_FOUND);
        } catch (KieServerControllerException e){
            return createCorrectVariant(REQUEST_FAILED_TOBE_PROCESSED + e.getMessage(), headers, Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Save container spec request for server template id {} failed due to {}", serverTemplateId, e.getMessage(), e);
            return createCorrectVariant("Unknown error " + e.getMessage(), headers, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Updates information about a specified KIE container in a specified KIE Server template", code = 201)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "KIE container successfully updated"),
            @ApiResponse(code = 404, message = "KIE Server template or KIE container not found"),
            @ApiResponse(code = 400, message = "Controller exception"),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @POST
    @Path("servers/{serverTemplateId}/containers/{containerId}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response updateContainerSpec(@Context HttpHeaders headers,
                                        @ApiParam(name = "serverTemplateId", value = "ID of the KIE Server template associated with the KIE container", required = true, example = "test-kie-server") @PathParam("serverTemplateId") String serverTemplateId,
                                        @ApiParam(name = "containerId", value = "ID of the KIE container to be updated", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam("containerId") String containerId,
                                        @ApiParam(name = "body", value = "A map containing the updated specifications for the KIE container", required = true, examples =
                                        @Example(value = {
                                                @ExampleProperty(mediaType = JSON, value = CONTAINER_SPEC_JSON),
                                                @ExampleProperty(mediaType = XML, value = CONTAINER_SPEC_XML)
                                        })) String containerSpecPayload,
                                        @ApiParam(value = "Optional param to reset current environment before updating on dev mode, defaults to false") @QueryParam("resetBeforeUpdate")  @DefaultValue("false") boolean resetBeforeUpdate) {

        String contentType = getContentType(headers);
        try {
            logger.debug("Received update container spec request for server template with id {}", serverTemplateId);
            ContainerSpec containerSpec = unmarshal(containerSpecPayload, contentType, ContainerSpec.class);
            logger.debug("Container spec is {}", containerSpec);

            specManagementService.updateContainerSpec(serverTemplateId, containerId, containerSpec, resetBeforeUpdate);
            logger.debug("Returning response for update container spec request for server template with id '{}': CREATED", serverTemplateId);
            return createCorrectVariant("", headers, Response.Status.CREATED);
        } catch (KieServerControllerIllegalArgumentException e) {
            return createCorrectVariant(e.getMessage(), headers, Response.Status.NOT_FOUND);
        } catch (KieServerControllerException e){
            return createCorrectVariant(REQUEST_FAILED_TOBE_PROCESSED + e.getMessage(), headers, Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Save container spec request for server template id {} failed due to {}", serverTemplateId, e.getMessage(), e);
            return createCorrectVariant("Unknown error " + e.getMessage(), headers, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Creates a new KIE Server template with a specified ID", code = 201)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "KIE Server template successfully created", examples = @Example(value = {
                    @ExampleProperty(mediaType = JSON, value = SERVER_TEMPLATE_SAVE_JSON),
                    @ExampleProperty(mediaType = XML, value = SERVER_TEMPLATE_SAVE_XML)
            })),
            @ApiResponse(code = 400, message = "Controller exception"),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @PUT
    @Path("servers/{serverTemplateId}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response saveServerTemplate(@Context HttpHeaders headers,
                                       @ApiParam(name = "serverTemplateId", value = "ID of the new KIE Server template", required = true, example = "sample-server") @PathParam("serverTemplateId") String serverTemplateId,
                                       @ApiParam(name = "body", value = "A map containing the server-name, capabilities, and other components of the new KIE Server template", required = true, examples =
                                       @Example(value = {
                                               @ExampleProperty(mediaType = JSON, value = SERVER_TEMPLATE_JSON),
                                               @ExampleProperty(mediaType = XML, value = SERVER_TEMPLATE_XML)
                                       })) String serverTemplatePayload,
                                       @Context UriInfo uriInfo) {
        String contentType = getContentType(headers);
        try {
            logger.debug("Received save server template with id {}", serverTemplateId);
            ServerTemplate serverTemplate = unmarshal(serverTemplatePayload, contentType, ServerTemplate.class);
            if (serverTemplate == null) {
                return createCorrectVariant("Server template " + serverTemplateId + " not found", headers, Response.Status.NOT_FOUND);
            }
            logger.debug("Server template is {}", serverTemplate);

            specManagementService.saveServerTemplate(serverTemplate);
            logger.debug("Returning response for save server template with id '{}': CREATED", serverTemplateId);
            UriBuilder builder = uriInfo.getAbsolutePathBuilder();
            return createCreatedVariant("", headers, builder);
        } catch (KieServerControllerException e){
            return createCorrectVariant(REQUEST_FAILED_TOBE_PROCESSED + e.getMessage(), headers, Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Save server template id {} failed due to {}", serverTemplateId, e.getMessage(), e);
            return createCorrectVariant("Unknown error " + e.getMessage(), headers, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Returns information about a specified KIE Server template", response = ServerTemplate.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "KIE Server template", examples = @Example({
                    @ExampleProperty(mediaType = JSON, value = SERVER_TEMPLATE_GET_JSON),
                    @ExampleProperty(mediaType = XML, value = SERVER_TEMPLATE_GET_XML)
            })),
            @ApiResponse(code = 404, message = "KIE Server template not found"),
            @ApiResponse(code = 400, message = "Controller exception"),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @GET
    @Path("servers/{serverTemplateId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getServerTemplate(@Context HttpHeaders headers,
                                      @ApiParam(name = "serverTemplateId", value = "ID of the KIE Server template to be retrieved", required = true, example = "test-kie-server") @PathParam("serverTemplateId") String serverTemplateId) {
        String contentType = getContentType(headers);
        try {
            logger.debug("Received get server template with id {}", serverTemplateId);
            final ServerTemplate serverTemplate = specManagementService.getServerTemplate(serverTemplateId);
            String response = marshal(contentType, serverTemplate);
            logger.debug("Returning response for get server template with id '{}': {}", serverTemplateId, response);

            return createCorrectVariant(response, headers, Response.Status.OK);
        } catch (KieServerControllerIllegalArgumentException e) {
            return createCorrectVariant(e.getMessage(), headers, Response.Status.NOT_FOUND);
        } catch (KieServerControllerException e){
            return createCorrectVariant(REQUEST_FAILED_TOBE_PROCESSED + e.getMessage(), headers, Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Get server template id {} failed due to {}", serverTemplateId, e.getMessage(), e);
            return createCorrectVariant("Unknown error " + e.getMessage(), headers, Response.Status.INTERNAL_SERVER_ERROR);
        }

    }

    @ApiOperation(value = "Returns all KIE Server templates", response = ServerTemplateList.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "KIE Server templates", examples = @Example(value = {
                    @ExampleProperty(mediaType = JSON, value = SERVER_TEMPLATE_LIST_JSON),
                    @ExampleProperty(mediaType = XML, value = SERVER_TEMPLATE_LIST_XML)
            })),
            @ApiResponse(code = 404, message = "KIE Server template not found"),
            @ApiResponse(code = 400, message = "Controller exception"),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @GET
    @Path("servers")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response listServerTemplates(@Context HttpHeaders headers) {

        String contentType = getContentType(headers);
        try {
            logger.debug("Received get server templates");

            String response = marshal(contentType, specManagementService.listServerTemplates());
            logger.debug("Returning response for get server templates: {}", response);

            return createCorrectVariant(response, headers, Response.Status.OK);
        } catch (KieServerControllerIllegalArgumentException e) {
            return createCorrectVariant(e.getMessage(), headers, Response.Status.NOT_FOUND);
        } catch (KieServerControllerException e){
            return createCorrectVariant(REQUEST_FAILED_TOBE_PROCESSED + e.getMessage(), headers, Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Get server templates failed due to {}", e.getMessage(), e);
            return createCorrectVariant("Unknown error " + e.getMessage(), headers, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Returns all KIE containers for a specified KIE Server template", response = ContainerSpecList.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "KIE containers", examples = @Example({
                @ExampleProperty(mediaType = JSON, value = CONTAINER_SPEC_LIST_JSON),
                @ExampleProperty(mediaType = XML, value = CONTAINER_SPEC_LIST_XML)
            })),
            @ApiResponse(code = 404, message = "KIE Server template not found"),
            @ApiResponse(code = 400, message = "Controller exception"),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @GET
    @Path("servers/{serverTemplateId}/containers")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response listContainerSpec(@Context HttpHeaders headers,
                                      @ApiParam(name = "serverTemplateId", value = "ID of the KIE Server template for which you are retrieving KIE containers", required = true, example = "test-kie-server") @PathParam("serverTemplateId") String serverTemplateId) {
        String contentType = getContentType(headers);
        try {
            logger.debug("Received get containers for server template with id {}", serverTemplateId);

            String response = marshal(contentType, specManagementService.listContainerSpec(serverTemplateId));
            logger.debug("Returning response for get containers for server templates with id {}: {}", serverTemplateId, response);

            return createCorrectVariant(response, headers, Response.Status.OK);
        } catch (KieServerControllerIllegalArgumentException e) {
            return createCorrectVariant(e.getMessage(), headers, Response.Status.NOT_FOUND);
        } catch (KieServerControllerException e){
            return createCorrectVariant(REQUEST_FAILED_TOBE_PROCESSED + e.getMessage(), headers, Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Get server templates failed due to {}", e.getMessage(), e);
            return createCorrectVariant("Unknown error " + e.getMessage(), headers, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Returns information about a specified KIE container for a specified KIE Server template", response = ContainerSpec.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "KIE container", examples = @Example({
                    @ExampleProperty(mediaType = JSON, value = CONTAINER_SPEC_GET_JSON),
                    @ExampleProperty(mediaType = XML, value = CONTAINER_SPEC_GET_XML),
            })),
            @ApiResponse(code = 404, message = "Container Specification or KIE Server template not found"),
            @ApiResponse(code = 400, message = "Controller exception"),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @GET
    @Path("servers/{serverTemplateId}/containers/{containerId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getContainerSpec(@Context HttpHeaders headers,
                                     @ApiParam(name = "serverTemplateId", value = "ID of the KIE Server template associated with the KIE container", required = true, example = "test-kie-server") @PathParam("serverTemplateId") String serverTemplateId,
                                     @ApiParam(name = "containerId", value = "ID of the KIE container to be retrieved", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam("containerId") String containerId) {
        String contentType = getContentType(headers);
        try {
            logger.debug("Received get container {} for server template with id {}", containerId, serverTemplateId);

            ContainerSpec containerSpec = specManagementService.getContainerInfo(serverTemplateId, containerId);
            // set it as server template key only to avoid cyclic references between containers and templates
            containerSpec.setServerTemplateKey(new ServerTemplateKey(containerSpec.getServerTemplateKey().getId(), containerSpec.getServerTemplateKey().getName()));

            String response = marshal(contentType, containerSpec);
            logger.debug("Returning response for get container {} for server templates with id {}: {}", containerId, serverTemplateId, response);

            return createCorrectVariant(response, headers, Response.Status.OK);
        } catch (KieServerControllerIllegalArgumentException e) {
            return createCorrectVariant(e.getMessage(), headers, Response.Status.NOT_FOUND);
        } catch (KieServerControllerException e){
            return createCorrectVariant(REQUEST_FAILED_TOBE_PROCESSED + e.getMessage(), headers, Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Get server templates failed due to {}", e.getMessage(), e);
            return createCorrectVariant("Unknown error " + e.getMessage(), headers, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Disposes a specified KIE container in a specified KIE Server template.", code = 204)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "KIE container successfully disposed"),
            @ApiResponse(code = 404, message = "Container Specification or KIE Server template not found"),
            @ApiResponse(code = 400, message = "Controller exception"),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @DELETE
    @Path("servers/{serverTemplateId}/containers/{containerId}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response deleteContainerSpec(@Context HttpHeaders headers,
                                        @ApiParam(name = "serverTemplateId", value = "ID of the KIE Server template associated with the KIE container", required = true, example = "test-kie-server") @PathParam("serverTemplateId") String serverTemplateId,
                                        @ApiParam(name = "containerId", value = "ID of the KIE container to be disposed", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam("containerId") String containerSpecId) {

        try {

            specManagementService.deleteContainerSpec(serverTemplateId, containerSpecId);
            // return null to produce 204
            return null;
        } catch (KieServerControllerIllegalArgumentException e) {
            return createCorrectVariant(e.getMessage(), headers, Response.Status.NOT_FOUND);
        } catch (KieServerControllerException e){
            return createCorrectVariant(REQUEST_FAILED_TOBE_PROCESSED + e.getMessage(), headers, Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Remove container with id {} from server template with id {} failed due to {}", containerSpecId, serverTemplateId, e.getMessage(), e);
            return createCorrectVariant("Unknown error " + e.getMessage(), headers, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Deletes a specified KIE Server template", code = 204)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "KIE Server template successfully deleted"),
            @ApiResponse(code = 404, message = "KIE Server template not found"),
            @ApiResponse(code = 400, message = "Controller exception"),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @DELETE
    @Path("servers/{serverTemplateId}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response deleteServerTemplate(@Context HttpHeaders headers,
                                         @ApiParam(name = "serverTemplateId", value = "ID of the KIE Server template to be deleted", required = true, example = "test-kie-server") @PathParam("serverTemplateId") String serverTemplateId) {
        try {
            specManagementService.deleteServerTemplate(serverTemplateId);
            // return null to produce 204
            return null;
        } catch (KieServerControllerIllegalArgumentException e) {
            return createCorrectVariant(e.getMessage(), headers, Response.Status.NOT_FOUND);
        } catch (KieServerControllerException e){
            return createCorrectVariant(REQUEST_FAILED_TOBE_PROCESSED + e.getMessage(), headers, Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Remove server template with id {} failed due to {}", serverTemplateId, e.getMessage(), e);
            return createCorrectVariant("Unknown error " + e.getMessage(), headers, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Updates configurations for a specified KIE container in a specified KIE Server template", code = 201)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "KIE container successfully updated"),
            @ApiResponse(code = 404, message = "KIE container or KIE Server template not found"),
            @ApiResponse(code = 400, message = "Controller exception"),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @POST
    @Path("servers/{serverTemplateId}/containers/{containerId}/config/{capability}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response updateContainerConfig(@Context HttpHeaders headers,
                                          @ApiParam(name = "serverTemplateId", value = "ID of the KIE Server template associated with the KIE container", required = true, example = "test-kie-server") @PathParam("serverTemplateId") String serverTemplateId,
                                          @ApiParam(name = "containerId", value = "ID of the KIE container to be updated", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam("containerId") String containerSpecId,
                                          @ApiParam(name = "capability", value = "KIE container capability to be applied (RULE, PROCESS, or PLANNING, case sensitive)", required = true, example = "PROCESS") @PathParam("capability") String capabilityStr,
                                          @ApiParam(name = "body", value = "An org.kie.server.controller.api.model.spec.<capability>Config map containing the configurations for the specified KIE container capability, such as runtimeStrategy, kbase, ksession, and mergeMode for process configuration", required = true, examples =
                                          @Example(value = {
                                                  @ExampleProperty(mediaType = JSON, value = CONTAINER_CONFIG_JSON),
                                                  @ExampleProperty(mediaType = XML, value = CONTAINER_CONFIG_XML)
                                          })) String containerConfigPayload) {

        String contentType = getContentType(headers);
        try {
            ContainerConfig containerConfig;
            Capability capability;
            if (capabilityStr.equals(Capability.PROCESS.name())) {
                capability = Capability.PROCESS;
                logger.debug("Received update container (with id {}) process config request for server template with id {}", containerSpecId , serverTemplateId);
                containerConfig = unmarshal(containerConfigPayload, contentType, ProcessConfig.class);
            } else if (capabilityStr.equals(Capability.RULE.name())) {
                capability = Capability.RULE;
                logger.debug("Received update container (with id {}) rule config request for server template with id {}", containerSpecId, serverTemplateId);
                containerConfig = unmarshal(containerConfigPayload, contentType, RuleConfig.class);
            } else {
                logger.debug("Not supported configuration type {}, returning bad request response", capabilityStr);
                return createCorrectVariant("Not supported configuration " + capabilityStr, headers, Response.Status.BAD_REQUEST);
            }
            logger.debug("Container configuration is {}", containerConfig);

            specManagementService.updateContainerConfig(serverTemplateId, containerSpecId, capability, containerConfig);
            logger.debug("Returning response for update container (with id {}) config '{}': CREATED", containerSpecId, containerConfig);
            return createCorrectVariant("", headers, Response.Status.CREATED);
        } catch (KieServerControllerIllegalArgumentException e) {
            return createCorrectVariant(e.getMessage(), headers, Response.Status.NOT_FOUND);
        } catch (KieServerControllerException e){
            return createCorrectVariant(REQUEST_FAILED_TOBE_PROCESSED + e.getMessage(), headers, Response.Status.BAD_REQUEST);
        }  catch (Exception e) {
            logger.error("Remove server template with id {} failed due to {}", serverTemplateId, e.getMessage(), e);
            return createCorrectVariant("Unknown error " + e.getMessage(), headers, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Stops a specified KIE container in a specified KIE Server template")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "KIE container successfully stopped"),
            @ApiResponse(code = 404, message = "KIE container or KIE Server template not found"),
            @ApiResponse(code = 400, message = "Controller exception"),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @POST
    @Path("servers/{serverTemplateId}/containers/{containerId}/status/stopped")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response stopContainer(@Context HttpHeaders headers,
                                  @ApiParam(name = "serverTemplateId", value = "ID of the KIE Server template associated with the KIE container", required = true, example = "test-kie-server") @PathParam("serverTemplateId") String serverTemplateId,
                                  @ApiParam(name = "containerId", value = "ID of the KIE container to be stopped", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam("containerId") String containerId) {
        logger.debug("Requesting stop container with id {} server instance: {}", containerId, serverTemplateId);
        try {
            ContainerSpecKey containerSpecKey = new ContainerSpecKey();
            containerSpecKey.setId(containerId);
            containerSpecKey.setServerTemplateKey(new ServerTemplateKey(serverTemplateId, ""));
            specManagementService.stopContainer(containerSpecKey);

            logger.debug("Returning response for stop container with id {} server instance: {}", containerId, serverTemplateId);
            return createCorrectVariant("", headers, Response.Status.OK);
        } catch (KieServerControllerIllegalArgumentException e) {
            return createCorrectVariant(e.getMessage(), headers, Response.Status.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Stop container failed due to {}", e.getMessage(), e);
            return createCorrectVariant("Unknown error " + e.getMessage(), headers, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Starts a specified KIE container in a specified KIE Server template")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "KIE Container successfully started"),
            @ApiResponse(code = 404, message = "KIE container or KIE Server template not found"),
            @ApiResponse(code = 400, message = "Controller exception"),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @POST
    @Path("servers/{serverTemplateId}/containers/{containerId}/status/started")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response startContainer(@Context HttpHeaders headers,
                                   @ApiParam(name = "serverTemplateId", value = "ID of the KIE Server template associated with the KIE container", required = true, example = "test-kie-server") @PathParam("serverTemplateId") String serverTemplateId,
                                   @ApiParam(name = "containerId", value = "ID of the KIE container to be started", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam("containerId") String containerId) {
        logger.debug("Requesting start container with id {} server instance: {}", containerId, serverTemplateId);
        try {
            ContainerSpecKey containerSpecKey = new ContainerSpecKey();
            containerSpecKey.setId(containerId);
            containerSpecKey.setServerTemplateKey(new ServerTemplateKey(serverTemplateId, ""));
            specManagementService.startContainer(containerSpecKey);

            logger.debug("Returning response for start container with id {} server instance: {}", containerId, serverTemplateId);
            return createCorrectVariant("", headers, Response.Status.OK);
        } catch (KieServerControllerIllegalArgumentException e) {
            return createCorrectVariant(e.getMessage(), headers, Response.Status.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Start container failed due to {}", e.getMessage(), e);
            return createCorrectVariant("Unknown error " + e.getMessage(), headers, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Activates a specified KIE container in a specified KIE Server template")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "KIE container successfully activated"),
            @ApiResponse(code = 404, message = "KIE container or KIE Server template not found"),
            @ApiResponse(code = 400, message = "Controller exception"),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @POST
    @Path("servers/{serverTemplateId}/containers/{containerId}/status/activated")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response activateContainer(@Context HttpHeaders headers,
                                      @ApiParam(name = "serverTemplateId", value = "ID of the KIE Server template associated with the KIE container", required = true, example = "test-kie-server") @PathParam("serverTemplateId") String serverTemplateId,
                                      @ApiParam(name = "containerId", value = "ID of the KIE container to be activated", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam("containerId") String containerId) {
        logger.debug("Requesting activate container with id {} server instance: {}", containerId, serverTemplateId);
        try {
            ContainerSpecKey containerSpecKey = new ContainerSpecKey();
            containerSpecKey.setId(containerId);
            containerSpecKey.setServerTemplateKey(new ServerTemplateKey(serverTemplateId, ""));
            specManagementService.activateContainer(containerSpecKey);

            logger.debug("Returning response for activate container with id {} server instance: {}", containerId, serverTemplateId);
            return createCorrectVariant("", headers, Response.Status.OK);
        } catch (KieServerControllerIllegalArgumentException e) {
            return createCorrectVariant(e.getMessage(), headers, Response.Status.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Stop container failed due to {}", e.getMessage(), e);
            return createCorrectVariant("Unknown error " + e.getMessage(), headers, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Deactivates a specified KIE container in a specified KIE Server template")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "KIE container successfully deactivated"),
            @ApiResponse(code = 404, message = "KIE container or KIE Server template not found"),
            @ApiResponse(code = 400, message = "Controller exception"),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @POST
    @Path("servers/{serverTemplateId}/containers/{containerId}/status/deactivated")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response deactivateContainer(@Context HttpHeaders headers,
                                        @ApiParam(name = "serverTemplateId", value = "ID of the KIE Server template associated with the KIE container", required = true, example = "test-kie-server") @PathParam("serverTemplateId") String serverTemplateId,
                                        @ApiParam(name = "containerId", value = "ID of the KIE container to be deactivated", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam("containerId") String containerId) {
        logger.debug("Requesting deactivate container with id {} server instance: {}", containerId, serverTemplateId);
        try {
            ContainerSpecKey containerSpecKey = new ContainerSpecKey();
            containerSpecKey.setId(containerId);
            containerSpecKey.setServerTemplateKey(new ServerTemplateKey(serverTemplateId, ""));
            specManagementService.deactivateContainer(containerSpecKey);

            logger.debug("Returning response for deactivate container with id {} server instance: {}", containerId, serverTemplateId);
            return createCorrectVariant("", headers, Response.Status.OK);
        } catch (KieServerControllerIllegalArgumentException e) {
            return createCorrectVariant(e.getMessage(), headers, Response.Status.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Start container failed due to {}", e.getMessage(), e);
            return createCorrectVariant("Unknown error " + e.getMessage(), headers, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    public void setSpecManagementService(final SpecManagementServiceImpl specManagementService) {
        this.specManagementService = specManagementService;
    }
}

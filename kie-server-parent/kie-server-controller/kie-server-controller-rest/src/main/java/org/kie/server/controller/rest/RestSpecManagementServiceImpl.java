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

import java.util.Collection;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.kie.server.controller.api.KieServerControllerException;
import org.kie.server.controller.api.KieServerControllerNotFoundException;
import org.kie.server.controller.api.model.spec.Capability;
import org.kie.server.controller.api.model.spec.ContainerConfig;
import org.kie.server.controller.api.model.spec.ContainerSpecKey;
import org.kie.server.controller.api.model.spec.ContainerSpecList;
import org.kie.server.controller.api.model.spec.RuleConfig;
import org.kie.server.controller.api.model.spec.ServerTemplateKey;
import org.kie.server.controller.api.model.spec.ServerTemplateList;
import org.kie.server.controller.impl.service.SpecManagementServiceImpl;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.api.model.spec.ProcessConfig;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.controller.rest.ControllerUtils.*;

@Path("/controller/management")
public class RestSpecManagementServiceImpl extends SpecManagementServiceImpl {

    private static final Logger logger = LoggerFactory.getLogger(RestSpecManagementServiceImpl.class);

    @PUT
    @Path("servers/{id}/containers/{containerId}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response saveContainerSpec(@Context HttpHeaders headers, @PathParam("id") String serverTemplateId, @PathParam("containerId") String containerId, String containerSpecPayload) {

        String contentType = getContentType(headers);
        try {
            logger.debug("Received save container spec request for server template with id {}", serverTemplateId);
            ContainerSpec containerSpec = unmarshal(containerSpecPayload, contentType, ContainerSpec.class);
            logger.debug("Container spec is {}", containerSpec);

            super.saveContainerSpec(serverTemplateId, containerSpec);
            logger.debug("Returning response for save container spec request for server template with id '{}': CREATED", serverTemplateId);
            return createCorrectVariant("", headers, Response.Status.CREATED);
        } catch (KieServerControllerNotFoundException e) {
            return createCorrectVariant(e.getMessage(), headers, Response.Status.NOT_FOUND);
        } catch (KieServerControllerException e){
            return createCorrectVariant("Request failed to be processed due to" + e.getMessage(), headers, Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Save container spec request for server template id {} failed due to {}", serverTemplateId, e.getMessage(), e);
            return createCorrectVariant("Unknown error " + e.getMessage(), headers, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @POST
    @Path("servers/{id}/containers/{containerId}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response updateContainerSpec(@Context HttpHeaders headers, @PathParam("id") String serverTemplateId, @PathParam("containerId") String containerId, String containerSpecPayload) {

        String contentType = getContentType(headers);
        try {
            logger.debug("Received update container spec request for server template with id {}", serverTemplateId);
            ContainerSpec containerSpec = unmarshal(containerSpecPayload, contentType, ContainerSpec.class);
            logger.debug("Container spec is {}", containerSpec);

            super.updateContainerSpec(serverTemplateId, containerSpec);
            logger.debug("Returning response for update container spec request for server template with id '{}': CREATED", serverTemplateId);
            return createCorrectVariant("", headers, Response.Status.CREATED);
        } catch (KieServerControllerNotFoundException e) {
            return createCorrectVariant(e.getMessage(), headers, Response.Status.NOT_FOUND);
        } catch (KieServerControllerException e){
            return createCorrectVariant("Request failed to be processed due to" + e.getMessage(), headers, Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Save container spec request for server template id {} failed due to {}", serverTemplateId, e.getMessage(), e);
            return createCorrectVariant("Unknown error " + e.getMessage(), headers, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @PUT
    @Path("servers/{id}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response saveServerTemplate(@Context HttpHeaders headers, @PathParam("id") String serverTemplateId, String serverTemplatePayload) {

        String contentType = getContentType(headers);
        try {
            if (super.getServerTemplate(serverTemplateId) != null) {
                return createCorrectVariant("Server template " + serverTemplateId + " already registered", headers, Response.Status.NOT_FOUND);
            }

            logger.debug("Received save server template with id {}", serverTemplateId);
            ServerTemplate serverTemplate = unmarshal(serverTemplatePayload, contentType, ServerTemplate.class);
            if (serverTemplate == null) {
                return createCorrectVariant("Server template " + serverTemplateId + " not found", headers, Response.Status.NOT_FOUND);
            }
            logger.debug("Server template is {}", serverTemplate);

            super.saveServerTemplate(serverTemplate);
            logger.debug("Returning response for save server template with id '{}': CREATED", serverTemplateId);
            return createCorrectVariant("", headers, Response.Status.CREATED);
        } catch (KieServerControllerNotFoundException e) {
            return createCorrectVariant(e.getMessage(), headers, Response.Status.NOT_FOUND);
        } catch (KieServerControllerException e){
            return createCorrectVariant("Request failed to be processed due to" + e.getMessage(), headers, Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Save server template id {} failed due to {}", serverTemplateId, e.getMessage(), e);
            return createCorrectVariant("Unknown error " + e.getMessage(), headers, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("servers/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getServerTemplate(@Context HttpHeaders headers, @PathParam("id") String serverTemplateId) {
        String contentType = getContentType(headers);
        try {
            logger.debug("Received get server template with id {}", serverTemplateId);
            ServerTemplate serverTemplate = super.getServerTemplate(serverTemplateId);
            if (serverTemplate == null) {
                return createCorrectVariant("Server template " + serverTemplateId + " not found", headers, Response.Status.NOT_FOUND);
            }
            String response = marshal(contentType, serverTemplate);
            logger.debug("Returning response for get server template with id '{}': {}", serverTemplateId, response);

            return createCorrectVariant(response, headers, Response.Status.OK);
        } catch (KieServerControllerNotFoundException e) {
            return createCorrectVariant(e.getMessage(), headers, Response.Status.NOT_FOUND);
        } catch (KieServerControllerException e){
            return createCorrectVariant("Request failed to be processed due to" + e.getMessage(), headers, Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Get server template id {} failed due to {}", serverTemplateId, e.getMessage(), e);
            return createCorrectVariant("Unknown error " + e.getMessage(), headers, Response.Status.INTERNAL_SERVER_ERROR);
        }

    }

    @GET
    @Path("servers")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response listServerTemplates(@Context HttpHeaders headers) {

        String contentType = getContentType(headers);
        try {
            logger.debug("Received get server templates");
            Collection<ServerTemplate> servers = super.listServerTemplates();

            String response = marshal(contentType, new ServerTemplateList(servers));
            logger.debug("Returning response for get server templates: {}", response);

            return createCorrectVariant(response, headers, Response.Status.OK);
        } catch (KieServerControllerNotFoundException e) {
            return createCorrectVariant(e.getMessage(), headers, Response.Status.NOT_FOUND);
        } catch (KieServerControllerException e){
            return createCorrectVariant("Request failed to be processed due to" + e.getMessage(), headers, Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Get server templates failed due to {}", e.getMessage(), e);
            return createCorrectVariant("Unknown error " + e.getMessage(), headers, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }


    @GET
    @Path("servers/{id}/containers")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response listContainerSpec(@Context HttpHeaders headers, @PathParam("id") String serverTemplateId) {
        String contentType = getContentType(headers);
        try {
            logger.debug("Received get containers for server template with id {}", serverTemplateId);

            Collection<ContainerSpec> containerSpecs =  super.listContainerSpec(serverTemplateId);

            String response = marshal(contentType, new ContainerSpecList(containerSpecs));
            logger.debug("Returning response for get containers for server templates with id {}: {}", serverTemplateId, response);

            return createCorrectVariant(response, headers, Response.Status.OK);
        } catch (KieServerControllerNotFoundException e) {
            return createCorrectVariant(e.getMessage(), headers, Response.Status.NOT_FOUND);
        } catch (KieServerControllerException e){
            return createCorrectVariant("Request failed to be processed due to" + e.getMessage(), headers, Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Get server templates failed due to {}", e.getMessage(), e);
            return createCorrectVariant("Unknown error " + e.getMessage(), headers, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("servers/{id}/containers/{containerId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getContainerSpec(@Context HttpHeaders headers, @PathParam("id") String serverTemplateId, @PathParam("containerId") String containerId) {
        String contentType = getContentType(headers);
        try {
            logger.debug("Received get container {} for server template with id {}", containerId, serverTemplateId);

            ServerTemplate serverTemplate = super.getServerTemplate(serverTemplateId);
            if (serverTemplate == null) {
                return createCorrectVariant("Server template " + serverTemplateId + " not found", headers, Response.Status.NOT_FOUND);
            }
            ContainerSpec containerSpec = serverTemplate.getContainerSpec(containerId);
            if (containerSpec == null) {
                return createCorrectVariant("Server template " + serverTemplateId + " does not have container with id " + containerId, headers, Response.Status.NOT_FOUND);
            }
            // set it as server template key only to avoid cyclic references between containers and templates
            containerSpec.setServerTemplateKey(new ServerTemplateKey(serverTemplate.getId(), serverTemplate.getName()));

            String response = marshal(contentType, containerSpec);
            logger.debug("Returning response for get container {} for server templates with id {}: {}", containerId, serverTemplateId, response);

            return createCorrectVariant(response, headers, Response.Status.OK);
        } catch (KieServerControllerNotFoundException e) {
            return createCorrectVariant(e.getMessage(), headers, Response.Status.NOT_FOUND);
        } catch (KieServerControllerException e){
            return createCorrectVariant("Request failed to be processed due to" + e.getMessage(), headers, Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Get server templates failed due to {}", e.getMessage(), e);
            return createCorrectVariant("Unknown error " + e.getMessage(), headers, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @DELETE
    @Path("servers/{id}/containers/{containerId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response deleteContainerSpec(@Context HttpHeaders headers, @PathParam("id") String serverTemplateId, @PathParam("containerId") String containerSpecId) {

        try {

            super.deleteContainerSpec(serverTemplateId, containerSpecId);
            // return null to produce 204
            return null;
        } catch (KieServerControllerNotFoundException e) {
            return createCorrectVariant(e.getMessage(), headers, Response.Status.NOT_FOUND);
        } catch (KieServerControllerException e){
            return createCorrectVariant("Request failed to be processed due to" + e.getMessage(), headers, Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Remove container with id {} from server template with id {} failed due to {}", containerSpecId, serverTemplateId, e.getMessage(), e);
            return createCorrectVariant("Unknown error " + e.getMessage(), headers, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @DELETE
    @Path("servers/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response deleteServerTemplate(@Context HttpHeaders headers, @PathParam("id") String serverTemplateId) {
        try {
            super.deleteServerTemplate(serverTemplateId);
            // return null to produce 204
            return null;
        } catch (KieServerControllerNotFoundException e) {
            return createCorrectVariant(e.getMessage(), headers, Response.Status.NOT_FOUND);
        } catch (KieServerControllerException e){
            return createCorrectVariant("Request failed to be processed due to" + e.getMessage(), headers, Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Remove server template with id {} failed due to {}", serverTemplateId, e.getMessage(), e);
            return createCorrectVariant("Unknown error " + e.getMessage(), headers, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @POST
    @Path("servers/{id}/containers/{containerId}/config/{capability}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response updateContainerConfig(@Context HttpHeaders headers, @PathParam("id") String serverTemplateId, @PathParam("containerId") String containerSpecId,
            @PathParam("capability") String capabilityStr, String containerConfigPayload) {

        String contentType = getContentType(headers);
        try {
            ContainerConfig containerConfig = null;
            Capability capability = null;
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

            super.updateContainerConfig(serverTemplateId, containerSpecId, capability, containerConfig);
            logger.debug("Returning response for update container (with id {}) config '{}': CREATED", containerSpecId);
            return createCorrectVariant("", headers, Response.Status.CREATED);
        } catch (KieServerControllerNotFoundException e) {
            return createCorrectVariant(e.getMessage(), headers, Response.Status.NOT_FOUND);
        } catch (KieServerControllerException e){
            return createCorrectVariant("Request failed to be processed due to" + e.getMessage(), headers, Response.Status.BAD_REQUEST);
        }  catch (Exception e) {
            logger.error("Remove server template with id {} failed due to {}", serverTemplateId, e.getMessage(), e);
            return createCorrectVariant("Unknown error " + e.getMessage(), headers, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @POST
    @Path("servers/{id}/containers/{containerId}/status/stopped")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response stopContainer(@Context HttpHeaders headers, @PathParam("id") String serverTemplateId, @PathParam("containerId") String containerId) {
        logger.debug("Requesting stop container with id {} server instance: {}", containerId, serverTemplateId);
        try {
            ContainerSpecKey containerSpecKey = new ContainerSpecKey();
            containerSpecKey.setId(containerId);
            containerSpecKey.setServerTemplateKey(new ServerTemplateKey(serverTemplateId, ""));
            super.stopContainer(containerSpecKey);

            logger.debug("Returning response for stop container with id {} server instance: {}", containerId, serverTemplateId);
            return createCorrectVariant("", headers, Response.Status.OK);
        } catch (KieServerControllerNotFoundException e) {
            return createCorrectVariant(e.getMessage(), headers, Response.Status.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Stop container failed due to {}", e.getMessage(), e);
            return createCorrectVariant("Unknown error " + e.getMessage(), headers, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @POST
    @Path("servers/{id}/containers/{containerId}/status/started")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response startContainer(@Context HttpHeaders headers, @PathParam("id") String serverTemplateId, @PathParam("containerId") String containerId) {
        logger.debug("Requesting start container with id {} server instance: {}", containerId, serverTemplateId);
        try {
            ContainerSpecKey containerSpecKey = new ContainerSpecKey();
            containerSpecKey.setId(containerId);
            containerSpecKey.setServerTemplateKey(new ServerTemplateKey(serverTemplateId, ""));
            super.startContainer(containerSpecKey);

            logger.debug("Returning response for start container with id {} server instance: {}", containerId, serverTemplateId);
            return createCorrectVariant("", headers, Response.Status.OK);
        } catch (KieServerControllerNotFoundException e) {
            return createCorrectVariant(e.getMessage(), headers, Response.Status.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Start container failed due to {}", e.getMessage(), e);
            return createCorrectVariant("Unknown error " + e.getMessage(), headers, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}

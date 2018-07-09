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

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.kie.server.controller.api.KieServerControllerException;
import org.kie.server.controller.api.KieServerControllerIllegalArgumentException;
import org.kie.server.controller.api.model.runtime.ContainerList;
import org.kie.server.controller.api.model.runtime.ServerInstanceKeyList;
import org.kie.server.controller.impl.service.RuntimeManagementServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.controller.rest.ControllerUtils.*;

@Path("/controller/runtime")
public class RestRuntimeManagementServiceImpl extends RuntimeManagementServiceImpl {

    private static final Logger logger = LoggerFactory.getLogger(RestRuntimeManagementServiceImpl.class);
    private static final String REQUEST_FAILED_TOBE_PROCESSED = "Request failed to be processed due to: ";

    @GET
    @Path("servers/{id}/instances")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getServerInstances(@Context HttpHeaders headers, @PathParam("id") String serverTemplateId) {
        String contentType = getContentType(headers);
        try {
            logger.debug("Received get server template with id {}", serverTemplateId);
            final ServerInstanceKeyList instances = super.getServerInstances(serverTemplateId);
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

    @GET
    @Path("servers/{id}/instances/{instanceId}/containers")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getServerInstanceContainers(@Context HttpHeaders headers, @PathParam("id") String serverTemplateId, @PathParam("instanceId") String instanceId) {
        String contentType = getContentType(headers);
        try {
            logger.debug("Received get containers for server template with id {} and instance id {}", serverTemplateId, instanceId);

            ContainerList containers = super.getServerInstanceContainers(serverTemplateId, instanceId);
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

    @GET
    @Path("servers/{id}/containers/{containerId}/instances")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getServerTemplateContainers(@Context HttpHeaders headers, @PathParam("id") String serverTemplateId, @PathParam("containerId") String containerId) {
        String contentType = getContentType(headers);
        try {
            logger.debug("Received get container {} for server template with id {}", containerId, serverTemplateId);

            ContainerList containers = super.getServerTemplateContainers(serverTemplateId, containerId);
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

}

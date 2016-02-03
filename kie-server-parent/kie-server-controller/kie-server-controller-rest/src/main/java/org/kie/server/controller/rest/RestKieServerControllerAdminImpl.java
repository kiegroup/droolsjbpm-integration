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

package org.kie.server.controller.rest;

import java.util.List;
import java.util.Set;
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

import org.kie.remote.common.rest.KieRemoteHttpRequest;
import org.kie.remote.common.rest.KieRemoteHttpResponse;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingException;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.controller.api.KieServerControllerException;
import org.kie.server.controller.api.model.KieServerInstance;
import org.kie.server.controller.api.model.KieServerInstanceInfo;
import org.kie.server.controller.api.model.KieServerInstanceList;
import org.kie.server.controller.api.model.KieServerStatus;
import org.kie.server.controller.impl.KieServerControllerAdminImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.controller.rest.ControllerUtils.*;

/**
 * This admin api is deprecated from 6.4.x and should not be used, instead
 * RestSpecManagementServiceImpl should be used
 * @see org.kie.server.controller.rest.RestSpecManagementServiceImpl
 */
@Deprecated
@Path("/controller/admin")
public class RestKieServerControllerAdminImpl extends KieServerControllerAdminImpl {

    private static final Logger logger = LoggerFactory.getLogger(RestKieServerControllerAdminImpl.class);

    @PUT
    @Path("server/{id}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response addKieServerInstance( @Context HttpHeaders headers, @PathParam("id") String id, String serverInfoPayload ) {

        String contentType = getContentType(headers);
        logger.debug("Received add kie server instance request for server with id {}", id);
        KieServerInfo serverInfo = unmarshal(serverInfoPayload, contentType, KieServerInfo.class);
        logger.debug("Server info {}", serverInfo);
        try {
            KieServerInstance kieServerInstance = addKieServerInstance(serverInfo);

            logger.info("Server with id '{}' added", id);
            String response = marshal(contentType, kieServerInstance);

            logger.debug("Returning response for connect of server '{}': {}", id, response);
            return createCorrectVariant(response, headers, Response.Status.CREATED);
        } catch (KieServerControllerException e){
            return createCorrectVariant("Request failed to be processed due to" + e.getMessage(), headers, Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Add kie server instance for server id {} failed due to {}", id, e.getMessage(), e);
            return createCorrectVariant("Unknown error " + e.getMessage(), headers, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @DELETE
    @Path("server/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response removeKieServerInstance( @Context HttpHeaders headers, @PathParam("id") String id) {

        logger.debug("Received remove kie server instance request for server with id {}", id);

        try {
            removeKieServerInstance(id);

            // return null to produce 204
            return null;
        } catch (KieServerControllerException e){
            return createCorrectVariant("Request failed to be processed due to" + e.getMessage(), headers, Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Remove kie server instance for server id {} failed due to {}", id, e.getMessage(), e);
            return createCorrectVariant("Unknown error " + e.getMessage(), headers, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("server/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getKieServerInstance( @Context HttpHeaders headers, @PathParam("id") String id) {
        String contentType = getContentType(headers);
        logger.debug("Received get kie server instance request for server with id {}", id);

        try {
            KieServerInstance kieServerInstance = getKieServerInstance(id);

            String response = marshal(contentType, kieServerInstance);

            logger.debug("Returning response for connect of server '{}': {}", id, response);
            return createCorrectVariant(response, headers, Response.Status.OK);
        } catch (KieServerControllerException e){
            return createCorrectVariant("Request failed to be processed due to" + e.getMessage(), headers, Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Get kie server instance for server id {} failed due to {}", id, e.getMessage(), e);
            return createCorrectVariant("Unknown error " + e.getMessage(), headers, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("servers")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getKieServerInstances( @Context HttpHeaders headers) {
        String contentType = getContentType(headers);
        logger.debug("Received get kie server instances request");

        try {
            List<KieServerInstance> kieServerInstance = listKieServerInstances();

            String response = marshal(contentType, new KieServerInstanceList(kieServerInstance));

            logger.debug("Returning response for get server instances: {}", response);
            return createCorrectVariant(response, headers, Response.Status.OK);
        } catch (KieServerControllerException e){
            return createCorrectVariant("Request failed to be processed due to" + e.getMessage(), headers, Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Get kie server instances failed due to {}", e.getMessage(), e);
            return createCorrectVariant("Unknown error " + e.getMessage(), headers, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @PUT
    @Path("server/{id}/containers/{containerId}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response createContainer( @Context HttpHeaders headers, @PathParam("id") String id, @PathParam("containerId") String containerId, String containerPayload ) {
        String contentType = getContentType(headers);
        try {
            KieContainerResource container = unmarshal(containerPayload, contentType, KieContainerResource.class);

            KieContainerResource result = createContainer(id, containerId, container);

            String response = marshal(contentType, result);

            logger.debug("Returning response for create container with id {} server instances: {}", containerId, response);
            return createCorrectVariant(response, headers, Response.Status.CREATED);
        } catch (KieServerControllerException e){
            return createCorrectVariant("Request failed to be processed due to" + e.getMessage(), headers, Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Create container failed due to {}", e.getMessage(), e);
            return createCorrectVariant("Unknown error " + e.getMessage(), headers, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @POST
    @Path("server/{id}/containers/{containerId}/status/started")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response startContainer( @Context HttpHeaders headers, @PathParam("id") String id, @PathParam("containerId") String containerId) {
        String contentType = getContentType(headers);
        try {

            startContainer(id, containerId);

            logger.debug("Returning response for start container with id {} server instances: {}", containerId, id);
            return createCorrectVariant("", headers, Response.Status.OK);
        } catch (KieServerControllerException e){
            return createCorrectVariant("Request failed to be processed due to" + e.getMessage(), headers, Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Create container failed due to {}", e.getMessage(), e);
            return createCorrectVariant("Unknown error " + e.getMessage(), headers, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }


    @POST
    @Path("server/{id}/containers/{containerId}/status/stopped")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response stopContainer( @Context HttpHeaders headers, @PathParam("id") String id, @PathParam("containerId") String containerId) {
        String contentType = getContentType(headers);
        try {

            stopContainer(id, containerId);

            logger.debug("Returning response for stop container with id {} server instances: {}", containerId, id);
            return createCorrectVariant("", headers, Response.Status.OK);
        } catch (KieServerControllerException e){
            return createCorrectVariant("Request failed to be processed due to" + e.getMessage(), headers, Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Create container failed due to {}", e.getMessage(), e);
            return createCorrectVariant("Unknown error " + e.getMessage(), headers, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }


    @GET
    @Path("server/{id}/containers/{containerId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getContainerInfo( @Context HttpHeaders headers, @PathParam("id") String id, @PathParam("containerId") String containerId ) {
        String contentType = getContentType(headers);
        try {
            KieContainerResource containerResource = getContainer(id, containerId);

            String response = marshal(contentType, containerResource);

            logger.debug("Returning response for create container with id {} server instances: {}", containerId, response);
            return createCorrectVariant(response, headers, Response.Status.OK);
        } catch (KieServerControllerException e){
            return createCorrectVariant("Request failed to be processed due to" + e.getMessage(), headers, Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Get container info for container id {} within server id {} failed due to {}", containerId, id, e.getMessage(), e);
            return createCorrectVariant("Unknown error " + e.getMessage(), headers, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @DELETE
    @Path("server/{id}/containers/{containerId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response disposeContainer( @Context HttpHeaders headers, @PathParam("id") String id, @PathParam("containerId") String containerId  ) {
        try {
            deleteContainer(id, containerId);

            // return null to produce 204
            return null;
        } catch (KieServerControllerException e){
            return createCorrectVariant("Request failed to be processed due to" + e.getMessage(), headers, Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Remove container with id {} for server id {} failed due to {}", containerId, id, e.getMessage(), e);
            return createCorrectVariant("Unknown error " + e.getMessage(), headers, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void notifyKieServersOnCreateContainer(KieServerInstance kieServerInstance, KieContainerResource container) {

        Set<KieServerInstanceInfo> managedInstances = kieServerInstance.getManagedInstances();
        if (managedInstances != null) {
            for (KieServerInstanceInfo instanceInfo : managedInstances) {
                if (KieServerStatus.UP.equals(instanceInfo.getStatus())) {
                    logger.debug("Server at {} is in online, sending notification to create container...", instanceInfo.getLocation());
                    try {
                        String uri = instanceInfo.getLocation() + "/containers/" + container.getContainerId();
                        makeHttpPutRequestAndCreateCustomResponse(uri, serialize(container), ServiceResponse.class, getUser(), getPassword());
                    } catch (Throwable e) {
                        logger.error("Unable to notify kie server instance at {} about new container {} due to {}"
                                , instanceInfo.getLocation(), container, e.getMessage(), e );
                    }
                }
            }
        }
    }

    @Override
    public void notifyKieServersOnDeleteContainer(KieServerInstance kieServerInstance, String containerId) {
        Set<KieServerInstanceInfo> managedInstances = kieServerInstance.getManagedInstances();
        if (managedInstances != null) {
            for (KieServerInstanceInfo instanceInfo : managedInstances) {
                if (KieServerStatus.UP.equals(instanceInfo.getStatus())) {
                    logger.debug("Server at {} is in online, sending notification to create container...", instanceInfo.getLocation());
                    try {
                        String uri = instanceInfo.getLocation() + "/containers/" + containerId;
                        makeHttpDeleteRequestAndCreateCustomResponse(uri, null, getUser(), getPassword());
                    } catch (Throwable e) {
                        logger.error("Unable to notify kie server instance at {} about deleted container {} due to {}"
                                , instanceInfo.getLocation(), containerId, e.getMessage(), e );
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected <T> T makeHttpPutRequestAndCreateCustomResponse(String uri, String body, Class<T> resultType, String user, String password) {
        logger.debug("About to send PUT request to '{}' with payload '{}'", uri, body);
        KieRemoteHttpRequest request = newRequest( uri, user, password ).body(body).put();
        KieRemoteHttpResponse response = request.response();

        if ( response.code() == Response.Status.CREATED.getStatusCode() ||
                response.code() == Response.Status.BAD_REQUEST.getStatusCode() ) {
            T serviceResponse = deserialize( response.body(), resultType );

            return serviceResponse;
        } else {
            throw new IllegalStateException( "Error while sending PUT request to " + uri + " response code " + response.code() );
        }
    }

    @SuppressWarnings("unchecked")
    protected <T> T makeHttpDeleteRequestAndCreateCustomResponse(String uri, Class<T> resultType, String user, String password) {
        logger.debug("About to send DELETE request to '{}' ", uri);
        KieRemoteHttpRequest request = newRequest( uri, user, password ).delete();
        KieRemoteHttpResponse response = request.response();

        if ( response.code() == Response.Status.OK.getStatusCode() ||
                response.code() == Response.Status.BAD_REQUEST.getStatusCode() ) {
            T serviceResponse = deserialize( response.body(), resultType);

            return serviceResponse;
        } else {
            throw new IllegalStateException( "Error while sending PUT request to " + uri + " response code " + response.code() );
        }
    }

    private KieRemoteHttpRequest newRequest(String uri, String userName, String password) {

        KieRemoteHttpRequest httpRequest = KieRemoteHttpRequest.newRequest(uri).followRedirects(true).timeout(5000);
        httpRequest.accept(MediaType.APPLICATION_JSON);
        httpRequest.basicAuthorization(userName, password);

        return httpRequest;

    }

    private <T> T deserialize(String content, Class<T> type) {
        if (type == null) {
            return null;
        }

        try {
            return MarshallerFactory.getMarshaller(MarshallingFormat.JSON, this.getClass().getClassLoader()).unmarshall(content, type);
        } catch ( MarshallingException e ) {
            throw new IllegalStateException( "Error while deserializing data received from server!", e );
        }
    }

    protected String serialize(Object object) {
        if (object == null) {
            return "";
        }

        try {
            return MarshallerFactory.getMarshaller(MarshallingFormat.JSON, this.getClass().getClassLoader()).marshall(object);
        } catch ( MarshallingException e ) {
            throw new IllegalStateException( "Error while serializing request data!", e );
        }
    }
}

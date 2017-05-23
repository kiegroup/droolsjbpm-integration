/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.controller.service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.kie.server.controller.service.ControllerUtils.*;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.kie.server.api.model.KieServerInfo;
import org.kie.server.controller.api.KieServerControllerException;
import org.kie.server.controller.api.KieServerControllerNotFoundException;
import org.kie.server.controller.api.model.KieServerInstance;
import org.kie.server.controller.api.model.KieServerInstanceInfo;
import org.kie.server.controller.api.model.KieServerInstanceList;
import org.kie.server.controller.api.model.KieServerSetup;
import org.kie.server.controller.api.model.KieServerStatus;
import org.kie.server.controller.api.model.runtime.Container;
import org.kie.server.controller.api.model.runtime.ServerInstanceKey;
import org.kie.server.controller.api.model.spec.Capability;
import org.kie.server.controller.api.model.spec.ContainerConfig;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.api.model.spec.ContainerSpecKey;
import org.kie.server.controller.api.model.spec.ContainerSpecList;
import org.kie.server.controller.api.model.spec.ProcessConfig;
import org.kie.server.controller.api.model.spec.RuleConfig;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.api.model.spec.ServerTemplateKey;
import org.kie.server.controller.api.model.spec.ServerTemplateList;
import org.kie.server.controller.api.service.RuntimeManagementService;
import org.kie.server.controller.impl.KieServerControllerImpl;
import org.kie.server.controller.impl.KieServerInstanceManager;
import org.kie.server.controller.impl.service.RuntimeManagementServiceImpl;
import org.kie.server.controller.impl.service.SpecManagementServiceImpl;
import org.kie.server.controller.impl.storage.FileBasedKieServerTemplateStorage;
import org.kie.server.controller.impl.storage.InMemoryKieServerControllerStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/standalone")
public class StandaloneController extends KieServerControllerImpl {
    private static Logger logger = LoggerFactory.getLogger(StandaloneController.class);
    private static KieServerInstanceManager instanceManager = KieServerInstanceManager.getInstance();
    private static InMemoryKieServerControllerStorage kieServerControllerStorage = InMemoryKieServerControllerStorage.getInstance();
    private static FileBasedKieServerTemplateStorage templateStorage = FileBasedKieServerTemplateStorage.getInstance();
    private static final String REQUEST_FAILED_ERR = "Request failed to be processed due to ";
    private static final String STOP_CONTAINER = "stopContainer";
    private static final String START_CONTAINER = "startContainer";
    private RuntimeManagementServiceImpl runtimeManagementService;
    private SpecManagementServiceImpl specManagementService;


    public StandaloneController() {
        super();
        String templateStorageLocation = System.getProperty(FileBasedKieServerTemplateStorage.SERVER_TEMPLATE_FILE_NAME_PROP);
        if (templateStorageLocation == null || templateStorageLocation.trim().isEmpty()) {
            logger.warn("Template storage file name is not set - System property {} is not found or is empty",
                        FileBasedKieServerTemplateStorage.SERVER_TEMPLATE_FILE_NAME_PROP);
            logger.warn("Controller will attempt to use {} to store server templates",
                        FileBasedKieServerTemplateStorage.DEFAULT_SERVER_TEMPLATE_FILENAME);
        } else {
            templateStorage.setTemplatesLocation(templateStorageLocation);
            logger.debug("Server templates will be stored in {}",templateStorageLocation);
        }
        // Override the default value for template storage (an InMemory... version)
        super.setTemplateStorage(this.templateStorage);

        // Create the instance of RuntimeManagementService that will be used, and override the default values
        // for template storage and the server instance manager
        runtimeManagementService = new RuntimeManagementServiceImpl();
        runtimeManagementService.setTemplateStorage(this.templateStorage);
        runtimeManagementService.setKieServerInstanceManager(instanceManager);

        // Create the instance of SpecManagementServiceImpl that will be used, and override the default values
        // for template storage and the server instance manager
        specManagementService = new SpecManagementServiceImpl();
        specManagementService.setTemplateStorage(templateStorage);
        specManagementService.setKieServerInstanceManager(instanceManager);
    }


    @PUT
    @Path("server/{id}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response connectKieServer(@Context HttpHeaders headers, @PathParam("id")String id, String serverPayload) {
        String contentType = getContentType(headers);
        KieServerInfo serverInfo = unmarshal(serverPayload, contentType, KieServerInfo.class);

        // Create the connection and marshal the resultant KieServerSetup
        KieServerSetup setup = super.connect(serverInfo);
        String responseString = marshal(contentType,setup);

        // Save the server instance in the InMemoryKieServerControllerStorage
        KieServerInstanceInfo instanceInfo = new KieServerInstanceInfo(serverInfo.getLocation(),
                                                                       KieServerStatus.UNKNOWN,serverInfo.getCapabilities());
        Set<KieServerInstanceInfo> infoSet = new HashSet<>();
        infoSet.add(instanceInfo);
        KieServerInstance instance = new KieServerInstance(serverInfo.getServerId(),serverInfo.getName(),serverInfo.getVersion(),infoSet,KieServerStatus.UNKNOWN,setup);
        kieServerControllerStorage.store(instance);

        return createCorrectVariant(responseString, headers,Response.Status.CREATED);
    }

    @PUT
    @Path("server/{id}/containers")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response saveContainer(@Context HttpHeaders headers, @PathParam("id")String id, String containerSpec) {
        Response response = null;
        String contentType = getContentType(headers);
        ContainerSpec spec = unmarshal(containerSpec, contentType, ContainerSpec.class);
        try {
            specManagementService.saveContainerSpec(id, spec);
            response = createCorrectVariant("", headers, Response.Status.CREATED);
        } catch (KieServerControllerNotFoundException e) {
            logger.error("Kie Server Controller not found while saving container",e);
            response = createCorrectVariant(e.getMessage(), headers, Response.Status.NOT_FOUND);
        } catch (KieServerControllerException e) {
            logger.error("Kie Server Controller exception while saving container",e);
            response = createCorrectVariant(REQUEST_FAILED_ERR+e.getMessage(), headers, Response.Status.BAD_REQUEST);
        } catch (Throwable t) {
            logger.error("Unknown error occurred while saving container",t);
            response = createCorrectVariant("Unknown error has occurred", headers, Response.Status.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    @DELETE
    @Path("server/{serverTemplateId}/container/{containerSpecId}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response deleteContainer(@Context HttpHeaders headers, @PathParam("serverTemplateId")String serverTemplateId,
                                    @PathParam("containerSpecId")String containerSpecId) {
        Response response = null;
        try {
            specManagementService.deleteContainerSpec(serverTemplateId, containerSpecId);
            response = Response.noContent().build();
        } catch (KieServerControllerNotFoundException e){
            response = createCorrectVariant(e.getMessage(),
                                        headers,
                                        Response.Status.NOT_FOUND);
        } catch (KieServerControllerException e) {
            response = createCorrectVariant(REQUEST_FAILED_ERR+e.getMessage(), headers, Response.Status.BAD_REQUEST);
        } catch (Throwable t) {
            response = createCorrectVariant("Unknown error has occurred", headers, Response.Status.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    @DELETE
    @Path("server/{id}")
    public Response disposeKieServer(@Context HttpHeaders headers, @PathParam("id")String id, @QueryParam("location")String serverLocation) {
        KieServerInfo kieServerInfo = null;
        try {
            kieServerInfo = new KieServerInfo(id, "", "", Collections.<String>emptyList(), URLDecoder.decode(serverLocation, "UTF-8"));
            super.disconnect(kieServerInfo);
            logger.info("Kie server {} disconnected",kieServerInfo.getServerId());
        } catch (UnsupportedEncodingException e) {
            logger.error("Unable to URL decode kie server location",e);
        }
        return Response.noContent().build();
    }

    @GET
    @Path("server/{id}/containers")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response listContainers(@Context HttpHeaders headers, @PathParam("id")String id) {
        Response response = null;
        String contentType = getContentType(headers);
        try {
            Collection<ContainerSpec> containerSpecs = specManagementService.listContainerSpec(id);
            if (containerSpecs != null && !containerSpecs.isEmpty()) {
                ContainerSpecList specList = new ContainerSpecList(containerSpecs);
                String responseString = marshal(contentType,
                                                specList);
                response = createCorrectVariant(responseString,
                                                headers,
                                                Response.Status.OK);
            } else {
                response = Response.noContent().build();
            }
        } catch (KieServerControllerNotFoundException e){
            response = createCorrectVariant(e.getMessage(),
                                            headers,
                                            Response.Status.NOT_FOUND);
        } catch (KieServerControllerException e) {
            response = createCorrectVariant(REQUEST_FAILED_ERR+e.getMessage(), headers, Response.Status.BAD_REQUEST);
        } catch (Throwable t) {
            response = createCorrectVariant("Unknown error has occurred", headers, Response.Status.INTERNAL_SERVER_ERROR);
        }

        return response;
    }

    @GET
    @Path("servers")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response listServerTemplates(@Context HttpHeaders headers) {
        Response response = null;
        String contentType = getContentType(headers);
        try {
            Collection<ServerTemplate> templates = specManagementService.listServerTemplates();
            if (templates != null && !templates.isEmpty()) {
                ServerTemplateList templateList = new ServerTemplateList(templates);
                String responseString = marshal(contentType,templateList);
                response = createCorrectVariant(responseString, headers, Response.Status.OK);
            } else {
                response = Response.noContent().build();
            }
        } catch (KieServerControllerNotFoundException e){
            response = createCorrectVariant(e.getMessage(),
                                            headers,
                                            Response.Status.NOT_FOUND);
        } catch (KieServerControllerException e) {
            response = createCorrectVariant(REQUEST_FAILED_ERR+e.getMessage(), headers, Response.Status.BAD_REQUEST);
        } catch (Throwable t) {
            response = createCorrectVariant("Unknown error has occurred", headers, Response.Status.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    @POST
    @Path("server/{serverTemplateId}/container/{containerId}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response updateContainerSpec(@Context HttpHeaders headers,
                                        @PathParam("serverTemplateId")String serverTemplateId,
                                        @PathParam("containerId")String containerId,
                                        String newContainerSpec) {
        Response response = null;
        String contentType = getContentType(headers);
        try {
            ContainerSpec newSpec = unmarshal(newContainerSpec,contentType,ContainerSpec.class);
            specManagementService.updateContainerSpec(serverTemplateId,containerId,newSpec);
            logger.debug("ContainerSpec updated on container {}",containerId);
            response = createCorrectVariant("", headers, Response.Status.ACCEPTED);
        } catch (KieServerControllerNotFoundException e){
            response = createCorrectVariant(e.getMessage(),
                                            headers,
                                            Response.Status.NOT_FOUND);
        } catch (KieServerControllerException e) {
            response = createCorrectVariant(REQUEST_FAILED_ERR+e.getMessage(), headers, Response.Status.BAD_REQUEST);
        } catch (Throwable t) {
            response = createCorrectVariant("Unknown error has occurred", headers, Response.Status.INTERNAL_SERVER_ERROR);
        }

        return response;
    }

    @POST
    @Path("server/{serverTemplateId}/container/{containerSpecId}/config/{capability}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response updateContainerConfig(@Context HttpHeaders headers,
                                          @PathParam("serverTemplateId")String serverTemplateId,
                                          @PathParam("containerSpecId")String containerSpecId,
                                          @PathParam("capability")String capability,
                                          String newContainerConfig) {
        Response response = null;
        String contentType = getContentType(headers);
        try {
            Capability cap = Capability.valueOf(capability);
            ContainerConfig containerConfig = null;
            switch (cap) {
                case PROCESS:
                    containerConfig = unmarshal(newContainerConfig, contentType, ProcessConfig.class);
                    break;
                case RULE:
                    containerConfig = unmarshal(newContainerConfig, contentType, RuleConfig.class);
                    break;
                default:
                    response = createCorrectVariant("Unsupported configuration type: "+cap.name(), headers, Response.Status.BAD_REQUEST);
            }
            if (containerConfig != null) {
                specManagementService.updateContainerConfig(serverTemplateId,
                                                            containerSpecId,
                                                            cap,
                                                            containerConfig);
                response = createCorrectVariant("", headers, Response.Status.ACCEPTED);
            }
        } catch (IllegalArgumentException e) {
            response = createCorrectVariant("Illegal value for capability: "+capability,
                                            headers,
                                            Response.Status.BAD_REQUEST);
        } catch (KieServerControllerNotFoundException e){
            response = createCorrectVariant(e.getMessage(),
                                            headers,
                                            Response.Status.NOT_FOUND);
        } catch (KieServerControllerException e) {
            response = createCorrectVariant(REQUEST_FAILED_ERR+e.getMessage(), headers, Response.Status.BAD_REQUEST);
        } catch (Throwable t) {
            response = createCorrectVariant("Unknown error has occurred", headers, Response.Status.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    @POST
    @Path("server/{serverTemplateId}/container/{containerId}/status/started")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response startContainer(@Context HttpHeaders headers,
                                   @PathParam("serverTemplateId") String serverTemplateId,
                                   @PathParam("containerId") String containerId) {
        return takeContainerAction(serverTemplateId, containerId, headers, START_CONTAINER);
    }

    @POST
    @Path("server/{serverTemplateId}/container/{containerId}/status/stopped")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response stopContainer(@Context HttpHeaders headers,
                                   @PathParam("serverTemplateId") String serverTemplateId,
                                   @PathParam("containerId") String containerId) {
        return takeContainerAction(serverTemplateId, containerId, headers, STOP_CONTAINER);
    }

    /**
     * This method abstracts out all of the duplicated functionality between the startContainer and stopContainer methods
     * @param serverTemplateId The id of the serverTemplate that holds the container we want to start/stop
     * @param containerId The id of the container that is to be started/stopped
     * @param headers HttpHeaders to get the content type accepted for return
     * @param action The action (start or stop) that is to be executed
     * @return The Response that is to be returned
     */
    private Response takeContainerAction(String serverTemplateId, String containerId, HttpHeaders headers, String action) {
        Response response = null;
        String contentType = getContentType(headers);
        try {
            ContainerSpecKey containerSpecKey = new ContainerSpecKey();
            containerSpecKey.setId(containerId);
            containerSpecKey.setServerTemplateKey(new ServerTemplateKey(serverTemplateId,
                                                                        ""));
            if (action.equals(START_CONTAINER)) {
                specManagementService.startContainer(containerSpecKey);
            } else {
                specManagementService.stopContainer(containerSpecKey);
            }
            response = createCorrectVariant("", headers, Response.Status.OK);
        } catch (KieServerControllerNotFoundException e){
            response = createCorrectVariant(e.getMessage(),
                                            headers,
                                            Response.Status.NOT_FOUND);
        } catch (KieServerControllerException e) {
            response = createCorrectVariant(REQUEST_FAILED_ERR+e.getMessage(), headers, Response.Status.BAD_REQUEST);
        } catch (Throwable t) {
            response = createCorrectVariant("Unknown error has occurred", headers, Response.Status.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

}

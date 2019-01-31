/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.controller.client.rest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.kie.server.api.KieServerConstants;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingException;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.common.rest.Authenticator;
import org.kie.server.controller.api.model.KieServerInstance;
import org.kie.server.controller.api.model.KieServerInstanceInfo;
import org.kie.server.controller.api.model.KieServerInstanceList;
import org.kie.server.controller.api.model.KieServerSetup;
import org.kie.server.controller.api.model.KieServerStatus;
import org.kie.server.controller.api.model.runtime.Container;
import org.kie.server.controller.api.model.runtime.ContainerKey;
import org.kie.server.controller.api.model.runtime.ContainerList;
import org.kie.server.controller.api.model.runtime.ServerInstance;
import org.kie.server.controller.api.model.runtime.ServerInstanceKey;
import org.kie.server.controller.api.model.runtime.ServerInstanceKeyList;
import org.kie.server.controller.api.model.spec.*;
import org.kie.server.controller.client.KieServerControllerClient;
import org.kie.server.controller.client.exception.KieServerControllerHTTPClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestKieServerControllerClient implements KieServerControllerClient {

    private static Logger logger = LoggerFactory.getLogger(RestKieServerControllerClient.class);

    private static final String MANAGEMENT_LAST_URI_PART = "/management/servers";
    private static final String CONTAINERS_LAST_URI_PART = "/containers";
    private static final String RUNTIME_LAST_URI_PART = "/runtime/servers";
    private static final String INSTANCES_LAST_URI_PART = "/instances";

    private static final String MANAGEMENT_URI_PART = MANAGEMENT_LAST_URI_PART + "/";
    private static final String CONTAINERS_URI_PART = CONTAINERS_LAST_URI_PART + "/";
    private static final String RUNTIME_URI_PART = RUNTIME_LAST_URI_PART + "/";
    private static final String INSTANCES_URI_PART = INSTANCES_LAST_URI_PART + "/";

    private static final String STARTED_STATUS_URI_PART = "/status/started";
    private static final String STOPPED_STATUS_URI_PART = "/status/stopped";
    private static final String ACTIVATED_STATUS_URI_PART = "/status/activated";
    private static final String DEACTIVATED_STATUS_URI_PART = "/status/deactivated";
    private static final String CONFIG_URI_PART = "/config/";

    private String controllerBaseUrl;
    private static final MarshallingFormat DEFAULT_MARSHALLING_FORMAT = MarshallingFormat.JAXB;
    private MarshallingFormat format;
    private Client httpClient;
    protected Marshaller marshaller;

    public RestKieServerControllerClient(String controllerBaseUrl, String login, String password) {
        this(controllerBaseUrl, login, password, DEFAULT_MARSHALLING_FORMAT);
    }

    public RestKieServerControllerClient(String controllerBaseUrl, String login, String password, MarshallingFormat format) {
        this(controllerBaseUrl, login, password, format, null);
    }

    public RestKieServerControllerClient(String controllerBaseUrl, String login, String password, MarshallingFormat format, Configuration configuration) {
        this.controllerBaseUrl = controllerBaseUrl;
        httpClient = (configuration == null ? ClientBuilder.newClient() : ClientBuilder.newClient(configuration) ).register(new Authenticator(login, password));
        setMarshallingFormat(format);
    }

    @Override
    public ServerTemplate getServerTemplate(String serverTemplateId) {
        return makeGetRequestAndCreateCustomResponse(controllerBaseUrl + MANAGEMENT_URI_PART + serverTemplateId, ServerTemplate.class);
    }

    @Override
    public void saveContainerSpec(String serverTemplateId, ContainerSpec containerSpec ) {
        makePutRequestAndCreateCustomResponse(controllerBaseUrl + MANAGEMENT_URI_PART + serverTemplateId + CONTAINERS_URI_PART + containerSpec.getId(), containerSpec, Object.class);
    }

    @Override
    public void updateContainerSpec(String serverTemplateId, ContainerSpec containerSpec ) {
        updateContainerSpec(serverTemplateId, containerSpec.getId(), containerSpec);
    }

    @Override
    public void updateContainerSpec(String serverTemplateId, String containerId, ContainerSpec containerSpec) {
        updateContainerSpec(serverTemplateId, containerId, containerSpec, false);
    }

    @Override
    public void updateContainerSpec(String serverTemplateId, String containerId, ContainerSpec containerSpec, Boolean resetBeforeUpdate) {

        Map<String, Object> params = new HashMap<>();
        params.put(KieServerConstants.RESET_CONTAINER_BEFORE_UPDATE, resetBeforeUpdate);

        makePostRequestAndCreateCustomResponse(controllerBaseUrl + MANAGEMENT_URI_PART + serverTemplateId + CONTAINERS_URI_PART + containerId, containerSpec, Object.class, params);
    }

    @Override
    public void saveServerTemplate(ServerTemplate serverTemplate) {
        makePutRequestAndCreateCustomResponse(controllerBaseUrl + MANAGEMENT_URI_PART + serverTemplate.getId(), serverTemplate, Object.class);
    }

    @Override
    public void deleteServerTemplate(String serverTemplateId) {
        makeDeleteRequest(controllerBaseUrl + MANAGEMENT_URI_PART + serverTemplateId);
    }

    @Override
    public ContainerSpec getContainerInfo(String serverTemplateId, String containerId) {
        return makeGetRequestAndCreateCustomResponse(controllerBaseUrl + MANAGEMENT_URI_PART + serverTemplateId + CONTAINERS_URI_PART + containerId, ContainerSpec.class);
    }

    @Override
    public void deleteContainerSpec(String serverTemplateId, String containerId) {
        makeDeleteRequest(controllerBaseUrl + MANAGEMENT_URI_PART + serverTemplateId + CONTAINERS_URI_PART + containerId);
    }

    @Override
    public ServerTemplateList listServerTemplates() {
        return makeGetRequestAndCreateCustomResponse(controllerBaseUrl + MANAGEMENT_LAST_URI_PART, ServerTemplateList.class);
    }

    @Override
    public ContainerSpecList listContainerSpec(String serverTemplateId) {
        return makeGetRequestAndCreateCustomResponse(controllerBaseUrl + MANAGEMENT_URI_PART + serverTemplateId + CONTAINERS_LAST_URI_PART, ContainerSpecList.class);
    }

    @Override
    public void startContainer(ContainerSpecKey containerSpecKey) {
        makePostRequestAndCreateCustomResponse(controllerBaseUrl + MANAGEMENT_URI_PART + containerSpecKey.getServerTemplateKey().getId() + CONTAINERS_URI_PART + containerSpecKey.getId() + STARTED_STATUS_URI_PART, "", null);
    }

    @Override
    public void stopContainer(ContainerSpecKey containerSpecKey) {
        makePostRequestAndCreateCustomResponse(controllerBaseUrl + MANAGEMENT_URI_PART + containerSpecKey.getServerTemplateKey().getId() + CONTAINERS_URI_PART + containerSpecKey.getId() + STOPPED_STATUS_URI_PART, "", null);
    }
    
    @Override
    public void activateContainer(ContainerSpecKey containerSpecKey) {
        makePostRequestAndCreateCustomResponse(controllerBaseUrl + MANAGEMENT_URI_PART + containerSpecKey.getServerTemplateKey().getId() + CONTAINERS_URI_PART + containerSpecKey.getId() + ACTIVATED_STATUS_URI_PART, "", null);
    }

    @Override
    public void deactivateContainer(ContainerSpecKey containerSpecKey) {
        makePostRequestAndCreateCustomResponse(controllerBaseUrl + MANAGEMENT_URI_PART + containerSpecKey.getServerTemplateKey().getId() + CONTAINERS_URI_PART + containerSpecKey.getId() + DEACTIVATED_STATUS_URI_PART, "", null);
    }

    @Override
    public void updateContainerConfig(String serverTemplateId, String containerId, Capability capability, ContainerConfig config) {
        makePostRequestAndCreateCustomResponse(controllerBaseUrl + MANAGEMENT_URI_PART + serverTemplateId + CONTAINERS_URI_PART + containerId + CONFIG_URI_PART + capability.toString(), config, Object.class);
    }

    private <T> T throwUnsupportedException(){
        throw new UnsupportedOperationException("Not supported for REST implementation");
    }

    @Override
    public ServerTemplateKeyList listServerTemplateKeys() {
        return throwUnsupportedException();
    }

    @Override
    public void copyServerTemplate(String serverTemplateId,
                                   String newServerTemplateId,
                                   String newServerTemplateName) {
        throwUnsupportedException();
    }

    @Override
    public void updateServerTemplateConfig(String serverTemplateId,
                                           Capability capability,
                                           ServerConfig serverTemplateConfig) {
        throwUnsupportedException();
    }

    @Override
    public void scanNow(ContainerSpecKey containerSpecKey) {
        throwUnsupportedException();
    }

    @Override
    public void startScanner(ContainerSpecKey containerSpecKey,
                             Long interval) {
        throwUnsupportedException();
    }

    @Override
    public ServerInstanceKeyList getServerInstances(String serverTemplateId) {
        return makeGetRequestAndCreateCustomResponse(controllerBaseUrl + RUNTIME_URI_PART + serverTemplateId + INSTANCES_URI_PART, ServerInstanceKeyList.class);
    }

    @Override
    public void stopScanner(ContainerSpecKey containerSpecKey) {
        throwUnsupportedException();
    }

    @Override
    public ContainerList getContainers(ServerInstanceKey serverInstanceKey) {
        return makeGetRequestAndCreateCustomResponse(controllerBaseUrl + RUNTIME_URI_PART + serverInstanceKey.getServerTemplateId() + INSTANCES_URI_PART + serverInstanceKey.getServerInstanceId() + CONTAINERS_URI_PART, ContainerList.class);
    }

    @Override
    public ContainerList getContainers(ServerTemplate serverTemplate,
                                       ContainerSpec containerSpec) {
        return makeGetRequestAndCreateCustomResponse(controllerBaseUrl + RUNTIME_URI_PART + serverTemplate.getId() + CONTAINERS_URI_PART + containerSpec.getId() + INSTANCES_LAST_URI_PART, ContainerList.class);
    }

    @Override
    public void upgradeContainer(ContainerSpecKey containerSpecKey,
                                 ReleaseId releaseId) {
        throwUnsupportedException();
    }

    @Override
    public void deleteServerInstance(ServerInstanceKey serverInstanceKey) {
        throwUnsupportedException();
    }

    private <T> T makeGetRequestAndCreateCustomResponse(String uri, Class<T> resultType) {
        WebTarget clientRequest = httpClient.target(uri);
        Response response;

        response = clientRequest.request(getMediaType(format)).get();

        try {
            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                return deserialize(response,
                                   resultType);
            } else {
                throw createExceptionForUnexpectedResponseCode(clientRequest,
                                                               response);
            }
        } finally {
            response.close();
        }
    }

    private void makeDeleteRequest(String uri) {
        WebTarget clientRequest = httpClient.target(uri);

        Response response;
        try {
            response = clientRequest.request(getMediaType(format)).delete();
        } catch (Exception e) {
            throw createExceptionForUnexpectedFailure(clientRequest,
                                                      e);
        }

        try {
            if (response.getStatus() != Response.Status.NO_CONTENT.getStatusCode()) {
                throw createExceptionForUnexpectedResponseCode(clientRequest,
                                                               response);
            }
        } finally {
            response.close();
        }
    }

    private <T> T makePutRequestAndCreateCustomResponse(String uri, Object bodyObject, Class<T> resultType) {
        WebTarget clientRequest = httpClient.target(uri);

        Response response;
        try {
            Entity<String> requestEntity = Entity.entity(serialize(bodyObject), getMediaType(format));
            response = clientRequest.request(getMediaType(format)).put(requestEntity);
        } catch (Exception e) {
            throw createExceptionForUnexpectedFailure(clientRequest, e);
        }

        try {
            if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
                return deserialize(response,
                                   resultType);
            } else {
                throw createExceptionForUnexpectedResponseCode(clientRequest,
                                                               response);
            }
        } finally {
            response.close();
        }
    }

    private <T> T makePostRequestAndCreateCustomResponse(String uri, Object bodyObject, Class<T> resultType) {
        return makePostRequestAndCreateCustomResponse(uri, bodyObject, resultType, new HashMap<>());
    }

    private <T> T makePostRequestAndCreateCustomResponse(String uri, Object bodyObject, Class<T> resultType, Map<String, Object> params) {
        WebTarget clientRequest = httpClient.target(uri);

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            clientRequest = clientRequest.queryParam(entry.getKey(), entry.getValue());
        }

        Response response;
        try {
            Entity<String> requestEntity = Entity.entity(serialize(bodyObject), getMediaType(format));
            response = clientRequest.request(getMediaType(format)).post(requestEntity);
        } catch (Exception e) {
            throw createExceptionForUnexpectedFailure(clientRequest, e);
        }

        try {
            if (response.getStatus() == Response.Status.CREATED.getStatusCode() ||
                    response.getStatus() == Response.Status.OK.getStatusCode()) {
                return deserialize(response,
                                   resultType);
            } else {
                throw createExceptionForUnexpectedResponseCode(clientRequest,
                                                               response);
            }
        } finally {
            response.close();
        }
    }

    private RuntimeException createExceptionForUnexpectedResponseCode(WebTarget request,
                                                                      Response response) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("Unexpected HTTP response code when requesting URI '");
        stringBuffer.append(getClientRequestUri(request));
        stringBuffer.append("'! Response code: ");
        stringBuffer.append(response.getStatus());
        try {
            String responseEntity = response.readEntity(String.class);
            stringBuffer.append(" Response message: ");
            stringBuffer.append(responseEntity);
        } catch (IllegalStateException e) {
            logger.warn("Error trying to read response entity: {}", e.getMessage(), e);
        }

        logger.debug(stringBuffer.toString());
        return new KieServerControllerHTTPClientException(response.getStatus(), stringBuffer.toString());
    }

    private RuntimeException createExceptionForUnexpectedFailure(WebTarget request, Exception e) {
        String summaryMessage = "Unexpected exception when requesting URI '" + getClientRequestUri(request) + "'!";
        logger.debug(summaryMessage);
        return new RuntimeException(summaryMessage, e);
    }

    private String getClientRequestUri(WebTarget clientRequest) {
        try {
            return clientRequest.getUri().toString();
        } catch (Exception e) {
            throw new RuntimeException("Malformed client URL was specified!", e);
        }
    }

    public void close() {
        try {
            httpClient.close();
        } catch (Exception e) {
            logger.error("Exception thrown while closing resources!", e);
        }
    }

    public MarshallingFormat getMarshallingFormat() {
        return format;
    }

    public void setMarshallingFormat(MarshallingFormat format) {
        this.format = format;
        Set<Class<?>> controllerClasses = new HashSet<Class<?>>();
        controllerClasses.add(KieServerInstance.class);
        controllerClasses.add(KieServerInstanceList.class);
        controllerClasses.add(KieServerInstanceInfo.class);
        controllerClasses.add(KieServerSetup.class);
        controllerClasses.add(KieServerStatus.class);

        controllerClasses.add(ServerInstance.class);
        controllerClasses.add(ServerInstanceKey.class);
        controllerClasses.add(ServerInstanceKeyList.class);
        controllerClasses.add(ServerTemplate.class);
        controllerClasses.add(ServerTemplateKey.class);
        controllerClasses.add(ServerConfig.class);
        controllerClasses.add(RuleConfig.class);
        controllerClasses.add(ProcessConfig.class);
        controllerClasses.add(ContainerSpec.class);
        controllerClasses.add(ContainerSpecKey.class);
        controllerClasses.add(Container.class);
        controllerClasses.add(ContainerList.class);
        controllerClasses.add(ContainerKey.class);
        controllerClasses.add(ServerTemplateList.class);
        controllerClasses.add(ContainerSpecList.class);

        switch ( format ) {
            case JAXB:
                this.marshaller = MarshallerFactory.getMarshaller(controllerClasses, format, RestKieServerControllerClient.class.getClassLoader());
                break;
            case JSON:
                this.marshaller = MarshallerFactory.getMarshaller(null, format, RestKieServerControllerClient.class.getClassLoader());
                break;
            default:
                this.marshaller = MarshallerFactory.getMarshaller(controllerClasses, format, RestKieServerControllerClient.class.getClassLoader());
        }

    }

    private String getMediaType( MarshallingFormat format ) {
        switch ( format ) {
            case JAXB: return MediaType.APPLICATION_XML;
            case JSON: return MediaType.APPLICATION_JSON;
            default: return MediaType.APPLICATION_XML;
        }
    }

    protected String serialize(Object object) {
        if (object == null) {
            return "";
        }

        try {
            return marshaller.marshall( object );
        } catch ( MarshallingException e ) {
            throw new RuntimeException( "Error while serializing request data!", e );
        }
    }

    protected <T> T deserialize(Response response, Class<T> type) {
        try {
            if(type == null) {
                return null;
            }
            String content = response.readEntity(String.class);
            logger.debug("About to deserialize content: \n '{}' \n into type: '{}'", content, type);
            if (content == null || content.isEmpty()) {
                return null;
            }

            return deserialize(content, type);
        } catch ( MarshallingException e ) {
            throw new RuntimeException( "Error while deserializing data received from server!", e );
        }
    }

    protected <T> T deserialize(String content, Class<T> type) {
        return marshaller.unmarshall( content, type );
    }
}

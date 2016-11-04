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

package org.kie.server.services.impl.controller;

import java.net.URLEncoder;
import java.util.Set;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.kie.server.common.rest.KieServerHttpRequest;
import org.kie.server.common.rest.KieServerHttpResponse;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.KieServerEnvironment;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingException;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieServerConfig;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.controller.api.KieServerController;
import org.kie.server.controller.api.model.KieServerSetup;
import org.kie.server.services.api.KieControllerNotConnectedException;
import org.kie.server.services.api.KieControllerNotDefinedException;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.storage.KieServerState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultRestControllerImpl implements KieServerController {

    private static final Logger logger = LoggerFactory.getLogger(DefaultRestControllerImpl.class);

    private final KieServerRegistry context;

    public DefaultRestControllerImpl(KieServerRegistry context) {
        this.context = context;
    }

    @SuppressWarnings("unchecked")
    protected <T> T makeHttpPutRequestAndCreateCustomResponse(String uri, String body, Class<T> resultType, String user, String password, String token) {
        logger.debug("About to send PUT request to '{}' with payload '{}'", uri, body);
        KieServerHttpRequest request = newRequest( uri, user, password, token ).body(body).put();
        KieServerHttpResponse response = request.response();

        if ( response.code() == Response.Status.CREATED.getStatusCode() ||
                response.code() == Response.Status.BAD_REQUEST.getStatusCode() ) {
            T serviceResponse = deserialize( response.body(), resultType );

            return serviceResponse;
        } else {
            throw new IllegalStateException( "Error while sending PUT request to " + uri + " response code " + response.code() );
        }
    }

    @SuppressWarnings("unchecked")
    protected <T> T makeHttpPostRequestAndCreateCustomResponse(String uri, String body, Class<T> resultType, String user, String password, String token) {
        logger.debug("About to send POST request to '{}' with payload '{}'", uri, body);
        KieServerHttpRequest request = newRequest( uri, user, password, token ).body(body).post();
        KieServerHttpResponse response = request.response();

        if ( response.code() == Response.Status.CREATED.getStatusCode() ||
                response.code() == Response.Status.BAD_REQUEST.getStatusCode() ||
                response.code() == Response.Status.OK.getStatusCode()) {
            T serviceResponse = deserialize( response.body(), resultType );

            return serviceResponse;
        } else {
            throw new IllegalStateException( "Error while sending POST request to " + uri + " response code " + response.code() );
        }
    }

    @SuppressWarnings("unchecked")
    protected <T> T makeHttpDeleteRequestAndCreateCustomResponse(String uri, Class<T> resultType, String user, String password, String token) {
        logger.debug("About to send DELETE request to '{}' ", uri);
        KieServerHttpRequest request = newRequest( uri, user, password, token ).delete();
        KieServerHttpResponse response = request.response();

        if ( response.code() == Response.Status.OK.getStatusCode() ||
                response.code() == Response.Status.NO_CONTENT.getStatusCode() ) {
            T serviceResponse = deserialize( response.body(), resultType);

            return serviceResponse;
        } else {
            throw new IllegalStateException( "Error while sending DELETE request to " + uri + " response code " + response.code() );
        }
    }

    private KieServerHttpRequest newRequest(String uri, String userName, String password, String token) {

        KieServerHttpRequest httpRequest = KieServerHttpRequest.newRequest(uri).followRedirects(true).timeout(5000);
        httpRequest.accept(MediaType.APPLICATION_JSON);
        if (token != null && !token.isEmpty()) {
            httpRequest.tokenAuthorization(token);
        } else {
            httpRequest.basicAuthorization(userName, password);
        }

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

    @Override
    public KieServerSetup connect(KieServerInfo serverInfo) {

        KieServerState currentState = context.getStateRepository().load(KieServerEnvironment.getServerId());
        Set<String> controllers = currentState.getControllers();

        KieServerConfig config = currentState.getConfiguration();
        if (controllers != null && !controllers.isEmpty()) {
            for (String controllerUrl : controllers) {

                if (controllerUrl != null && !controllerUrl.isEmpty()) {
                    String connectAndSyncUrl = controllerUrl + "/server/" + KieServerEnvironment.getServerId();

                    String userName = config.getConfigItemValue(KieServerConstants.CFG_KIE_CONTROLLER_USER, "kieserver");
                    String password = config.getConfigItemValue(KieServerConstants.CFG_KIE_CONTROLLER_PASSWORD, "kieserver1!");
                    String token = config.getConfigItemValue(KieServerConstants.CFG_KIE_CONTROLLER_TOKEN);

                    try {
                        KieServerSetup kieServerSetup = makeHttpPutRequestAndCreateCustomResponse(connectAndSyncUrl, serialize(serverInfo), KieServerSetup.class, userName, password, token);

                        if (kieServerSetup != null) {
                            // once there is non null list let's return it
                            return kieServerSetup;

                        }

                        break;
                    } catch (Exception e) {
                        // let's check all other controllers in case of running in cluster of controllers
                        logger.warn("Exception encountered while syncing with controller at {} error {}", connectAndSyncUrl, e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
                        logger.debug("Exception encountered while syncing with controller at {} error {}", connectAndSyncUrl, e.getMessage(), e);
                    }

                }
            }

            throw new KieControllerNotConnectedException("Unable to connect to any controller");
        } else {
            throw new KieControllerNotDefinedException("Unable to connect to any controller");
        }
    }

    @Override
    public void disconnect(KieServerInfo serverInfo) {
        KieServerState currentState = context.getStateRepository().load(KieServerEnvironment.getServerId());
        Set<String> controllers = currentState.getControllers();

        KieServerConfig config = currentState.getConfiguration();

        for (String controllerUrl : controllers ) {

            if (controllerUrl != null && !controllerUrl.isEmpty()) {
                String connectAndSyncUrl = null;
                try {
                    connectAndSyncUrl = controllerUrl + "/server/" + KieServerEnvironment.getServerId()+"/?location="+ URLEncoder.encode(serverInfo.getLocation(), "UTF-8");

                    String userName = config.getConfigItemValue(KieServerConstants.CFG_KIE_CONTROLLER_USER, "kieserver");
                    String password = config.getConfigItemValue(KieServerConstants.CFG_KIE_CONTROLLER_PASSWORD, "kieserver1!");
                    String token = config.getConfigItemValue(KieServerConstants.CFG_KIE_CONTROLLER_TOKEN);

                    makeHttpDeleteRequestAndCreateCustomResponse(connectAndSyncUrl, null, userName, password, token);


                    break;
                } catch (Exception e) {
                    // let's check all other controllers in case of running in cluster of controllers
                    logger.debug("Exception encountered while syncing with controller at {} error {}", connectAndSyncUrl, e.getMessage(), e);
                }

            }
        }
    }

    public void startContainer(String containerId) {

        KieServerState currentState = context.getStateRepository().load(KieServerEnvironment.getServerId());
        Set<String> controllers = currentState.getControllers();

        KieServerConfig config = currentState.getConfiguration();
        if (controllers != null && !controllers.isEmpty()) {
            for (String controllerUrl : controllers) {

                if (controllerUrl != null && !controllerUrl.isEmpty()) {
                    String connectAndSyncUrl = controllerUrl + "/management/servers/" + KieServerEnvironment.getServerId() + "/containers/" + containerId + "/status/started";

                    String userName = config.getConfigItemValue(KieServerConstants.CFG_KIE_CONTROLLER_USER, "kieserver");
                    String password = config.getConfigItemValue(KieServerConstants.CFG_KIE_CONTROLLER_PASSWORD, "kieserver1!");
                    String token = config.getConfigItemValue(KieServerConstants.CFG_KIE_CONTROLLER_TOKEN);

                    try {
                        makeHttpPostRequestAndCreateCustomResponse(connectAndSyncUrl, "", null, userName, password, token);
                        break;
                    } catch (Exception e) {
                        // let's check all other controllers in case of running in cluster of controllers
                        logger.warn("Exception encountered while syncing with controller at {} error {}", connectAndSyncUrl, e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
                        logger.debug("Exception encountered while syncing with controller at {} error {}", connectAndSyncUrl, e.getMessage(), e);
                    }

                }
            }

        }
    }

    public void stopContainer(String containerId) {

        KieServerState currentState = context.getStateRepository().load(KieServerEnvironment.getServerId());
        Set<String> controllers = currentState.getControllers();

        KieServerConfig config = currentState.getConfiguration();
        if (controllers != null && !controllers.isEmpty()) {
            for (String controllerUrl : controllers) {

                if (controllerUrl != null && !controllerUrl.isEmpty()) {
                    String connectAndSyncUrl = controllerUrl + "/management/servers/" + KieServerEnvironment.getServerId() + "/containers/" + containerId + "/status/stopped";

                    String userName = config.getConfigItemValue(KieServerConstants.CFG_KIE_CONTROLLER_USER, "kieserver");
                    String password = config.getConfigItemValue(KieServerConstants.CFG_KIE_CONTROLLER_PASSWORD, "kieserver1!");
                    String token = config.getConfigItemValue(KieServerConstants.CFG_KIE_CONTROLLER_TOKEN);

                    try {
                        makeHttpPostRequestAndCreateCustomResponse(connectAndSyncUrl, "", null, userName, password, token);
                        break;
                    } catch (Exception e) {
                        // let's check all other controllers in case of running in cluster of controllers
                        logger.warn("Exception encountered while syncing with controller at {} error {}", connectAndSyncUrl, e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
                        logger.debug("Exception encountered while syncing with controller at {} error {}", connectAndSyncUrl, e.getMessage(), e);
                    }

                }
            }
        }
    }
}

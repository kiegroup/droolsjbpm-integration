/*
 * Copyright 2015 JBoss Inc
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

package org.kie.server.services.impl;

import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.kie.remote.common.rest.KieRemoteHttpRequest;
import org.kie.remote.common.rest.KieRemoteHttpResponse;
import org.kie.server.api.KieController;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.KieServerEnvironment;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingException;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieServerConfig;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.controller.api.KieServerController;
import org.kie.server.controller.api.model.KieServerSetup;
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
                response.code() == Response.Status.NO_CONTENT.getStatusCode() ) {
            T serviceResponse = deserialize( response.body(), resultType);

            return serviceResponse;
        } else {
            throw new IllegalStateException( "Error while sending DELETE request to " + uri + " response code " + response.code() );
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

    @Override
    public KieServerSetup connect(KieServerInfo serverInfo) {

        KieServerState currentState = context.getStateRepository().load(KieServerEnvironment.getServerId());
        Set<String> controllers = currentState.getControllers();

        KieServerConfig config = currentState.getConfiguration();

        for (String controllerUrl : controllers ) {

            if (controllerUrl != null && !controllerUrl.isEmpty()) {
                String connectAndSyncUrl = controllerUrl + "/" + KieServerEnvironment.getServerId();

                String userName = config.getConfigItemValue(KieServerConstants.CFG_KIE_CONTROLLER_USER, "kieserver");
                String password = config.getConfigItemValue(KieServerConstants.CFG_KIE_CONTROLLER_PASSWORD, "kieserver1!");

                try {
                    KieServerSetup kieServerSetup = makeHttpPutRequestAndCreateCustomResponse(connectAndSyncUrl, serialize(serverInfo), KieServerSetup.class, userName, password);

                    if (kieServerSetup != null) {
                        // once there is non null list let's return it
                        return kieServerSetup;

                    }

                    break;
                } catch (Exception e) {
                    // let's check all other controllers in case of running in cluster of controllers
                    logger.warn("Exception encountered while syncing with controller at {} error {}", connectAndSyncUrl, e.getMessage());
                    logger.debug("Exception encountered while syncing with controller at {} error {}", connectAndSyncUrl, e.getMessage(), e);
                }

            }
        }

        return new KieServerSetup();
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
                    connectAndSyncUrl = controllerUrl + "/controller/server/" + KieServerEnvironment.getServerId()+"/?location="+ URLEncoder.encode(serverInfo.getLocation(), "UTF-8");

                    String userName = config.getConfigItemValue(KieServerConstants.CFG_KIE_CONTROLLER_USER, "kieserver");
                    String password = config.getConfigItemValue(KieServerConstants.CFG_KIE_CONTROLLER_PASSWORD, "kieserver1!");


                    makeHttpDeleteRequestAndCreateCustomResponse(connectAndSyncUrl, null, userName, password);


                    break;
                } catch (Exception e) {
                    // let's check all other controllers in case of running in cluster of controllers
                    logger.debug("Exception encountered while syncing with controller at {} error {}", connectAndSyncUrl, e.getMessage(), e);
                }

            }
        }
    }
}

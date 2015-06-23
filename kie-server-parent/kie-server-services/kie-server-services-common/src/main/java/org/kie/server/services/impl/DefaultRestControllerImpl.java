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

import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.kie.remote.common.rest.KieRemoteHttpRequest;
import org.kie.remote.common.rest.KieRemoteHttpResponse;
import org.kie.server.api.KieController;
import org.kie.server.api.KieServerEnvironment;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingException;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultRestControllerImpl implements KieController {

    private static final Logger logger = LoggerFactory.getLogger(DefaultRestControllerImpl.class);

    @Override
    public Set<KieContainerResource> getContainers(Set<String> controllers, String serverId) {

        for (String controllerUrl : controllers ) {

            if (controllerUrl != null && !controllerUrl.isEmpty()) {
                String connectAndSyncUrl = controllerUrl + "/controller/server/" + KieServerEnvironment.getServerId();

                try {
                    KieContainerResourceList containerResourceList = makeHttpGetRequestAndCreateServiceResponse(connectAndSyncUrl, KieContainerResourceList.class);

                    if (containerResourceList != null) {
                        // once there is non null list let's return it
                        return new HashSet<KieContainerResource>(containerResourceList.getContainers());

                    }

                    break;
                } catch (Exception e) {
                    // let's check all other controllers in case of running in cluster of controllers
                    logger.debug("Exception encountered while syncing with controller at {} error {}", connectAndSyncUrl, e.getMessage(), e);
                }

            }
        }

        return null;
    }

    private <T> T makeHttpGetRequestAndCreateServiceResponse(String uri, Class<T> resultType) {
        try {
            KieRemoteHttpRequest request = newRequest( uri ).get();
            KieRemoteHttpResponse response = request.response();

            if ( response.code() == Response.Status.OK.getStatusCode() ) {
                KieContainerResourceList serviceResponse = deserialize( response.body(), KieContainerResourceList.class );

                return (T)serviceResponse;
            } else {
                throw new IllegalStateException("No response from controller server at " + uri);
            }

        } catch (IllegalStateException e){
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("No response from controller server at " + uri);
        }
    }

    private KieRemoteHttpRequest newRequest(String uri) {

        KieRemoteHttpRequest httpRequest = KieRemoteHttpRequest.newRequest(uri).followRedirects(true).timeout(5000);
        httpRequest.accept(MediaType.APPLICATION_JSON);
        httpRequest.basicAuthorization(KieServerEnvironment.getUsername(), KieServerEnvironment.getPassword());

        return httpRequest;

    }

    private <T> T deserialize(String content, Class<T> type) {
        try {
            return MarshallerFactory.getMarshaller(MarshallingFormat.JSON, this.getClass().getClassLoader()).unmarshall(content, type);
        } catch ( MarshallingException e ) {
            throw new IllegalStateException( "Error while deserializing data received from server!", e );
        }
    }
}

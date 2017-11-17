/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.controller.management.client;

import java.util.Map;

import javax.ws.rs.core.Configuration;

import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.controller.management.client.rest.RestKieServerMgmtControllerClient;

public class KieServerMgmtControllerClientFactory {

    private KieServerMgmtControllerClientFactory() {
    }

    /**
     * Creates a new Kie Controller Management Client using REST based service
     * @param controllerUrl the URL to the server (e.g.: "http://localhost:8080")
     * @param login user login
     * @param password user password
     * @return client instance
     */
    public static KieServerMgmtControllerClient newRestClient(final String controllerUrl,
                                                              final String login,
                                                              final String password) {
        return new RestKieServerMgmtControllerClient(controllerUrl,
                                                     login,
                                                     password);
    }

    /**
     * Creates a new Kie Controller Management Client using REST based service
     * @param controllerUrl the URL to the server (e.g.: "http://localhost:8080")
     * @param login user login
     * @param password user password
     * @param format marshaling format
     * @return client instance
     */
    public static KieServerMgmtControllerClient newRestClient(final String controllerUrl,
                                                              final String login,
                                                              final String password,
                                                              final MarshallingFormat format) {
        return new RestKieServerMgmtControllerClient(controllerUrl,
                                                     login,
                                                     password,
                                                     format);
    }

    /**
     * Creates a new Kie Controller Management Client using REST based service
     * @param controllerUrl the URL to the server (e.g.: "http://localhost:8080")
     * @param login user login
     * @param password user password
     * @param format marshaling format
     * @param configuration REST client configuration
     * @return client instance
     */
    public static KieServerMgmtControllerClient newRestClient(final String controllerUrl,
                                                              final String login,
                                                              final String password,
                                                              final MarshallingFormat format,
                                                              final Configuration configuration) {
        return new RestKieServerMgmtControllerClient(controllerUrl,
                                                     login,
                                                     password,
                                                     format,
                                                     configuration);
    }
}

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

package org.kie.server.gateway;

import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ServiceResponse;

public class KieServerGateway {

    private final ResteasyClient client;

    public KieServerGateway(String username, String password, Integer connectionTimeout, Integer socketTimeout) {

        client = new ResteasyClientBuilder()
            .connectionPoolSize(1)
            .establishConnectionTimeout(connectionTimeout, TimeUnit.SECONDS)
            .socketTimeout(socketTimeout, TimeUnit.SECONDS)
            .register(new Authenticator(username, password))
            .register(new ErrorResponseFilter())
            .build();

    }

    public KieContainerResource getContainer(String serverUrl, String container) {

        ServiceResponse<KieContainerResource> response = client.target(serverUrl)
            .path("containers")
            .path(container)
            .request(MediaType.APPLICATION_JSON)
            .get(ServiceResponse.class);

        return response.getResult();

    }

    public void close() {

        client.close();

    }

}

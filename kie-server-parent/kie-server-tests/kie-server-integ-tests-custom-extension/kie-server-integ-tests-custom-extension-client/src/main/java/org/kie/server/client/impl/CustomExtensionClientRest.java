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

package org.kie.server.client.impl;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.drools.core.runtime.impl.ExecutionResultImpl;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.kie.api.runtime.ExecutionResults;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.client.CustomExtensionClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.common.rest.Authenticator;

public class CustomExtensionClientRest implements CustomExtensionClient {

    private final KieServicesConfiguration config;
    private final Marshaller marshaller;
    private final Client httpClient;

    public CustomExtensionClientRest(KieServicesConfiguration configuration, ClassLoader classLoader) {
        this.config = configuration.clone();
        this.marshaller = MarshallerFactory.getMarshaller(configuration.getExtraClasses(), configuration.getMarshallingFormat(), classLoader);

        httpClient = new ResteasyClientBuilder()
                .establishConnectionTimeout(configuration.getTimeout(), TimeUnit.MILLISECONDS)
                .socketTimeout(configuration.getTimeout(), TimeUnit.MILLISECONDS)
                .register(new Authenticator(configuration.getUserName(), configuration.getPassword()))
                .build();
    }

    @Override
    public ExecutionResults insertFireReturn(String containerId, String kieSession, List<Object> listOfFacts) {
        String requestBody = marshaller.marshall(listOfFacts);

        Entity<?> entity = Entity.entity(requestBody, getMediaType(config.getMarshallingFormat()));
        String url = getCustomEndpointUrl(containerId, kieSession);

        Response response = httpClient.target(url)
                .request(getMediaType(config.getMarshallingFormat()))
                .header(KieServerConstants.KIE_CONTENT_TYPE_HEADER, config.getMarshallingFormat())
                .post(entity);

        String marshalledResponse = response.readEntity(String.class);
        ExecutionResults result = marshaller.unmarshall(marshalledResponse, ExecutionResultImpl.class);
        return result;
    }

    private String getCustomEndpointUrl(String containerId, String kieSession) {
        StringBuilder sb = new StringBuilder();
        sb.append(config.getServerUrl());
        sb.append("/containers/instances/");
        sb.append(containerId);
        sb.append("/ksession/");
        sb.append(kieSession);
        return sb.toString();
    }

    private String getMediaType( MarshallingFormat format ) {
        switch ( format ) {
            case JAXB: return MediaType.APPLICATION_XML;
            case JSON: return MediaType.APPLICATION_JSON;
            default: return MediaType.APPLICATION_XML;
        }
    }

    @Override
    public void close() {
        httpClient.close();
    }
}

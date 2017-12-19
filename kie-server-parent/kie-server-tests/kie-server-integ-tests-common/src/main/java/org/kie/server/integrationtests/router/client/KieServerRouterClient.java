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

package org.kie.server.integrationtests.router.client;

import java.io.StringReader;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.kie.server.controller.management.client.exception.KieServerControllerHTTPClientException;
import org.kie.server.router.Configuration;
import org.kie.server.router.repository.ConfigurationMarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KieServerRouterClient implements AutoCloseable {

    private static Logger logger = LoggerFactory.getLogger(KieServerRouterClient.class);

    private static final String MANAGEMENT_LIST_URI_PART = "/mgmt/list";

    private String routerBaseUrl;
    private Client httpClient;
    private String mediaType = MediaType.APPLICATION_JSON;
    private ConfigurationMarshaller marshaller = new ConfigurationMarshaller();

    public KieServerRouterClient(String routerBaseUrl) {
        this.routerBaseUrl = routerBaseUrl;
        httpClient = new ResteasyClientBuilder()
                .establishConnectionTimeout(10, TimeUnit.SECONDS)
                .socketTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    public Configuration getRouterConfig() {
        return makeGetRequestAndCreateCustomResponse(routerBaseUrl + MANAGEMENT_LIST_URI_PART);
    }

    @Override
    public void close() {
        try {
            httpClient.close();
        } catch (Exception e) {
            logger.error("Exception thrown while closing resources!", e);
        }
    }

    private Configuration makeGetRequestAndCreateCustomResponse(String uri) {
        WebTarget clientRequest = httpClient.target(uri);
        Response response;

        response = clientRequest.request(mediaType).get();

        if ( response.getStatus() == Response.Status.OK.getStatusCode() ) {
            return deserialize(response);
        } else {
            throw createExceptionForUnexpectedResponseCode( clientRequest, response );
        }
    }

    private RuntimeException createExceptionForUnexpectedResponseCode(
            WebTarget request,
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
            response.close();
            // Exception while reading response - most probably empty response and closed input stream
        }

        logger.debug( stringBuffer.toString());
        return new KieServerControllerHTTPClientException(response.getStatus(), stringBuffer.toString());
    }

    private String getClientRequestUri(WebTarget clientRequest) {
        String uri;
        try {
            uri = clientRequest.getUri().toString();
        } catch (Exception e) {
            throw new RuntimeException("Malformed client URL was specified!", e);
        }
        return uri;
    }

    private Configuration deserialize(Response response) {
        try {
            String content = response.readEntity(String.class);
            logger.debug("About to deserialize content: \n '{}'", content);
            if (content == null || content.isEmpty()) {
                return null;
            }

            return marshaller.unmarshall(new StringReader(content));
        } catch ( Exception e ) {
            throw new RuntimeException( "Error while deserializing data received from server!", e );
        } finally {
            response.close();
        }
    }
}

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

package org.kie.server.integrationtests.drools.rest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.drools.core.command.impl.GenericCommand;
import org.drools.core.command.runtime.BatchExecutionCommandImpl;
import org.drools.core.command.runtime.rule.FireAllRulesCommand;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.ClientResponseFailure;
import org.jboss.resteasy.util.GenericType;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.api.KieServices;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.internal.runtime.helper.BatchExecutionHelper;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.integrationtests.category.RESTOnly;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.RestOnlyBaseIntegrationTest;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;

@Category(RESTOnly.class)
public class RestMalformedRequestIntegrationTest extends RestOnlyBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "state-is-kept-for-stateful-session",
            "1.0.0-SNAPSHOT");

    private static final String CONTAINER_ID = "stateful-session";

    @BeforeClass
    public static void deployArtifacts() {
        buildAndDeployCommonMavenParent();
        buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/state-is-kept-for-stateful-session").getFile());

    }

    @Test
    public void testCreateContainerNonExistingGAV2() throws Exception {
        KieContainerResource resource = new KieContainerResource("no-gav2-container",
                new ReleaseId("foo", "bar", "0.0.0"));

        ClientResponse<ServiceResponse<KieContainerResource>> response = null;
        try {
            ClientRequest clientRequest = newRequest(TestConfig.getKieServerHttpUrl() + "/containers/" + resource.getContainerId());
            response = clientRequest.body(
                    getMediaType(), resource).accept(getMediaType()).put(
                    new GenericType<ServiceResponse<KieContainerResource>>() {
                    });
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            response.releaseConnection();
        } catch (Exception e) {
            throw new ClientResponseFailure(
                    "Unexpected exception creating container: " + resource.getContainerId() + " with release-id " + resource.getReleaseId(),
                    e, response);
        }
    }

    @Test
    public void testCreateContainerEmptyBody() throws Exception {
        ClientResponse<ServiceResponse<KieContainerResource>> response = null;
        try {
            ClientRequest clientRequest = newRequest(TestConfig.getKieServerHttpUrl() + "/containers/empty-body-container");
            response = clientRequest.body(
                    getMediaType(), "").accept(getMediaType()).put(
                    new GenericType<ServiceResponse<KieContainerResource>>() {
                    });
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            response.releaseConnection();
        } catch (Exception e) {
            throw new ClientResponseFailure("Unexpected exception on empty body", e, response);
        }
    }

    @Test
    public void testInvalidCommandBodyOnCallContainer() throws Exception {
        Marshaller marshaller = MarshallerFactory.getMarshaller(marshallingFormat, this.getClass().getClassLoader());
        client.createContainer(CONTAINER_ID, new KieContainerResource(CONTAINER_ID, releaseId));

        ClientResponse<ServiceResponse<KieContainerResource>> response = null;
        try {
            // empty commands can be considered as invalid request
            String body = marshaller.marshall(new BatchExecutionCommandImpl());

            ClientRequest clientRequest = newRequest(TestConfig.getKieServerHttpUrl() + "/containers/instances/" + CONTAINER_ID);
            response = clientRequest.body(
                    getMediaType(), body).accept(getMediaType()).post(
                    new GenericType<ServiceResponse<KieContainerResource>>() {
                    });
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

            ServiceResponse serviceResponse = response.getEntity();
            assertEquals(ServiceResponse.ResponseType.FAILURE, serviceResponse.getType());
            assertEquals("Bad request, no commands to be executed - either wrong format or no data", serviceResponse.getMsg());
            response.releaseConnection();
        } catch (Exception e) {
            throw new ClientResponseFailure("Unexpected exception on empty body", e, response);
        }
    }

    @Test
    public void testInvalidBodyOnCallContainer() throws Exception {

        client.createContainer(CONTAINER_ID, new KieContainerResource(CONTAINER_ID, releaseId));

        ClientResponse<ServiceResponse<KieContainerResource>> response = null;
        try {
            // empty commands can be considered as invalid request
            String body = "invalid content that cannot be parsed";

            MediaType mediaType = getMediaType();

            ClientRequest clientRequest = newRequest(TestConfig.getKieServerHttpUrl() + "/containers/instances/" + CONTAINER_ID);
            response = clientRequest.body(
                    mediaType, body).accept(mediaType).post(
                    new GenericType<ServiceResponse<KieContainerResource>>() {
                    });
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

            ServiceResponse serviceResponse = response.getEntity();
            assertEquals(ServiceResponse.ResponseType.FAILURE, serviceResponse.getType());
            switch (marshallingFormat) {
                case JAXB:
                    assertEquals("Error calling container stateful-session: Can't unmarshall input string: invalid content that cannot be parsed", serviceResponse.getMsg());
                    break;
                case JSON:
                    assertEquals("Error calling container stateful-session: Error unmarshalling input", serviceResponse.getMsg());
                    break;
            }
            response.releaseConnection();
        } catch (Exception e) {
            throw new ClientResponseFailure("Unexpected exception on empty body", e, response);
        }
    }

}

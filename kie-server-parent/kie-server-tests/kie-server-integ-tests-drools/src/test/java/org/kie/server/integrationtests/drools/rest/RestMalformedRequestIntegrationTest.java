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

import static org.junit.Assert.assertEquals;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.drools.core.command.runtime.BatchExecutionCommandImpl;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.integrationtests.category.RESTOnly;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.basetests.RestOnlyBaseIntegrationTest;

@Category(RESTOnly.class)
public class RestMalformedRequestIntegrationTest extends RestOnlyBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "state-is-kept-for-stateful-session",
            "1.0.0-SNAPSHOT");

    private static final String CONTAINER_ID = "stateful-session";

    @BeforeClass
    public static void deployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/state-is-kept-for-stateful-session").getFile());

        createContainer(CONTAINER_ID, releaseId);
    }

    @Test
    public void testInvalidCommandBodyOnCallContainer() throws Exception {
        Marshaller marshaller = MarshallerFactory.getMarshaller(marshallingFormat, this.getClass().getClassLoader());

        Response response = null;
        try {
            // empty commands can be considered as invalid request
            String body = marshaller.marshall(new BatchExecutionCommandImpl());

            WebTarget clientRequest = newRequest(TestConfig.getKieServerHttpUrl() + "/containers/instances/" + CONTAINER_ID);
            response = clientRequest.request(getMediaType()).post(createEntity(body));
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

            ServiceResponse<KieContainerResource> serviceResponse =
                    response.readEntity(new GenericType<ServiceResponse<KieContainerResource>>(){});
            assertEquals(ServiceResponse.ResponseType.FAILURE, serviceResponse.getType());
            assertEquals("Bad request, no commands to be executed - either wrong format or no data", serviceResponse.getMsg());
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception on empty body", e);
        } finally {
            if(response != null) {
                response.close();
            }
        }
    }

    @Test
    public void testInvalidBodyOnCallContainer() throws Exception {

        Response response = null;
        try {
            // empty commands can be considered as invalid request
            String body = "invalid content that cannot be parsed";

            WebTarget clientRequest = newRequest(TestConfig.getKieServerHttpUrl() + "/containers/instances/" + CONTAINER_ID);
            response = clientRequest.request(getMediaType()).post(createEntity(body));
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

            ServiceResponse<KieContainerResource> serviceResponse =
                    response.readEntity(new GenericType<ServiceResponse<KieContainerResource>>(){});
            assertEquals(ServiceResponse.ResponseType.FAILURE, serviceResponse.getType());
            switch (marshallingFormat) {
                case JAXB:
                    assertEquals("Error calling container stateful-session: Can't unmarshall input string: invalid content that cannot be parsed", serviceResponse.getMsg());
                    break;
                case JSON:
                    assertEquals("Error calling container stateful-session: Error unmarshalling input", serviceResponse.getMsg());
                    break;
            }
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception on invalid body", e);
        } finally {
            if(response != null) {
                response.close();
            }
        }
    }

}

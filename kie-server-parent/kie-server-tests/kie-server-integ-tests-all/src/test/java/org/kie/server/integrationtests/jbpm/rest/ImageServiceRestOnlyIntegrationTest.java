/*
 * Copyright 2016 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.server.integrationtests.jbpm.rest;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.ClientResponseFailure;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.type.JaxbLong;
import org.kie.server.api.rest.RestURI;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.jbpm.DBExternalResource;
import org.kie.server.integrationtests.shared.RestOnlyBaseIntegrationTest;

import static org.junit.Assert.*;
import static org.kie.server.api.rest.RestURI.*;
import org.kie.server.client.KieServicesConfiguration;

public class ImageServiceRestOnlyIntegrationTest extends RestOnlyBaseIntegrationTest{

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.0.Final");

    private static final String CONTAINER_ID = "definition-project";
    private static final String HIRING_PROCESS_ID = "hiring";

    private ClientResponse<?> response = null;

    @ClassRule
    public static ExternalResource StaticResource = new DBExternalResource();

    @BeforeClass
    public static void buildAndDeployArtifacts() {

        buildAndDeployCommonMavenParent();
        buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project").getFile());

    }

    @Override
    protected void additionalConfiguration(KieServicesConfiguration configuration) {
        configuration.setTimeout(30000);
    }

    @Before
    public void cleanup() {
        cleanupSingletonSessionId();

    }

    @After
    public void releaseConnection() {
        if (response != null) {
            response.releaseConnection();
        }
    }

    @Test
    public void testGetProcessImageTest() throws Exception {
        KieContainerResource resource = new KieContainerResource(CONTAINER_ID, releaseId);
        assertSuccess(client.createContainer(CONTAINER_ID, resource));

        Map<String, Object> valuesMap = new HashMap<String, Object>();
        valuesMap.put(RestURI.CONTAINER_ID, resource.getContainerId());
        valuesMap.put(RestURI.PROCESS_ID, HIRING_PROCESS_ID);

        try {
            ClientRequest clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), IMAGE_URI + "/" + PROCESS_IMG_GET_URI, valuesMap))
                    .header("Content-Type", getMediaType().toString())
                    .header("Accept", MediaType.APPLICATION_SVG_XML);
            logger.info("[GET] " + clientRequest.getUri());

            response = clientRequest.get();
            Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

            String result = response.getEntity(String.class);
            logger.debug("Image content is '{}'", result);
            assertNotNull(result);
            assertFalse(result.isEmpty());


        } catch (Exception e) {
            throw new ClientResponseFailure(e, response);
        }
    }

    @Test
    public void testGetProcessInstanceImageTest() throws Exception {
        KieContainerResource resource = new KieContainerResource(CONTAINER_ID, releaseId);
        assertSuccess(client.createContainer(CONTAINER_ID, resource));

        Map<String, Object> valuesMap = new HashMap<String, Object>();
        valuesMap.put(RestURI.CONTAINER_ID, resource.getContainerId());
        valuesMap.put(RestURI.PROCESS_ID, HIRING_PROCESS_ID);

        Marshaller marshaller = MarshallerFactory.getMarshaller(marshallingFormat, ClassLoader.getSystemClassLoader());

        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("name", "john");

            // start process instance
            ClientRequest clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), PROCESS_URI + "/" + START_PROCESS_POST_URI, valuesMap))
                    .header("Content-Type", getMediaType().toString())
                    .header("Accept", getMediaType().toString())
                    .body(getMediaType(), marshaller.marshall(params));
            logger.info("[POST] " + clientRequest.getUri());
            response = clientRequest.post();
            Assert.assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

            Long result = response.getEntity(JaxbLong.class).unwrap();
            assertNotNull(result);

            valuesMap.put(RestURI.PROCESS_INST_ID, result);

            clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), IMAGE_URI + "/" + PROCESS_INST_IMG_GET_URI, valuesMap))
                    .header("Content-Type", getMediaType().toString())
                    .header("Accept", MediaType.APPLICATION_SVG_XML);
            logger.info("[GET] " + clientRequest.getUri());

            response = clientRequest.get();
            Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

            String image = response.getEntity(String.class);
            logger.debug("Image content is '{}'", image);
            assertNotNull(image);
            assertFalse(image.isEmpty());

            clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), PROCESS_URI + "/" + ABORT_PROCESS_INST_DEL_URI, valuesMap)).header("Content-Type", getMediaType().toString());
            logger.info("[DELETE] " + clientRequest.getUri());
            response = clientRequest.delete();
            Assert.assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());

        } catch (Exception e) {
            throw new ClientResponseFailure(e, response);
        }
    }

    @Test
    public void testGetProcessImageNotExistingTest() throws Exception {
        KieContainerResource resource = new KieContainerResource(CONTAINER_ID, releaseId);
        assertSuccess(client.createContainer(CONTAINER_ID, resource));

        Map<String, Object> valuesMap = new HashMap<String, Object>();
        valuesMap.put(RestURI.CONTAINER_ID, resource.getContainerId());
        valuesMap.put(RestURI.PROCESS_ID, "not-existing");

        try {
            ClientRequest clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), IMAGE_URI + "/" + PROCESS_IMG_GET_URI, valuesMap))
                    .header("Content-Type", getMediaType().toString())
                    .header("Accept", MediaType.APPLICATION_SVG_XML);
            logger.info("[GET] " + clientRequest.getUri());

            response = clientRequest.get();
            Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());

        } catch (Exception e) {
            throw new ClientResponseFailure(e, response);
        }
    }

    @Test
    public void testGetProcessInstanceImageNotExistingTest() throws Exception {
        KieContainerResource resource = new KieContainerResource(CONTAINER_ID, releaseId);
        assertSuccess(client.createContainer(CONTAINER_ID, resource));

        Map<String, Object> valuesMap = new HashMap<String, Object>();
        valuesMap.put(RestURI.CONTAINER_ID, resource.getContainerId());
        valuesMap.put(RestURI.PROCESS_INST_ID, 9999);

        try {
            ClientRequest clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), IMAGE_URI + "/" + PROCESS_INST_IMG_GET_URI, valuesMap))
                    .header("Content-Type", getMediaType().toString())
                    .header("Accept", MediaType.APPLICATION_SVG_XML);
            logger.info("[GET] " + clientRequest.getUri());

            response = clientRequest.get();
            Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());

        } catch (Exception e) {
            throw new ClientResponseFailure(e, response);
        }
    }
}

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
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.api.model.instance.TaskSummaryList;
import org.kie.server.api.model.type.JaxbLong;
import org.kie.server.api.rest.RestURI;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.jbpm.DBExternalResource;

import org.kie.server.integrationtests.shared.KieServerDeployer;

public class FormServiceRestOnlyIntegrationTest extends RestOnlyBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.0.Final");

    private static final String CONTAINER_ID = "definition-project";
    private static final String HIRING_PROCESS_ID = "hiring";

    private ClientResponse<?> response = null;

    @ClassRule
    public static ExternalResource StaticResource = new DBExternalResource();

    @BeforeClass
    public static void buildAndDeployArtifacts() {

        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project").getFile());

        createContainer(CONTAINER_ID, releaseId);
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
    public void testGetProcessFormTest() throws Exception {
        Map<String, Object> valuesMap = new HashMap<String, Object>();
        valuesMap.put(RestURI.CONTAINER_ID, CONTAINER_ID);
        valuesMap.put(RestURI.PROCESS_ID, HIRING_PROCESS_ID);

        try {
            ClientRequest clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), FORM_URI + "/" + PROCESS_FORM_GET_URI, valuesMap))
                    .header("Content-Type", getMediaType().toString())
                    .header("Accept", getMediaType().toString());
            logger.info("[GET] " + clientRequest.getUri());

            response = clientRequest.get();
            Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

            String result = response.getEntity(String.class);
            logger.debug("Form content is '{}'", result);
            assertNotNull(result);
            assertFalse(result.isEmpty());

        } catch (Exception e) {
            throw new ClientResponseFailure(e, response);
        }
    }

    @Test
    public void testGetTaskFormTest() throws Exception {
        Map<String, Object> valuesMap = new HashMap<String, Object>();
        valuesMap.put(RestURI.CONTAINER_ID, CONTAINER_ID);
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

            // find tasks by process instance id
            valuesMap.put(RestURI.PROCESS_INST_ID, result);

            clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), QUERY_URI + "/" + TASK_BY_PROCESS_INST_ID_GET_URI, valuesMap))
                    .header("Content-Type", getMediaType().toString())
                    .header("Accept", getMediaType().toString());
            logger.info("[GET] " + clientRequest.getUri());

            response = clientRequest.get();
            Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

            TaskSummaryList taskSummaryList = marshaller.unmarshall(response.getEntity(String.class), TaskSummaryList.class);
            logger.debug("Form content is '{}'", taskSummaryList);

            assertNotNull(taskSummaryList);
            TaskSummary[] task = taskSummaryList.getTasks();
            assertEquals(1, task.length);

            Long taskId = task[0].getId();

            valuesMap.put(RestURI.TASK_INSTANCE_ID, taskId);

            clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), FORM_URI + "/" + TASK_FORM_GET_URI, valuesMap))
                    .header("Content-Type", getMediaType().toString())
                    .header("Accept", getMediaType().toString());
            logger.info("[GET] " + clientRequest.getUri());

            response = clientRequest.get();
            Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

            String formdata = response.getEntity(String.class);
            logger.debug("Form content is '{}'", formdata);
            assertNotNull(formdata);
            assertFalse(formdata.isEmpty());

            clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), PROCESS_URI + "/" + ABORT_PROCESS_INST_DEL_URI, valuesMap)).header("Content-Type", getMediaType().toString());
            logger.info("[DELETE] " + clientRequest.getUri());

            response = clientRequest.delete();
            int noContentStatusCode = Response.Status.NO_CONTENT.getStatusCode();
            int okStatusCode = Response.Status.OK.getStatusCode();
            assertTrue("Wrong status code returned: " + response.getStatus(),
                    response.getStatus() == noContentStatusCode || response.getStatus() == okStatusCode);

        } catch (Exception e) {
            throw new ClientResponseFailure(e, response);
        }
    }

    @Test
    public void testGetProcessDoesNotExistFormTest() throws Exception {
        Map<String, Object> valuesMap = new HashMap<String, Object>();
        valuesMap.put(RestURI.CONTAINER_ID, CONTAINER_ID);
        valuesMap.put(RestURI.PROCESS_ID, "not-existing");
        valuesMap.put(RestURI.TASK_INSTANCE_ID, 99999);

        try {
            ClientRequest clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), FORM_URI + "/" + TASK_FORM_GET_URI, valuesMap))
                    .header("Content-Type", getMediaType().toString())
                    .header("Accept", getMediaType().toString());
            logger.info("[GET] " + clientRequest.getUri());

            response = clientRequest.get();
            Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());

        } catch (Exception e) {
            throw new ClientResponseFailure(e, response);
        }
    }

    @Test
    public void testGetTaskDoesNotExistFormTest() throws Exception {
        Map<String, Object> valuesMap = new HashMap<String, Object>();
        valuesMap.put(RestURI.CONTAINER_ID, CONTAINER_ID);
        valuesMap.put(RestURI.PROCESS_ID, "not-existing");

        try {
            ClientRequest clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), FORM_URI + "/" + PROCESS_FORM_GET_URI, valuesMap))
                    .header("Content-Type", getMediaType().toString())
                    .header("Accept", getMediaType().toString());
            logger.info("[GET] " + clientRequest.getUri());

            response = clientRequest.get();
            Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());

        } catch (Exception e) {
            throw new ClientResponseFailure(e, response);
        }
    }
}

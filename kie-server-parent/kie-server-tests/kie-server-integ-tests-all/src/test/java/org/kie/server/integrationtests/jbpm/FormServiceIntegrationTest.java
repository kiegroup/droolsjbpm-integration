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

package org.kie.server.integrationtests.jbpm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.ClientResponseFailure;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.kie.api.KieServices;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.api.model.instance.TaskSummaryList;
import org.kie.server.api.model.type.JaxbLong;
import org.kie.server.api.rest.RestURI;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.RestOnlyBaseIntegrationTest;

import static org.junit.Assert.*;
import static org.kie.server.api.rest.RestURI.*;

public class FormServiceIntegrationTest extends RestOnlyBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.0.Final");

    private static final String CONTAINER_ID = "definition-project";

    private static final String HIRING_PROCESS_ID = "hiring";

    @ClassRule
    public static ExternalResource StaticResource = new DBExternalResource();

    @BeforeClass
    public static void buildAndDeployArtifacts() {

        buildAndDeployCommonMavenParent();
        buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project").getFile());

    }

    @Before
    public void cleanup() {
        cleanupSingletonSessionId();

    }

    @Test
    public void testGetProcessFormTest() throws Exception {
        KieContainerResource resource = new KieContainerResource(CONTAINER_ID, releaseId);
        assertSuccess(client.createContainer(CONTAINER_ID, resource));

        Map<String, Object> valuesMap = new HashMap<String, Object>();
        valuesMap.put(RestURI.CONTAINER_ID, resource.getContainerId());
        valuesMap.put(RestURI.PROCESS_ID, HIRING_PROCESS_ID);

        ClientResponse<?> response = null;
        try {

            ClientRequest clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), FORM_URI + "/" + PROCESS_FORM_URI, valuesMap))
                    .header("Content-Type", getMediaType().toString())
                    .header("Accept", getMediaType().toString());
            logger.info( "[GET] " + clientRequest.getUri());

            response = clientRequest.get();
            Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

            Object result = response.getEntity(String.class);
            logger.debug("Form content is '{}'", result);

            response.releaseConnection();
        } catch (Exception e) {
            throw new ClientResponseFailure(e, response);
        }

    }

    @Test
    public void testGetTaskFormTest() throws Exception {
        KieContainerResource resource = new KieContainerResource(CONTAINER_ID, releaseId);
        assertSuccess(client.createContainer(CONTAINER_ID, resource));

        Map<String, Object> valuesMap = new HashMap<String, Object>();
        valuesMap.put(RestURI.CONTAINER_ID, resource.getContainerId());
        valuesMap.put(RestURI.PROCESS_ID, HIRING_PROCESS_ID);

        Marshaller marshaller = MarshallerFactory.getMarshaller(marshallingFormat, ClassLoader.getSystemClassLoader());

        ClientResponse<?> response = null;
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

            clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), FORM_URI + "/" + TASK_FORM_URI, valuesMap))
                    .header("Content-Type", getMediaType().toString())
                    .header("Accept", getMediaType().toString());
            logger.info("[GET] " + clientRequest.getUri());

            response = clientRequest.get();
            Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

            Object formdata = response.getEntity(String.class);
            logger.info("Form content is '{}'", formdata);

            clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), PROCESS_URI + "/" + ABORT_PROCESS_INST_DEL_URI, valuesMap)).header("Content-Type", getMediaType().toString());
            logger.info("[DELETE] " + clientRequest.getUri());
            response = clientRequest.delete();
            Assert.assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());

            response.releaseConnection();
        } catch (Exception e) {
            throw new ClientResponseFailure(e, response);
        }
    }
}

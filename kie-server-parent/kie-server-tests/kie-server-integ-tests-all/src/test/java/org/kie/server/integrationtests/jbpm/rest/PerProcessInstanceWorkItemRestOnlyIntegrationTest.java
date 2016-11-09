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

import static org.junit.Assert.assertNotNull;
import static org.kie.server.api.rest.RestURI.ABORT_PROCESS_INST_DEL_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_INSTANCE_WORK_ITEMS_BY_PROC_INST_ID_GET_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_INSTANCE_WORK_ITEM_BY_ID_GET_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_URI;
import static org.kie.server.api.rest.RestURI.START_PROCESS_POST_URI;
import static org.kie.server.api.rest.RestURI.build;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.WorkItemInstance;
import org.kie.server.api.model.instance.WorkItemInstanceList;
import org.kie.server.api.model.type.JaxbLong;
import org.kie.server.api.rest.RestURI;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.KieServerDeployer;

public class PerProcessInstanceWorkItemRestOnlyIntegrationTest extends RestJbpmBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "per-process-instance-project",
            "1.0.0.Final");

    private static final String CONTAINER_ID = "per-process-instance-project";
    private static final String PROCESS_ID = "per-process-instance-project.email";

    private Response response = null;

    @BeforeClass
    public static void buildAndDeployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/per-process-instance-project").getFile());

        createContainer(CONTAINER_ID, releaseId);
    }

    @After
    public void releaseConnection() {
        if (response != null) {
            response.close();
        }
    }

    @Test
    public void testGetWorkItemByProcessInstance() throws Exception {
        Long processInstanceId = startProcess(CONTAINER_ID, PROCESS_ID);

        response = callGetWorkItemByProcessInstance(CONTAINER_ID, processInstanceId);
        Assert.assertEquals("Expected HTTP 200 to be returned as one work item should be returned.", Response.Status.OK.getStatusCode(), response.getStatus());

        WorkItemInstanceList result = response.readEntity(WorkItemInstanceList.class);
        Assert.assertEquals(1, result.getItems().size());

        WorkItemInstance workItem = result.getItems().get(0);
        Assert.assertEquals("Email", workItem.getName());
        Assert.assertEquals(processInstanceId, workItem.getProcessInstanceId());
    }

    @Test
    public void testGetWorkItemByProcessInstanceAbortedProcess() throws Exception {
        Long processInstanceId = startProcess(CONTAINER_ID, PROCESS_ID);
        abortProcess(CONTAINER_ID, processInstanceId);

        response = callGetWorkItemByProcessInstance(CONTAINER_ID, processInstanceId);
        Assert.assertEquals("Expected HTTP 404 to be returned as process instance is aborted.", Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void testGetWorkItemByProcessInstanceNonExistingProcess() throws Exception {
        response = callGetWorkItemByProcessInstance(CONTAINER_ID, 123456L);
        Assert.assertEquals("Expected HTTP 404 to be returned as process instance doesn't exist.", Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void testGetWorkItemNonExistingItem() throws Exception {
        Long processInstanceId = startProcess(CONTAINER_ID, PROCESS_ID);

        response = callGetWorkItem(CONTAINER_ID, processInstanceId, 123456L);
        Assert.assertEquals("Expected HTTP 404 to be returned as work item doesn't exist.", Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void testGetWorkItemAbortedProcess() throws Exception {
        Long processInstanceId = startProcess(CONTAINER_ID, PROCESS_ID);
        abortProcess(CONTAINER_ID, processInstanceId);

        response = callGetWorkItem(CONTAINER_ID, processInstanceId, 123456L);
        Assert.assertEquals("Expected HTTP 404 to be returned as process instance is aborted.", Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void testGetWorkItemNonExistingProcess() throws Exception {
        response = callGetWorkItem(CONTAINER_ID, 123456L, 123456L);
        Assert.assertEquals("Expected HTTP 404 to be returned as process instance doesn't exist.", Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    private Long startProcess(String containerId, String processId) {
        Map<String, Object> valuesMap = new HashMap<String, Object>();
        valuesMap.put(RestURI.CONTAINER_ID, CONTAINER_ID);
        valuesMap.put(RestURI.PROCESS_ID, PROCESS_ID);

        WebTarget clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), PROCESS_URI + "/" + START_PROCESS_POST_URI, valuesMap));
        logger.debug("Start process: [POST] " + clientRequest.getUri());

        Response response = null;
        try {
            response = clientRequest.request(getMediaType()).post(Entity.entity("", getMediaType()));
            Assert.assertEquals("Expected HTTP 201 to be returned for process start.", Response.Status.CREATED.getStatusCode(), response.getStatus());

            Long result = response.readEntity(JaxbLong.class).unwrap();
            assertNotNull(result);

            return result;
        } finally {
            response.close();
        }
    }

    private void abortProcess(String containerId, Long processInstanceId) {
        Map<String, Object> valuesMap = new HashMap<String, Object>();
        valuesMap.put(RestURI.CONTAINER_ID, CONTAINER_ID);
        valuesMap.put(RestURI.PROCESS_INST_ID, processInstanceId);

        WebTarget clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), PROCESS_URI + "/" + ABORT_PROCESS_INST_DEL_URI, valuesMap));
        logger.debug("Abort process: [DELETE] " + clientRequest.getUri());

        Response response = null;
        try {
            response = clientRequest.request().delete();
            Assert.assertEquals("Expected HTTP 204 to be returned for process abort.", Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        } finally {
            response.close();
        }
    }

    private Response callGetWorkItemByProcessInstance(String containerId, Long processInstanceId) {
        // get work item by process instance
        Map<String, Object> valuesMap = new HashMap<String, Object>();
        valuesMap.put(RestURI.CONTAINER_ID, CONTAINER_ID);
        valuesMap.put(RestURI.PROCESS_INST_ID, processInstanceId);

        WebTarget clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), PROCESS_URI + "/" + PROCESS_INSTANCE_WORK_ITEMS_BY_PROC_INST_ID_GET_URI, valuesMap));
        logger.debug("Get work items by process instance: [GET] " + clientRequest.getUri());

        return clientRequest.request().get();
    }

    private Response callGetWorkItem(String containerId, Long processInstanceId, Long workItemId) {
        Map<String, Object> valuesMap = new HashMap<String, Object>();
        valuesMap.put(RestURI.CONTAINER_ID, CONTAINER_ID);
        valuesMap.put(RestURI.PROCESS_INST_ID, processInstanceId);
        valuesMap.put(RestURI.WORK_ITEM_ID, workItemId);

        WebTarget clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), PROCESS_URI + "/" + PROCESS_INSTANCE_WORK_ITEM_BY_ID_GET_URI, valuesMap));
        logger.debug("Get work item: [GET] " + clientRequest.getUri());

        return clientRequest.request().get();
    }
}

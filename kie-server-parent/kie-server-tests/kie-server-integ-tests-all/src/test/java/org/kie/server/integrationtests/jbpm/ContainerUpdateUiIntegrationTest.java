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

package org.kie.server.integrationtests.jbpm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotEquals;

import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;

public class ContainerUpdateUiIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static final ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.0.Final");
    private static final ReleaseId releaseId101 = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.1.Final");

    private static final String CONTAINER_ID = "definition-project";
    private static final String HIRING_PROCESS_ID = "hiring";

    @BeforeClass
    public static void buildAndDeployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project").getFile());
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project-101").getFile());
    }

    @Before
    public void cleanContainers() {
        disposeAllContainers();
        createContainer(CONTAINER_ID, releaseId);
    }

    @Test
    public void testGetProcessFormAfterContainerUpdate() throws Exception {
        String result = uiServicesClient.getProcessForm(CONTAINER_ID, HIRING_PROCESS_ID, "en");
        assertNotNull(result);
        assertTrue("Form doesn't contain original label!", result.contains("Candidate Name"));

        // Update container to new version.
        KieServerAssert.assertSuccess(client.updateReleaseId(CONTAINER_ID, releaseId101));

        result = uiServicesClient.getProcessForm(CONTAINER_ID, HIRING_PROCESS_ID, "en");
        assertNotNull(result);
        assertTrue("Form doesn't contain updated label!", result.contains("Candidate First Name And Surname"));
    }

    @Test
    public void testGetTaskFormAfterContainerUpdate() throws Exception {
        long processInstanceId = processClient.startProcess(CONTAINER_ID, HIRING_PROCESS_ID);
        assertTrue(processInstanceId > 0);
        try {
            String result = getFirstTaskForm(processInstanceId);
            assertNotNull(result);
            assertTrue("Form doesn't contain original label!", result.contains("Candidate Name"));

            // Update container to new version and restart process.
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
            KieServerAssert.assertSuccess(client.updateReleaseId(CONTAINER_ID, releaseId101));
            processInstanceId = processClient.startProcess(CONTAINER_ID, HIRING_PROCESS_ID);

            result = getFirstTaskForm(processInstanceId);
            assertNotNull(result);
            assertTrue("Form doesn't contain updated label!", result.contains("Candidate Whole Name"));
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }

    @Test
    public void testGetProcessImageViaUIClientTest() throws Exception {
        String originalResult = uiServicesClient.getProcessImage(CONTAINER_ID, HIRING_PROCESS_ID);
        assertNotNull(originalResult);
        assertFalse(originalResult.isEmpty());

        // Update container to new version.
        KieServerAssert.assertSuccess(client.updateReleaseId(CONTAINER_ID, releaseId101));

        String updatedResult = uiServicesClient.getProcessImage(CONTAINER_ID, HIRING_PROCESS_ID);
        assertNotNull(updatedResult);
        assertFalse(updatedResult.isEmpty());
        assertNotEquals("Process image wasn't updated!", originalResult, updatedResult);
    }

    @Test
    public void testGetProcessInstanceImageViaUIClientTest() throws Exception {
        long processInstanceId = processClient.startProcess(CONTAINER_ID, HIRING_PROCESS_ID);
        assertTrue(processInstanceId > 0);
        try {
            String originalResult = uiServicesClient.getProcessInstanceImage(CONTAINER_ID, processInstanceId);
            assertNotNull(originalResult);
            assertFalse(originalResult.isEmpty());

            // Update container to new version and restart process.
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
            KieServerAssert.assertSuccess(client.updateReleaseId(CONTAINER_ID, releaseId101));
            processInstanceId = processClient.startProcess(CONTAINER_ID, HIRING_PROCESS_ID);

            String updatedResult = uiServicesClient.getProcessInstanceImage(CONTAINER_ID, processInstanceId);
            assertNotNull(updatedResult);
            assertFalse(updatedResult.isEmpty());
            assertNotEquals("Process instance image wasn't updated!", originalResult, updatedResult);
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }

    private String getFirstTaskForm(long processInstanceId) {
        List<TaskSummary> tasks = taskClient.findTasksByStatusByProcessInstanceId(processInstanceId, null, 0, 10);
        assertNotNull(tasks);
        assertEquals(1, tasks.size());

        Long taskId = tasks.get(0).getId();
        return uiServicesClient.getTaskForm(CONTAINER_ID, taskId, "en");
    }
}

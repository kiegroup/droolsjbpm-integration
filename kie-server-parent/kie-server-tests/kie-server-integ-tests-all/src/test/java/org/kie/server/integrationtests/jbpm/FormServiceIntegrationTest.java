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

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.client.KieServicesException;

import static org.junit.Assert.*;
import org.kie.server.integrationtests.shared.KieServerDeployer;

public class FormServiceIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.0.Final");

    private static final String CONTAINER_ID = "definition-project";
    private static final String HIRING_PROCESS_ID = "hiring";
    private static final String HIRING_2_PROCESS_ID = "hiring2";
    private static final String HIRING_SUBFORM_PROCESS_ID = "hiringSubform";

    @BeforeClass
    public static void buildAndDeployArtifacts() {

        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project").getFile());

        createContainer(CONTAINER_ID, releaseId);
    }

    @Test
    public void testGetProcessFormViaUIClientTest() throws Exception {
        String result = uiServicesClient.getProcessForm(CONTAINER_ID, HIRING_PROCESS_ID, "en");
        logger.debug("Form content is '{}'", result);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test(expected = KieServicesException.class)
    public void testGetProcessNotExistingFormViaUIClientTest() throws Exception {
        uiServicesClient.getProcessForm(CONTAINER_ID, "not-existing", "en");
    }

    @Test
    public void testGetTaskFormViaUIClientTest() throws Exception {
        long processInstanceId = processClient.startProcess(CONTAINER_ID, HIRING_PROCESS_ID);
        assertTrue(processInstanceId > 0);
        try {
            List<TaskSummary> tasks = taskClient.findTasksByStatusByProcessInstanceId(processInstanceId, null, 0, 10);
            assertNotNull(tasks);
            assertEquals(1, tasks.size());

            Long taskId = tasks.get(0).getId();

            String result = uiServicesClient.getTaskForm(CONTAINER_ID, taskId, "en");
            logger.debug("Form content is '{}'", result);
            assertNotNull(result);
            assertFalse(result.isEmpty());
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }

    @Test(expected = KieServicesException.class)
    public void testGetTaskNotExistingFormViaUIClientTest() throws Exception {
        uiServicesClient.getTaskForm(CONTAINER_ID, 9999l, "en");
    }

    @Test
    public void testGetProcessFormInPackageViaUIClientTest() throws Exception {
        String result = uiServicesClient.getProcessForm(CONTAINER_ID, HIRING_2_PROCESS_ID, "en");
        logger.debug("Form content is '{}'", result);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    public void testGetTaskFormInPackageViaUIClientTest() throws Exception {
        changeUser(USER_YODA);
        long processInstanceId = processClient.startProcess(CONTAINER_ID, HIRING_2_PROCESS_ID);
        assertTrue(processInstanceId > 0);
        try {
            List<TaskSummary> tasks = taskClient.findTasksByStatusByProcessInstanceId(processInstanceId, null, 0, 10);
            assertNotNull(tasks);
            assertEquals(1, tasks.size());

            Long taskId = tasks.get(0).getId();

            String result = uiServicesClient.getTaskForm(CONTAINER_ID, taskId, "en");
            logger.debug("Form content is '{}'", result);
            assertNotNull(result);
            assertFalse(result.isEmpty());
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }

    @Test(expected = KieServicesException.class)
    public void testGetTaskFormWithoutPermissioneViaUIClientTest() throws Exception {
        changeUser(USER_YODA);
        long processInstanceId = processClient.startProcess(CONTAINER_ID, HIRING_PROCESS_ID);
        assertTrue(processInstanceId > 0);
        try {
            List<TaskSummary> tasks = taskClient.findTasksByStatusByProcessInstanceId(processInstanceId, null, 0, 10);
            assertNotNull(tasks);
            assertEquals(1, tasks.size());

            Long taskId = tasks.get(0).getId();

            changeUser(USER_JOHN);

            uiServicesClient.getTaskForm(CONTAINER_ID, taskId, "en");

            fail();
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }

    @Test
    public void testGetProcessWithSubFormRawFormViaUIClient() throws Exception {
        String result = uiServicesClient.getProcessForm( CONTAINER_ID, HIRING_SUBFORM_PROCESS_ID );
        logger.debug("Form content is '{}'", result);
        assertNotNull(result);
        assertFalse(result.isEmpty());

        // check there is content from subform
        assertTrue("Missing subform content", result.contains("creationDate"));

        // check there is content from multi subform
        assertTrue("Missing multi subform content", result.contains("unitPrice"));
    }

    @Test
    public void testGetTaskFormWithSubformsViaUIClientTest() throws Exception {
        long processInstanceId = processClient.startProcess(CONTAINER_ID, HIRING_SUBFORM_PROCESS_ID);
        assertTrue(processInstanceId > 0);
        try {
            List<TaskSummary> tasks = taskClient.findTasksByStatusByProcessInstanceId(processInstanceId, null, 0, 10);
            assertNotNull(tasks);
            assertEquals(1, tasks.size());

            Long taskId = tasks.get(0).getId();

            String result = uiServicesClient.getTaskForm(CONTAINER_ID, taskId);
            logger.debug("Form content is '{}'", result);
            assertNotNull(result);
            assertFalse(result.isEmpty());

            // check there is content from subform
            assertTrue("Missing subform content", result.contains("creationDate"));

            // check there is content from multi subform
            assertTrue("Missing multi subform content", result.contains("unitPrice"));
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }
}

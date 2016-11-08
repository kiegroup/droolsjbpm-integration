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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.client.KieServicesException;

import static org.junit.Assert.*;

import org.kie.server.client.UIServicesClient;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.KieServerDeployer;

public class FormServiceIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.0.Final");

    private static final String CONTAINER_ID = "definition-project";
    private static final String HIRING_PROCESS_ID = "hiring";
    private static final String HIRING_2_PROCESS_ID = "hiring2";

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
        runGetTaskFormTest( processInstanceId );
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
        long processInstanceId = processClient.startProcess(CONTAINER_ID, HIRING_2_PROCESS_ID);
        runGetTaskFormTest( processInstanceId );
    }

    protected void runGetTaskFormTest( long processInstanceId ) {
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

    @Test
    public void testGetTaskRawFormViaUIClient() throws Exception {
        long processInstanceId = processClient.startProcess(CONTAINER_ID, HIRING_2_PROCESS_ID);
        assertTrue(processInstanceId > 0);
        try {
            List<TaskSummary> tasks = taskClient.findTasksByStatusByProcessInstanceId(processInstanceId, null, 0, 10);
            assertNotNull(tasks);
            assertEquals(1, tasks.size());

            Long taskId = tasks.get(0).getId();

            String result = uiServicesClient.getTaskRawForm( CONTAINER_ID, taskId );
            logger.debug("Form content is '{}'", result);
            assertNotNull(result);
            assertFalse(result.isEmpty());
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }

    @Test
    public void testGetProcessFormViaUIClientTestByType() throws Exception {
        String result = uiServicesClient.getProcessFormByType(CONTAINER_ID, HIRING_PROCESS_ID, "en", UIServicesClient.FORM_TYPE);
        logger.debug("Form content is '{}'", result);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.contains("hiring-taskform.frm"));
    }

    @Test
    public void testGetProcessRawFormViaUIClient() throws Exception {
        String result = uiServicesClient.getProcessRawForm( CONTAINER_ID, HIRING_PROCESS_ID );
        logger.debug("Form content is '{}'", result);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    public void testGetTaskFormInPackageViaUIClientTestByType() throws Exception {
        changeUser(USER_JOHN);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", "john");
        parameters.put("age", 33);
        parameters.put("mail", "john@doe.org");
        long processInstanceId = processClient.startProcess(CONTAINER_ID, HIRING_2_PROCESS_ID, parameters);
        assertTrue(processInstanceId > 0);
        try {
            List<TaskSummary> tasks = taskClient.findTasksByStatusByProcessInstanceId(processInstanceId, null, 0, 10);
            assertNotNull(tasks);
            assertEquals(1, tasks.size());

            Long taskId = tasks.get(0).getId();

            String result = uiServicesClient.getTaskFormByType(CONTAINER_ID, taskId, "en", UIServicesClient.FORM_MODELLER_TYPE);
            logger.debug("Form content is '{}'", result);
            assertNotNull(result);
            assertFalse(result.isEmpty());

            parameters.put("out_age", 33);
            parameters.put("out_mail", "john@doe.org");
            taskClient.completeAutoProgress(CONTAINER_ID, taskId, USER_JOHN, parameters);

            tasks = taskClient.findTasksByStatusByProcessInstanceId(processInstanceId, null, 0, 10);
            assertNotNull(tasks);
            assertEquals(1, tasks.size());

            taskId = tasks.get(0).getId();

            result = uiServicesClient.getTaskFormByType(CONTAINER_ID, taskId, "en", UIServicesClient.FREE_MARKER_TYPE);
            logger.debug("Form content is '{}'", result);
            assertNotNull(result);
            assertFalse(result.isEmpty());

            taskClient.completeAutoProgress(CONTAINER_ID, taskId, USER_JOHN, parameters);

            tasks = taskClient.findTasksByStatusByProcessInstanceId(processInstanceId, null, 0, 10);
            assertNotNull(tasks);
            assertEquals(1, tasks.size());

            taskId = tasks.get(0).getId();

            result = uiServicesClient.getTaskFormByType(CONTAINER_ID, taskId, "en", UIServicesClient.FORM_TYPE);
            logger.debug("Form content is '{}'", result);
            assertNotNull(result);
            assertFalse(result.isEmpty());
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
            changeUser(TestConfig.getUsername());
        }
    }
}

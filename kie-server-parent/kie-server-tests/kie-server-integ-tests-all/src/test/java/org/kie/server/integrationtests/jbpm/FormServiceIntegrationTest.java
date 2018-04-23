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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.exception.KieServicesException;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.client.UIServicesClient;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;

public class FormServiceIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.0.Final");
    private static final ReleaseId releaseId101 = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.1.Final");

    protected static final String CONTAINER_ALIAS = "project";
    private static final String CONTAINER_ID = "definition-project";
    private static final String CONTAINER_ID_V2 = "definition-project-2";
    protected static final String CONTAINER_ID_101 = "definition-project-101";

    private static final String HIRING_PROCESS_ID = "hiring";
    private static final String HIRING_2_PROCESS_ID = "hiring2";
    private static final String HIRING_SUBFORM_PROCESS_ID = "hiringSubform";

    @BeforeClass
    public static void buildAndDeployArtifacts() {

        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project").getFile());
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project-101").getFile());

        createContainer(CONTAINER_ID, releaseId, CONTAINER_ALIAS);
    }

    @After
    public void removeExtraContainer() {
        abortAllProcesses();
        client.disposeContainer(CONTAINER_ID_101);
    }

    protected void createExtraContainer() {
        KieContainerResource containerResource = new KieContainerResource(CONTAINER_ID_101, releaseId101);
        containerResource.setContainerAlias(CONTAINER_ALIAS);
        client.createContainer(CONTAINER_ID_101, containerResource);
    }

    @Test
    public void testGetProcessFormViaUIClientTest() throws Exception {
        String result = uiServicesClient.getProcessForm(CONTAINER_ID, HIRING_PROCESS_ID, "en");
        logger.debug("Form content is '{}'", result);
        assertThat(result).isNotNull().isNotEmpty();
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
        assertThat(result).isNotNull().isNotEmpty();
    }

    @Test
    public void testGetTaskFormInPackageViaUIClientTest() throws Exception {
        long processInstanceId = processClient.startProcess(CONTAINER_ID, HIRING_2_PROCESS_ID);
        runGetTaskFormTest( processInstanceId );
    }

    protected void runGetTaskFormTest( long processInstanceId ) {
        assertThat(processInstanceId).isGreaterThan(0);
        try {
            List<TaskSummary> tasks = taskClient.findTasksByStatusByProcessInstanceId(processInstanceId, null, 0, 10);
            assertThat(tasks).isNotNull().hasSize(1);

            Long taskId = tasks.get(0).getId();

            String result = uiServicesClient.getTaskForm(CONTAINER_ID, taskId, "en");
            logger.debug("Form content is '{}'", result);
            assertThat(result).isNotNull().isNotEmpty();
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
    public void testGetTaskRawFormViaUIClient() throws Exception {
        long processInstanceId = processClient.startProcess(CONTAINER_ID, HIRING_2_PROCESS_ID);
        assertThat(processInstanceId).isGreaterThan(0);
        try {
            List<TaskSummary> tasks = taskClient.findTasksByStatusByProcessInstanceId(processInstanceId, null, 0, 10);
            assertThat(tasks).isNotNull().hasSize(1);

            Long taskId = tasks.get(0).getId();

            String result = uiServicesClient.getTaskRawForm( CONTAINER_ID, taskId );
            logger.debug("Form content is '{}'", result);
            assertThat(result).isNotNull().isNotEmpty();
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }

    @Test
    public void testGetProcessFormViaUIClientTestByType() throws Exception {
        String result = uiServicesClient.getProcessFormByType(CONTAINER_ID, HIRING_PROCESS_ID, "en", UIServicesClient.FORM_TYPE);
        logger.debug("Form content is '{}'", result);
        assertThat(result).isNotNull().isNotEmpty();
        assertThat(result).contains("hiring-taskform.frm");
    }

    @Test
    public void testGetProcessRawFormViaUIClient() throws Exception {
        String result = uiServicesClient.getProcessRawForm( CONTAINER_ID, HIRING_PROCESS_ID );
        logger.debug("Form content is '{}'", result);
        assertThat(result).isNotNull().isNotEmpty();
    }

    @Test
    public void testGetTaskFormInPackageViaUIClientTestByType() throws Exception {
        changeUser(USER_JOHN);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", "john");
        parameters.put("age", 33);
        parameters.put("mail", "john@doe.org");
        long processInstanceId = processClient.startProcess(CONTAINER_ID, HIRING_2_PROCESS_ID, parameters);
        assertThat(processInstanceId).isGreaterThan(0);
        try {
            List<TaskSummary> tasks = taskClient.findTasksByStatusByProcessInstanceId(processInstanceId, null, 0, 10);
            assertThat(tasks).isNotNull().hasSize(1);

            Long taskId = tasks.get(0).getId();

            String result = uiServicesClient.getTaskFormByType(CONTAINER_ID, taskId, "en", UIServicesClient.FORM_MODELLER_TYPE);
            logger.debug("Form content is '{}'", result);
            assertThat(result).isNotNull().isNotEmpty();

            parameters.put("out_age", 33);
            parameters.put("out_mail", "john@doe.org");
            taskClient.completeAutoProgress(CONTAINER_ID, taskId, USER_JOHN, parameters);

            tasks = taskClient.findTasksByStatusByProcessInstanceId(processInstanceId, null, 0, 10);
            assertThat(tasks).isNotNull().hasSize(1);

            taskId = tasks.get(0).getId();

            result = uiServicesClient.getTaskFormByType(CONTAINER_ID, taskId, "en", UIServicesClient.FREE_MARKER_TYPE);
            logger.debug("Form content is '{}'", result);
            assertThat(result).isNotNull().isNotEmpty();

            taskClient.completeAutoProgress(CONTAINER_ID, taskId, USER_JOHN, parameters);

            tasks = taskClient.findTasksByStatusByProcessInstanceId(processInstanceId, null, 0, 10);
            assertThat(tasks).isNotNull().hasSize(1);

            taskId = tasks.get(0).getId();

            result = uiServicesClient.getTaskFormByType(CONTAINER_ID, taskId, "en", UIServicesClient.FORM_TYPE);
            logger.debug("Form content is '{}'", result);
            assertThat(result).isNotNull().isNotEmpty();
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
            changeUser(TestConfig.getUsername());
        }
    }

    @Test
    public void testGetProcessWithSubFormRawFormViaUIClient() throws Exception {
        String result = uiServicesClient.getProcessRawForm( CONTAINER_ID, HIRING_SUBFORM_PROCESS_ID );
        logger.debug("Form content is '{}'", result);
        assertThat(result).isNotNull().isNotEmpty();

        // check there is content from subform
        assertThat(result).contains("creationDate").withFailMessage("Missing subform content");

        // check there is content from multi subform
        assertThat(result).contains("unitPrice").withFailMessage("Missing multi subform content");
    }

    @Test
    public void testGetTaskFormWithSubformsViaUIClientTest() throws Exception {
        long processInstanceId = processClient.startProcess(CONTAINER_ID, HIRING_SUBFORM_PROCESS_ID);
        assertThat(processInstanceId).isGreaterThan(0);
        try {
            List<TaskSummary> tasks = taskClient.findTasksByStatusByProcessInstanceId(processInstanceId, null, 0, 10);
            assertThat(tasks).isNotNull().hasSize(1);

            Long taskId = tasks.get(0).getId();

            String result = uiServicesClient.getTaskRawForm(CONTAINER_ID, taskId);
            logger.debug("Form content is '{}'", result);
            assertThat(result).isNotNull().isNotEmpty();

            // check there is content from subform
            assertThat(result).contains("creationDate").withFailMessage("Missing subform content");

            // check there is content from multi subform
            assertThat(result).contains("unitPrice").withFailMessage("Missing multi subform content");
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }

    @Test
    public void testGetProcessFormViaUIClientWithAliasTest() throws Exception {
        String oldResultViaContainerId = uiServicesClient.getProcessRawForm(CONTAINER_ID, HIRING_PROCESS_ID);
        logger.debug("Form content is '{}'", oldResultViaContainerId);
        assertThat(oldResultViaContainerId).isNotNull().isNotEmpty();

        String oldResultViaAlias = uiServicesClient.getProcessRawForm(CONTAINER_ALIAS, HIRING_PROCESS_ID);
        logger.debug("Form content is '{}'", oldResultViaAlias);
        assertThat(oldResultViaAlias).isNotNull().isNotEmpty();

        assertThat(oldResultViaAlias).isEqualTo(oldResultViaContainerId);
        assertThat(oldResultViaAlias).contains("Candidate Name");
        assertThat(oldResultViaAlias).doesNotContain("Candidate First Name And Surname");

        createExtraContainer();

        String newResultViaContainerId = uiServicesClient.getProcessRawForm(CONTAINER_ID_101, HIRING_PROCESS_ID);
        logger.debug("Form content is '{}'", newResultViaContainerId);
        assertThat(newResultViaContainerId).isNotNull().isNotEmpty();

        String newResultViaAlias = uiServicesClient.getProcessRawForm(CONTAINER_ALIAS, HIRING_PROCESS_ID);
        logger.debug("Form content is '{}'", newResultViaAlias);
        assertThat(newResultViaAlias).isNotNull().isNotEmpty();

        assertThat(newResultViaAlias).isEqualTo(newResultViaContainerId);
        assertThat(newResultViaAlias).contains("Candidate First Name And Surname");
        assertThat(newResultViaAlias).doesNotContain("Candidate Name");

        assertThat(oldResultViaAlias).isNotEqualTo(newResultViaAlias);

    }

    @Test
    public void testGetTaskFormViaUIClientWithAliasTest() throws Exception {
        long processInstanceId = processClient.startProcess(CONTAINER_ALIAS, HIRING_PROCESS_ID);
        assertThat(processInstanceId).isGreaterThan(0);

        ProcessInstance pi = processClient.getProcessInstance(CONTAINER_ALIAS, processInstanceId);
        assertThat(pi.getContainerId()).isEqualTo(CONTAINER_ID);

        List<TaskSummary> tasks = taskClient.findTasksByStatusByProcessInstanceId(processInstanceId, null, 0, 10);
        assertThat(tasks).isNotNull().hasSize(1);

        Long taskId = tasks.get(0).getId();

        String oldResultViaContainerId = uiServicesClient.getTaskRawForm(CONTAINER_ID, taskId);
        logger.debug("Form content is '{}'", oldResultViaContainerId);
        assertThat(oldResultViaContainerId).isNotNull().isNotEmpty();

        String oldResultViaAlias = uiServicesClient.getTaskRawForm(CONTAINER_ALIAS, taskId);
        logger.debug("Form content is '{}'", oldResultViaAlias);
        assertThat(oldResultViaAlias).isNotNull().isNotEmpty();

        assertThat(oldResultViaAlias).isEqualTo(oldResultViaContainerId);
        assertThat(oldResultViaAlias).contains("Candidate Name");
        assertThat(oldResultViaAlias).doesNotContain("Candidate Whole Name");

        createExtraContainer();

        processInstanceId = processClient.startProcess(CONTAINER_ALIAS, HIRING_PROCESS_ID);
        assertThat(processInstanceId).isGreaterThan(0);

        pi = processClient.getProcessInstance(CONTAINER_ALIAS, processInstanceId);
        assertThat(pi.getContainerId()).isEqualTo(CONTAINER_ID_101);

        tasks = taskClient.findTasksByStatusByProcessInstanceId(processInstanceId, null, 0, 10);
        assertThat(tasks).isNotNull().hasSize(1);

        taskId = tasks.get(0).getId();

        String newResultViaContainerId = uiServicesClient.getTaskRawForm(CONTAINER_ID_101, taskId);
        logger.debug("Form content is '{}'", newResultViaContainerId);
        assertThat(newResultViaContainerId).isNotNull().isNotEmpty();

        String newResultViaAlias = uiServicesClient.getTaskRawForm(CONTAINER_ALIAS, taskId);
        logger.debug("Form content is '{}'", newResultViaAlias);
        assertThat(newResultViaAlias).isNotNull().isNotEmpty();

        assertThat(newResultViaAlias).isEqualTo(newResultViaContainerId);
        assertThat(newResultViaAlias).contains("Candidate Whole Name");
        assertThat(newResultViaAlias).doesNotContain("Candidate Name");

        assertThat(oldResultViaAlias).isNotEqualTo(newResultViaAlias);

    }
    
    @Test
    public void testGetTaskFormWithWrongContainerId() throws Exception {
        long processInstanceId = processClient.startProcess(CONTAINER_ID, HIRING_SUBFORM_PROCESS_ID);
        assertThat(processInstanceId).isGreaterThan(0);
        try {
            List<TaskSummary> tasks = taskClient.findTasksByStatusByProcessInstanceId(processInstanceId, null, 0, 10);
            assertThat(tasks).isNotNull().hasSize(1);

            Long taskId = tasks.get(0).getId();            
            // create another container with different id to make sure it cannot be used to stop task
            createContainer(CONTAINER_ID_V2, releaseId, "custom-alias");
            
            assertClientException(
                                  () -> uiServicesClient.getTaskRawForm(CONTAINER_ID_V2, taskId),
                                  404,
                                  "Could not find task instance",
                                  "Task with id " + taskId + " is not associated with " + CONTAINER_ID_V2);
            
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
            KieServerAssert.assertSuccess(client.disposeContainer(CONTAINER_ID_V2));
        }
    }

}

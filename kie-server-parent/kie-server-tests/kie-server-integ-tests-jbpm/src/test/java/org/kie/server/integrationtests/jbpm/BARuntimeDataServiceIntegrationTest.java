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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.task.model.Status;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.integrationtests.config.TestConfig;

import static org.junit.Assert.*;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;


public class BARuntimeDataServiceIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.0.Final");



    @BeforeClass
    public static void buildAndDeployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project").getFile());

        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);

        createContainer(CONTAINER_ID, releaseId);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        extraClasses.put(PERSON_CLASS_NAME, Class.forName(PERSON_CLASS_NAME, true, kieContainer.getClassLoader()));
    }

    @Test
    public void testFindTaskAssignedAsBusinessAdmin() throws Exception {
        changeUser(USER_ADMINISTRATOR);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance(USER_JOHN));

        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK, parameters);

        try {

            List<TaskSummary> tasks = taskClient.findTasksAssignedAsBusinessAdministrator(USER_ADMINISTRATOR, 0, 10);
            assertNotNull(tasks);
            assertEquals(1, tasks.size());

            TaskSummary taskInstance = tasks.get(0);
            assertNotNull(taskInstance);
            assertEquals("First task", taskInstance.getName());
            KieServerAssert.assertNullOrEmpty(taskInstance.getDescription());
            assertEquals("Reserved", taskInstance.getStatus());
            assertEquals(0, taskInstance.getPriority().intValue());
            assertEquals(USER_YODA, taskInstance.getActualOwner());
            assertEquals(USER_YODA, taskInstance.getCreatedBy());
            assertEquals(PROCESS_ID_USERTASK, taskInstance.getProcessId());
            assertEquals(CONTAINER_ID, taskInstance.getContainerId());
            assertEquals(-1, taskInstance.getParentId().longValue());
            assertEquals(processInstanceId, taskInstance.getProcessInstanceId());

            List<String> status = new ArrayList<String>();
            status.add(Status.InProgress.toString());

            tasks = taskClient.findTasksAssignedAsBusinessAdministrator(USER_ADMINISTRATOR, status, 0, 10);
            assertNotNull(tasks);
            assertEquals(0, tasks.size());


        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
            changeUser(TestConfig.getUsername());
        }
    }

    @Test
    public void testFindTaskAssignedAsBusinessAdminSorted() throws Exception {
        changeUser(USER_ADMINISTRATOR);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance(USER_JOHN));

        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK, parameters);
        Long processInstanceId2 = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK, parameters);

        try {

            List<TaskSummary> tasks = taskClient.findTasksAssignedAsBusinessAdministrator(USER_ADMINISTRATOR, 0, 10, "t.taskData.processInstanceId", true);
            assertNotNull(tasks);
            assertEquals(2, tasks.size());
            if (processInstanceId < processInstanceId2) {
                assertEquals(processInstanceId, tasks.get(0).getProcessInstanceId());
                assertEquals(processInstanceId2, tasks.get(1).getProcessInstanceId());
            } else {
                assertEquals(processInstanceId2, tasks.get(0).getProcessInstanceId());
                assertEquals(processInstanceId, tasks.get(1).getProcessInstanceId());
            }

            List<String> status = new ArrayList<>();
            status.add(Status.Reserved.toString());

            tasks = taskClient.findTasksAssignedAsBusinessAdministrator(USER_ADMINISTRATOR, status, 0, 10, "t.taskData.processInstanceId", false);
            assertNotNull(tasks);
            assertEquals(2, tasks.size());
            if (processInstanceId < processInstanceId2) {
                assertEquals(processInstanceId2, tasks.get(0).getProcessInstanceId());
                assertEquals(processInstanceId, tasks.get(1).getProcessInstanceId());
            } else {
                assertEquals(processInstanceId, tasks.get(0).getProcessInstanceId());
                assertEquals(processInstanceId2, tasks.get(1).getProcessInstanceId());
            }
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId2);
            changeUser(TestConfig.getUsername());
        }
    }
}

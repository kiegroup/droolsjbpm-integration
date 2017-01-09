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

package org.kie.server.integrationtests.jbpm.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.admin.MigrationReportInstance;
import org.kie.server.api.model.definition.UserTaskDefinition;
import org.kie.server.api.model.definition.UserTaskDefinitionList;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.client.KieServicesException;
import org.kie.server.integrationtests.jbpm.JbpmKieServerBaseIntegrationTest;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;

import static org.junit.Assert.*;

public class ProcessInstanceMigrationIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static final ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.0.Final");
    private static final ReleaseId releaseId101 = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.1.Final");

    protected static final String CONTAINER_ID_2 = "definition-project-2";
    protected static final String PROCESS_ID_EVALUATION_2 = "definition-project.evaluation2";

    @BeforeClass
    public static void buildAndDeployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project").getFile());
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project-101").getFile());
    }

    @Before
    public void cleanContainers() {
        disposeAllContainers();
        // use different aliases to avoid policy based removal - keep latest only
        createContainer(CONTAINER_ID, releaseId, CONTAINER_ID);
        createContainer(CONTAINER_ID_2, releaseId101, CONTAINER_ID_2);
    }

    @Test
    public void testUpgradeProcessInstance() throws Exception {
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_EVALUATION);

        try {
            List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertEquals(1, tasks.size());
            TaskSummary task = tasks.get(0);
            assertEquals("Evaluate items?", tasks.get(0).getName());

            assertEquals(CONTAINER_ID, task.getContainerId());
            assertEquals(PROCESS_ID_EVALUATION, task.getProcessId());
            assertEquals(processInstanceId, task.getProcessInstanceId());

            // migrate process instance to evaluation 2 in container 2
            MigrationReportInstance report = processAdminClient.migrateProcessInstance(CONTAINER_ID, processInstanceId, CONTAINER_ID_2, PROCESS_ID_EVALUATION_2);
            assertNotNull(report);
            assertTrue(report.isSuccessful());

            // it stays in the same task
            tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertEquals(1, tasks.size());

            task = tasks.get(0);
            assertEquals("Evaluate items?", task.getName());
            assertEquals(CONTAINER_ID_2, task.getContainerId());
            assertEquals(PROCESS_ID_EVALUATION_2, task.getProcessId());
            assertEquals(processInstanceId, task.getProcessInstanceId());

            taskClient.completeAutoProgress(CONTAINER_ID_2, task.getId(), USER_YODA, null);

            // but next task should be Approve user task
            tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertEquals(1, tasks.size());

            task = tasks.get(0);
            assertEquals("Approve", task.getName());
            assertEquals(CONTAINER_ID_2, task.getContainerId());
            assertEquals(PROCESS_ID_EVALUATION_2, task.getProcessId());
            assertEquals(processInstanceId, task.getProcessInstanceId());

        } finally {
            try {
                processClient.getProcessInstance(CONTAINER_ID_2, processInstanceId);

                processClient.abortProcessInstance(CONTAINER_ID_2, processInstanceId);
            } catch (KieServicesException e){
                processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
            }
        }
    }

    @Test
    public void testUpgradeProcessInstanceWithNodeMapping() throws Exception {
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_EVALUATION);
        try {
            List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertEquals(1, tasks.size());
            TaskSummary task = tasks.get(0);
            assertEquals("Evaluate items?", tasks.get(0).getName());

            assertEquals(CONTAINER_ID, task.getContainerId());
            assertEquals(PROCESS_ID_EVALUATION, task.getProcessId());
            assertEquals(processInstanceId, task.getProcessInstanceId());

            Map<String, String> nodeMapping = new HashMap<String, String>();
            nodeMapping.put("_4E8E7545-FB70-494E-9136-2B9ABE655889", "_56FB3E50-DEDD-415B-94DD-0357C91836B9");
            // migrate process instance to evaluation 2 in container 2
            MigrationReportInstance report = processAdminClient.migrateProcessInstance(CONTAINER_ID, processInstanceId, CONTAINER_ID_2, PROCESS_ID_EVALUATION_2, nodeMapping);
            assertNotNull(report);
            assertTrue(report.isSuccessful());

            // migrated to Approve user task
            tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertEquals(1, tasks.size());

            task = tasks.get(0);
            assertEquals("Approve", task.getName());
            assertEquals(CONTAINER_ID_2, task.getContainerId());
            assertEquals(PROCESS_ID_EVALUATION_2, task.getProcessId());
            assertEquals(processInstanceId, task.getProcessInstanceId());

        } finally {
            try {
                processClient.getProcessInstance(CONTAINER_ID_2, processInstanceId);

                processClient.abortProcessInstance(CONTAINER_ID_2, processInstanceId);
            } catch (KieServicesException e){
                processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
            }
        }
    }

    @Test
    public void testUpgradeProcessInstances() throws Exception {

        List<Long> ids = new ArrayList<Long>();

        for (int i = 0; i < 5; i++) {
            Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_EVALUATION);
            ids.add(processInstanceId);
        }
        try {
            List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertEquals(5, tasks.size());

            for (TaskSummary task : tasks) {
                assertEquals("Evaluate items?", tasks.get(0).getName());

                assertEquals(CONTAINER_ID, task.getContainerId());
                assertEquals(PROCESS_ID_EVALUATION, task.getProcessId());
            }


            // migrate process instance to evaluation 2 in container 2
            List<MigrationReportInstance> reports = processAdminClient.migrateProcessInstances(CONTAINER_ID, ids, CONTAINER_ID_2, PROCESS_ID_EVALUATION_2);
            assertNotNull(reports);

            for (MigrationReportInstance report : reports) {
                assertTrue(report.isSuccessful());
            }

            // it stays in the same task
            tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertEquals(5, tasks.size());

            for (TaskSummary task : tasks) {
                assertEquals("Evaluate items?", tasks.get(0).getName());

                assertEquals(CONTAINER_ID_2, task.getContainerId());
                assertEquals(PROCESS_ID_EVALUATION_2, task.getProcessId());

                taskClient.completeAutoProgress(CONTAINER_ID_2, task.getId(), USER_YODA, null);
            }
            // but next task should be Approve user task
            tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertEquals(5, tasks.size());

            for (TaskSummary task : tasks) {
                assertEquals("Approve", task.getName());
                assertEquals(CONTAINER_ID_2, task.getContainerId());
                assertEquals(PROCESS_ID_EVALUATION_2, task.getProcessId());
            }
        } finally {
            for (Long processInstanceId : ids) {
                try {
                    processClient.getProcessInstance(CONTAINER_ID_2, processInstanceId);

                    processClient.abortProcessInstance(CONTAINER_ID_2, processInstanceId);
                } catch (KieServicesException e){
                    processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
                }
            }
        }
    }

    @Test
    public void testUpgradeProcessInstancesWithNodeMapping() throws Exception {

        List<Long> ids = new ArrayList<Long>();

        for (int i = 0; i < 5; i++) {
            Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_EVALUATION);
            ids.add(processInstanceId);
        }
        try {
            List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertEquals(5, tasks.size());

            for (TaskSummary task : tasks) {
                assertEquals("Evaluate items?", tasks.get(0).getName());

                assertEquals(CONTAINER_ID, task.getContainerId());
                assertEquals(PROCESS_ID_EVALUATION, task.getProcessId());
            }

            Map<String, String> nodeMapping = new HashMap<String, String>();
            nodeMapping.put("_4E8E7545-FB70-494E-9136-2B9ABE655889", "_56FB3E50-DEDD-415B-94DD-0357C91836B9");
            // migrate process instance to evaluation 2 in container 2
            List<MigrationReportInstance> reports = processAdminClient.migrateProcessInstances(CONTAINER_ID, ids, CONTAINER_ID_2, PROCESS_ID_EVALUATION_2, nodeMapping);
            assertNotNull(reports);

            for (MigrationReportInstance report : reports) {
                assertTrue(report.isSuccessful());
            }

            // but next task should be Approve user task
            tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertEquals(5, tasks.size());

            for (TaskSummary task : tasks) {
                assertEquals("Approve", task.getName());
                assertEquals(CONTAINER_ID_2, task.getContainerId());
                assertEquals(PROCESS_ID_EVALUATION_2, task.getProcessId());
            }
        } finally {
            for (Long processInstanceId : ids) {
                try {
                    processClient.getProcessInstance(CONTAINER_ID_2, processInstanceId);

                    processClient.abortProcessInstance(CONTAINER_ID_2, processInstanceId);
                } catch (KieServicesException e){
                    processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
                }
            }
        }
    }
}

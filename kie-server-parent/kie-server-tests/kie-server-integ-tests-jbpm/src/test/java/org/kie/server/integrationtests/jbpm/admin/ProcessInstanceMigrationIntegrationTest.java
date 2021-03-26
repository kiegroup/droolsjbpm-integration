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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.exception.KieServicesException;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.admin.MigrationProcessSpecification;
import org.kie.server.api.model.admin.MigrationReportInstance;
import org.kie.server.api.model.admin.MigrationSpecification;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.integrationtests.jbpm.JbpmKieServerBaseIntegrationTest;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class ProcessInstanceMigrationIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(ProcessInstanceMigrationIntegrationTest.class);

    private static final ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.0.Final");
    private static final ReleaseId releaseId101 = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.1.Final");

    private static final String CONTAINER_ID_2 = "definition-project-2";
    private static final String PROCESS_ID_EVALUATION_2 = "definition-project.evaluation2";
    private static final String PARENT_WITH_SUBPROCESSES_ID_2 = "definition-project.parentWithSubProcesses2";
    private static final String PARENT_WITH_SUBPROCESSES_ID = "definition-project.parentWithSubProcesses";
    private static final String PARENT_WITH_MULTIPLE_SUBPROCESSES_ID = "definition-project.parentWithMultipleSubProcesses";
    private static final String PARENT_WITH_MULTIPLE_SUBPROCESSES_ID_2 = "definition-project.parentWithMultipleSubProcesses2";
    private static final String SUBPROCESS_ID = "definition-project.subprocess";
    private static final String SUBPROCESS_ID_2 = "definition-project.subprocess2";
    private static final String SUBPROCESS_CALLING_ANOTHER_SUBPROCESS_ID = "definition-project.subprocessCallingAnotherSubProcess";
    private static final String SUBPROCESS_CALLING_ANOTHER_SUBPROCESS_ID_2 = "definition-project.subprocessCallingAnotherSubProcess2";


    @BeforeClass
    public static void buildAndDeployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/definition-project");
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/definition-project-101");
    }

    @Before
    public void cleanContainers() {
        disposeAllContainers();
        // use different aliases to avoid policy based removal - keep latest only
        createContainer(CONTAINER_ID, releaseId, CONTAINER_ID);
        createContainer(CONTAINER_ID_2, releaseId101, CONTAINER_ID_2);
    }

    @Test
    public void testUpgradeProcessInstance() {
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
    public void testUpgradeProcessInstanceWithNodeMapping() {
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
    public void testUpgradeProcessInstances() {

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
            assertEquals(5, reports.size());

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
    public void testUpgradeProcessInstancesWithNodeMapping() {

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
            assertEquals(5, reports.size());

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

    @Test
    public void testUpgradeParentInstanceWithSubprocesses() {
        Long processParentInstanceId = processClient.startProcess(CONTAINER_ID, PARENT_WITH_SUBPROCESSES_ID);
        assertNotNull(processParentInstanceId);
        assertTrue(processParentInstanceId > 0);

        logger.info("Process in container {} has started {}", CONTAINER_ID, processParentInstanceId);
        List<ProcessInstance> childrenProcessInstances = processClient.findProcessInstancesByParent(CONTAINER_ID, processParentInstanceId, 0, 10);
        assertEquals(2, childrenProcessInstances.size());
        assertProcessInstancesInfo(childrenProcessInstances, "subprocess", "1.0", CONTAINER_ID, SUBPROCESS_ID, processParentInstanceId);

        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        assertEquals(2, tasks.size());
        assertTasksInfo(tasks, "Evaluate items?", CONTAINER_ID, SUBPROCESS_ID);

        MigrationSpecification spec = new MigrationSpecification();
        MigrationProcessSpecification pSpecParent = new MigrationProcessSpecification();
        pSpecParent.setSourceProcessId(PARENT_WITH_SUBPROCESSES_ID);
        pSpecParent.setTargetProcessId(PARENT_WITH_SUBPROCESSES_ID_2);
        MigrationProcessSpecification pSpecChild = new MigrationProcessSpecification();
        pSpecChild.setSourceProcessId(SUBPROCESS_ID);
        pSpecChild.setTargetProcessId(SUBPROCESS_ID_2);
        spec.setProcesses(Arrays.asList(pSpecParent, pSpecChild));

        ProcessInstance piMigrate = processClient.getProcessInstance(CONTAINER_ID, processParentInstanceId);
        assertProcessInstanceInfo(piMigrate, "parentWithSubProcesses", "1.0", CONTAINER_ID, PARENT_WITH_SUBPROCESSES_ID);
        logger.info("about to process migration from container {} to {} with process definition {} with id {}", piMigrate.getContainerId(), CONTAINER_ID_2, piMigrate.getProcessId(), processParentInstanceId);
        assertMigrateProcessInstanceWithSubprocess(processParentInstanceId, CONTAINER_ID, CONTAINER_ID_2, spec, 3);

        piMigrate = processClient.getProcessInstance(CONTAINER_ID_2, processParentInstanceId);
        assertNotNull(piMigrate);
        assertProcessInstanceInfo(piMigrate, "parentWithSubProcesses2", "1.0.1", CONTAINER_ID_2, PARENT_WITH_SUBPROCESSES_ID_2);

        childrenProcessInstances = processClient.findProcessInstancesByParent(CONTAINER_ID_2, processParentInstanceId, 0, 10);
        logger.info("children instances from {} fetched", CONTAINER_ID_2);
        assertEquals(2, childrenProcessInstances.size());
        assertProcessInstancesInfo(childrenProcessInstances, "subprocess2", "1.0.1", CONTAINER_ID_2, SUBPROCESS_ID_2, processParentInstanceId);

        // it stays in the same task
        tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        assertAndCompleteTasks(tasks, 2, USER_YODA, CONTAINER_ID_2, SUBPROCESS_ID_2);

        logger.info("migration tested!");
    }

    @Test
    public void testUpgradeParentInstanceWithMultipleSubprocesses() {
        Long processParentInstanceId = processClient.startProcess(CONTAINER_ID, PARENT_WITH_MULTIPLE_SUBPROCESSES_ID);
        assertNotNull(processParentInstanceId);
        assertTrue(processParentInstanceId > 0);

        logger.info("Process in container {} has started {}", CONTAINER_ID, processParentInstanceId);
        List<ProcessInstance> childrenProcessInstance = processClient.findProcessInstancesByParent(CONTAINER_ID, processParentInstanceId, 0, 10);
        assertEquals(1, childrenProcessInstance.size());
        assertProcessInstancesInfo(childrenProcessInstance, "subprocessCallingAnotherSubProcess", "1.0",
                                   CONTAINER_ID, SUBPROCESS_CALLING_ANOTHER_SUBPROCESS_ID, processParentInstanceId);

        List<ProcessInstance> childrenSubProcessInstance = processClient.findProcessInstancesByParent(CONTAINER_ID, childrenProcessInstance.get(0).getId(), 0, 10);
        assertEquals(1, childrenSubProcessInstance.size());
        assertProcessInstancesInfo(childrenSubProcessInstance, "subprocess", "1.0",
                                   CONTAINER_ID, SUBPROCESS_ID, childrenSubProcessInstance.get(0).getParentId());

        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        assertEquals(1, tasks.size());
        assertTasksInfo(tasks, "Evaluate items?", CONTAINER_ID, SUBPROCESS_ID);

        MigrationSpecification spec = new MigrationSpecification();
        MigrationProcessSpecification pSpecParent = new MigrationProcessSpecification();
        pSpecParent.setSourceProcessId(PARENT_WITH_MULTIPLE_SUBPROCESSES_ID);
        pSpecParent.setTargetProcessId(PARENT_WITH_MULTIPLE_SUBPROCESSES_ID_2);
        MigrationProcessSpecification pSpecChild = new MigrationProcessSpecification();
        pSpecChild.setSourceProcessId(SUBPROCESS_CALLING_ANOTHER_SUBPROCESS_ID);
        pSpecChild.setTargetProcessId(SUBPROCESS_CALLING_ANOTHER_SUBPROCESS_ID_2);
        MigrationProcessSpecification pSpecSubChild = new MigrationProcessSpecification();
        pSpecSubChild.setSourceProcessId(SUBPROCESS_ID);
        pSpecSubChild.setTargetProcessId(SUBPROCESS_ID_2);
        spec.setProcesses(Arrays.asList(pSpecParent, pSpecChild, pSpecSubChild));

        ProcessInstance piMigrate = processClient.getProcessInstance(CONTAINER_ID, processParentInstanceId);
        assertProcessInstanceInfo(piMigrate, "parentWithMultipleSubProcesses", "1.0", CONTAINER_ID, PARENT_WITH_MULTIPLE_SUBPROCESSES_ID);
        logger.info("about to process migration from container {} to {} with process definition {} with id {}", piMigrate.getContainerId(), CONTAINER_ID_2, piMigrate.getProcessId(), processParentInstanceId);
        assertMigrateProcessInstanceWithSubprocess(processParentInstanceId, CONTAINER_ID, CONTAINER_ID_2, spec, 3);

        piMigrate = processClient.getProcessInstance(CONTAINER_ID_2, processParentInstanceId);
        assertNotNull(piMigrate);
        assertProcessInstanceInfo(piMigrate, "parentWithMultipleSubProcesses2", "1.0.1", CONTAINER_ID_2, PARENT_WITH_MULTIPLE_SUBPROCESSES_ID_2);

        childrenProcessInstance = processClient.findProcessInstancesByParent(CONTAINER_ID_2, processParentInstanceId, 0, 10);
        logger.info("children instance from {} fetched", CONTAINER_ID_2);
        assertEquals(1, childrenProcessInstance.size());
        assertProcessInstancesInfo(childrenProcessInstance, "subprocessCallingAnotherSubProcess2", "1.0.1", CONTAINER_ID_2, SUBPROCESS_CALLING_ANOTHER_SUBPROCESS_ID_2, processParentInstanceId);

        childrenSubProcessInstance = processClient.findProcessInstancesByParent(CONTAINER_ID_2, childrenProcessInstance.get(0).getId(), 0, 10);
        logger.info("children instance from {} fetched", CONTAINER_ID_2);
        assertEquals(1, childrenSubProcessInstance.size());
        assertProcessInstancesInfo(childrenSubProcessInstance, "subprocess2", "1.0.1", CONTAINER_ID_2, SUBPROCESS_ID_2, childrenProcessInstance.get(0).getId());

        // it stays in the same task
        tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        assertAndCompleteTasks(tasks, 1, USER_YODA, CONTAINER_ID_2, SUBPROCESS_ID_2);

        logger.info("migration tested!");
    }

    @Test
    public void testUpgradeInvalidParentInstanceWithSubprocesses () {
        Long processParentInstanceId = processClient.startProcess(CONTAINER_ID, PARENT_WITH_MULTIPLE_SUBPROCESSES_ID);
        try {
            assertNotNull(processParentInstanceId);
            assertTrue(processParentInstanceId > 0);

            logger.info("Process in container {} has started {}", CONTAINER_ID, processParentInstanceId);
            List<ProcessInstance> childrenProcessInstance = processClient.findProcessInstancesByParent(CONTAINER_ID, processParentInstanceId, 0, 10);
            assertEquals(1, childrenProcessInstance.size());
            Long childrenProcessInstanceId = childrenProcessInstance.get(0).getId();
            assertNotNull(childrenProcessInstanceId);
            assertTrue(childrenProcessInstanceId > 0);
            assertProcessInstancesInfo(childrenProcessInstance, "subprocessCallingAnotherSubProcess", "1.0",
                                       CONTAINER_ID, SUBPROCESS_CALLING_ANOTHER_SUBPROCESS_ID, processParentInstanceId);

            List<ProcessInstance> childrenSubProcessInstance = processClient.findProcessInstancesByParent(CONTAINER_ID, childrenProcessInstanceId, 0, 10);
            assertEquals(1, childrenSubProcessInstance.size());
            assertProcessInstancesInfo(childrenSubProcessInstance, "subprocess", "1.0",
                                       CONTAINER_ID, SUBPROCESS_ID, childrenSubProcessInstance.get(0).getParentId());

            List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertEquals(1, tasks.size());
            assertTasksInfo(tasks, "Evaluate items?", CONTAINER_ID, SUBPROCESS_ID);

            MigrationSpecification spec = new MigrationSpecification();
            MigrationProcessSpecification pSpecParent = new MigrationProcessSpecification();
            pSpecParent.setSourceProcessId(PARENT_WITH_MULTIPLE_SUBPROCESSES_ID);
            pSpecParent.setTargetProcessId(PARENT_WITH_MULTIPLE_SUBPROCESSES_ID_2);
            MigrationProcessSpecification pSpecChild = new MigrationProcessSpecification();
            pSpecChild.setSourceProcessId(SUBPROCESS_CALLING_ANOTHER_SUBPROCESS_ID);
            pSpecChild.setTargetProcessId(SUBPROCESS_CALLING_ANOTHER_SUBPROCESS_ID_2);
            MigrationProcessSpecification pSpecSubChild = new MigrationProcessSpecification();
            pSpecSubChild.setSourceProcessId(SUBPROCESS_ID);
            pSpecSubChild.setTargetProcessId(SUBPROCESS_ID_2);
            spec.setProcesses(Arrays.asList(pSpecParent, pSpecChild, pSpecSubChild));

            assertThrows(KieServicesException.class, () -> processAdminClient.migrateProcessInstanceWithSubprocess(CONTAINER_ID, childrenProcessInstanceId, CONTAINER_ID_2, spec));
        } finally {
            try {
                processClient.getProcessInstance(CONTAINER_ID_2, processParentInstanceId);
                processClient.abortProcessInstance(CONTAINER_ID_2, processParentInstanceId);
            } catch (KieServicesException e){
                processClient.abortProcessInstance(CONTAINER_ID, processParentInstanceId);
            }
        }
    }

    private void assertMigrateProcessInstanceWithSubprocess(Long processParentInstanceId, String sourceContainerId,
                                                            String targetContainerId, MigrationSpecification spec,
                                                            int totalMigratedInstances) {
        List<MigrationReportInstance> reports = processAdminClient.migrateProcessInstanceWithSubprocess(sourceContainerId, processParentInstanceId, targetContainerId, spec);
        assertNotNull(reports);
        for(MigrationReportInstance report : reports) {
            assertTrue(report.isSuccessful());
        }
        logger.info("process migration complete in container {} with id parent {}", targetContainerId, processParentInstanceId);
        assertEquals(totalMigratedInstances, reports.size());
    }

    private void assertAndCompleteTasks(List<TaskSummary> tasks, int expectedNumberOfTasks, String user,
                                        String containerId, String processId) {
        logger.info("children tasks instances from {} fetched", containerId);
        assertEquals(expectedNumberOfTasks, tasks.size());

        for (TaskSummary task : tasks) {
            assertTaskInfo(task, "Evaluate items?", containerId, processId);
            taskClient.completeAutoProgress(containerId, task.getId(), user, null);
        }

        tasks = taskClient.findTasksAssignedAsPotentialOwner(user, 0, 10);
        logger.info("children tasks instances from {} fetched", containerId);
        assertEquals(expectedNumberOfTasks, tasks.size());

        for (TaskSummary task : tasks) {
            assertTaskInfo(task, "Approve", containerId, processId);
            taskClient.completeAutoProgress(containerId, task.getId(), user, null);
        }
    }


    private void assertTasksInfo(List<TaskSummary> tasks, String taskName, String containerId, String processId){
        for (TaskSummary task : tasks) {
            assertTaskInfo(task, taskName, containerId, processId);
        }
    }

    private void assertTaskInfo(TaskSummary task, String taskName, String containerId, String processId){
        assertEquals(taskName, task.getName());
        assertEquals(containerId, task.getContainerId());
        assertEquals(processId, task.getProcessId());
    }

    private void assertProcessInstancesInfo(List<ProcessInstance> processInstances, String processName, String version, String containerId,
                                            String processId, long parentId) {
        for (ProcessInstance processInstance : processInstances) {
            assertProcessInstanceInfo(processInstance, processName, version, containerId, processId, parentId);
        }
    }

    private void assertProcessInstanceInfo(ProcessInstance processInstance, String processName, String version, String containerId,
                                           String processId) {
        assertProcessInstanceInfo(processInstance, processName, version, containerId, processId, -1);
    }

    private void assertProcessInstanceInfo(ProcessInstance processInstance, String processName, String version, String containerId,
                                           String processId, long parentId) {
        assertEquals(version, processInstance.getProcessVersion());
        assertEquals(processName, processInstance.getProcessName());
        assertEquals(containerId, processInstance.getContainerId());
        assertEquals(processId, processInstance.getProcessId());
        assertEquals(parentId, processInstance.getParentId().longValue());
    }

}

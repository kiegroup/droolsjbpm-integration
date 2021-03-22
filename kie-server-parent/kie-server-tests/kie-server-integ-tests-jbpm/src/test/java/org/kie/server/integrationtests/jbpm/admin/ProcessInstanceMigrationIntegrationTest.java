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
import static org.junit.Assert.assertTrue;

public class ProcessInstanceMigrationIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static Logger logger = LoggerFactory.getLogger(ProcessInstanceMigrationIntegrationTest.class);

    private static final ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.0.Final");
    private static final ReleaseId releaseId101 = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.1.Final");

    private static final String CONTAINER_ID_2 = "definition-project-2";
    private static final String PROCESS_ID_EVALUATION_2 = "definition-project.evaluation2";
    private static final String PARENT_WITH_INDEPENDENT_SUBPROCESS_ID = "definition-project.parentWithIndependentSubProcess";
    private static final String PARENT_WITH_NON_INDEPENDENT_SUBPROCESS_ID2 = "definition-project.parentWithNonIndependentSubProcess2";
    private static final String PARENT_WITH_NON_INDEPENDENT_SUBPROCESS_ID = "definition-project.parentWithNonIndependentSubProcess";
    private static final String PARENT_WITH_MULTIPLE_INDEPENDENT_SUBPROCESS_ID = "definition-project.parentWithMultipleIndependentSubProcesses";
    private static final String SUBPROCESS_ID = "definition-project.subprocess";
    private static final String SUBPROCESS_ID_2 = "definition-project.subprocess2";
    private static final String SUBPROCESS_CALLING_ANOTHER_SUBPROCESS_ID = "definition-project.subprocessCallingAnotherSubProcess";


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
    public void testUpgradeParentInstanceWithNonIndependentSubprocess() {
        Long processParentInstanceId = processClient.startProcess(CONTAINER_ID, PARENT_WITH_NON_INDEPENDENT_SUBPROCESS_ID);
        assertNotNull(processParentInstanceId);
        assertTrue(processParentInstanceId > 0);

        try {
            logger.info("Process in container {} has started {}", CONTAINER_ID, processParentInstanceId);
            List<ProcessInstance> childrenProcessInstances = processClient.findProcessInstancesByParent(CONTAINER_ID, processParentInstanceId, 0, 10);
            assertEquals(2, childrenProcessInstances.size());
            for (ProcessInstance processInstance : childrenProcessInstances) {
                logger.info("Subprocess in container {} has started {}", CONTAINER_ID, processInstance.getId());
                assertEquals("1.0", processInstance.getProcessVersion());
                assertEquals(CONTAINER_ID, processInstance.getContainerId());
                assertEquals(SUBPROCESS_ID, processInstance.getProcessId());
                assertEquals(processParentInstanceId, processInstance.getParentId());
            }

            List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertEquals(2, tasks.size());
            for (TaskSummary task : tasks) {
                assertEquals("Evaluate items?", task.getName());
                assertEquals(CONTAINER_ID, task.getContainerId());
                assertEquals(SUBPROCESS_ID, task.getProcessId());
            }

            MigrationSpecification spec = new MigrationSpecification();
            MigrationProcessSpecification pSpecParent = new MigrationProcessSpecification();
            pSpecParent.setSourceProcessId(PARENT_WITH_NON_INDEPENDENT_SUBPROCESS_ID);
            pSpecParent.setTargetProcessId(PARENT_WITH_NON_INDEPENDENT_SUBPROCESS_ID2);
            MigrationProcessSpecification pSpecChild = new MigrationProcessSpecification();
            pSpecChild.setSourceProcessId(SUBPROCESS_ID);
            pSpecChild.setTargetProcessId(SUBPROCESS_ID_2);
            spec.setProcesses(Arrays.asList(pSpecParent, pSpecChild));

            ProcessInstance piMigrate = processClient.getProcessInstance(CONTAINER_ID, processParentInstanceId);
            logger.info("about to process migration from container {} to {} with process definintion {} with id {}", piMigrate.getContainerId(), CONTAINER_ID_2, piMigrate.getProcessId(), processParentInstanceId);
            List<MigrationReportInstance> reports = processAdminClient.migrateProcessInstanceWithSubprocess(CONTAINER_ID, processParentInstanceId, CONTAINER_ID_2, spec);
            assertNotNull(reports);
            for(MigrationReportInstance report : reports) {
                assertTrue(report.isSuccessful());
            }
            logger.info("process migration complete in container {} with id parent {}", CONTAINER_ID_2, processParentInstanceId);
            assertEquals(3, reports.size());

            childrenProcessInstances = processClient.findProcessInstancesByParent(CONTAINER_ID_2, processParentInstanceId, 0, 10);
            logger.info("children instances from {} fetched", CONTAINER_ID_2);
            assertEquals(2, childrenProcessInstances.size());
            for (ProcessInstance processInstance : childrenProcessInstances) {
                assertEquals("subprocess", processInstance.getProcessName());
                assertEquals(CONTAINER_ID_2, processInstance.getContainerId());
                assertEquals(SUBPROCESS_ID_2, processInstance.getProcessId());
                assertEquals(processParentInstanceId, processInstance.getParentId());
            }

            // it stays in the same task
            tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            logger.info("children tasks instances from {} fetched", CONTAINER_ID_2);
            assertEquals(2, tasks.size());

            for (TaskSummary task : tasks) {
                assertEquals("Evaluate items?", task.getName());
                assertEquals(CONTAINER_ID_2, task.getContainerId());
                assertEquals(SUBPROCESS_ID_2, task.getProcessId());
                taskClient.completeAutoProgress(CONTAINER_ID_2, task.getId(), USER_YODA, null);
            }

            // it stays in the same task
            tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            logger.info("children tasks instances from {} fetched", CONTAINER_ID_2);
            assertEquals(2, tasks.size());

            for (TaskSummary task : tasks) {
                assertEquals("Approve", task.getName());
                assertEquals(CONTAINER_ID_2, task.getContainerId());
                assertEquals(SUBPROCESS_ID_2, task.getProcessId());
                taskClient.completeAutoProgress(CONTAINER_ID_2, task.getId(), USER_YODA, null);
            }


            logger.info("migration tested!");
        } catch (Exception e) {
            logger.error("there was an error during test", e);
        }
    }


}

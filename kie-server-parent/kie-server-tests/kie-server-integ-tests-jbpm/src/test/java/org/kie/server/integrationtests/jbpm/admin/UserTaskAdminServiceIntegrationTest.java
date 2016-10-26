/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.admin.EmailNotification;
import org.kie.server.api.model.admin.OrgEntities;
import org.kie.server.api.model.admin.TaskNotification;
import org.kie.server.api.model.admin.TaskReassignment;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.jbpm.JbpmKieServerBaseIntegrationTest;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerSynchronization;

import static org.junit.Assert.*;

public class UserTaskAdminServiceIntegrationTest extends JbpmKieServerBaseIntegrationTest {

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

    @After
    public void resetUser() throws Exception {
        changeUser(TestConfig.getUsername());
    }

    @Test
    public void testAddRemovePotOwners() throws Exception {
        changeUser(USER_ADMINISTRATOR);
        Map<String, Object> parameters = new HashMap<String, Object>();

        Long processInstanceId = null;
        try {
            processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_EVALUATION, parameters);
            assertNotNull(processInstanceId);
            assertTrue(processInstanceId.longValue() > 0);

            List<TaskSummary> tasks = taskClient.findTasksAssignedAsBusinessAdministrator(USER_ADMINISTRATOR, 0, 10);
            assertEquals(1, tasks.size());

            TaskSummary task = tasks.get(0);

            TaskInstance instance = taskClient.getTaskInstance(CONTAINER_ID, task.getId(), false, false, true);
            assertNotNull(instance);

            List<String> potOwners = instance.getPotentialOwners();
            assertEquals(3, potOwners.size());
            assertTrue(potOwners.contains(USER_YODA));
            assertTrue(potOwners.contains("PM"));
            assertTrue(potOwners.contains("HR"));

            OrgEntities add = OrgEntities.builder().users(Arrays.asList(USER_JOHN)).build();

            userTaskAdminClient.addPotentialOwners(CONTAINER_ID, task.getId(), false, add);

            instance = taskClient.getTaskInstance(CONTAINER_ID, task.getId(), false, false, true);
            assertNotNull(instance);
            potOwners = instance.getPotentialOwners();
            assertEquals(4, potOwners.size());
            assertTrue(potOwners.contains(USER_YODA));
            assertTrue(potOwners.contains(USER_JOHN));
            assertTrue(potOwners.contains("PM"));
            assertTrue(potOwners.contains("HR"));

            userTaskAdminClient.removePotentialOwnerUsers(CONTAINER_ID, task.getId(), USER_YODA);

            instance = taskClient.getTaskInstance(CONTAINER_ID, task.getId(), false, false, true);
            assertNotNull(instance);
            potOwners = instance.getPotentialOwners();
            assertEquals(3, potOwners.size());
            assertTrue(potOwners.contains(USER_JOHN));
            assertTrue(potOwners.contains("PM"));
            assertTrue(potOwners.contains("HR"));

            userTaskAdminClient.removePotentialOwnerGroups(CONTAINER_ID, task.getId(), "PM", "HR");
            instance = taskClient.getTaskInstance(CONTAINER_ID, task.getId(), false, false, true);
            assertNotNull(instance);
            potOwners = instance.getPotentialOwners();
            assertEquals(1, potOwners.size());
            assertTrue(potOwners.contains(USER_JOHN));

            add = OrgEntities.builder().users(Arrays.asList(USER_YODA)).groups(Arrays.asList("PM")).build();

            userTaskAdminClient.addPotentialOwners(CONTAINER_ID, task.getId(), false, add);

            instance = taskClient.getTaskInstance(CONTAINER_ID, task.getId(), false, false, true);
            assertNotNull(instance);
            potOwners = instance.getPotentialOwners();
            assertEquals(3, potOwners.size());
            assertTrue(potOwners.contains(USER_YODA));
            assertTrue(potOwners.contains(USER_JOHN));
            assertTrue(potOwners.contains("PM"));

        } finally {
            if (processInstanceId != null) {
                processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
            }
        }
    }

    @Test
    public void testAddRemoveExcludedOwners() throws Exception {
        changeUser(USER_ADMINISTRATOR);
        Map<String, Object> parameters = new HashMap<String, Object>();

        Long processInstanceId = null;
        try {
            processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_EVALUATION, parameters);
            assertNotNull(processInstanceId);
            assertTrue(processInstanceId.longValue() > 0);

            List<TaskSummary> tasks = taskClient.findTasksAssignedAsBusinessAdministrator(USER_ADMINISTRATOR, 0, 10);
            assertEquals(1, tasks.size());

            TaskSummary task = tasks.get(0);

            TaskInstance instance = taskClient.getTaskInstance(CONTAINER_ID, task.getId(), false, false, true);
            assertNotNull(instance);

            List<String> excludedOwners = instance.getExcludedOwners();
            assertEquals(0, excludedOwners.size());

            OrgEntities add = OrgEntities.builder().users(Arrays.asList(USER_JOHN)).build();

            userTaskAdminClient.addExcludedOwners(CONTAINER_ID, task.getId(), false, add);

            instance = taskClient.getTaskInstance(CONTAINER_ID, task.getId(), false, false, true);
            assertNotNull(instance);
            excludedOwners = instance.getExcludedOwners();
            assertEquals(1, excludedOwners.size());
            assertTrue(excludedOwners.contains(USER_JOHN));

            userTaskAdminClient.removeExcludedOwnerUsers(CONTAINER_ID, task.getId(), USER_JOHN);

            instance = taskClient.getTaskInstance(CONTAINER_ID, task.getId(), false, false, true);
            assertNotNull(instance);
            excludedOwners = instance.getExcludedOwners();
            assertEquals(0, excludedOwners.size());

            add = OrgEntities.builder().users(Arrays.asList(USER_YODA)).groups(Arrays.asList("PM")).build();

            userTaskAdminClient.addExcludedOwners(CONTAINER_ID, task.getId(), false, add);

            instance = taskClient.getTaskInstance(CONTAINER_ID, task.getId(), false, false, true);
            assertNotNull(instance);
            excludedOwners = instance.getExcludedOwners();
            assertEquals(2, excludedOwners.size());
            assertTrue(excludedOwners.contains(USER_YODA));
            assertTrue(excludedOwners.contains("PM"));

            userTaskAdminClient.removeExcludedOwnerGroups(CONTAINER_ID, task.getId(), "PM");

            instance = taskClient.getTaskInstance(CONTAINER_ID, task.getId(), false, false, true);
            assertNotNull(instance);
            excludedOwners = instance.getExcludedOwners();
            assertEquals(1, excludedOwners.size());
            assertTrue(excludedOwners.contains(USER_YODA));

        } finally {
            if (processInstanceId != null) {
                processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
            }
        }
    }

    @Test
    public void testAddRemoveBusinessAdmins() throws Exception {
        changeUser(USER_ADMINISTRATOR);
        Map<String, Object> parameters = new HashMap<String, Object>();

        Long processInstanceId = null;
        try {
            processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_EVALUATION, parameters);
            assertNotNull(processInstanceId);
            assertTrue(processInstanceId.longValue() > 0);

            List<TaskSummary> tasks = taskClient.findTasksAssignedAsBusinessAdministrator(USER_ADMINISTRATOR, 0, 10);
            assertEquals(1, tasks.size());

            TaskSummary task = tasks.get(0);

            TaskInstance instance = taskClient.getTaskInstance(CONTAINER_ID, task.getId(), false, false, true);
            assertNotNull(instance);

            List<String> businessAdmins = instance.getBusinessAdmins();
            assertEquals(2, businessAdmins.size());
            assertTrue(businessAdmins.contains(USER_ADMINISTRATOR));
            assertTrue(businessAdmins.contains("Administrators"));

            OrgEntities add = OrgEntities.builder().users(Arrays.asList(USER_JOHN)).build();

            userTaskAdminClient.addBusinessAdmins(CONTAINER_ID, task.getId(), false, add);

            instance = taskClient.getTaskInstance(CONTAINER_ID, task.getId(), false, false, true);
            assertNotNull(instance);
            businessAdmins = instance.getBusinessAdmins();
            assertEquals(3, businessAdmins.size());
            assertTrue(businessAdmins.contains(USER_ADMINISTRATOR));
            assertTrue(businessAdmins.contains("Administrators"));
            assertTrue(businessAdmins.contains(USER_JOHN));

            userTaskAdminClient.removeBusinessAdminUsers(CONTAINER_ID, task.getId(), USER_JOHN);

            instance = taskClient.getTaskInstance(CONTAINER_ID, task.getId(), false, false, true);
            assertNotNull(instance);
            businessAdmins = instance.getBusinessAdmins();
            assertEquals(2, businessAdmins.size());
            assertTrue(businessAdmins.contains(USER_ADMINISTRATOR));
            assertTrue(businessAdmins.contains("Administrators"));

            add = OrgEntities.builder().users(Arrays.asList(USER_YODA)).groups(Arrays.asList("Administrators2")).build();

            userTaskAdminClient.addBusinessAdmins(CONTAINER_ID, task.getId(), false, add);

            instance = taskClient.getTaskInstance(CONTAINER_ID, task.getId(), false, false, true);
            assertNotNull(instance);
            businessAdmins = instance.getBusinessAdmins();
            assertEquals(4, businessAdmins.size());
            assertTrue(businessAdmins.contains(USER_YODA));
            assertTrue(businessAdmins.contains(USER_ADMINISTRATOR));
            assertTrue(businessAdmins.contains("Administrators"));
            assertTrue(businessAdmins.contains("Administrators2"));


            userTaskAdminClient.removeBusinessAdminGroups(CONTAINER_ID, task.getId(), "Administrators2");
            instance = taskClient.getTaskInstance(CONTAINER_ID, task.getId(), false, false, true);
            assertNotNull(instance);
            businessAdmins = instance.getBusinessAdmins();
            assertEquals(3, businessAdmins.size());
            assertTrue(businessAdmins.contains(USER_ADMINISTRATOR));
            assertTrue(businessAdmins.contains("Administrators"));
            assertTrue(businessAdmins.contains(USER_YODA));

        } finally {
            if (processInstanceId != null) {
                processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
            }
        }
    }

    @Test
    public void testManageTaskInputAndOutput() throws Exception {
        changeUser(USER_ADMINISTRATOR);
        Map<String, Object> parameters = new HashMap<String, Object>();

        Long processInstanceId = null;
        try {
            processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_EVALUATION, parameters);
            assertNotNull(processInstanceId);
            assertTrue(processInstanceId.longValue() > 0);

            List<TaskSummary> tasks = taskClient.findTasksAssignedAsBusinessAdministrator(USER_ADMINISTRATOR, 0, 10);
            assertEquals(1, tasks.size());

            TaskSummary task = tasks.get(0);

            TaskInstance instance = taskClient.getTaskInstance(CONTAINER_ID, task.getId(), true, false, false);
            assertNotNull(instance);

            Map<String, Object> input = instance.getInputData();
            assertNotNull(input);
            assertEquals(4, input.size());
            assertFalse(input.containsKey("new content"));

            Map<String, Object> data = new HashMap<>();
            data.put("new content", "test");

            userTaskAdminClient.addTaskInputs(CONTAINER_ID, task.getId(), data);

            instance = taskClient.getTaskInstance(CONTAINER_ID, task.getId(), true, false, false);
            assertNotNull(instance);

            input = instance.getInputData();
            assertNotNull(input);
            assertEquals(5, input.size());
            assertEquals("test", input.get("new content"));

            userTaskAdminClient.removeTaskInputs(CONTAINER_ID, task.getId(), "new content");
            instance = taskClient.getTaskInstance(CONTAINER_ID, task.getId(), true, false, false);
            assertNotNull(instance);

            input = instance.getInputData();
            assertNotNull(input);
            assertEquals(4, input.size());
            assertFalse(input.containsKey("new content"));

            taskClient.saveTaskContent(CONTAINER_ID, task.getId(), data);
            instance = taskClient.getTaskInstance(CONTAINER_ID, task.getId(), false, true, false);
            assertNotNull(instance);

            Map<String, Object> output = instance.getOutputData();
            assertNotNull(output);
            assertEquals(1, output.size());
            assertEquals("test", output.get("new content"));

            userTaskAdminClient.removeTaskOutputs(CONTAINER_ID, task.getId(), "new content");
            instance = taskClient.getTaskInstance(CONTAINER_ID, task.getId(), false, true, false);
            assertNotNull(instance);

            output = instance.getOutputData();
            assertNotNull(output);
            assertEquals(0, output.size());

        } finally {
            if (processInstanceId != null) {
                processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
            }
        }
    }

    @Test
    public void testReassignmentWhenNotStarted() throws Exception {
        testReassignment(true);
    }

    @Test
    public void testReassignmentWhenNotCompleted() throws Exception {

        testReassignment(false);
    }


    private void testReassignment(boolean whenNotStarted) throws Exception {

        Map<String, Object> parameters = new HashMap<String, Object>();

        Long processInstanceId = null;
        try {
            processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_EVALUATION, parameters);
            assertNotNull(processInstanceId);
            assertTrue(processInstanceId.longValue() > 0);
            changeUser(USER_YODA);
            List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertEquals(1, tasks.size());
            long taskId = tasks.get(0).getId();
            taskClient.claimTask(CONTAINER_ID, taskId, USER_YODA);

            changeUser(USER_ADMINISTRATOR);

            TaskInstance instance = taskClient.getTaskInstance(CONTAINER_ID, taskId, false, false, true);
            assertNotNull(instance);
            assertEquals("Reserved", instance.getStatus());

            List<String> potOwners = instance.getPotentialOwners();
            assertEquals(3, potOwners.size());
            assertTrue(potOwners.contains(USER_YODA));
            assertTrue(potOwners.contains("PM"));
            assertTrue(potOwners.contains("HR"));

            OrgEntities reassign = OrgEntities.builder().users(Arrays.asList(USER_JOHN)).build();

            if (whenNotStarted) {
                userTaskAdminClient.reassignWhenNotStarted(CONTAINER_ID, taskId, "2s", reassign);
            } else {
                userTaskAdminClient.reassignWhenNotCompleted(CONTAINER_ID, taskId, "2s", reassign);
            }


            KieServerSynchronization.waitForTaskStatus(taskClient, taskId, "Ready");

            instance = taskClient.getTaskInstance(CONTAINER_ID, taskId, false, false, true);
            assertNotNull(instance);
            assertEquals("Ready", instance.getStatus());

            potOwners = instance.getPotentialOwners();
            assertEquals(1, potOwners.size());
            assertTrue(potOwners.contains(USER_JOHN));


        } finally {
            if (processInstanceId != null) {
                processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
            }
        }
    }

    @Test
    public void testCancelReassignWhenNotStarted() throws Exception {
        testCancelReassign(true);
    }

    @Test
    public void testCancelReassignWhenNotCompleted() throws Exception {

        testCancelReassign(false);
    }

    private void testCancelReassign(boolean whenNotStarted) throws Exception{
        Map<String, Object> parameters = new HashMap<String, Object>();

        Long processInstanceId = null;
        try {
            processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_EVALUATION, parameters);
            assertNotNull(processInstanceId);
            assertTrue(processInstanceId.longValue() > 0);
            changeUser(USER_YODA);
            List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertEquals(1, tasks.size());
            long taskId = tasks.get(0).getId();
            taskClient.claimTask(CONTAINER_ID, taskId, USER_YODA);

            changeUser(USER_ADMINISTRATOR);

            List<TaskReassignment> reassignments = userTaskAdminClient.getTaskReassignments(CONTAINER_ID, taskId, true);
            assertNotNull(reassignments);
            assertEquals(0, reassignments.size());

            OrgEntities reassign = OrgEntities.builder().users(Arrays.asList(USER_JOHN)).build();
            Long reassignmentId = null;
            if (whenNotStarted) {
                reassignmentId = userTaskAdminClient.reassignWhenNotStarted(CONTAINER_ID, taskId, "10s", reassign);
            } else {
                reassignmentId = userTaskAdminClient.reassignWhenNotCompleted(CONTAINER_ID, taskId, "10s", reassign);
            }

            reassignments = userTaskAdminClient.getTaskReassignments(CONTAINER_ID, taskId, true);
            assertNotNull(reassignments);
            assertEquals(1, reassignments.size());

            userTaskAdminClient.cancelReassignment(CONTAINER_ID, taskId, reassignmentId);

            reassignments = userTaskAdminClient.getTaskReassignments(CONTAINER_ID, taskId, true);
            assertNotNull(reassignments);
            assertEquals(0, reassignments.size());

        } finally {
            if (processInstanceId != null) {
                processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
            }
        }
    }

    @Test
    public void testCancelNotifyWhenNotStarted() throws Exception {
        testCancelNotify(true);
    }

    @Test
    public void testCancelNotifyWhenNotCompleted() throws Exception {

        testCancelNotify(false);
    }

    private void testCancelNotify(boolean whenNotStarted) throws Exception{
        Map<String, Object> parameters = new HashMap<String, Object>();

        Long processInstanceId = null;
        try {
            processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_EVALUATION, parameters);
            assertNotNull(processInstanceId);
            assertTrue(processInstanceId.longValue() > 0);
            changeUser(USER_YODA);
            List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertEquals(1, tasks.size());
            long taskId = tasks.get(0).getId();
            taskClient.claimTask(CONTAINER_ID, taskId, USER_YODA);

            changeUser(USER_ADMINISTRATOR);

            List<TaskNotification> notifications = userTaskAdminClient.getTaskNotifications(CONTAINER_ID, taskId, true);
            assertNotNull(notifications);
            assertEquals(0, notifications.size());

            EmailNotification emailNotification = EmailNotification.builder()
                    .from("test@jbpm.org")
                    .replyTo("no-reply@jbpm.org")
                    .subject("reminder")
                    .body("my test content")
                    .users(Arrays.asList(USER_JOHN))
                    .build();
            Long notificationId = null;
            if (whenNotStarted) {
                notificationId = userTaskAdminClient.notifyWhenNotStarted(CONTAINER_ID, taskId, "10s", emailNotification);
            } else {
                notificationId = userTaskAdminClient.notifyWhenNotCompleted(CONTAINER_ID, taskId, "10s", emailNotification);
            }

            notifications = userTaskAdminClient.getTaskNotifications(CONTAINER_ID, taskId, true);
            assertNotNull(notifications);
            assertEquals(1, notifications.size());

            userTaskAdminClient.cancelNotification(CONTAINER_ID, taskId, notificationId);

            notifications = userTaskAdminClient.getTaskNotifications(CONTAINER_ID, taskId, true);
            assertNotNull(notifications);
            assertEquals(0, notifications.size());

        } finally {
            if (processInstanceId != null) {
                processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
            }
        }
    }
}

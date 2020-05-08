/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.services.taskassigning.planning;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.kie.api.task.model.Status;
import org.kie.server.api.model.taskassigning.PlanningTask;
import org.kie.server.api.model.taskassigning.TaskData;
import org.kie.server.services.taskassigning.core.model.DefaultSolutionFactory;
import org.kie.server.services.taskassigning.core.model.SolutionFactory;
import org.kie.server.services.taskassigning.core.model.Task;
import org.kie.server.services.taskassigning.core.model.TaskAssigningSolution;
import org.kie.server.services.taskassigning.core.model.User;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import static org.junit.Assert.assertTrue;
import static org.kie.api.task.model.Status.InProgress;
import static org.kie.api.task.model.Status.Ready;
import static org.kie.api.task.model.Status.Reserved;
import static org.kie.api.task.model.Status.Suspended;
import static org.kie.server.api.model.taskassigning.util.StatusConverter.convertToString;
import static org.kie.server.services.taskassigning.core.model.ModelConstants.DUMMY_TASK;
import static org.kie.server.services.taskassigning.core.model.ModelConstants.PLANNING_USER_ID;
import static org.kie.server.services.taskassigning.core.model.solver.TaskHelper.extractTasks;
import static org.kie.server.services.taskassigning.planning.TestUtil.assertContains;
import static org.kie.server.services.taskassigning.planning.TestUtil.mockExternalUser;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class SolutionBuilderTest {

    // enabled user
    private static final String USER1 = "USER1";
    // disabled user
    private static final String USER2 = "USER2";
    // enabled user
    private static final String USER3 = "USER3";
    // user not present in the external users
    private static final String USER_NOT_PRESENT = "USER_NOT_PRESENT";

    private SolverHandlerContext context;

    private SolutionFactory solutionFactory;

    @Before
    public void setUp() {
        context = new SolverHandlerContext(Duration.ofMillis(2000));
        solutionFactory = spy(new DefaultSolutionFactory());
    }

    @Test
    public void buildAndCheckUsersWhereAdded() {
        List<org.kie.server.services.taskassigning.user.system.api.User> externalUsers = buildExternalUsers();
        TaskAssigningSolution solution = SolutionBuilder.create()
                .withTasks(Collections.emptyList())
                .withUsers(externalUsers)
                .withContext(context)
                .withSolutionFactory(solutionFactory)
                .build();

        assertContains(USER1, 1, solution.getUserList());
        assertContains(USER2, 1, solution.getUserList());
        assertContains(USER3, 1, solution.getUserList());
        assertContains(PLANNING_USER_ID, solution.getUserList()); //is always added.
        assertEquals(4, solution.getUserList().size(), 0);
    }

    @Test
    public void buildAndCheckDummyTaskWasAdded() {
        TaskAssigningSolution solution = SolutionBuilder.create()
                .withTasks(Collections.emptyList())
                .withUsers(Collections.emptyList())
                .withContext(context)
                .withSolutionFactory(solutionFactory)
                .build();
        assertEquals(1, solution.getTaskList().size());
        assertEquals(DUMMY_TASK, solution.getTaskList().get(0));
    }

    @Test
    public void buildAndCheckReadyTaskWasProcessedCorrect() {
        List<org.kie.server.services.taskassigning.user.system.api.User> externalUsers = buildExternalUsers();
        TaskData taskData = mockTaskData(1L, Ready);
        TaskAssigningSolution solution = SolutionBuilder.create()
                .withTasks(Collections.singletonList(taskData))
                .withUsers(externalUsers)
                .withContext(context)
                .withSolutionFactory(solutionFactory)
                .build();
        assertEquals(2, solution.getTaskList().size());
        assertContainsNotAssignedTask(taskData, solution);
        verify(solutionFactory).newSolution();
    }

    @Test
    public void buildAndCheckReservedTaskWithNoPlanningTaskWasProcessedCorrect() {
        buildAndCheckTaskWithNoPlanningTaskWasProcessedCorrect(mockTaskData(1L, Reserved, USER1), true);
    }

    @Test
    public void buildAndCheckInProgressTaskWithNoPlanningTaskWasProcessedCorrect() {
        buildAndCheckTaskWithNoPlanningTaskWasProcessedCorrect(mockTaskData(1L, InProgress, USER1), true);
    }

    @Test
    public void buildAndCheckSuspendedTaskWithNoPlanningTaskWasProcessedCorrect() {
        buildAndCheckTaskWithNoPlanningTaskWasProcessedCorrect(mockTaskData(1L, Suspended, USER1), true);
    }

    @Test
    public void buildAndCheckTaskForNotPresentUserWithNoPlanningTaskWasProcessedCorrect() {
        buildAndCheckTaskWithNoPlanningTaskWasProcessedCorrect(mockTaskData(1L, Reserved, USER_NOT_PRESENT), true);
    }

    @Test
    public void buildAndCheckReservedTaskWithUnChangedPlanningTaskPublishedWasProcessedCorrect() {
        PlanningTask planningTask = PlanningTask.builder()
                .taskId(1L)
                .assignedUser(USER1)
                .published(true)
                .index(1)
                .build();
        TaskData taskData = mockTaskData(1L, Reserved, planningTask.getAssignedUser());
        buildAndCheckTaskWithPlanningTaskWasProcessedCorrect(taskData, planningTask, planningTask.getAssignedUser(), true);
    }

    @Test
    public void buildAndCheckReservedTaskWithUnChangedPlanningTaskNotPublishedWasProcessedCorrect() {
        PlanningTask planningTask = PlanningTask.builder()
                .taskId(1L)
                .assignedUser(USER1)
                .published(false)
                .index(1)
                .build();
        TaskData taskData = mockTaskData(1L, Reserved, planningTask.getAssignedUser());
        buildAndCheckTaskWithPlanningTaskWasProcessedCorrect(taskData, planningTask, planningTask.getAssignedUser(), false);
    }

    @Test
    public void buildAndCheckInProgressTaskWithUnChangedPlanningTaskNotPublishedWasProcessedCorrect() {
        PlanningTask planningTask = PlanningTask.builder()
                .taskId(1L)
                .assignedUser(USER1)
                .published(false)
                .index(1)
                .build();
        TaskData taskData = mockTaskData(1L, InProgress, planningTask.getAssignedUser());
        buildAndCheckTaskWithPlanningTaskWasProcessedCorrect(taskData, planningTask, planningTask.getAssignedUser(), true);
    }

    @Test
    public void buildAndCheckInProgressTaskWithUnChangedPlanningTaskPublishedWasProcessedCorrect() {
        PlanningTask planningTask = PlanningTask.builder()
                .taskId(1L)
                .assignedUser(USER1)
                .published(true)
                .index(1)
                .build();
        TaskData taskData = mockTaskData(1L, InProgress, planningTask.getAssignedUser());
        buildAndCheckTaskWithPlanningTaskWasProcessedCorrect(taskData, planningTask, planningTask.getAssignedUser(), true);
    }

    @Test
    public void buildAndCheckSuspendedTaskWithUnChangedPlanningTaskNotPublishedWasProcessedCorrect() {
        PlanningTask planningTask = PlanningTask.builder()
                .taskId(1L)
                .assignedUser(USER1)
                .published(false)
                .index(1)
                .build();
        TaskData taskData = mockTaskData(1L, Suspended, planningTask.getAssignedUser());
        buildAndCheckTaskWithPlanningTaskWasProcessedCorrect(taskData, planningTask, planningTask.getAssignedUser(), true);
    }

    @Test
    public void buildAndCheckSuspendedTaskWithUnChangedPlanningTaskPublishedWasProcessedCorrect() {
        PlanningTask planningTask = PlanningTask.builder()
                .taskId(1L)
                .assignedUser(USER1)
                .published(true)
                .index(1)
                .build();
        TaskData taskData = mockTaskData(1L, Suspended, planningTask.getAssignedUser());
        buildAndCheckTaskWithPlanningTaskWasProcessedCorrect(taskData, planningTask, planningTask.getAssignedUser(), true);
    }

    @Test
    public void buildAndCheckReservedTaskWithModifiedPlanningTaskPublishedWasProcessedCorrect() {
        PlanningTask planningTask = PlanningTask.builder()
                .taskId(1L)
                .assignedUser(USER1)
                .published(true)
                .index(1)
                .build();
        TaskData taskData = mockTaskData(1L, Reserved, USER2);
        buildAndCheckTaskWithPlanningTaskWasProcessedCorrect(taskData, planningTask, taskData.getActualOwner(), true);
    }

    @Test
    public void buildAndCheckReservedTaskWithModifiedPlanningTaskNotPublishedWasProcessedCorrect() {
        PlanningTask planningTask = PlanningTask.builder()
                .taskId(1L)
                .assignedUser(USER1)
                .published(false)
                .index(1)
                .build();
        TaskData taskData = mockTaskData(1L, Reserved, USER2);
        buildAndCheckTaskWithPlanningTaskWasProcessedCorrect(taskData, planningTask, taskData.getActualOwner(), true);
    }

    @Test
    public void buildAndCheckReservedForPlanningUserTaskWithModifiedPlanningTaskPublishedWasProcessedCorrect() {
        PlanningTask planningTask = PlanningTask.builder()
                .taskId(1L)
                .assignedUser(USER1)
                .published(true)
                .index(1)
                .build();
        TaskData taskData = mockTaskData(1L, Reserved, PLANNING_USER_ID);
        buildAndCheckTaskWithPlanningTaskWasProcessedCorrect(taskData, planningTask, taskData.getActualOwner(), false);
    }

    @Test
    public void buildAndCheckReservedForPlanningUserTaskWithModifiedPlanningTaskNotPublishedWasProcessedCorrect() {
        PlanningTask planningTask = PlanningTask.builder()
                .taskId(1L)
                .assignedUser(USER1)
                .published(false)
                .index(1)
                .build();
        TaskData taskData = mockTaskData(1L, Reserved, PLANNING_USER_ID);
        buildAndCheckTaskWithPlanningTaskWasProcessedCorrect(taskData, planningTask, taskData.getActualOwner(), false);
    }

    @Test
    public void buildAndCheckInProgressTaskWithModifiedPlanningTaskPublishedWasProcessedCorrect() {
        PlanningTask planningTask = PlanningTask.builder()
                .taskId(1L)
                .assignedUser(USER1)
                .published(true)
                .index(1)
                .build();
        TaskData taskData = mockTaskData(1L, InProgress, USER2);
        buildAndCheckTaskWithPlanningTaskWasProcessedCorrect(taskData, planningTask, taskData.getActualOwner(), true);
    }

    @Test
    public void buildAndCheckInProgressTaskWithModifiedPlanningTaskNotPublishedWasProcessedCorrect() {
        PlanningTask planningTask = PlanningTask.builder()
                .taskId(1L)
                .assignedUser(USER1)
                .published(false)
                .index(1)
                .build();
        TaskData taskData = mockTaskData(1L, InProgress, USER2);
        buildAndCheckTaskWithPlanningTaskWasProcessedCorrect(taskData, planningTask, taskData.getActualOwner(), true);
    }

    @Test
    public void buildAndCheckSuspendedTaskWithModifiedPlanningTaskPublishedWasProcessedCorrect() {
        PlanningTask planningTask = PlanningTask.builder()
                .taskId(1L)
                .assignedUser(USER1)
                .published(true)
                .index(1)
                .build();
        TaskData taskData = mockTaskData(1L, Suspended, USER2);
        buildAndCheckTaskWithPlanningTaskWasProcessedCorrect(taskData, planningTask, taskData.getActualOwner(), true);
    }

    @Test
    public void buildAndCheckSuspendedTaskWithModifiedPlanningTaskNotPublishedWasProcessedCorrect() {
        PlanningTask planningTask = PlanningTask.builder()
                .taskId(1L)
                .assignedUser(USER1)
                .published(false)
                .index(1)
                .build();
        TaskData taskData = mockTaskData(1L, Suspended, USER2);
        buildAndCheckTaskWithPlanningTaskWasProcessedCorrect(taskData, planningTask, taskData.getActualOwner(), true);
    }

    @Test
    public void buildAnExample() {
        PlanningTask planningTask1 = PlanningTask.builder()
                .taskId(1L)
                .assignedUser(USER1)
                .published(false)
                .index(1)
                .build();
        TaskData taskData1 = mockTaskData(1L, Reserved, USER1);
        taskData1.setPlanningTask(planningTask1);

        PlanningTask planningTask2 = PlanningTask.builder()
                .taskId(2L)
                .assignedUser(USER1)
                .published(true)
                .index(2)
                .build();
        TaskData taskData2 = mockTaskData(2L, Suspended, USER1);
        taskData2.setPlanningTask(planningTask2);

        PlanningTask planningTask3 = PlanningTask.builder()
                .taskId(3L)
                .assignedUser(USER1)
                .published(true)
                .index(3)
                .build();
        TaskData taskData3 = mockTaskData(3L, InProgress, USER1);
        taskData3.setPlanningTask(planningTask3);

        PlanningTask planningTask4 = PlanningTask.builder()
                .taskId(4L)
                .assignedUser(USER2)
                .published(false)
                .index(4)
                .build();
        TaskData taskData4 = mockTaskData(4L, InProgress, USER1);
        taskData4.setPlanningTask(planningTask4);

        List<org.kie.server.services.taskassigning.user.system.api.User> externalUsers = buildExternalUsers();
        List<TaskData> taskDataList = Arrays.asList(taskData4, taskData1, taskData3, taskData2);
        TaskAssigningSolution<?> solution = SolutionBuilder.create()
                .withTasks(taskDataList)
                .withUsers(externalUsers)
                .withContext(context)
                .withSolutionFactory(solutionFactory)
                .build();

        assertEquals(5, solution.getTaskList().size());
        User user1 = solution.getUserList().stream()
                .filter(user -> user.getEntityId().equals(USER1))
                .findFirst().orElse(null);
        assertNotNull(user1);
        List<Task> user1Tasks = extractTasks(user1, (task) -> true);
        assertEquals(4L, user1Tasks.size(), 0);

        assertExpectedTaskAtPosition(taskData2.getTaskId(), 0, true, user1Tasks);
        assertExpectedTaskAtPosition(taskData3.getTaskId(), 1, true, user1Tasks);
        assertExpectedTaskAtPosition(taskData4.getTaskId(), 2, true, user1Tasks);
        assertExpectedTaskAtPosition(taskData1.getTaskId(), 3, false, user1Tasks);
        taskDataList.forEach(taskData -> assertTaskChangeWasProcessed(taskData.getTaskId(), taskData.getLastModificationDate()));
        verify(solutionFactory).newSolution();
    }

    private void assertExpectedTaskAtPosition(long taskId, int position, boolean pinned, List<Task> tasks) {
        assertEquals("Task: " + taskId + " is expected at position 0", taskId, tasks.get(position).getId(), 0);
        assertEquals("Task: " + taskId + " with pinned = " + pinned + " is expected at position 0", pinned, tasks.get(position).isPinned());
    }

    private void buildAndCheckTaskWithNoPlanningTaskWasProcessedCorrect(TaskData taskData, boolean pinned) {
        List<org.kie.server.services.taskassigning.user.system.api.User> externalUsers = buildExternalUsers();
        TaskAssigningSolution solution = SolutionBuilder.create()
                .withTasks(Collections.singletonList(taskData))
                .withUsers(externalUsers)
                .withContext(context)
                .withSolutionFactory(solutionFactory)
                .build();
        assertEquals(2, solution.getTaskList().size());
        assertContainsAssignedTask(taskData, taskData.getActualOwner(), pinned, solution);
        assertTaskChangeWasProcessed(taskData.getTaskId(), taskData.getLastModificationDate());
        verify(solutionFactory).newSolution();
    }

    private void buildAndCheckTaskWithPlanningTaskWasProcessedCorrect(TaskData taskData, PlanningTask planningTask, String owner, boolean pinned) {
        taskData.setPlanningTask(planningTask);
        List<org.kie.server.services.taskassigning.user.system.api.User> externalUsers = buildExternalUsers();
        TaskAssigningSolution solution = SolutionBuilder.create()
                .withTasks(Collections.singletonList(taskData))
                .withUsers(externalUsers)
                .withContext(context)
                .withSolutionFactory(solutionFactory)
                .build();
        assertEquals(2, solution.getTaskList().size());
        assertContainsAssignedTask(taskData, owner, pinned, solution);
        assertTaskChangeWasProcessed(taskData.getTaskId(), taskData.getLastModificationDate());
        verify(solutionFactory).newSolution();
    }

    private List<org.kie.server.services.taskassigning.user.system.api.User> buildExternalUsers() {
        org.kie.server.services.taskassigning.user.system.api.User externalUser1 =
                mockExternalUser(USER1, Collections.emptySet());
        org.kie.server.services.taskassigning.user.system.api.User externalUser2 =
                mockExternalUser(USER2, Collections.emptySet());
        org.kie.server.services.taskassigning.user.system.api.User externalUser3 =
                mockExternalUser(USER3, Collections.emptySet());
        return Arrays.asList(externalUser1, externalUser2, externalUser3, externalUser2,
                             externalUser3, externalUser1, externalUser1, externalUser3, externalUser3, externalUser2);
    }

    private TaskData mockTaskData(Long taskId, Status status) {
        return mockTaskData(taskId, status, null);
    }

    private TaskData mockTaskData(Long taskId, Status status, String actualOwner) {
        TaskData taskData = new TaskData();
        taskData.setTaskId(taskId);
        taskData.setActualOwner(actualOwner);
        taskData.setStatus(convertToString(status));
        taskData.setPriority(0);
        taskData.setProcessInstanceId(1L);
        taskData.setLastModificationDate(LocalDateTime.now());
        return taskData;
    }

    private void assertContainsNotAssignedTask(TaskData taskData, TaskAssigningSolution<?> solution) {
        Task task = solution.getTaskList().stream()
                .filter(t -> taskData.getTaskId().equals(t.getId()))
                .findFirst().orElse(null);
        assertNotNull("Task: " + taskData.getTaskId() + " must be present in the solution.", task);
        assertNull("Task: " + taskData.getTaskId() + " must not be assigned to any user", task.getUser());
        assertTaskChangeWasProcessed(taskData.getTaskId(), taskData.getLastModificationDate());
    }

    private void assertContainsAssignedTask(TaskData taskData, String userId, boolean pinned, TaskAssigningSolution<?> solution) {
        Task task = solution.getTaskList().stream()
                .filter(t -> taskData.getTaskId().equals(t.getId()))
                .findFirst().orElse(null);
        User user = solution.getUserList().stream()
                .filter(u -> userId.equals(u.getEntityId()))
                .findFirst().orElse(null);
        assertNotNull("Task: " + taskData.getTaskId() + " must be present in the solution.", task);
        assertNotNull("Task: " + taskData.getTaskId() + " must assigned to user: " + userId + " but user is not be present in the solution.", user);
        assertNotNull("Task: " + taskData.getTaskId() + " must be assigned to user: " + userId + " but has no assigned user.", task.getUser());
        assertEquals("Task: " + taskData.getTaskId() + " must be assigned to user: " + userId, userId, task.getUser().getEntityId());
        assertEquals("Task: " + taskData.getTaskId() + " must have pinned = " + pinned, pinned, task.isPinned());
    }

    private void assertTaskChangeWasProcessed(long taskId, LocalDateTime changeTime) {
        assertTrue("change: " + changeTime + " must have been processed for task: " + taskId, context.isProcessedTaskChange(taskId, changeTime));
    }
}

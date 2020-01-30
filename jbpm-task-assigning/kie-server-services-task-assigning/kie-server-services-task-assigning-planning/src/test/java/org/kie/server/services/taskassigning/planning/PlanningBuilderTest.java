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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.kie.server.api.model.taskassigning.PlanningItem;
import org.kie.server.services.taskassigning.core.model.Task;
import org.kie.server.services.taskassigning.core.model.TaskAssigningSolution;
import org.kie.server.services.taskassigning.core.model.User;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.kie.server.services.taskassigning.core.model.ModelConstants.PLANNING_USER_ID;
import static org.kie.server.services.taskassigning.planning.TestUtil.initializeUser;
import static org.kie.server.services.taskassigning.planning.TestUtil.mockUser;
import static org.kie.server.services.taskassigning.planning.util.UserUtil.extractTasks;

public class PlanningBuilderTest {

    private static final int USER1_TOTAL_TASKS = 5;
    private static final int USER1_PINNED_TASKS = 3;

    private static final int USER2_TOTAL_TASKS = 5;
    private static final int USER2_PINNED_TASKS = 2;

    private static final int USER3_TOTAL_TASKS = 5;
    private static final int USER3_PINNED_TASKS = 1;

    private static final int USER4_TOTAL_TASKS = 10;
    private static final int USER4_PINNED_TASKS = 0;

    private static final int USER5_TOTAL_TASKS = 10;
    private static final int USER5_PINNED_TASKS = 0;

    private static final int USER6_TOTAL_TASKS = 0;
    private static final int USER6_PINNED_TASKS = 0;

    private static final int PLANNING_USER_TASKS = 6;

    private long ids = 0;

    @Test
    public void buildForPublishWindowSize0() {
        build(0);
    }

    @Test
    public void buildForPublishWindowSize1() {
        build(1);
    }

    @Test
    public void buildForPublishWindowSize2() {
        build(2);
    }

    @Test
    public void buildForPublishWindowSize3() {
        build(3);
    }

    @Test
    public void buildForPublishWindowSize4() {
        build(4);
    }

    @Test
    public void buildForPublishWindowSize5() {
        build(5);
    }

    @Test
    public void buildForPublishWindowSize6() {
        build(6);
    }

    private void build(int publishWindowSize) {
        List<Task> user1Tasks = mockTasks(USER1_TOTAL_TASKS, USER1_PINNED_TASKS, "container1", 1L);
        User user1 = mockUser(1, user1Tasks);

        List<Task> user2Tasks = mockTasks(USER2_TOTAL_TASKS, USER2_PINNED_TASKS, "container2", 2L);
        User user2 = mockUser(2, user2Tasks);

        List<Task> user3Tasks = mockTasks(USER3_TOTAL_TASKS, USER3_PINNED_TASKS, "container3", 3L);
        User user3 = mockUser(3, user3Tasks);

        List<Task> user4Tasks = mockTasks(USER4_TOTAL_TASKS, USER4_PINNED_TASKS, "container4", 4L);
        User user4 = mockUser(4, user4Tasks);

        List<Task> user5Tasks = mockTasks(USER5_TOTAL_TASKS, USER5_PINNED_TASKS, "container5", 5L);
        User user5 = mockUser(5, user5Tasks);

        List<Task> user6Tasks = mockTasks(USER6_TOTAL_TASKS, USER6_PINNED_TASKS, "container6", 6L);
        User user6 = mockUser(6, user6Tasks);

        List<Task> planningUserTasks = mockTasks(PLANNING_USER_TASKS, 0, "container7", 7L);
        User planningUser = initializeUser(new User(-1, PLANNING_USER_ID), planningUserTasks);

        List<Task> totalTasks = new ArrayList<>();
        totalTasks.addAll(user1Tasks);
        totalTasks.addAll(user2Tasks);
        totalTasks.addAll(user3Tasks);
        totalTasks.addAll(user4Tasks);
        totalTasks.addAll(user5Tasks);
        totalTasks.addAll(user6Tasks);
        totalTasks.addAll(planningUserTasks);

        List<User> totalUsers = Arrays.asList(user1, user2, user3, user4, user5, user6, planningUser);

        TaskAssigningSolution solution = new TaskAssigningSolution(1, totalUsers, totalTasks);

        List<PlanningItem> planningItems = PlanningBuilder.create()
                .withPublishWindowSize(publishWindowSize)
                .withSolution(solution)
                .build();

        assertPlanningItems(user1, publishWindowSize, planningItems);
        assertPlanningItems(user2, publishWindowSize, planningItems);
        assertPlanningItems(user3, publishWindowSize, planningItems);
        assertPlanningItems(user4, publishWindowSize, planningItems);
        assertPlanningItems(user5, publishWindowSize, planningItems);
        assertPlanningItems(user6, publishWindowSize, planningItems);
        assertPlanningUserPlanningItems(planningUser, publishWindowSize, planningItems);
    }

    private void assertPlanningItems(User user, int publishWindowSize, List<PlanningItem> planningItems) {
        List<Task> nonPinnedTasks = extractTasks(user, task -> !task.isPinned());
        List<Task> pinnedTasks = extractTasks(user, Task::isPinned);

        List<PlanningItem> userPlanningItems = planningItems.stream()
                .filter(item -> user.getEntityId().equals(item.getPlanningTask().getAssignedUser()))
                .collect(Collectors.toList());

        // 1) all the tasks that where pinned must necessary be present.
        assertTasksArePlanned(pinnedTasks, userPlanningItems);

        // 2) if the publishWindowsSize is greater that the pinnedTasks it must be completed with non pinned tasks.
        int publishWindowSizeFreeRoom = publishWindowSize - pinnedTasks.size();
        List<Task> additionalTasks = new ArrayList<>();
        for (int i = 0; publishWindowSizeFreeRoom > 0 && i < nonPinnedTasks.size(); i++) {
            publishWindowSizeFreeRoom--;
            additionalTasks.add(nonPinnedTasks.get(i));
        }
        assertTasksArePlanned(additionalTasks, userPlanningItems);

        // 3) finally the total planningItems for the user must be exactly pinnedTasks.size() + additionalTasks.size();
        assertEquals("The total planningItems for user: " + user.getId() + " is not the expected value",
                     userPlanningItems.size(), pinnedTasks.size() + additionalTasks.size());
    }

    private void assertPlanningUserPlanningItems(User user, int publishWindowSize, List<PlanningItem> planningItems) {
        List<PlanningItem> userPlanningItems = planningItems.stream()
                .filter(item -> user.getEntityId().equals(item.getPlanningTask().getAssignedUser()))
                .collect(Collectors.toList());
        List<Task> tasks = extractTasks(user, (task) -> true);
        // all the tasks assigned to the planning user are published no matter the publishWidowSize.
        assertTasksArePlanned(tasks, userPlanningItems);

        assertEquals("The total planningItems for user: " + user.getId() + " is not the expected value",
                     userPlanningItems.size(), tasks.size());
    }

    private void assertTasksArePlanned(List<Task> tasks, List<PlanningItem> planningItems) {
        for (Task task : tasks) {
            PlanningItem peerItem = planningItems.stream()
                    .filter(item -> item.getTaskId().equals(task.getId()))
                    .findFirst().orElse(null);
            if (peerItem == null) {
                fail("Task: " + task.getId() + " for user: " + task.getUser().getId() + " must be part of the generated planning");
            }
            assertEquals("PlanningItem containerId for task: " + task.getId() + " and user: " + task.getUser().getId() + " don't have the expected value.",
                         task.getContainerId(), peerItem.getContainerId());
            assertEquals("PlanningItem processInstanceId for task: " + task.getId() + " and user: " + task.getUser().getId() + " don't have the expected value.",
                         task.getProcessInstanceId(), task.getProcessInstanceId());
            assertEquals("PlanningItem assignedUser for task: " + task.getId() + " and user: " + task.getUser().getId() + " don't have the expected value.",
                         task.getUser().getEntityId(), peerItem.getPlanningTask().getAssignedUser());
        }
    }

    private List<Task> mockTasks(int totalTasks, int totalPinnedTasks, String containerId, long processInstanceId) {
        List<Task> tasks = new ArrayList<>();
        int pinnedTasks = 0;
        for (int i = 0; i < totalTasks; i++) {
            tasks.add(mockTask(nextId(), pinnedTasks++ < totalPinnedTasks, containerId, processInstanceId));
        }
        return tasks;
    }

    private Task mockTask(long taskId, boolean pinned, String containerId, long processInstanceId) {
        Task result = new Task(taskId, "Task_" + taskId, 0);
        result.setContainerId(containerId);
        result.setProcessInstanceId(processInstanceId);
        result.setPinned(pinned);
        return result;
    }

    private long nextId() {
        return ids++;
    }
}

/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.taskassigning.core.model.solver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.server.services.taskassigning.core.model.Group;
import org.kie.server.services.taskassigning.core.model.OrganizationalEntity;
import org.kie.server.services.taskassigning.core.model.Task;
import org.kie.server.services.taskassigning.core.model.TaskOrUser;
import org.kie.server.services.taskassigning.core.model.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.server.services.taskassigning.core.model.solver.TaskHelper.isPotentialOwner;

@RunWith(Parameterized.class)
public class TaskHelperTest {

    private static final String LABEL_NAME1 = "LABEL_NAME1";
    private static final String LABEL_NAME2 = "LABEL_NAME2";
    private static final String LABEL_VALUE1 = "LABEL_VALUE1";
    private static final Integer LABEL_VALUE2 = 2;

    private static final int SIZE = 2;

    private List<User> availableUsers;
    private List<Group> availableGroups;
    private Task task;

    @Parameterized.Parameter
    public String taskLabelName;

    @Parameterized.Parameter(1)
    public Set<Object> taskLabelValues;

    @Parameterized.Parameter(2)
    public String userLabelName;

    @Parameterized.Parameter(3)
    public Set<Object> userLabelValues;

    @Parameterized.Parameter(4)
    public boolean hasAllLabelsResult;

    @Parameterized.Parameter(5)
    public int matchingLabelsResult;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        List<Object[]> data = new ArrayList<>();
        data.add(new Object[]{LABEL_NAME1, new HashSet<>(Arrays.asList(LABEL_VALUE1, LABEL_VALUE2)), LABEL_NAME1, new HashSet<>(Arrays.asList(LABEL_VALUE1, LABEL_VALUE2)), true, 2});
        data.add(new Object[]{LABEL_NAME1, new HashSet<>(Collections.singletonList(LABEL_VALUE1)), LABEL_NAME1, new HashSet<>(Arrays.asList(LABEL_VALUE1, LABEL_VALUE2)), true, 1});
        data.add(new Object[]{LABEL_NAME1, Collections.emptySet(), LABEL_NAME1, new HashSet<>(Arrays.asList(LABEL_VALUE1, LABEL_VALUE2)), true, 0});
        data.add(new Object[]{LABEL_NAME1, null, LABEL_NAME1, new HashSet<>(Arrays.asList(LABEL_VALUE1, LABEL_VALUE2)), true, 0});
        data.add(new Object[]{LABEL_NAME1, null, LABEL_NAME1, Collections.emptySet(), true, 0});
        data.add(new Object[]{LABEL_NAME1, null, LABEL_NAME1, null, true, 0});
        data.add(new Object[]{LABEL_NAME1, new HashSet<>(Arrays.asList(LABEL_VALUE1, LABEL_VALUE2)), LABEL_NAME1, new HashSet<>(Collections.singletonList(LABEL_VALUE2)), false, 1});
        data.add(new Object[]{LABEL_NAME1, new HashSet<>(Arrays.asList(LABEL_VALUE1, LABEL_VALUE2)), LABEL_NAME1, new HashSet<>(), false, 0});
        data.add(new Object[]{LABEL_NAME1, new HashSet<>(Arrays.asList(LABEL_VALUE1, LABEL_VALUE2)), LABEL_NAME1, null, false, 0});
        data.add(new Object[]{LABEL_NAME1, new HashSet<>(Arrays.asList(LABEL_VALUE1, LABEL_VALUE2)), LABEL_NAME2, new HashSet<>(Arrays.asList(LABEL_VALUE1, LABEL_VALUE2)), false, 0});
        return data;
    }

    @Before
    public void setUp() {
        availableUsers = buildUsers(SIZE);
        availableGroups = buildGroups(SIZE);
        List<OrganizationalEntity> potentialOwners = new ArrayList<>(availableUsers);
        potentialOwners.addAll(availableGroups);
        task = buildTask(potentialOwners);
    }

    @Test
    public void isPotentialOwnerDirectAssignmentTrue() {
        for (User user : availableUsers) {
            assertThat(isPotentialOwner(task, user)).isTrue();
        }
    }

    @Test
    public void isPotentialOwnerDirectAssignmentFalse() {
        for (User user : availableUsers) {
            task.getPotentialOwners().remove(user);
            assertThat(isPotentialOwner(task, user)).isFalse();
        }
    }

    @Test
    public void isPotentialOwnerInDirectAssignmentTrue() {
        for (User user : availableUsers) {
            task.getPotentialOwners().remove(user);
            for (Group group : availableGroups) {
                user.getGroups().add(group);
                assertThat(isPotentialOwner(task, user)).isTrue();
                user.getGroups().remove(group);
            }
        }
    }

    @Test
    public void isPotentialOwnerInDirectAssignmentFalse() {
        for (User user : availableUsers) {
            task.getPotentialOwners().remove(user);
            for (Group group : availableGroups) {
                user.getGroups().add(group);
                assertThat(isPotentialOwner(task, user)).isTrue();
                user.getGroups().remove(group);
                assertThat(isPotentialOwner(task, user)).isFalse();
            }
        }
    }

    @Test
    public void hasAllLabels() {
        Task task = mockTask(taskLabelName, taskLabelValues);
        User user = mockUser(userLabelName, userLabelValues);
        assertThat(TaskHelper.hasAllLabels(task, user, taskLabelName)).isEqualTo(hasAllLabelsResult);
    }

    @Test
    public void matchingLabels() {
        Task task = mockTask(taskLabelName, taskLabelValues);
        User user = mockUser(userLabelName, userLabelValues);
        assertThat(TaskHelper.matchingLabels(task, user, taskLabelName)).isEqualTo(matchingLabelsResult);
    }

    @Test
    public void extractTaskList() {
        TaskOrUser taskOrUser = new Task();
        Task task1 = new Task();
        Task task2 = new Task();
        Task task3 = new Task();
        taskOrUser.setNextTask(task1);
        task1.setNextTask(task2);
        task2.setNextTask(task3);
        assertThat(TaskHelper.extractTaskList(taskOrUser)).isEqualTo(Arrays.asList(task1, task2, task3));
    }

    private static Task buildTask(List<OrganizationalEntity> potentialOwners) {
        Task task = new Task(1, "TaskName", 1);
        task.getPotentialOwners().addAll(potentialOwners);
        return task;
    }

    private static List<User> buildUsers(int size) {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            users.add(new User(i, "User" + i));
        }
        return users;
    }

    private static List<Group> buildGroups(int size) {
        List<Group> groupList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            groupList.add(new Group(i, "Group" + i));
        }
        return groupList;
    }

    private static User mockUser(String labelName, Set<Object> labelValues) {
        User user = new User();
        user.setLabelValues(labelName, labelValues);
        return user;
    }

    private static Task mockTask(String labelName, Set<Object> labelValues) {
        Task task = new Task();
        task.setLabelValues(labelName, labelValues);
        return task;
    }
}
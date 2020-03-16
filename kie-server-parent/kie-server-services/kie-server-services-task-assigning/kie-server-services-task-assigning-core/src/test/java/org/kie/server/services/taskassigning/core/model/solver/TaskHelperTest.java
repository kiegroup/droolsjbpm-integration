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
import java.util.List;

import org.kie.server.services.taskassigning.core.model.Group;
import org.kie.server.services.taskassigning.core.model.OrganizationalEntity;
import org.kie.server.services.taskassigning.core.model.Task;
import org.kie.server.services.taskassigning.core.model.User;
import org.junit.Before;
import org.junit.Test;

import static org.kie.server.services.taskassigning.core.model.solver.TaskHelper.isPotentialOwner;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TaskHelperTest {

    private static final int SIZE = 2;

    private List<User> availableUsers;
    private List<Group> availableGroups;
    private Task task;

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
            assertTrue(isPotentialOwner(task, user));
        }
    }

    @Test
    public void isPotentialOwnerDirectAssignmentFalse() {
        for (User user : availableUsers) {
            task.getPotentialOwners().remove(user);
            assertFalse(isPotentialOwner(task, user));
        }
    }

    @Test
    public void isPotentialOwnerInDirectAssignmentTrue() {
        for (User user : availableUsers) {
            task.getPotentialOwners().remove(user);
            for (Group group : availableGroups) {
                user.getGroups().add(group);
                assertTrue(isPotentialOwner(task, user));
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
                assertTrue(isPotentialOwner(task, user));
                user.getGroups().remove(group);
                assertFalse(isPotentialOwner(task, user));
            }
        }
    }

    private Task buildTask(List<OrganizationalEntity> potentialOwners) {
        Task task = new Task(1, "TaskName", 1);
        task.getPotentialOwners().addAll(potentialOwners);
        return task;
    }

    private List<User> buildUsers(int size) {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            users.add(new User(i, "User" + i));
        }
        return users;
    }

    private List<Group> buildGroups(int size) {
        List<Group> groupList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            groupList.add(new Group(i, "Group" + i));
        }
        return groupList;
    }
}

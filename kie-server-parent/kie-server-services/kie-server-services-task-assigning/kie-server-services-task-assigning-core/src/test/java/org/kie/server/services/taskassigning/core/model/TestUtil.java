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

package org.kie.server.services.taskassigning.core.model;

import java.util.List;
import java.util.Set;

public class TestUtil {

    private TestUtil() {
    }

    public static Task mockTask(long taskId, boolean pinned) {
        Task task = new Task(taskId, "Task_" + taskId, 0);
        task.setPinned(pinned);
        return task;
    }

    public static User mockUser(String id, List<Task> tasks) {
        User user = new User(id.hashCode(), id);
        TaskOrUser previous = user;
        for (Task taskOrUser : tasks) {
            taskOrUser.setPreviousTaskOrUser(previous);
            taskOrUser.setUser(user);
            previous.setNextTask(taskOrUser);
            previous = taskOrUser;
        }
        return user;
    }

    public static Task mockTask(List<OrganizationalEntity> potentialOwners, Set<Object> skills) {
        Task task = new Task(1, "TaskName", 1);
        task.getPotentialOwners().addAll(potentialOwners);
        task.setLabelValues(DefaultLabels.SKILLS.name(), skills);
        return task;
    }

    public static User mockUser(String userId, boolean enabled, List<Group> groups, Set<Object> skills) {
        User user = new User(userId.hashCode(), userId, enabled);
        user.getGroups().addAll(groups);
        user.setLabelValues(DefaultLabels.SKILLS.name(), skills);
        return user;
    }

    public static Group mockGroup(String groupId) {
        return new Group(groupId.hashCode(), groupId);
    }
}

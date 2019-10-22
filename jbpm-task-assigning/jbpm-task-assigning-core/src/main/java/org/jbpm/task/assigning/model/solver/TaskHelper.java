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

package org.jbpm.task.assigning.model.solver;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.jbpm.task.assigning.model.OrganizationalEntity;
import org.jbpm.task.assigning.model.Task;
import org.jbpm.task.assigning.model.TaskOrUser;
import org.jbpm.task.assigning.model.User;

public class TaskHelper {

    /**
     * @return true if the user is a potential owner for the given task. This basically means that the user can be
     * assigned to the given task, false in any other case.
     */
    public static boolean isPotentialOwner(Task task, User user) {
        //user appears directly in the list of potential owners.
        final boolean directlyAssigned = task.getPotentialOwners().stream()
                .filter(OrganizationalEntity::isUser)
                .anyMatch(entity -> Objects.equals(entity.getEntityId(), user.getEntityId()));
        if (directlyAssigned) {
            return true;
        }

        //the user has at least one of the enabled groups for executing the task.
        final Set<String> acceptedGroupIds = task.getPotentialOwners().stream()
                .filter(entity -> !entity.isUser())
                .map(OrganizationalEntity::getEntityId).collect(Collectors.toSet());
        final boolean indirectlyAssigned = user.getGroups().stream()
                .anyMatch(group -> acceptedGroupIds.contains(group.getEntityId()));
        return indirectlyAssigned;
    }

    public static List<Task> extractTaskList(TaskOrUser taskOrUser) {
        List<Task> result = new ArrayList<>();
        Task task = taskOrUser.getNextTask();
        while (task != null) {
            result.add(task);
            task = task.getNextTask();
        }
        return result;
    }
}

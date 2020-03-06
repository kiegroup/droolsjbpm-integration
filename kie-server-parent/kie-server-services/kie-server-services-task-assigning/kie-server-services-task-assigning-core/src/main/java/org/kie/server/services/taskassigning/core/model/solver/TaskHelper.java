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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.kie.server.services.taskassigning.core.model.OrganizationalEntity;
import org.kie.server.services.taskassigning.core.model.Task;
import org.kie.server.services.taskassigning.core.model.TaskOrUser;
import org.kie.server.services.taskassigning.core.model.User;

public class TaskHelper {

    private TaskHelper() {
    }

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
        return user.getGroups().stream()
                .anyMatch(group -> acceptedGroupIds.contains(group.getEntityId()));
    }

    /**
     * Calculates if a given user has all the label values that are declared for the task in the label with name labelName.
     * @param task a task instance for the evaluation.
     * @param user a user instance for the evaluation.
     * @param labelName name of the label for the calculation.
     * @return true if the user.getLabelValues("labelName") set "contains" the task.getLabelValues("labelName") set,
     * false in any other case.
     */
    public static boolean hasAllLabels(Task task, User user, String labelName) {
        final Set<Object> taskLabelValues = task.getLabelValues(labelName);
        if (taskLabelValues == null || taskLabelValues.isEmpty()) {
            return true;
        }

        final Set<Object> userLabelValues = user.getLabelValues(labelName);
        return userLabelValues != null && userLabelValues.containsAll(taskLabelValues);
    }

    /**
     * Calculates the number labels in the user label value set that are contained in the task label value set for the
     * label labelName.
     * @param task a task instance for the calculation.
     * @param user a task instance for the calculation.
     * @param labelName name of the label for the calculation.
     * @return the number of elements in the intersection between the task.getLabelValues("labelName") and the
     * user.getLabelValues("labelName") sets.
     */
    public static int matchingLabels(Task task, User user, String labelName) {
        final Set<Object> taskLabelValues = task.getLabelValues(labelName);
        if (taskLabelValues == null || taskLabelValues.isEmpty()) {
            return 0;
        }
        final Set<Object> userLabelValues = user.getLabelValues(labelName);
        if (userLabelValues == null) {
            return 0;
        }
        return userLabelValues.stream().mapToInt(labelValue -> taskLabelValues.contains(labelValue) ? 1 : 0).sum();
    }

    /**
     * @param taskOrUser a TaskOrUser instance for the evaluation.
     * @return a list with the tasks linked to the taskOrUser.
     */
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

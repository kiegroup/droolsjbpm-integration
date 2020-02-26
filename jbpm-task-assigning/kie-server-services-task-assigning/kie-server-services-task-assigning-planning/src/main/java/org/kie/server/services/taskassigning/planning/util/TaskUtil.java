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

package org.kie.server.services.taskassigning.planning.util;

import org.kie.server.api.model.taskassigning.TaskData;
import org.kie.server.services.taskassigning.core.model.Group;
import org.kie.server.services.taskassigning.core.model.Task;
import org.kie.server.services.taskassigning.core.model.User;

import static org.kie.server.services.taskassigning.planning.util.UserUtil.isUser;

public class TaskUtil {

    private TaskUtil() {
    }

    public static Task fromTaskData(TaskData taskData) {
        final Task task = new Task(taskData.getTaskId(),
                                   taskData.getProcessInstanceId(),
                                   taskData.getProcessId(),
                                   taskData.getContainerId(),
                                   taskData.getName(),
                                   taskData.getPriority(),
                                   taskData.getStatus(),
                                   taskData.getInputData());
        if (taskData.getPotentialOwners() != null) {
            taskData.getPotentialOwners().forEach(potentialOwner -> {
                if (isUser(potentialOwner.getType())) {
                    task.getPotentialOwners().add(new User(potentialOwner.getName().hashCode(), potentialOwner.getName()));
                } else {
                    task.getPotentialOwners().add(new Group(potentialOwner.getName().hashCode(), potentialOwner.getName()));
                }
            });
        }
        return task;
    }
}

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

package org.kie.server.services.taskassigning.planning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.kie.api.task.model.Status;
import org.kie.server.api.model.taskassigning.PlanningTask;
import org.kie.server.api.model.taskassigning.TaskData;
import org.kie.server.services.taskassigning.core.model.Task;
import org.kie.server.services.taskassigning.core.model.TaskAssigningSolution;
import org.kie.server.services.taskassigning.core.model.TaskOrUser;
import org.kie.server.services.taskassigning.core.model.User;
import org.kie.server.services.taskassigning.planning.util.IndexedElement;
import org.kie.server.services.taskassigning.planning.util.UserUtil;

import static org.apache.commons.lang3.StringUtils.isNoneEmpty;
import static org.kie.api.task.model.Status.InProgress;
import static org.kie.api.task.model.Status.Reserved;
import static org.kie.api.task.model.Status.Suspended;
import static org.kie.server.api.model.taskassigning.util.StatusConverter.convertFromString;
import static org.kie.server.services.taskassigning.core.model.ModelConstants.DUMMY_TASK;
import static org.kie.server.services.taskassigning.core.model.ModelConstants.IS_PLANNING_USER;
import static org.kie.server.services.taskassigning.core.model.ModelConstants.PLANNING_USER;
import static org.kie.server.services.taskassigning.planning.util.IndexedElement.addInOrder;
import static org.kie.server.services.taskassigning.planning.util.TaskUtil.fromTaskData;

/**
 * This class is intended for the restoring of a TaskAssigningSolution given a set of TaskData, a set of User and the
 * corresponding PlanningTask for each task. I'ts typically used when the solver needs to be started during the
 * application startup procedure.
 */
public class SolutionBuilder {

    private List<TaskData> taskDataList;
    private List<org.kie.server.services.taskassigning.user.system.api.User> externalUsers;

    private SolutionBuilder() {
    }

    public static SolutionBuilder create() {
        return new SolutionBuilder();
    }

    public SolutionBuilder withTasks(List<TaskData> taskDataList) {
        this.taskDataList = taskDataList;
        return this;
    }

    public SolutionBuilder withUsers(List<org.kie.server.services.taskassigning.user.system.api.User> externalUsers) {
        this.externalUsers = externalUsers;
        return this;
    }

    public TaskAssigningSolution build() {
        final List<Task> tasks = new ArrayList<>();
        final Map<String, List<IndexedElement<Task>>> assignedTasksByUserId = new HashMap<>();
        final Map<String, User> usersById = externalUsers.stream()
                .filter(org.kie.server.services.taskassigning.user.system.api.User::isActive)
                .map(UserUtil::fromExternalUser)
                .collect(Collectors.toMap(User::getEntityId, Function.identity()));
        usersById.put(PLANNING_USER.getEntityId(), PLANNING_USER);

        taskDataList.forEach(taskData -> {
            final Task task = fromTaskData(taskData);
            final Status status = convertFromString(task.getStatus());
            switch (status) {
                case Ready:
                    tasks.add(task);
                    break;
                case Reserved:
                case InProgress:
                case Suspended:
                    if (isNoneEmpty(taskData.getActualOwner())) {
                        // If actualOwner is empty the only chance is that the task was in Ready status and changed to
                        // Suspended, since Reserved and InProgress tasks has always an owner in jBPM.
                        // Finally tasks with no actualOwner (Suspended) are skipped, since they'll be properly added to
                        // the solution when they change to Ready status and the proper jBPM event is raised.
                        tasks.add(task);
                        final PlanningTask planningTask = taskData.getPlanningTask();
                        if (planningTask != null && taskData.getActualOwner().equals(planningTask.getAssignedUser())) {
                            boolean pinned = InProgress == status || Suspended == status ||
                                    planningTask.getPublished() || !usersById.containsKey(taskData.getActualOwner());
                            addTaskToUser(assignedTasksByUserId, task, planningTask.getAssignedUser(), planningTask.getIndex(), pinned);
                        } else {
                            boolean pinned = (Reserved == status && !IS_PLANNING_USER.test(taskData.getActualOwner())) ||
                                    InProgress == status || Suspended == status;
                            addTaskToUser(assignedTasksByUserId, task, taskData.getActualOwner(), -1, pinned);
                        }
                    }
                    break;
                default:
                    //no other cases exists, sonar required.
                    throw new IndexOutOfBoundsException("Value: " + taskData.getStatus() + " is out of range in current switch");
            }
        });

        assignedTasksByUserId.forEach((key, assignedTasks) -> {
            User user = usersById.get(key);
            if (user == null) {
                //create the user by convention.
                user = new User(key.hashCode(), key);
                usersById.put(key, user);
            }
            final List<Task> userTasks = assignedTasks.stream().map(IndexedElement::getElement).collect(Collectors.toList());
            addTasksToUser(user, userTasks);
        });

        //Add the DUMMY_TASK to avoid running into scenarios where the solution remains with no tasks.
        tasks.add(DUMMY_TASK);
        final List<User> users = new ArrayList<>(usersById.values());
        return new TaskAssigningSolution(-1, users, tasks);
    }

    /**
     * Link the list of tasks to the given user. The tasks comes in the expected order.
     * @param user the user that will "own" the tasks in the chained graph.
     * @param tasks the tasks to link.
     */
    private static void addTasksToUser(User user, List<Task> tasks) {
        TaskOrUser previousTask = user;
        // startTime, endTime, nextTask and user are shadow variables that should be calculated by the solver at
        // start time. However this is not yet implemented see: https://issues.jboss.org/browse/PLANNER-1316 so by now
        // they are initialized as part of the solution restoring.
        for (Task nextTask : tasks) {
            previousTask.setNextTask(nextTask);

            nextTask.setStartTimeInMinutes(previousTask.getEndTimeInMinutes());
            nextTask.setEndTime(nextTask.getStartTimeInMinutes() + nextTask.getDurationInMinutes());
            nextTask.setPreviousTaskOrUser(previousTask);
            nextTask.setUser(user);

            previousTask = nextTask;
        }
    }

    private static void addTaskToUser(Map<String, List<IndexedElement<Task>>> tasksByUser,
                                      Task task,
                                      String actualOwner,
                                      int index,
                                      boolean pinned) {
        task.setPinned(pinned);
        final List<IndexedElement<Task>> userAssignedTasks = tasksByUser.computeIfAbsent(actualOwner, key -> new ArrayList<>());
        addInOrder(userAssignedTasks, new IndexedElement<>(task, index, task.isPinned()));
    }
}


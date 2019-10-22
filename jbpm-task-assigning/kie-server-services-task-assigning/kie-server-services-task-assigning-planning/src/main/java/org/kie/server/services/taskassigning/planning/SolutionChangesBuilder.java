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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.kie.server.services.taskassigning.core.model.Task;
import org.kie.server.services.taskassigning.core.model.TaskAssigningSolution;
import org.kie.server.services.taskassigning.core.model.User;
import org.kie.server.services.taskassigning.core.model.solver.realtime.AddTaskProblemFactChange;
import org.kie.server.services.taskassigning.core.model.solver.realtime.AssignTaskProblemFactChange;
import org.kie.server.services.taskassigning.core.model.solver.realtime.ReleaseTaskProblemFactChange;
import org.kie.server.services.taskassigning.core.model.solver.realtime.RemoveTaskProblemFactChange;
import org.kie.server.services.taskassigning.core.model.solver.realtime.TaskPropertyChangeProblemFactChange;
import org.kie.server.services.taskassigning.user.system.api.UserSystemService;
import org.kie.server.api.model.taskassigning.TaskData;
import org.optaplanner.core.impl.solver.ProblemFactChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.services.taskassigning.core.model.Task.DUMMY_TASK_PLANNER_241;
import static org.kie.server.services.taskassigning.core.model.Task.IS_NOT_DUMMY;
import static org.kie.server.services.taskassigning.core.model.User.PLANNING_USER;
import static org.kie.server.api.model.taskassigning.TaskStatus.Completed;
import static org.kie.server.api.model.taskassigning.TaskStatus.Error;
import static org.kie.server.api.model.taskassigning.TaskStatus.Exited;
import static org.kie.server.api.model.taskassigning.TaskStatus.Failed;
import static org.kie.server.api.model.taskassigning.TaskStatus.InProgress;
import static org.kie.server.api.model.taskassigning.TaskStatus.Obsolete;
import static org.kie.server.api.model.taskassigning.TaskStatus.Ready;
import static org.kie.server.api.model.taskassigning.TaskStatus.Reserved;
import static org.kie.server.api.model.taskassigning.TaskStatus.Suspended;
import static org.kie.server.services.taskassigning.planning.SolutionBuilder.addInOrder;
import static org.kie.server.services.taskassigning.planning.SolutionBuilder.fromTaskData;
import static org.kie.server.services.taskassigning.planning.util.UserUtil.fromExternalUser;

/**
 * This class performs the calculation of the impact (i.e. the set of changes to be applied) on a solution given the
 * updated information about the tasks in the jBPM runtime.
 */
public class SolutionChangesBuilder {

    private static Logger LOGGER = LoggerFactory.getLogger(SolutionChangesBuilder.class);

    private TaskAssigningSolution solution;

    private List<TaskData> taskDataList;

    private UserSystemService systemService;

    private SolverHandlerContext context;

    public SolutionChangesBuilder() {
    }

    public SolutionChangesBuilder withSolution(TaskAssigningSolution solution) {
        this.solution = solution;
        return this;
    }

    public SolutionChangesBuilder withTasks(List<TaskData> taskDataList) {
        this.taskDataList = taskDataList;
        return this;
    }

    public SolutionChangesBuilder withUserSystem(UserSystemService userSystemService) {
        this.systemService = userSystemService;
        return this;
    }

    public SolutionChangesBuilder withContext(SolverHandlerContext context) {
        this.context = context;
        return this;
    }

    public List<ProblemFactChange<TaskAssigningSolution>> build() {
        final Map<Long, Task> taskById = solution.getTaskList()
                .stream()
                .filter(IS_NOT_DUMMY)
                .collect(Collectors.toMap(Task::getId, Function.identity()));

        final Map<String, User> usersById = solution.getUserList()
                .stream()
                .collect(Collectors.toMap(User::getEntityId, Function.identity()));

        final List<AddTaskProblemFactChange> newTaskChanges = new ArrayList<>();
        final List<ReleaseTaskProblemFactChange> releasedTasksChanges = new ArrayList<>();
        final List<RemoveTaskProblemFactChange> removedTaskChanges = new ArrayList<>();
        final Set<Task> removedTasksSet = new HashSet<>();
        final List<TaskPropertyChangeProblemFactChange> propertyChanges = new ArrayList<>();
        final Map<String, List<SolutionBuilder.IndexedElement<AssignTaskProblemFactChange>>> changesByUserId = new HashMap<>();

        Task task;
        for (TaskData taskData : taskDataList) {
            task = taskById.remove(taskData.getTaskId());
            if (task == null) {
                Task newTask;
                switch (taskData.getStatus()) {
                    case Ready:
                        newTask = fromTaskData(taskData);
                        newTaskChanges.add(new AddTaskProblemFactChange(newTask));
                        break;
                    case Reserved:
                    case InProgress:
                    case Suspended:

                        // if Reserved:
                        //        the task was created and reserved completely outside of the planner. We add it to the
                        //        solution.
                        // if InProgress:
                        //        the task was created, reserved and started completely outside of the planner.
                        //        We add it to the solution since this assignment might affect the workload, etc., of the plan.
                        // if Suspended:
                        //        the task was created, eventually assigned and started, suspended etc. completely outside of
                        //        the planner.
                        //        if (taskData.getActualOwner() == null) {
                        //            do nothing, the task was assigned to nobody. So it was necessary in Ready status
                        //            prior Suspension.
                        //            It'll be added to the solution if it comes into Ready or Reserved status in a later moment.
                        //        }

                        if (taskData.getActualOwner() != null) {
                            newTask = fromTaskData(taskData);
                            final User user = getUser(usersById, taskData.getActualOwner());
                            // assign and ensure the task is published since the task was already seen by the public audience.
                            AssignTaskProblemFactChange change = new AssignTaskProblemFactChange(newTask, user, true);
                            addChangeToUser(changesByUserId, change, user, -1, true);
                        }
                        break;
                }
            } else {
                switch (taskData.getStatus()) {
                    case Ready:
                        if (!Ready.equals(task.getStatus())) {
                            // task was probably assigned to someone else in the past and released from the task
                            // list administration
                            releasedTasksChanges.add(new ReleaseTaskProblemFactChange(task));
                        }
                        break;
                    case Reserved:
                    case InProgress:
                    case Suspended:
                        if (taskData.getActualOwner() == null) {
                            // Task was necessary in Ready status prior going into Suspension. Remove it from solution
                            // and let it be added again if it comes into Ready or Reserved status in a later moment.
                            removedTasksSet.add(task);
                        } else if (!taskData.getActualOwner().equals(task.getUser().getEntityId())) {
                            // if Reserved:
                            //       the task was probably manually re-assigned from the task list to another user.
                            //       We must respect this assignment.
                            // if InProgress:
                            //       the task was probably re-assigned to another user from the task list prior to start.
                            //       We must correct this assignment so it's reflected in the plan and also respect it.
                            // if Suspended:
                            //       the task was assigned to someone else from the task list prior to the suspension,
                            //       we must reflect that change in the plan.

                            final User user = getUser(usersById, taskData.getActualOwner());

                            // assign and ensure the task is published since the task was already seen by the public audience.
                            AssignTaskProblemFactChange change = new AssignTaskProblemFactChange(task, user, true);
                            addChangeToUser(changesByUserId, change, user, -1, true);
                        } else if ((taskData.getPlanningTask() == null || taskData.getPlanningTask().getPublished()) && !task.isPinned()) {
                            // The task was published and not yet pinned
                            final User user = getUser(usersById, taskData.getActualOwner());
                            AssignTaskProblemFactChange change = new AssignTaskProblemFactChange(task, user, true);
                            int index = taskData.getPlanningTask() != null ? taskData.getPlanningTask().getIndex() : -1;
                            addChangeToUser(changesByUserId, change, user, index, true);
                        }
                        break;
                    case Completed:
                    case Exited:
                    case Failed:
                    case Error:
                    case Obsolete:
                        removedTasksSet.add(task);
                        break;
                }

                if (!removedTasksSet.contains(task) && (taskData.getPriority() != task.getPriority() || !taskData.getStatus().equals(task.getStatus()))) {
                    TaskPropertyChangeProblemFactChange propertyChange = new TaskPropertyChangeProblemFactChange(task);
                    if (taskData.getPriority() != task.getPriority()) {
                        propertyChange.setPriority(taskData.getPriority());
                    }
                    if (!taskData.getStatus().equals(task.getStatus())) {
                        propertyChange.setStatus(taskData.getStatus());
                    }
                    propertyChanges.add(propertyChange);
                }
            }
        }

        for (Task removedTask : removedTasksSet) {
            removedTaskChanges.add(new RemoveTaskProblemFactChange(removedTask));
        }

        List<ProblemFactChange<TaskAssigningSolution>> totalChanges = new ArrayList<>();
        totalChanges.addAll(removedTaskChanges);
        totalChanges.addAll(releasedTasksChanges);
        changesByUserId.values().forEach(userChanges -> userChanges.forEach(change -> totalChanges.add(change.getElement())));
        totalChanges.addAll(propertyChanges);
        totalChanges.addAll(newTaskChanges);

        if (LOGGER.isTraceEnabled()) {
            if (!totalChanges.isEmpty()) {
                traceProgrammedChanges(removedTaskChanges, releasedTasksChanges, changesByUserId, propertyChanges, newTaskChanges);
            } else {
                LOGGER.trace("No changes has been calculated.");
            }
        }

        applyWorkaroundForPLANNER_241(solution, totalChanges);

        if (!totalChanges.isEmpty()) {
            totalChanges.add(0, scoreDirector -> {
                context.setCurrentChangeSetId(context.nextChangeSetId());
            });
        }
        return totalChanges;
    }

    static void addChangeToUser(Map<String, List<SolutionBuilder.IndexedElement<AssignTaskProblemFactChange>>> changesByUserId,
                                AssignTaskProblemFactChange change,
                                User user,
                                int index,
                                boolean pinned) {
        final List<SolutionBuilder.IndexedElement<AssignTaskProblemFactChange>> userChanges = changesByUserId.computeIfAbsent(user.getEntityId(), key -> new ArrayList<>());
        addInOrder(userChanges, new SolutionBuilder.IndexedElement<>(change, index, pinned));
    }

    private User getUser(Map<String, User> usersById, String userId) {
        User user = usersById.get(userId);
        if (user == null) {
            LOGGER.debug("User {} was not found in current solution, it'll we looked up in the external user system .", userId);
            org.kie.server.services.taskassigning.user.system.api.User externalUser = systemService.findUser(userId);
            if (externalUser != null) {
                user = fromExternalUser(externalUser);
            } else {
                // We add it by convention, since the task list administration supports the delegation to non-existent users.
                LOGGER.debug("User {} was not found in the external user system, it looks like it's a manual" +
                                     " assignment from the tasks administration. It'll be added to the solution" +
                                     " to respect the assignment.", userId);
                user = new User(userId.hashCode(), userId);
            }
        }
        return user;
    }

    /**
     * This method adds a second dummy task for avoiding the issue produced by https://issues.jboss.org/browse/PLANNER-241
     * and will be removed as soon it's fixed. Note that workaround doesn't have a huge impact on the solution since
     * the dummy task is added only once and to the planning user.
     */
    private void applyWorkaroundForPLANNER_241(TaskAssigningSolution solution, List<ProblemFactChange<TaskAssigningSolution>> changes) {
        boolean hasDummyTask241 = solution.getTaskList().stream().anyMatch(task -> DUMMY_TASK_PLANNER_241.getId().equals(task.getId()));
        if (!hasDummyTask241) {
            changes.add(new AssignTaskProblemFactChange(DUMMY_TASK_PLANNER_241, PLANNING_USER));
        }
    }

    private void traceProgrammedChanges(List<RemoveTaskProblemFactChange> removedTasksChanges,
                                        List<ReleaseTaskProblemFactChange> releasedTasksChanges,
                                        Map<String, List<SolutionBuilder.IndexedElement<AssignTaskProblemFactChange>>> changesByUserId,
                                        List<TaskPropertyChangeProblemFactChange> propertyChanges,
                                        List<AddTaskProblemFactChange> newTaskChanges) {
        LOGGER.trace("\n");

        LOGGER.trace("*** Removed tasks ***");
        LOGGER.trace("Total tasks removed from solution is {}", removedTasksChanges.size());
        removedTasksChanges.forEach(change -> LOGGER.trace(" -> (" + change.getTask().getId() + ", " + change.getTask().getName() + ")"));
        LOGGER.trace("*** End of Removed tasks ***");

        LOGGER.trace("\n");

        LOGGER.trace("*** Released tasks ***");
        LOGGER.trace("Total tasks released from solution is {}", releasedTasksChanges.size());
        releasedTasksChanges.forEach(change -> LOGGER.trace(" -> (" + change.getTask().getId() + ", " + change.getTask().getName() + ")"));
        LOGGER.trace("*** End of Released tasks ***");

        LOGGER.trace("\n");

        LOGGER.trace("*** Changes per user ***");
        LOGGER.trace("Total users with programmed changes is {}", changesByUserId.size());
        changesByUserId.forEach((key, userChanges) -> {
            if (userChanges != null) {
                userChanges.forEach(change -> {
                    LOGGER.trace("\n");
                    LOGGER.trace("  AssignTaskToUserChanges for user: " + key);

                    LOGGER.trace("   -> taskId: " + change.getElement().getTask().getId() +
                                         ", pinned: " + change.isPinned() +
                                         ", index: " + change.getIndex() +
                                         ", status: " + change.getElement().getTask().getStatus());
                    LOGGER.trace("  End of AssignTaskToUserChanges for user: " + key);
                    LOGGER.trace("\n");
                });
            }
        });
        LOGGER.trace("*** End of Changes per user ***");

        LOGGER.trace("\n");

        LOGGER.trace("*** Property changes ***");
        LOGGER.trace("Total tasks with property changes is {}", propertyChanges.size());

        propertyChanges.forEach(change -> {
            String changeDesc = "";
            if (change.getPriority() != null) {
                changeDesc = " setPriority = " + change.getPriority();
            }
            if (change.getStatus() != null) {
                changeDesc = " setStatus = " + change.getStatus();
            }
            LOGGER.trace(" -> (" + change.getTask().getId() + ", " + change.getTask().getName() + ")" + changeDesc);
        });
        LOGGER.trace("*** End of Property changes ***");

        LOGGER.trace("\n");

        LOGGER.trace("*** New tasks ***");
        LOGGER.trace("Total new tasks added to solution is {}", newTaskChanges.size());
        newTaskChanges.forEach(change -> LOGGER.trace(" -> (" + change.getTask().getId() + ", " + change.getTask().getName() + ")"));
        LOGGER.trace("*** End of New tasks ***");

        LOGGER.trace("\n");
    }
}

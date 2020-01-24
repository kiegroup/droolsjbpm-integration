/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.services.taskassigning.runtime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;
import org.jbpm.services.api.UserTaskService;
import org.jbpm.services.api.query.QueryService;
import org.jbpm.services.task.commands.CompositeCommand;
import org.jbpm.services.task.commands.DelegateTaskCommand;
import org.jbpm.services.task.commands.TaskCommand;
import org.jbpm.services.task.commands.TaskContext;
import org.kie.api.runtime.Context;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.User;
import org.kie.internal.task.api.TaskPersistenceContext;
import org.kie.server.api.model.taskassigning.ExecutePlanningResult;
import org.kie.server.api.model.taskassigning.PlanningItem;
import org.kie.server.api.model.taskassigning.PlanningItemList;
import org.kie.server.api.model.taskassigning.PlanningTask;
import org.kie.server.api.model.taskassigning.TaskData;
import org.kie.server.services.taskassigning.runtime.persistence.PlanningTaskImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.kie.server.api.model.taskassigning.TaskStatus.InProgress;
import static org.kie.server.api.model.taskassigning.TaskStatus.Ready;
import static org.kie.server.api.model.taskassigning.TaskStatus.Reserved;
import static org.kie.server.api.model.taskassigning.TaskStatus.Suspended;

public class TaskAssigningRuntimeServiceBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskAssigningRuntimeServiceBase.class);

    private static final String TASK_MODIFIED_ERROR_MSG_1 = "Task: %s was modified by an external action since the last executed plan," +
            " actualOwner is %s but the last assignedUser is %s";

    private static final String TASK_MODIFIED_ERROR_MSG_2 = "Task: %s was modified by an external action since the last executed plan," +
            " actualOwner is %s but the expected is %s";

    private static final String TASK_MODIFIED_ERROR_MSG_3 = "Task: %s was modified by an external action since the last executed plan," +
            " and is no longer in one of the expected status %s";

    private static final String TASK_MODIFIED_ERROR_MSG_4 = "Task: %s was modified by an external action since the last executed plan," +
            " current status is %s but the expected should be in %s";

    private UserTaskService userTaskService;
    private TaskAssigningRuntimeServiceQueryHelper queryHelper;

    public TaskAssigningRuntimeServiceBase(UserTaskService userTaskService, QueryService queryService) {
        this.userTaskService = userTaskService;
        this.queryHelper = new TaskAssigningRuntimeServiceQueryHelper(userTaskService, queryService);
    }

    public List<TaskData> executeFindTasksQuery(Map<String, Object> params) {
        return queryHelper.executeFindTasksQuery(params);
    }

    public ExecutePlanningResult executePlanning(PlanningItemList planningItemList, String userId) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        final Map<String, List<PlanningCommand>> commandsByContainer = new HashMap<>();
        final Map<Long, TaskData> taskDataById = prepareTaskDataForExecutePlanning();

        stopWatch.stop();
        LOGGER.debug("Time to prepareTaskDataForExecutePlanning: " + stopWatch.toString());

        try {
            for (PlanningItem planningItem : planningItemList.getItems()) {
                final TaskData taskData = taskDataById.remove(planningItem.getTaskId());
                if (taskData == null) {
                    // Un-common case, task is no longer in one of the managed status. It was probably assigned, started,
                    // and completed in middle.
                    // Proposal A) rollbacks this change since the solution is being updated at this moment
                    // and a new plan will arrive soon.
                    throw new PlanningException(String.format(TASK_MODIFIED_ERROR_MSG_3,
                                                              planningItem.getPlanningTask().getTaskId(),
                                                              Arrays.toString(new String[]{Ready, Reserved, InProgress, Suspended})),
                                                planningItem.getContainerId(),
                                                ExecutePlanningResult.ErrorCode.TASK_MODIFIED_SINCE_PLAN_CALCULATION_ERROR);
                }

                final String actualOwner = taskData.getActualOwner();
                final PlanningTask actualPlanningTask = taskData.getPlanningTask();
                final String taskStatus = taskData.getStatus();
                boolean delegateAndSave = false;

                if (isNotEmpty(actualOwner) &&
                        actualPlanningTask != null &&
                        actualOwner.equals(actualPlanningTask.getAssignedUser()) &&
                        actualPlanningTask.equals(planningItem.getPlanningTask())) {
                    continue;
                }

                switch (taskStatus) {
                    case Ready:
                        delegateAndSave = true;
                        break;

                    case Reserved:
                        if (actualPlanningTask == null) {
                            delegateAndSave = true;
                        } else if (!actualOwner.equals(actualPlanningTask.getAssignedUser()) && !actualOwner.equals(planningItem.getPlanningTask().getAssignedUser())) {
                            // the task was manually reassigned from the task list "in the middle" and we are not updating to
                            // current user, so it's not the last update that is coming.
                            // Proposal A) rollbacks this change since the solution is being updated at this moment
                            // and a new plan will arrive soon.
                            throw new PlanningException(String.format(TASK_MODIFIED_ERROR_MSG_1,
                                                                      planningItem.getPlanningTask().getTaskId(),
                                                                      actualOwner,
                                                                      actualPlanningTask.getAssignedUser()),
                                                        planningItem.getContainerId(),
                                                        ExecutePlanningResult.ErrorCode.TASK_MODIFIED_SINCE_PLAN_CALCULATION_ERROR);
                        } else {
                            delegateAndSave = true;
                        }
                        break;

                    case InProgress:
                    case Suspended:
                        if (actualOwner == null || !actualOwner.equals(planningItem.getPlanningTask().getAssignedUser())) {
                            // task changed in "in the middle" and we are not updating to current user, so it's not the last
                            // update that is coming.
                            // Proposal A) rollbacks this change since the solution is being updated at this moment
                            // and a new plan will arrive soon.
                            throw new PlanningException(String.format(TASK_MODIFIED_ERROR_MSG_2,
                                                                      planningItem.getPlanningTask().getTaskId(),
                                                                      actualOwner,
                                                                      planningItem.getPlanningTask().getAssignedUser()),
                                                        planningItem.getContainerId(),
                                                        ExecutePlanningResult.ErrorCode.TASK_MODIFIED_SINCE_PLAN_CALCULATION_ERROR);
                        } else {
                            // task might have been created, assigned and started/suspended completely out of the task
                            // or the planning data might have changed. Just update the planning data.
                            delegateAndSave = false;
                        }
                        break;
                }

                PlanningCommand command;
                if (delegateAndSave) {
                    command = new DelegateAndSaveCommand(planningItem, userId);
                } else {
                    command = new SavePlanningItemCommand(planningItem);
                }
                commandsByContainer.computeIfAbsent(planningItem.getContainerId(), k -> new ArrayList<>()).add(command);
            }

            for (TaskData taskData : taskDataById.values()) {
                if ((taskData.getStatus().equals(Ready) || taskData.getStatus().equals(Reserved) || taskData.getStatus().equals(Suspended)) && taskData.getPlanningTask() != null) {
                    commandsByContainer.computeIfAbsent(taskData.getContainerId(), k -> new ArrayList<>()).add(new DeletePlanningItemCommand(taskData.getTaskId()));
                }
            }
        } catch (PlanningException e) {
            LOGGER.debug("An error was produced during plan execution calculation, containerId: {}, error code: {}, message: {}",
                         e.getContainerId(), e.getCode(), e.getMessage());
            return ExecutePlanningResult.builder()
                    .error(e.getCode())
                    .errorMessage(e.getMessage())
                    .containerId(e.getContainerId())
                    .build();
        } catch (Exception e) {
            final String msg = String.format("An unexpected was produced during plan execution calculation: %s", e.getMessage());
            LOGGER.error(msg, e);
            return ExecutePlanningResult.builder()
                    .error(ExecutePlanningResult.ErrorCode.UNEXPECTED_ERROR)
                    .errorMessage(msg)
                    .build();
        }

        stopWatch.reset();
        stopWatch.start();
        for (Map.Entry<String, List<PlanningCommand>> entry : commandsByContainer.entrySet()) {
            try {
                executeContainerCommands(entry.getKey(), entry.getValue());
            } catch (PlanningException e) {
                LOGGER.debug("An error was produced during plan execution on containerId: {}, error code: {}, message: {}",
                             entry.getKey(), e.getCode(), e.getMessage());
                return ExecutePlanningResult.builder()
                        .error(e.getCode())
                        .errorMessage(e.getMessage())
                        .containerId(e.getContainerId())
                        .build();
            } catch (Exception e) {
                final String msg = String.format("An unexpected was produced during plan execution on containerId: %s, message: %s", entry.getKey(), e.getMessage());
                LOGGER.error(msg, e);
                return ExecutePlanningResult.builder()
                        .error(ExecutePlanningResult.ErrorCode.UNEXPECTED_ERROR)
                        .errorMessage(msg)
                        .containerId(entry.getKey())
                        .build();
            }
        }
        stopWatch.stop();
        LOGGER.debug("Time for executing the planning with planning items: " + planningItemList.getItems().size() + "  ->  " + stopWatch.toString());
        return ExecutePlanningResult.builder().build();
    }

    public static class PlanningException extends RuntimeException {

        private final String containerId;
        private final ExecutePlanningResult.ErrorCode code;

        public PlanningException(String message, String containerId, ExecutePlanningResult.ErrorCode code) {
            super(message);
            this.containerId = containerId;
            this.code = code;
        }

        public ExecutePlanningResult.ErrorCode getCode() {
            return code;
        }

        public String getContainerId() {
            return containerId;
        }
    }

    private Map<Long, TaskData> prepareTaskDataForExecutePlanning() {
        //optimized reading, only taskId, taskStatus, actualOwner, deploymentId, and the PlanningTask is needed.
        List<TaskData> result = queryHelper.readTasksDataSummary(0,
                                                                 Arrays.asList(Ready, Reserved, InProgress, Suspended),
                                                                 3);
        return result.stream().collect(Collectors.toMap(TaskData::getTaskId, Function.identity()));
    }

    private void executeContainerCommands(String containerId, List<PlanningCommand> commands) {
        LOGGER.debug("Executing planning commands for container: {}", containerId);
        List<DelegateAndSaveCommand> delegations = new ArrayList<>();
        List<SavePlanningItemCommand> saves = new ArrayList<>();
        List<DeletePlanningItemCommand> deletes = new ArrayList<>();
        for (PlanningCommand command : commands) {
            if (command instanceof DelegateAndSaveCommand) {
                delegations.add((DelegateAndSaveCommand) command);
            } else if (command instanceof SavePlanningItemCommand) {
                saves.add((SavePlanningItemCommand) command);
            } else if (command instanceof DeletePlanningItemCommand) {
                deletes.add((DeletePlanningItemCommand) command);
            }
        }

        bulkDelegate(containerId, delegations);
        List<PlanningCommand> onlyDBCommands = new ArrayList<>(saves);
        onlyDBCommands.addAll(deletes);
        if (!onlyDBCommands.isEmpty()) {
            CompositeCommand onlyDBCommand = new CompositeCommand<>(new TaskCommand<TaskCommand>() {
                @Override
                public TaskCommand execute(Context context) {
                    return null;
                }
            }, onlyDBCommands.toArray(new TaskCommand[0]));
            userTaskService.execute(containerId, onlyDBCommand);
        }
        LOGGER.debug("Planning commands execution for container: {} finished successfully", containerId);
    }

    private void bulkDelegate(String containerId, List<DelegateAndSaveCommand> delegations) {
        LOGGER.debug("Executing bulk delegation for container: {}", containerId);
        for (DelegateAndSaveCommand command : delegations) {
            userTaskService.execute(containerId, command);
        }
        LOGGER.debug("Bulk delegation for container: {} finished successfully", containerId);
    }

    private abstract static class PlanningCommand extends TaskCommand {

        protected PlanningItem planningItem;
        protected TaskContext taskContext;
        protected TaskPersistenceContext persistenceContext;

        public PlanningCommand(PlanningItem planningItem) {
            this.planningItem = planningItem;
        }

        @Override
        public Object execute(Context context) {
            taskContext = (TaskContext) context;
            persistenceContext = taskContext.getPersistenceContext();
            return null;
        }
    }

    private static class SavePlanningItemCommand extends PlanningCommand {

        public SavePlanningItemCommand(PlanningItem planningItem) {
            super(planningItem);
        }

        @Override
        public Object execute(Context context) {
            super.execute(context);
            persistenceContext.merge(new PlanningTaskImpl(planningItem.getTaskId(),
                                                          planningItem.getPlanningTask().getAssignedUser(),
                                                          planningItem.getPlanningTask().getIndex(),
                                                          planningItem.getPlanningTask().isPublished(),
                                                          new Date()));
            return null;
        }
    }

    private static class DeletePlanningItemCommand extends PlanningCommand {

        private long itemId;

        public DeletePlanningItemCommand(long itemId) {
            super(null);
            this.itemId = itemId;
        }

        @Override
        public Object execute(Context context) {
            super.execute(context);
            PlanningTaskImpl instance = persistenceContext.find(PlanningTaskImpl.class, itemId);
            if (instance != null) {
                persistenceContext.remove(instance);
            }
            return null;
        }
    }

    private static class DelegateAndSaveCommand extends PlanningCommand {

        public DelegateAndSaveCommand(PlanningItem planningItem, String userId) {
            super(planningItem);
            this.userId = userId;
        }

        @Override
        public Void execute(Context context) {
            final TaskContext taskContext = (TaskContext) context;
            final Task task = taskContext.getPersistenceContext().findTask(planningItem.getTaskId());
            final org.kie.api.task.model.TaskData taskData = task.getTaskData();
            String status = taskData.getStatus().name();
            if (!(Ready.equals(status) || Reserved.equals(status))) {
                throw new PlanningException(String.format(TASK_MODIFIED_ERROR_MSG_4,
                                                          planningItem.getTaskId(),
                                                          status,
                                                          Arrays.toString(new String[]{Ready, Reserved})),
                                            planningItem.getContainerId(),
                                            ExecutePlanningResult.ErrorCode.TASK_MODIFIED_SINCE_PLAN_CALCULATION_ERROR);
            }

            // the by default jBPM task delegation adds the delegated user as a potential owner of the task, but this is
            // something we don't from the task assigning perspective. So by now we ensure that the tasks assigning
            // api doesn't add it.
            // If provided, the bulk delegation should skipp this automatic adding. (https://issues.redhat.com/browse/JBPM-8924)
            final OrganizationalEntity existingPotentialOwner = findPotentialOwner(task, planningItem.getPlanningTask().getAssignedUser());

            // perform the delegation
            DelegateTaskCommand delegateTaskCommand = new DelegateTaskCommand(planningItem.getTaskId(), getUserId(), planningItem.getPlanningTask().getAssignedUser());
            delegateTaskCommand.execute(context);

            if (existingPotentialOwner == null) {
                // we remove it.
                OrganizationalEntity addedPotentialOwner = findPotentialOwner(task, planningItem.getPlanningTask().getAssignedUser());
                if (addedPotentialOwner != null) {
                    task.getPeopleAssignments().getPotentialOwners().remove(addedPotentialOwner);
                }
            }

            TaskPersistenceContext persistenceContext = taskContext.getPersistenceContext();
            persistenceContext.merge(new PlanningTaskImpl(planningItem.getTaskId(),
                                                          planningItem.getPlanningTask().getAssignedUser(),
                                                          planningItem.getPlanningTask().getIndex(),
                                                          planningItem.getPlanningTask().isPublished(),
                                                          new Date()));
            return null;
        }

        private static OrganizationalEntity findPotentialOwner(Task task, String potentialOwnerId) {
            if (task.getPeopleAssignments() != null && task.getPeopleAssignments().getPotentialOwners() != null) {
                return task.getPeopleAssignments().getPotentialOwners().stream()
                        .filter(organizationalEntity -> organizationalEntity.getId().equals(potentialOwnerId) && organizationalEntity instanceof User)
                        .findFirst().orElse(null);
            }
            return null;
        }
    }
}

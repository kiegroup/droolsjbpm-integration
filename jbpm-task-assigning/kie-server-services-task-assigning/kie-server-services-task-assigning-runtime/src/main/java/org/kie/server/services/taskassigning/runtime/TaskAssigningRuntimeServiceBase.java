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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;
import org.jbpm.services.api.UserTaskService;
import org.jbpm.services.api.query.QueryService;
import org.jbpm.services.task.commands.CompositeCommand;
import org.jbpm.services.task.commands.TaskCommand;
import org.kie.api.runtime.Context;
import org.kie.api.task.model.Status;
import org.kie.server.api.exception.KieServicesException;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.taskassigning.PlanningExecutionResult;
import org.kie.server.api.model.taskassigning.PlanningItem;
import org.kie.server.api.model.taskassigning.PlanningItemList;
import org.kie.server.api.model.taskassigning.PlanningTask;
import org.kie.server.api.model.taskassigning.TaskData;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.KieContainerInstanceImpl;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.services.taskassigning.runtime.command.DelegateAndSaveCommand;
import org.kie.server.services.taskassigning.runtime.command.DeletePlanningItemCommand;
import org.kie.server.services.taskassigning.runtime.command.PlanningCommand;
import org.kie.server.services.taskassigning.runtime.command.PlanningException;
import org.kie.server.services.taskassigning.runtime.command.SavePlanningItemCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.kie.api.task.model.Status.InProgress;
import static org.kie.api.task.model.Status.Ready;
import static org.kie.api.task.model.Status.Reserved;
import static org.kie.api.task.model.Status.Suspended;
import static org.kie.server.api.model.taskassigning.util.StatusConverter.convertFromString;
import static org.kie.server.api.model.taskassigning.util.StatusConverter.convertToStringList;

public class TaskAssigningRuntimeServiceBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskAssigningRuntimeServiceBase.class);

    private static final int INTERNAL_QUERY_PAGE_SIZE = 3000;

    static final String TASK_MODIFIED_ERROR_MSG = "Task: %s was modified by an external action since the last executed plan";

    static final String TASK_MODIFIED_ERROR_MSG_1 = TASK_MODIFIED_ERROR_MSG + " actualOwner is %s but the last assignedUser is %s";

    static final String TASK_MODIFIED_ERROR_MSG_2 = TASK_MODIFIED_ERROR_MSG + " actualOwner is %s but the expected is %s";

    static final String TASK_MODIFIED_ERROR_MSG_3 = TASK_MODIFIED_ERROR_MSG + " and is no longer in one of the expected status %s";

    static final String UNEXPECTED_ERROR_DURING_PLAN_CALCULATION = "An unexpected error was produced during plan calculation: %s";

    static final String UNEXPECTED_ERROR_DURING_PLAN_EXECUTION = "An unexpected error was produced during plan execution on containerId: %s, message: %s";

    static final String SERVER_NOT_READY_ERROR = "Current server is not ready to serve requests";

    private KieServerImpl kieServer;
    private KieServerRegistry registry;
    private UserTaskService userTaskService;
    private TaskAssigningRuntimeServiceQueryHelper queryHelper;

    public TaskAssigningRuntimeServiceBase(KieServerImpl kieServer, KieServerRegistry registry, UserTaskService userTaskService, QueryService queryService) {
        this.kieServer = kieServer;
        this.registry = registry;
        this.userTaskService = userTaskService;
        this.queryHelper = createQueryHelper(registry, userTaskService, queryService);
    }

    public List<TaskData> executeFindTasksQuery(Map<String, Object> params) {
        checkServerStatus();
        return queryHelper.executeFindTasksQuery(params);
    }

    public PlanningExecutionResult executePlanning(PlanningItemList planningItemList, String userId) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        checkServerStatus();
        Map<String, List<PlanningCommand>> commandsByContainer;
        try {
            commandsByContainer = calculatePlanningCommands(planningItemList, userId);
        } catch (PlanningException e) {
            LOGGER.debug("An error was produced during plan calculation, containerId: {}, error code: {}, message: {}",
                         e.getContainerId(), e.getCode(), e.getMessage());
            return PlanningExecutionResult.builder()
                    .error(e.getCode())
                    .errorMessage(e.getMessage())
                    .containerId(e.getContainerId())
                    .build();
        } catch (Exception e) {
            final String msg = String.format(UNEXPECTED_ERROR_DURING_PLAN_CALCULATION, e.getMessage());
            LOGGER.error(msg, e);
            return PlanningExecutionResult.builder()
                    .error(PlanningExecutionResult.ErrorCode.UNEXPECTED_ERROR)
                    .errorMessage(msg)
                    .build();
        }
        stopWatch.stop();
        LOGGER.debug("Time to calculate the planning commands: {}", stopWatch);

        stopWatch.reset();
        stopWatch.start();
        for (Map.Entry<String, List<PlanningCommand>> entry : commandsByContainer.entrySet()) {
            try {
                executeContainerCommands(entry.getKey(), entry.getValue());
            } catch (PlanningException e) {
                LOGGER.debug("An error was produced during plan execution on containerId: {}, error code: {}, message: {}",
                             entry.getKey(), e.getCode(), e.getMessage());
                return PlanningExecutionResult.builder()
                        .error(e.getCode())
                        .errorMessage(e.getMessage())
                        .containerId(e.getContainerId())
                        .build();
            } catch (Exception e) {
                final String msg = String.format(UNEXPECTED_ERROR_DURING_PLAN_EXECUTION, entry.getKey(), e.getMessage());
                LOGGER.error(msg, e);
                return PlanningExecutionResult.builder()
                        .error(PlanningExecutionResult.ErrorCode.UNEXPECTED_ERROR)
                        .errorMessage(msg)
                        .containerId(entry.getKey())
                        .build();
            }
        }
        stopWatch.stop();
        LOGGER.debug("Time for executing the planning with planning items: {}  ->  {}", planningItemList.getItems().size(), stopWatch);
        return PlanningExecutionResult.builder().build();
    }

    private Map<String, List<PlanningCommand>> calculatePlanningCommands(PlanningItemList planningItemList, String userId) {
        final Map<String, List<PlanningCommand>> commandsByContainer = new HashMap<>();
        final Map<Long, TaskData> taskDataById = prepareTaskDataForExecutePlanning();
        for (PlanningItem planningItem : planningItemList.getItems()) {
            final TaskData taskData = taskDataById.remove(planningItem.getTaskId());
            if (taskData == null) {
                // Un-common case, task is no longer in one of the managed status. It was probably assigned, started,
                // and completed in middle.
                // Proposal A) rollbacks this change since the solution is being updated at this moment
                // and a new plan will arrive soon.
                throw new PlanningException(String.format(TASK_MODIFIED_ERROR_MSG_3,
                                                          planningItem.getPlanningTask().getTaskId(),
                                                          Arrays.toString(new Status[]{Ready, Reserved, InProgress, Suspended})),
                                            planningItem.getContainerId(),
                                            PlanningExecutionResult.ErrorCode.TASK_MODIFIED_SINCE_PLAN_CALCULATION_ERROR);
            }

            final String actualOwner = taskData.getActualOwner();
            final PlanningTask actualPlanningTask = taskData.getPlanningTask();
            final Status taskStatus = convertFromString(taskData.getStatus());

            if (isNotEmpty(actualOwner) &&
                    actualPlanningTask != null &&
                    actualOwner.equals(actualPlanningTask.getAssignedUser()) &&
                    actualPlanningTask.equals(planningItem.getPlanningTask())) {
                continue;
            }

            switch (taskStatus) {
                case Ready:
                    addCommand(commandsByContainer, planningItem.getContainerId(), new DelegateAndSaveCommand(planningItem, userId));
                    break;

                case Reserved:
                    if (actualPlanningTask != null && !actualOwner.equals(actualPlanningTask.getAssignedUser()) && !actualOwner.equals(planningItem.getPlanningTask().getAssignedUser())) {
                        // the task was manually reassigned from the task list "in the middle" and we are not updating to
                        // current user, so it's not the last update that is coming.
                        // Proposal A) rollbacks this change since the solution is being updated at this moment
                        // and a new plan will arrive soon.
                        throw new PlanningException(String.format(TASK_MODIFIED_ERROR_MSG_1,
                                                                  planningItem.getPlanningTask().getTaskId(),
                                                                  actualOwner,
                                                                  actualPlanningTask.getAssignedUser()),
                                                    planningItem.getContainerId(),
                                                    PlanningExecutionResult.ErrorCode.TASK_MODIFIED_SINCE_PLAN_CALCULATION_ERROR);
                    } else {
                        addCommand(commandsByContainer, planningItem.getContainerId(), new DelegateAndSaveCommand(planningItem, userId));
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
                                                    PlanningExecutionResult.ErrorCode.TASK_MODIFIED_SINCE_PLAN_CALCULATION_ERROR);
                    } else {
                        // task might have been created, assigned and started/suspended completely out of the task
                        // or the planning data might have changed. Just update the planning data.
                        addCommand(commandsByContainer, planningItem.getContainerId(), new SavePlanningItemCommand(planningItem));
                    }
                    break;
                default:
                    // sonar required, no more cases are expected for this switch by construction.
                    throw new IndexOutOfBoundsException("Value: " + taskData.getStatus() + " is out of range in current switch");
            }
        }
        for (TaskData taskData : taskDataById.values()) {
            final Status status = convertFromString(taskData.getStatus());
            if ((status == Ready || status == Reserved || status == Suspended) && taskData.getPlanningTask() != null) {
                commandsByContainer.computeIfAbsent(taskData.getContainerId(), k -> new ArrayList<>()).add(new DeletePlanningItemCommand(taskData.getTaskId()));
            }
        }
        return commandsByContainer;
    }

    TaskAssigningRuntimeServiceQueryHelper createQueryHelper(KieServerRegistry registry, UserTaskService userTaskService, QueryService queryService) {
        return new TaskAssigningRuntimeServiceQueryHelper(registry, userTaskService, queryService);
    }

    private Map<Long, TaskData> prepareTaskDataForExecutePlanning() {
        //optimized reading, only taskId, taskStatus, actualOwner, deploymentId, and the PlanningTask is needed.
        List<TaskData> result = queryHelper.readTasksDataSummary(0,
                                                                 convertToStringList(Ready, Reserved, InProgress, Suspended),
                                                                 INTERNAL_QUERY_PAGE_SIZE);
        return result.stream().collect(Collectors.toMap(TaskData::getTaskId, Function.identity()));
    }

    private void executeContainerCommands(String containerId, List<PlanningCommand> commands) {
        LOGGER.debug("Executing planning commands for container: {}", containerId);
        List<DelegateAndSaveCommand> delegations = new ArrayList<>();
        List<SavePlanningItemCommand> saves = new ArrayList<>();
        List<DeletePlanningItemCommand> deletes = new ArrayList<>();
        validateContainer(containerId);
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

    private void validateContainer(String containerId) {
        KieContainerInstanceImpl container = registry.getContainer(containerId);
        if (container == null || (container.getStatus() != KieContainerStatus.STARTED && container.getStatus() != KieContainerStatus.DEACTIVATED)) {
            throw new KieServicesException("Container " + containerId + " is not available to serve requests");
        }
    }

    private void bulkDelegate(String containerId, List<DelegateAndSaveCommand> delegations) {
        LOGGER.debug("Executing bulk delegation for container: {}", containerId);
        for (DelegateAndSaveCommand command : delegations) {
            userTaskService.execute(containerId, command);
        }
        LOGGER.debug("Bulk delegation for container: {} finished successfully", containerId);
    }

    private void addCommand(Map<String, List<PlanningCommand>> commandsByContainer, String containerId, PlanningCommand command) {
        commandsByContainer.computeIfAbsent(containerId, k -> new ArrayList<>()).add(command);
    }

    private void checkServerStatus() {
        if (!kieServer.isKieServerReady()) {
            throw new KieServicesException(SERVER_NOT_READY_ERROR);
        }
    }
}

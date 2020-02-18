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

package org.kie.server.services.taskassigning.runtime;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.services.api.UserTaskService;
import org.jbpm.services.api.query.QueryService;
import org.jbpm.services.task.commands.CompositeCommand;
import org.jbpm.services.task.commands.TaskCommand;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.task.model.Status;
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
import org.kie.server.services.taskassigning.runtime.command.SavePlanningItemCommand;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.kie.api.task.model.Status.InProgress;
import static org.kie.api.task.model.Status.Ready;
import static org.kie.api.task.model.Status.Reserved;
import static org.kie.api.task.model.Status.Suspended;
import static org.kie.server.api.model.taskassigning.util.StatusConverter.convertToString;
import static org.kie.server.services.taskassigning.runtime.TaskAssigningRuntimeServiceBase.TASK_MODIFIED_ERROR_MSG_1;
import static org.kie.server.services.taskassigning.runtime.TaskAssigningRuntimeServiceBase.TASK_MODIFIED_ERROR_MSG_2;
import static org.kie.server.services.taskassigning.runtime.TaskAssigningRuntimeServiceBase.TASK_MODIFIED_ERROR_MSG_3;
import static org.kie.server.services.taskassigning.runtime.TaskAssigningRuntimeServiceBase.UNEXPECTED_ERROR_DURING_PLAN_CALCULATION;
import static org.kie.server.services.taskassigning.runtime.TaskAssigningRuntimeServiceBase.UNEXPECTED_ERROR_DURING_PLAN_EXECUTION;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TaskAssigningRuntimeServiceBaseTest {

    private static final String USER_ID = "USER_ID";
    private static final long TASK_ID = 1L;
    private static final String CONTAINER_ID = "CONTAINER_ID";
    private static final String ASSIGNED_USER_ID = "ASSIGNED_USER_ID";
    private static final String PREVIOUS_ASSIGNED_USER_ID = "PREVIOUS_ASSIGNED_USER_ID";
    private static final String PREVIOUS_ASSIGNED_USER_ID_CHANGED = "PREVIOUS_ASSIGNED_USER_ID_CHANGED";
    private static final String ERROR_MESSAGE = "ERROR_MESSAGE";

    @Mock
    private KieServerImpl kieServer;
    @Mock
    private KieServerRegistry registry;

    @Mock
    private UserTaskService userTaskService;

    @Mock
    private QueryService queryService;

    @Mock
    private TaskAssigningRuntimeServiceQueryHelper queryHelper;

    @Mock
    private KieContainerInstanceImpl container;

    @Captor
    private ArgumentCaptor<TaskCommand> planningCommandCaptor;

    private TaskAssigningRuntimeServiceBase serviceBase;

    @Before
    public void setUp() {
        when(kieServer.isKieServerReady()).thenReturn(true);
        serviceBase = new TaskAssigningRuntimeServiceBaseMock(kieServer, registry, userTaskService, queryService);
    }

    @Test
    public void executePlanningWithTaskNoLongerInActiveStatus() {
        List<TaskData> taskDataList = Collections.emptyList();
        PlanningItem planningItem = mockPlanningItem(TASK_ID, CONTAINER_ID, ASSIGNED_USER_ID);
        PlanningItemList planningItemList = new PlanningItemList(Collections.singletonList(planningItem));

        prepareExecution(taskDataList, CONTAINER_ID);
        PlanningExecutionResult result = serviceBase.executePlanning(planningItemList, USER_ID);

        String errorMessage = String.format(TASK_MODIFIED_ERROR_MSG_3,
                                            planningItem.getPlanningTask().getTaskId(),
                                            Arrays.toString(new Status[]{Ready, Reserved, InProgress, Suspended}));
        assertHasError(result,
                       PlanningExecutionResult.ErrorCode.TASK_MODIFIED_SINCE_PLAN_CALCULATION_ERROR,
                       errorMessage,
                       CONTAINER_ID);
    }

    @Test
    public void executePlanningWithTaskInReadyStatus() {
        TaskData taskData = mockTaskData(TASK_ID, Ready);
        List<TaskData> taskDataList = Collections.singletonList(taskData);
        PlanningItem planningItem = mockPlanningItem(TASK_ID, CONTAINER_ID, ASSIGNED_USER_ID);
        PlanningItemList planningItemList = new PlanningItemList(Collections.singletonList(planningItem));

        prepareExecution(taskDataList, CONTAINER_ID);
        PlanningExecutionResult result = serviceBase.executePlanning(planningItemList, USER_ID);
        verify(userTaskService).execute(eq(CONTAINER_ID), planningCommandCaptor.capture());

        assertDelegateAndSaveCommand(planningCommandCaptor.getAllValues(), 0, USER_ID, planningItem);
        assertNoError(result);
    }

    @Test
    public void executePlanningWithTaskInReservedStatusWithNoPlanningTask() {
        TaskData taskData = mockTaskData(TASK_ID, Reserved);
        List<TaskData> taskDataList = Collections.singletonList(taskData);
        PlanningItem planningItem = mockPlanningItem(TASK_ID, CONTAINER_ID, ASSIGNED_USER_ID);
        PlanningItemList planningItemList = new PlanningItemList(Collections.singletonList(planningItem));

        prepareExecution(taskDataList, CONTAINER_ID);
        PlanningExecutionResult result = serviceBase.executePlanning(planningItemList, USER_ID);
        verify(userTaskService).execute(eq(CONTAINER_ID), planningCommandCaptor.capture());

        assertDelegateAndSaveCommand(planningCommandCaptor.getAllValues(), 0, USER_ID, planningItem);
        assertNoError(result);
    }

    @Test
    public void executePlanningWithTaskInReservedStatusWithPlanningTaskDontReassigned() {
        PlanningTask planningTask = mockPlanningTask(TASK_ID, PREVIOUS_ASSIGNED_USER_ID);
        TaskData taskData = mockTaskData(TASK_ID, Reserved, PREVIOUS_ASSIGNED_USER_ID, planningTask);
        List<TaskData> taskDataList = Collections.singletonList(taskData);
        PlanningItem planningItem = mockPlanningItem(TASK_ID, CONTAINER_ID, ASSIGNED_USER_ID);
        PlanningItemList planningItemList = new PlanningItemList(Collections.singletonList(planningItem));

        prepareExecution(taskDataList, CONTAINER_ID);
        PlanningExecutionResult result = serviceBase.executePlanning(planningItemList, USER_ID);
        verify(userTaskService).execute(eq(CONTAINER_ID), planningCommandCaptor.capture());

        assertDelegateAndSaveCommand(planningCommandCaptor.getAllValues(), 0, USER_ID, planningItem);
        assertNoError(result);
    }

    @Test
    public void executePlanningWithTaskInReservedStatusWithPlanningTaskButReassigned() {
        PlanningTask planningTask = mockPlanningTask(TASK_ID, PREVIOUS_ASSIGNED_USER_ID);
        TaskData taskData = mockTaskData(TASK_ID, Reserved, PREVIOUS_ASSIGNED_USER_ID_CHANGED, planningTask);
        List<TaskData> taskDataList = Collections.singletonList(taskData);
        PlanningItem planningItem = mockPlanningItem(TASK_ID, CONTAINER_ID, ASSIGNED_USER_ID);
        PlanningItemList planningItemList = new PlanningItemList(Collections.singletonList(planningItem));

        prepareExecution(taskDataList, CONTAINER_ID);
        PlanningExecutionResult result = serviceBase.executePlanning(planningItemList, USER_ID);
        verify(userTaskService, never()).execute(eq(CONTAINER_ID), any());

        String errorMessage = String.format(TASK_MODIFIED_ERROR_MSG_1,
                                            taskData.getTaskId(),
                                            PREVIOUS_ASSIGNED_USER_ID_CHANGED,
                                            PREVIOUS_ASSIGNED_USER_ID);
        assertHasError(result,
                       PlanningExecutionResult.ErrorCode.TASK_MODIFIED_SINCE_PLAN_CALCULATION_ERROR,
                       errorMessage,
                       CONTAINER_ID);
    }

    @Test
    public void executePlanningWithTaskInInProgressStatusWithActualOwnerChanged() {
        executePlanningWithTaskInInProgressOrSuspendedStatusWithActualOwnerChanged(InProgress);
    }

    @Test
    public void executePlanningWithTaskInSuspendedStatusWithActualOwnerChanged() {
        executePlanningWithTaskInInProgressOrSuspendedStatusWithActualOwnerChanged(Suspended);
    }

    @Test
    public void executePlanningWithTaskInInProgressStatusWithActualOwnerUnChanged() {
        executePlanningWithTaskInInProgressOrSuspendedStatusWithActualOwnerUnChanged(InProgress);
    }

    @Test
    public void executePlanningWithTaskInSuspendedStatusWithActualOwnerUnChanged() {
        executePlanningWithTaskInInProgressOrSuspendedStatusWithActualOwnerUnChanged(Suspended);
    }

    @Test
    public void executePlanningWithDetachedReadyTask() {
        executePlanningWithDetachedTask(Ready);
    }

    @Test
    public void executePlanningWithDetachedReservedTask() {
        executePlanningWithDetachedTask(Reserved);
    }

    @Test
    public void executePlanningWithDetachedSuspendedTask() {
        executePlanningWithDetachedTask(Suspended);
    }

    @Test
    public void unexpectedErrorDuringPlanCalculation() {
        when(queryHelper.readTasksDataSummary(anyInt(), any(), anyInt())).thenThrow(new RuntimeException(ERROR_MESSAGE));
        PlanningExecutionResult result = serviceBase.executePlanning(new PlanningItemList(Collections.emptyList()), USER_ID);
        assertHasError(result, PlanningExecutionResult.ErrorCode.UNEXPECTED_ERROR, String.format(UNEXPECTED_ERROR_DURING_PLAN_CALCULATION, ERROR_MESSAGE), null);
    }

    @Test
    public void unexpectedErrorDuringPlanExecution() {
        TaskData taskData = mockTaskData(TASK_ID, Ready);
        List<TaskData> taskDataList = Collections.singletonList(taskData);
        PlanningItem planningItem = mockPlanningItem(TASK_ID, CONTAINER_ID, ASSIGNED_USER_ID);
        PlanningItemList planningItemList = new PlanningItemList(Collections.singletonList(planningItem));

        prepareExecution(taskDataList, CONTAINER_ID);
        when(userTaskService.execute(eq(CONTAINER_ID), any())).thenThrow(new RuntimeException(ERROR_MESSAGE));
        PlanningExecutionResult result = serviceBase.executePlanning(planningItemList, USER_ID);
        assertHasError(result, PlanningExecutionResult.ErrorCode.UNEXPECTED_ERROR, String.format(UNEXPECTED_ERROR_DURING_PLAN_EXECUTION, CONTAINER_ID, ERROR_MESSAGE), CONTAINER_ID);
    }

    @Test
    public void executeFindTasksQuery() {
        Map<String, Object> params = new HashMap<>();
        serviceBase.executeFindTasksQuery(params);
        verify(queryHelper).executeFindTasksQuery(params);
    }

    private void prepareExecution(List<TaskData> taskDataList, String containerId) {
        when(queryHelper.readTasksDataSummary(anyInt(), any(), anyInt())).thenReturn(taskDataList);
        when(registry.getContainer(containerId)).thenReturn(container);
        when(container.getStatus()).thenReturn(KieContainerStatus.STARTED);
    }

    private void executePlanningWithTaskInInProgressOrSuspendedStatusWithActualOwnerChanged(Status status) {
        TaskData taskData = mockTaskData(TASK_ID, status, PREVIOUS_ASSIGNED_USER_ID_CHANGED, null);
        List<TaskData> taskDataList = Collections.singletonList(taskData);
        PlanningItem planningItem = mockPlanningItem(TASK_ID, CONTAINER_ID, ASSIGNED_USER_ID);
        PlanningItemList planningItemList = new PlanningItemList(Collections.singletonList(planningItem));

        prepareExecution(taskDataList, CONTAINER_ID);
        PlanningExecutionResult result = serviceBase.executePlanning(planningItemList, USER_ID);
        verify(userTaskService, never()).execute(eq(CONTAINER_ID), any());

        String errorMessage = String.format(TASK_MODIFIED_ERROR_MSG_2,
                                            planningItem.getPlanningTask().getTaskId(),
                                            PREVIOUS_ASSIGNED_USER_ID_CHANGED,
                                            ASSIGNED_USER_ID);

        assertHasError(result,
                       PlanningExecutionResult.ErrorCode.TASK_MODIFIED_SINCE_PLAN_CALCULATION_ERROR,
                       errorMessage,
                       CONTAINER_ID);
    }

    private void executePlanningWithTaskInInProgressOrSuspendedStatusWithActualOwnerUnChanged(Status status) {
        TaskData taskData = mockTaskData(TASK_ID, status, ASSIGNED_USER_ID, null);
        List<TaskData> taskDataList = Collections.singletonList(taskData);
        PlanningItem planningItem = mockPlanningItem(TASK_ID, CONTAINER_ID, ASSIGNED_USER_ID);
        PlanningItemList planningItemList = new PlanningItemList(Collections.singletonList(planningItem));

        prepareExecution(taskDataList, CONTAINER_ID);
        PlanningExecutionResult result = serviceBase.executePlanning(planningItemList, USER_ID);
        verify(userTaskService).execute(eq(CONTAINER_ID), planningCommandCaptor.capture());

        CompositeCommand compositeCommand = (CompositeCommand) planningCommandCaptor.getValue();
        assertSavePlanningItemCommand(compositeCommand.getCommands(), 0, planningItem);
        assertNoError(result);
    }

    private void executePlanningWithDetachedTask(Status status) {
        TaskData taskData = mockTaskData(TASK_ID, status, ASSIGNED_USER_ID, CONTAINER_ID, mockPlanningTask(TASK_ID, ASSIGNED_USER_ID));
        List<TaskData> taskDataList = Collections.singletonList(taskData);
        PlanningItemList planningItemList = new PlanningItemList(Collections.emptyList());

        prepareExecution(taskDataList, CONTAINER_ID);
        PlanningExecutionResult result = serviceBase.executePlanning(planningItemList, USER_ID);
        verify(userTaskService).execute(eq(CONTAINER_ID), planningCommandCaptor.capture());

        CompositeCommand compositeCommand = (CompositeCommand) planningCommandCaptor.getValue();
        assertDeletePlanningItemCommand(compositeCommand.getCommands(), 0, TASK_ID);
        assertNoError(result);
    }

    private void assertHasError(PlanningExecutionResult result, PlanningExecutionResult.ErrorCode error, String message, String containerId) {
        assertEquals(error, result.getError());
        assertEquals(message, result.getErrorMessage());
        assertEquals(containerId, result.getContainerId());
    }

    private void assertNoError(PlanningExecutionResult result) {
        assertFalse(result.hasError());
    }

    private void assertDelegateAndSaveCommand(List<TaskCommand> commands, int index, String userId, PlanningItem planningItem) {
        assertTrue("DelegateAndSaveCommand is expected at index: " + index, commands.get(index) instanceof DelegateAndSaveCommand);
        DelegateAndSaveCommand delegateAndSaveCommand = (DelegateAndSaveCommand) commands.get(index);
        assertEquals("DelegateAndSaveCommand for userId: " + userId + " is expected at index: " + index, userId, delegateAndSaveCommand.getUserId());
        assertEquals("DelegateAndSaveCommand for planningItem: " + planningItem + " is expected at index: ", planningItem, delegateAndSaveCommand.getPlanningItem());
    }

    private void assertSavePlanningItemCommand(List<TaskCommand> commands, int index, PlanningItem planningItem) {
        assertTrue("SavePlanningItemCommand is expected at index: " + index, commands.get(index) instanceof SavePlanningItemCommand);
        SavePlanningItemCommand savePlanningItemCommand = (SavePlanningItemCommand) commands.get(index);
        assertEquals("SavePlanningItemCommand for planningItem: " + planningItem + " is expected at index: ", planningItem, savePlanningItemCommand.getPlanningItem());
    }

    private void assertDeletePlanningItemCommand(List<TaskCommand> commands, int index, Long taskId) {
        assertTrue("DeletePlanningItemCommand is expected at index: " + index, commands.get(index) instanceof DeletePlanningItemCommand);
        DeletePlanningItemCommand deletePlanningItemCommand = (DeletePlanningItemCommand) commands.get(index);
        assertEquals("DeletePlanningItemCommand for taskId: " + taskId + " is expected at index: ", taskId, deletePlanningItemCommand.getItemId(), 0);
    }

    private class TaskAssigningRuntimeServiceBaseMock extends TaskAssigningRuntimeServiceBase {

        public TaskAssigningRuntimeServiceBaseMock(KieServerImpl kieServer, KieServerRegistry registry, UserTaskService userTaskService, QueryService queryService) {
            super(kieServer, registry, userTaskService, queryService);
        }

        @Override
        TaskAssigningRuntimeServiceQueryHelper createQueryHelper(KieServerRegistry registry, UserTaskService userTaskService, QueryService queryService) {
            return queryHelper;
        }
    }

    private TaskData mockTaskData(long taskId, Status status) {
        return mockTaskData(taskId, status, null, null);
    }

    private TaskData mockTaskData(long taskId, Status status, String actualOwner, PlanningTask planningTask) {
        return mockTaskData(taskId, status, actualOwner, null, planningTask);
    }

    private TaskData mockTaskData(long taskId, Status status, String actualOwner, String containerId, PlanningTask planningTask) {
        return TaskData.builder()
                .taskId(taskId)
                .status(convertToString(status))
                .actualOwner(actualOwner)
                .containerId(containerId)
                .planningTask(planningTask)
                .build();
    }

    private PlanningItem mockPlanningItem(long taskId, String containerId, String assignedUser) {
        return PlanningItem.builder()
                .taskId(taskId)
                .containerId(containerId)
                .planningTask(PlanningTask.builder()
                                      .taskId(taskId)
                                      .assignedUser(assignedUser)
                                      .published(true)
                                      .build()
                )
                .build();
    }

    private PlanningTask mockPlanningTask(long taskId, String assignedUser) {
        return PlanningTask.builder()
                .taskId(taskId)
                .assignedUser(assignedUser)
                .build();
    }
}

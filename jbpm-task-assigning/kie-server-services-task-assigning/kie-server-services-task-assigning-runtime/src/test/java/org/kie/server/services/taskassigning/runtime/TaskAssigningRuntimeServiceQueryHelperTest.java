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

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.services.api.UserTaskService;
import org.jbpm.services.api.query.QueryService;
import org.jbpm.services.api.query.model.QueryParam;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.runtime.query.QueryContext;
import org.kie.api.task.model.Status;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.taskassigning.PlanningTask;
import org.kie.server.api.model.taskassigning.TaskData;
import org.kie.server.api.model.taskassigning.TaskInputVariablesReadMode;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.KieContainerInstanceImpl;
import org.kie.server.services.taskassigning.runtime.query.AbstractTaskAssigningQueryMapper;
import org.kie.server.services.taskassigning.runtime.query.TaskAssigningTaskDataQueryMapper;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.kie.server.api.model.taskassigning.QueryParamName.FROM_LAST_MODIFICATION_DATE;
import static org.kie.server.api.model.taskassigning.QueryParamName.FROM_TASK_ID;
import static org.kie.server.api.model.taskassigning.QueryParamName.PAGE;
import static org.kie.server.api.model.taskassigning.QueryParamName.PAGE_SIZE;
import static org.kie.server.api.model.taskassigning.QueryParamName.STATUS;
import static org.kie.server.api.model.taskassigning.QueryParamName.TASK_INPUT_VARIABLES_MODE;
import static org.kie.server.api.model.taskassigning.QueryParamName.TO_TASK_ID;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TaskAssigningRuntimeServiceQueryHelperTest {

    private static final String GREATER_OR_EQUALS_TO = "GREATER_OR_EQUALS_TO";
    private static final String LOWER_OR_EQUALS_TO = "LOWER_OR_EQUALS_TO";
    private static final String EQUALS_TO = "EQUALS_TO";

    private static final String FROM_LAST_MODIFICATION_STR_VALUE = "2020-02-11T16:07:34.332";
    private static final LocalDateTime FROM_LAST_MODIFICATION_VALUE = LocalDateTime.parse(FROM_LAST_MODIFICATION_STR_VALUE);
    private static final List<String> STATUS_VALUE = Arrays.asList("Ready", "Reserved");

    private static final Long FROM_TASK_ID_VALUE = 1L;
    private static final Long TO_TASK_ID_VALUE = 2L;
    private static final Integer PAGE_VALUE = 3;
    private static final Integer PAGE_SIZE_VALUE = 4;

    //  Task1 is Ready and has PlanningTask
    private static final Long TASK1_ID = 1L;
    // Task2 is Reserved and doesn't have PlanningTask
    private static final Long TASK2_ID = 2L;
    // Task3 is Completed
    private static final Long TASK3_ID = 3L;

    private static final Map<String, Object> TASK1_INPUTS = new HashMap<>();
    private static final Map<String, Object> TASK2_INPUTS = new HashMap<>();
    private static final Map<String, Object> TASK3_INPUTS = new HashMap<>();

    private static final String CONTAINER_ID = "CONTAINER_ID";

    @Mock
    private KieServerRegistry registry;

    @Mock
    private QueryService queryService;

    @Mock
    private UserTaskService userTaskService;

    private TaskAssigningRuntimeServiceQueryHelper helper;

    @Captor
    private ArgumentCaptor<QueryParam[]> paramsCaptor;

    @Captor
    private ArgumentCaptor<QueryContext> contextCaptor;

    @Before
    public void setUp() {
        this.helper = spy(new TaskAssigningRuntimeServiceQueryHelper(registry, userTaskService, queryService));
    }

    @Test
    public void executeFindTaskQueryReadNoInputs() {
        Map<String, Object> params = prepareQuery(TaskInputVariablesReadMode.DONT_READ);

        List<TaskData> result = helper.executeFindTasksQuery(params);

        verifyQueryWasExecuted();

        // no inputs were loaded
        assertNull(result.get(0).getInputData());
        assertNull(result.get(1).getInputData());
        assertNull(result.get(2).getInputData());
    }

    @Test
    public void executeFindTaskQueryReadNoInputsByDefault() {
        Map<String, Object> params = prepareQuery(null);

        List<TaskData> result = helper.executeFindTasksQuery(params);

        verifyQueryWasExecuted();

        // no inputs were loaded
        assertNull(result.get(0).getInputData());
        assertNull(result.get(1).getInputData());
        assertNull(result.get(2).getInputData());
    }

    @Test
    public void executeFindTaskQueryReadInputsForAll() {
        Map<String, Object> params = prepareQuery(TaskInputVariablesReadMode.READ_FOR_ALL);

        List<TaskData> result = helper.executeFindTasksQuery(params);

        verifyQueryWasExecuted();

        // all the inputs were loaded
        assertEquals(TASK1_INPUTS, result.get(0).getInputData());
        assertEquals(TASK2_INPUTS, result.get(1).getInputData());
        assertEquals(TASK3_INPUTS, result.get(2).getInputData());
    }

    @Test
    public void executeFindTaskQueryReadInputsForActiveTasks() {
        Map<String, Object> params = prepareQuery(TaskInputVariablesReadMode.READ_FOR_ACTIVE_TASKS_WITH_NO_PLANNING_ENTITY);

        List<TaskData> result = helper.executeFindTasksQuery(params);

        verifyQueryWasExecuted();

        // task1 is Ready and has PlanningTask
        assertEquals(TASK1_INPUTS, result.get(0).getInputData());
        // task2 is Reserved but hasn't PlanningTask
        assertNull(result.get(1).getInputData());
        // task3 is not active.
        assertNull(result.get(2).getInputData());
    }

    @Test
    public void readTaskDataSummary() {
        List<TaskData> invocation0 = mockTasks();
        List<TaskData> invocation1 = Arrays.asList(mockTaskData(4L), mockTaskData(5L));
        List<TaskData> invocation2 = Collections.singletonList(mockTaskData(6L));
        doAnswer(new Answer() {
            private int invocations = 0;

            public Object answer(InvocationOnMock invocation) {
                switch (invocations++) {
                    case 0:
                        return invocation0;
                    case 1:
                        return invocation1;
                    case 2:
                        return invocation2;
                    default:
                        return Collections.emptyList();
                }
            }
        }).when(helper).executeQuery(eq(queryService), anyString(), any(TaskAssigningTaskDataQueryMapper.class), any(), any());
        List<TaskData> result = helper.readTasksDataSummary(0, Collections.emptyList(), 10);
        assertEquals(TASK1_ID, result.get(0).getTaskId(), 0);
        assertEquals(TASK2_ID, result.get(1).getTaskId(), 0);
        assertEquals(TASK3_ID, result.get(2).getTaskId(), 0);
        assertEquals(4L, result.get(3).getTaskId(), 0);
        assertEquals(5L, result.get(4).getTaskId(), 0);
        assertEquals(6L, result.get(5).getTaskId(), 0);
    }

    private Map<String, Object> prepareQuery(TaskInputVariablesReadMode readMode) {
        Map<String, Object> params = mockQueryParams(readMode);
        List<TaskData> taskDataList = mockTasks();
        doReturn(taskDataList).when(helper)
                .executeQuery(eq(queryService), anyString(), any(TaskAssigningTaskDataQueryMapper.class), any(), any());
        KieContainerInstanceImpl container = mock(KieContainerInstanceImpl.class);
        when(container.getStatus()).thenReturn(KieContainerStatus.STARTED);
        when(registry.getContainer(CONTAINER_ID)).thenReturn(container);
        return params;
    }

    private void verifyQueryWasExecuted() {
        verify(helper).executeQuery(eq(queryService), anyString(), any(TaskAssigningTaskDataQueryMapper.class), contextCaptor.capture(), paramsCaptor.capture());
        QueryParam[] queryParams = paramsCaptor.getValue();
        assertContainsParam(queryParams, AbstractTaskAssigningQueryMapper.TASK_QUERY_COLUMN.TASK_ID.columnName(), GREATER_OR_EQUALS_TO, FROM_TASK_ID_VALUE, 0);
        assertContainsParam(queryParams, AbstractTaskAssigningQueryMapper.TASK_QUERY_COLUMN.TASK_ID.columnName(), LOWER_OR_EQUALS_TO, TO_TASK_ID_VALUE, 1);
        assertContainsParam(queryParams, AbstractTaskAssigningQueryMapper.TASK_QUERY_COLUMN.LAST_MODIFICATION_DATE.columnName(), GREATER_OR_EQUALS_TO, Date.from(FROM_LAST_MODIFICATION_VALUE.atZone(ZoneId.systemDefault()).toInstant()), 2);
        assertContainsParam(queryParams, AbstractTaskAssigningQueryMapper.TASK_QUERY_COLUMN.STATUS.columnName(), EQUALS_TO, STATUS_VALUE, 3);

        QueryContext context = contextCaptor.getValue();
        assertEquals(AbstractTaskAssigningQueryMapper.TASK_QUERY_COLUMN.TASK_ID.columnName(), context.getOrderBy());
        assertEquals(PAGE_SIZE_VALUE, context.getCount(), 0);
        assertEquals(PAGE_VALUE * PAGE_SIZE_VALUE, context.getOffset(), 0);
        assertTrue(context.isAscending());
    }

    private void assertContainsParam(QueryParam[] params, String columnName, String operation, Object value, int index) {
        QueryParam param = params[index];
        assertEquals(columnName, param.getColumn());
    }

    private List<TaskData> mockTasks() {
        TaskData task1 = mockTaskData(TASK1_ID, Status.Ready, false, CONTAINER_ID);
        TaskData task2 = mockTaskData(TASK2_ID, Status.Reserved, true, CONTAINER_ID);
        TaskData task3 = mockTaskData(TASK3_ID, Status.Completed, true, CONTAINER_ID);

        TASK1_INPUTS.put("task1.param", "task1.param.value");
        TASK2_INPUTS.put("task2.param", "task2.param.value");
        TASK3_INPUTS.put("task3.param", "task3.param.value");

        when(userTaskService.getTaskInputContentByTaskId(any(), eq(TASK1_ID))).thenReturn(TASK1_INPUTS);
        when(userTaskService.getTaskInputContentByTaskId(any(), eq(TASK2_ID))).thenReturn(TASK2_INPUTS);
        when(userTaskService.getTaskInputContentByTaskId(any(), eq(TASK3_ID))).thenReturn(TASK3_INPUTS);

        return Arrays.asList(task1, task2, task3);
    }

    private Map<String, Object> mockQueryParams(TaskInputVariablesReadMode readMode) {
        Map<String, Object> params = new HashMap<>();
        params.put(FROM_TASK_ID, FROM_TASK_ID_VALUE);
        params.put(TO_TASK_ID, TO_TASK_ID_VALUE);
        params.put(FROM_LAST_MODIFICATION_DATE, FROM_LAST_MODIFICATION_VALUE);
        params.put(STATUS, STATUS_VALUE);
        params.put(PAGE, PAGE_VALUE);
        params.put(PAGE_SIZE, PAGE_SIZE_VALUE);
        if (readMode != null) {
            params.put(TASK_INPUT_VARIABLES_MODE, readMode.name());
        }
        return params;
    }

    private TaskData mockTaskData(Long taskId) {
        return TaskData.builder().taskId(taskId).build();
    }

    private TaskData mockTaskData(Long taskId, Status status, boolean hasPlanningTask, String containerId) {
        TaskData taskData = TaskData.builder()
                .taskId(taskId)
                .status(status.name())
                .containerId(containerId)
                .build();
        if (hasPlanningTask) {
            PlanningTask planningTask = PlanningTask.builder()
                    .taskId(taskId)
                    .build();
            taskData.setPlanningTask(planningTask);
        }
        return taskData;
    }
}

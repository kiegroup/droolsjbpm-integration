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

package org.kie.server.client.impl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.taskassigning.PlanningExecutionResult;
import org.kie.server.api.model.taskassigning.PlanningItemList;
import org.kie.server.api.model.taskassigning.QueryParamName;
import org.kie.server.api.model.taskassigning.TaskDataList;
import org.kie.server.api.model.taskassigning.TaskInputVariablesReadMode;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.balancer.LoadBalancer;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.kie.server.api.model.taskassigning.TaskAssigningRestURI.TASK_ASSIGNING_EXECUTE_PLANNING_URI;
import static org.kie.server.api.model.taskassigning.TaskAssigningRestURI.TASK_ASSIGNING_QUERIES_TASK_DATA_URI;
import static org.kie.server.api.model.taskassigning.TaskAssigningRestURI.TASK_ASSIGNING_RUNTIME_URI;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TaskAssigningRuntimeClientImplTest {

    private static final String SERVER_URL = "SERVER_URL";
    private static final String USER_ID = "USER_ID";

    private static final Long FROM_TASK_ID = 1L;
    private static final List<String> STATUS = Arrays.asList("Reserved", "InProgress");
    private static final LocalDateTime FROM_LAST_MODIFICATION_DATE = LocalDateTime.now();
    private static final Integer PAGE = 1;
    private static final Integer PAGE_SIZE = 10;

    @Mock
    private KieServicesConfiguration config;

    @Mock
    private LoadBalancer loadBalancer;

    @Mock
    private ClassLoader classLoader;

    @Mock
    private PlanningExecutionResult planningExecutionResult;

    @Mock
    private PlanningItemList planningItemList;

    @Mock
    private TaskDataList taskDataList;

    private Map<String, Object> queryParams;

    private TaskAssigningRuntimeClientImpl runtimeClient;

    @Before
    public void setUp() {
        when(config.getMarshallingFormat()).thenReturn(MarshallingFormat.XSTREAM);
        when(config.getLoadBalancer()).thenReturn(loadBalancer);
        when(config.clone()).thenReturn(config);
        when(loadBalancer.getUrl()).thenReturn(SERVER_URL);
        runtimeClient = spy(new TaskAssigningRuntimeClientImpl(config, classLoader));
    }

    @Test
    public void executePlanningRest() {
        when(config.isRest()).thenReturn(true);
        String expectedUri = SERVER_URL + "/" + TASK_ASSIGNING_RUNTIME_URI + "/" + TASK_ASSIGNING_EXECUTE_PLANNING_URI + "?user=" + USER_ID;
        doReturn(planningExecutionResult)
                .when(runtimeClient)
                .makeHttpPostRequestAndCreateCustomResponse(eq(expectedUri),
                                                            eq(planningItemList),
                                                            eq(PlanningExecutionResult.class));
        PlanningExecutionResult result = runtimeClient.executePlanning(planningItemList, USER_ID);
        assertEquals(planningExecutionResult, result);
    }

    @Test
    public void executePlanningJms() {
        when(config.isRest()).thenReturn(false);
        Assertions.assertThatThrownBy(() -> runtimeClient.executePlanning(planningItemList, USER_ID))
                .hasMessage("JMS protocol is not implemented for this service.");
    }

    @Test
    public void findTasksWithReadModeRest() {
        when(config.isRest()).thenReturn(true);
        findTasksRest(TaskInputVariablesReadMode.READ_FOR_ALL);
    }

    @Test
    public void findTasksWithoutReadModeRest() {
        when(config.isRest()).thenReturn(true);
        findTasksRest(null);
    }

    @Test
    public void findTasksWithReadModeJms() {
        when(config.isRest()).thenReturn(false);
        Assertions.assertThatThrownBy(() -> runtimeClient.findTasks(FROM_TASK_ID, STATUS, FROM_LAST_MODIFICATION_DATE, PAGE, PAGE_SIZE, TaskInputVariablesReadMode.READ_FOR_ALL))
                .hasMessage("JMS protocol is not implemented for this service.");
    }

    @Test
    public void findTasksWithoutReadModeJms() {
        when(config.isRest()).thenReturn(false);
        Assertions.assertThatThrownBy(() -> runtimeClient.findTasks(FROM_TASK_ID, STATUS, FROM_LAST_MODIFICATION_DATE, PAGE, PAGE_SIZE))
                .hasMessage("JMS protocol is not implemented for this service.");
    }

    private void findTasksRest(TaskInputVariablesReadMode mode) {
        queryParams = TaskAssigningRuntimeClientImpl.TaskQueryParamsBuilder.builder()
                .fromTaskId(FROM_TASK_ID)
                .status(STATUS)
                .fromLastModificationDate(FROM_LAST_MODIFICATION_DATE)
                .page(PAGE)
                .pageSize(PAGE_SIZE)
                .build();
        if (mode == null) {
            queryParams.put(QueryParamName.TASK_INPUT_VARIABLES_MODE, TaskInputVariablesReadMode.DONT_READ.name());
        } else {
            queryParams.put(QueryParamName.TASK_INPUT_VARIABLES_MODE, mode.name());
        }
        when(config.isRest()).thenReturn(true);
        String expectedUri = SERVER_URL + "/" + TASK_ASSIGNING_RUNTIME_URI + "/" + TASK_ASSIGNING_QUERIES_TASK_DATA_URI;

        doReturn(taskDataList)
                .when(runtimeClient)
                .makeHttpPostRequestAndCreateCustomResponse(eq(expectedUri),
                                                            eq(queryParams),
                                                            eq(TaskDataList.class));
        TaskDataList result;
        if (mode == null) {
            result = runtimeClient.findTasks(FROM_TASK_ID, STATUS, FROM_LAST_MODIFICATION_DATE, PAGE, PAGE_SIZE);
        } else {
            result = runtimeClient.findTasks(FROM_TASK_ID, STATUS, FROM_LAST_MODIFICATION_DATE, PAGE, PAGE_SIZE, mode);
        }
        assertEquals(taskDataList, result);
    }
}

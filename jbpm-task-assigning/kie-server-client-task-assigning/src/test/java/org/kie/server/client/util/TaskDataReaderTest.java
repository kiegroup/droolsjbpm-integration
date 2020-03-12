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

package org.kie.server.client.util;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.server.api.model.taskassigning.PlanningExecutionResult;
import org.kie.server.api.model.taskassigning.OrganizationalEntity;
import org.kie.server.api.model.taskassigning.PlanningItemList;
import org.kie.server.api.model.taskassigning.TaskData;
import org.kie.server.api.model.taskassigning.TaskDataList;
import org.kie.server.api.model.taskassigning.TaskInputVariablesReadMode;
import org.kie.server.client.TaskAssigningRuntimeClient;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class TaskDataReaderTest {

    private long TASK1 = 1;
    private long TASK2 = 2;
    private long TASK3 = 3;
    private long TASK4 = 4;
    private long TASK5 = 5;

    private String PO1 = "PO1";
    private String PO2 = "PO2";
    private String PO3 = "PO3";
    private String PO4 = "PO4";
    private String PO5 = "PO5";

    private TaskAssigningRuntimeClient runtimeClient;

    private List<Pair<Long, String>> dataSet = new ArrayList<>();

    private TaskDataReader reader;

    @Parameterized.Parameter
    public int pageSize;

    @Parameterized.Parameters(name = "readTasks({0})")
    public static Object[] data() {
        return new Object[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14};
    }

    @Before
    public void setUp() {
        runtimeClient = new TaskAssigningRuntimeClientMock();
        this.reader = TaskDataReader.from(runtimeClient);

        dataSet = Arrays.asList(Pair.of(TASK1, PO1),
                                Pair.of(TASK1, PO2),
                                Pair.of(TASK1, PO3),
                                Pair.of(TASK2, null),
                                Pair.of(TASK3, PO1),
                                Pair.of(TASK3, PO2),
                                Pair.of(TASK3, PO3),
                                Pair.of(TASK3, PO4),
                                Pair.of(TASK4, PO5),
                                Pair.of(TASK5, PO2),
                                Pair.of(TASK5, PO3),
                                Pair.of(TASK5, PO4),
                                Pair.of(TASK5, PO5));
    }

    @Test
    public void readTasks1() {
        TaskDataReader.Result result = reader.readTasks(0, null, null, pageSize, null);
        verifyTasks(result.getTasks());
    }

    @Test
    public void readTasks2() {
        TaskDataReader.Result result = reader.readTasks(0, null, null, pageSize);
        verifyTasks(result.getTasks());
    }

    private void verifyTasks(List<TaskData> taskDataList) {
        assertEquals(mockTaskData(TASK1, PO1, PO2, PO3), taskDataList.get(0));
        assertEquals(mockTaskData(TASK2), taskDataList.get(1));
        assertEquals(mockTaskData(TASK3, PO1, PO2, PO3, PO4), taskDataList.get(2));
        assertEquals(mockTaskData(TASK4, PO5), taskDataList.get(3));
        assertEquals(mockTaskData(TASK5, PO2, PO3, PO4, PO5), taskDataList.get(4));
    }

    private class TaskAssigningRuntimeClientMock implements TaskAssigningRuntimeClient {

        @Override
        public PlanningExecutionResult executePlanning(PlanningItemList planningItemList, String userId) {
            return null;
        }

        @Override
        public TaskDataList findTasks(Long fromTaskId, List<String> status, LocalDateTime fromLastModificationDate, Integer page, Integer pageSize) {
            return findTasks(fromTaskId, status, fromLastModificationDate, page, pageSize);
        }

        /**
         * emulate the DB paged querying.
         */
        @Override
        public TaskDataList findTasks(Long fromTaskId, List<String> status, LocalDateTime fromLastModificationDate,
                                      Integer page, Integer pageSize, TaskInputVariablesReadMode taskInputVariablesReadMode) {
            int offset = page * pageSize;
            int count = 0;
            long taskId;
            long previousTaskId = -1;
            String potentialOwnerId;
            List<TaskData> result = new ArrayList<>();
            TaskData taskData = null;
            // Emulate the DB query. For the test purposes the other parameters like lastModificationDate, status, etc.
            // are not relevant since the goal is to check the consumption of the total tasks in a paged based basis
            // and not the query filtering.
            List<Pair<Long, String>> filteredDataSet = dataSet.stream().filter(row -> row.getLeft() >= fromTaskId).collect(Collectors.toList());

            while (offset < filteredDataSet.size() && count < pageSize) {
                taskId = filteredDataSet.get(offset).getLeft();
                if (previousTaskId != taskId) {
                    previousTaskId = taskId;
                    taskData = TaskData.builder().taskId(taskId).potentialOwners(new HashSet<>()).build();
                    result.add(taskData);
                }
                potentialOwnerId = filteredDataSet.get(offset).getRight();
                if (!isEmpty(potentialOwnerId)) {
                    taskData.getPotentialOwners().add(OrganizationalEntity.builder().name(potentialOwnerId).build());
                }
                count++;
                offset++;
            }
            return new TaskDataList(result);
        }
    }

    private TaskData mockTaskData(Long taskId, String... potentialOwners) {
        Set<OrganizationalEntity> potentialOwnersSet = Stream.of(potentialOwners).map(po -> OrganizationalEntity.builder()
                .name(po)
                .build())
                .collect(Collectors.toSet());
        return TaskData.builder()
                .taskId(taskId)
                .potentialOwners(potentialOwnersSet)
                .build();
    }
}

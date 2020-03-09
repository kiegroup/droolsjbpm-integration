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

package org.kie.server.services.taskassigning.runtime.query;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSet;
import org.junit.Before;
import org.junit.Test;
import org.kie.server.api.model.taskassigning.PlanningTask;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractTaskAssigningDataQueryMapperTest<T extends AbstractTaskAssigningQueryMapper<?>> {

    /**
     * Task1 has planning task and potential owners.
     */
    static final Long TASK1_ID = 1L;
    static final Date TASK1_CREATED_ON = new Date();
    static final String TASK1_ACTUAL_OWNER = "TASK1_ACTUAL_OWNER";
    static final String TASK1_DEPLOYMENT_ID = "TASK1_DEPLOYMENT_ID";
    static final String TASK1_NAME = "TASK1_NAME";
    static final Integer TASK1_PRIORITY = 1;
    static final String TASK1_PROCESS_ID = "TASK1_PROCESS_ID";
    static final Long TASK1_PROCESS_INSTANCE_ID = 1L;
    static final String TASK1_STATUS = "TASK1_STATUS";
    static final Date TASK1_LAST_MODIFICATION_DATE = new Date();
    static final Long TASK1_PLANNING_TASK_ID = 1L;
    static final String TASK1_PLANNING_TASK_ASSIGNED_USER = "TASK1_PLANNING_ASSIGNED_USER";
    static final Boolean TASK1_PLANNING_TASK_PUBLISHED = true;
    static final Integer TASK1_PLANNING_TASK_INDEX = 1;

    static final String TASK1_PO_1_ID = "TASK1_PO_1_ID";
    static final String TASK1_PO_1_TYPE = "User";
    static final String TASK1_PO_2_ID = "TASK1_PO_2_ID";
    static final String TASK1_PO_2_TYPE = "Group";

    /**
     * Task2 has planning task and no potential owners.
     */
    static final Long TASK2_ID = 2L;
    static final Date TASK2_CREATED_ON = new Date();
    static final String TASK2_ACTUAL_OWNER = "TASK2_ACTUAL_OWNER";
    static final String TASK2_DEPLOYMENT_ID = "TASK2_DEPLOYMENT_ID";
    static final String TASK2_NAME = "TASK2_NAME";
    static final Integer TASK2_PRIORITY = 2;
    static final String TASK2_PROCESS_ID = "TASK2_PROCESS_ID";
    static final Long TASK2_PROCESS_INSTANCE_ID = 2L;
    static final String TASK2_STATUS = "TASK2_STATUS";
    static final Date TASK2_LAST_MODIFICATION_DATE = new Date();
    static final Long TASK2_PLANNING_TASK_ID = 2L;
    static final String TASK2_PLANNING_TASK_ASSIGNED_USER = "TASK2_PLANNING_ASSIGNED_USER";
    static final Boolean TASK2_PLANNING_TASK_PUBLISHED = false;
    static final Integer TASK2_PLANNING_TASK_INDEX = 2;

    /**
     * Task3 has potential owners and no planning task.
     */
    static final Long TASK3_ID = 3L;
    static final Date TASK3_CREATED_ON = new Date();
    static final String TASK3_ACTUAL_OWNER = "TASK3_ACTUAL_OWNER";
    static final String TASK3_DEPLOYMENT_ID = "TASK3_DEPLOYMENT_ID";
    static final String TASK3_NAME = "TASK3_NAME";
    static final Integer TASK3_PRIORITY = 3;
    static final String TASK3_PROCESS_ID = "TASK3_PROCESS_ID";
    static final Long TASK3_PROCESS_INSTANCE_ID = 3L;
    static final String TASK3_STATUS = "TASK3_STATUS";
    static final Date TASK3_LAST_MODIFICATION_DATE = new Date();

    static final String TASK3_PO_1_ID = "TASK3_PO_1_ID";
    static final String TASK3_PO_1_TYPE = "User";
    static final String TASK3_PO_2_ID = "TASK3_PO_2_ID";
    static final String TASK3_PO_2_TYPE = "Group";

    T queryMapper;

    @Before
    public void setUp() {
        queryMapper = createQueryMapper();
    }

    protected abstract T createQueryMapper();

    protected abstract void verifyResult(List<?> result);

    protected abstract String getExpectedName();

    protected abstract boolean readPotentialOwnersExpectedValue();

    @Test
    public void map() {
        List<List<Object>> rawValues = createTestValues();
        DataSet dataSet = mockDataSet(rawValues);
        List<?> result = queryMapper.map(dataSet);
        verifyResult(result);
    }

    @Test
    public void getName() {
        assertEquals(getExpectedName(), queryMapper.getName());
    }

    @Test
    public void toLocalDateTimeNull() {
        assertNull(null, TaskAssigningTaskDataWithPotentialOwnersQueryMapper.toLocalDateTime(null));
    }

    @Test
    public void toLocalDateTimeFromDate() {
        Date date = new Date();
        LocalDateTime expectedValue = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        assertEquals(expectedValue, TaskAssigningTaskDataWithPotentialOwnersQueryMapper.toLocalDateTime(date));
    }

    @Test
    public void toLocalDateTimeFromLocalDateTime() {
        LocalDateTime value = LocalDateTime.now();
        assertEquals(value, TaskAssigningTaskDataWithPotentialOwnersQueryMapper.toLocalDateTime(value));
    }

    @Test
    public void readPotentialOwners() {
        assertEquals(readPotentialOwnersExpectedValue(), queryMapper.readPotentialOwners());
    }

    protected List<List<Object>> createTestValues() {
        PlanningTask task1PlanningTask = PlanningTask.builder()
                .taskId(TASK1_PLANNING_TASK_ID)
                .assignedUser(TASK1_PLANNING_TASK_ASSIGNED_USER)
                .index(TASK1_PLANNING_TASK_INDEX)
                .published(TASK1_PLANNING_TASK_PUBLISHED)
                .build();
        List<Pair<String, String>> task1PotentialOwners = Arrays.asList(Pair.of(TASK1_PO_1_ID, TASK1_PO_1_TYPE),
                                                                        Pair.of(TASK1_PO_2_ID, TASK1_PO_2_TYPE));

        List<List<Object>> task1RawValues = mockRawValues(TASK1_ID,
                                                          TASK1_CREATED_ON,
                                                          TASK1_ACTUAL_OWNER,
                                                          TASK1_DEPLOYMENT_ID,
                                                          TASK1_NAME,
                                                          TASK1_PRIORITY,
                                                          TASK1_PROCESS_ID,
                                                          TASK1_PROCESS_INSTANCE_ID,
                                                          TASK1_STATUS,
                                                          TASK1_LAST_MODIFICATION_DATE,
                                                          task1PlanningTask,
                                                          task1PotentialOwners);

        PlanningTask task2PlanningTask = PlanningTask.builder()
                .taskId(TASK2_PLANNING_TASK_ID)
                .assignedUser(TASK2_PLANNING_TASK_ASSIGNED_USER)
                .index(TASK2_PLANNING_TASK_INDEX)
                .published(TASK2_PLANNING_TASK_PUBLISHED)
                .build();

        List<List<Object>> task2RawValues = mockRawValues(TASK2_ID,
                                                          TASK2_CREATED_ON,
                                                          TASK2_ACTUAL_OWNER,
                                                          TASK2_DEPLOYMENT_ID,
                                                          TASK2_NAME,
                                                          TASK2_PRIORITY,
                                                          TASK2_PROCESS_ID,
                                                          TASK2_PROCESS_INSTANCE_ID,
                                                          TASK2_STATUS,
                                                          TASK2_LAST_MODIFICATION_DATE,
                                                          task2PlanningTask,
                                                          Collections.emptyList());

        List<Pair<String, String>> task3PotentialOwners = Arrays.asList(Pair.of(TASK3_PO_1_ID, TASK3_PO_1_TYPE),
                                                                        Pair.of(TASK3_PO_2_ID, TASK3_PO_2_TYPE));

        List<List<Object>> task3RawValues = mockRawValues(TASK3_ID,
                                                          TASK3_CREATED_ON,
                                                          TASK3_ACTUAL_OWNER,
                                                          TASK3_DEPLOYMENT_ID,
                                                          TASK3_NAME,
                                                          TASK3_PRIORITY,
                                                          TASK3_PROCESS_ID,
                                                          TASK3_PROCESS_INSTANCE_ID,
                                                          TASK3_STATUS,
                                                          TASK3_LAST_MODIFICATION_DATE,
                                                          null,
                                                          task3PotentialOwners);

        List<List<Object>> rawValues = new ArrayList<>();
        rawValues.addAll(task1RawValues);
        rawValues.addAll(task2RawValues);
        rawValues.addAll(task3RawValues);
        return rawValues;
    }

    protected List<List<Object>> mockRawValues(Long taskId, Date createdOn, String actualOwner, String deploymentId, String taskName,
                                               Integer priority, String processId, Long processInstanceId, String status, Date lastModificationDate,
                                               PlanningTask planningTask,
                                               List<Pair<String, String>> potentialOwners) {
        List<List<Object>> result = new ArrayList<>();

        if (!potentialOwners.isEmpty()) {
            potentialOwners.forEach(potentialOwner -> {
                result.add(mockRawValues(taskId, createdOn, actualOwner, deploymentId, taskName, priority, processId, processInstanceId,
                                         status, lastModificationDate, planningTask, potentialOwner.getLeft(), potentialOwner.getRight()));
            });
        } else {
            result.add(mockRawValues(taskId, createdOn, actualOwner, deploymentId, taskName, priority, processId, processInstanceId,
                                     status, lastModificationDate, planningTask, null, null));
        }
        return result;
    }

    protected List<Object> mockRawValues(Long taskId, Date createdOn, String actualOwner, String deploymentId, String taskName,
                                         Integer priority, String processId, Long processInstanceId, String status, Date lastModificationDate,
                                         PlanningTask planningTask, String potentialOwnerId, String potentialOwnerType) {

        return mockRawValues(taskId, createdOn, actualOwner, deploymentId, taskName, priority, processId, processInstanceId,
                             status, lastModificationDate,
                             planningTask != null ? planningTask.getTaskId() : null,
                             planningTask != null ? planningTask.getAssignedUser() : null,
                             planningTask != null ? planningTask.getIndex() : null,
                             planningTask != null ? toInteger(planningTask.getPublished()) : null,
                             potentialOwnerId,
                             potentialOwnerType);
    }

    protected List<Object> mockRawValues(Long taskId, Date createdOn, String actualOwner, String deploymentId, String taskName,
                                         Integer priority, String processId, Long processInstanceId, String status, Date lastModificationDate,
                                         Long planningTaskId, String planningTaskAssignedUser, Integer planningTaskIndex, Integer planningTaskPublished,
                                         String potentialOwnerId, String potentialOwnerType) {
        List<Object> row = new ArrayList<>();
        row.add(taskId);
        row.add(createdOn);
        row.add(actualOwner);
        row.add(deploymentId);
        row.add(taskName);
        row.add(priority);
        row.add(processId);
        row.add(processInstanceId);
        row.add(status);
        row.add(lastModificationDate);
        row.add(planningTaskId);
        row.add(planningTaskAssignedUser);
        row.add(planningTaskIndex);
        row.add(planningTaskPublished);
        row.add(potentialOwnerId);
        row.add(potentialOwnerType);
        return row;
    }

    protected Integer toInteger(Boolean value) {
        return value != null && value ? 1 : 0;
    }

    protected DataSet mockDataSet(List<List<Object>> dbRows) {

        DataSet dataSet = mock(DataSet.class);
        addColumn(dataSet, AbstractTaskAssigningQueryMapper.TASK_QUERY_COLUMN.TASK_ID.columnName(), dbRows, 0);
        addColumn(dataSet, AbstractTaskAssigningQueryMapper.TASK_QUERY_COLUMN.CREATED_ON.columnName(), dbRows, 1);
        addColumn(dataSet, AbstractTaskAssigningQueryMapper.TASK_QUERY_COLUMN.ACTUAL_OWNER.columnName(), dbRows, 2);
        addColumn(dataSet, AbstractTaskAssigningQueryMapper.TASK_QUERY_COLUMN.DEPLOYMENT_ID.columnName(), dbRows, 3);
        addColumn(dataSet, AbstractTaskAssigningQueryMapper.TASK_QUERY_COLUMN.TASK_NAME.columnName(), dbRows, 4);
        addColumn(dataSet, AbstractTaskAssigningQueryMapper.TASK_QUERY_COLUMN.PRIORITY.columnName(), dbRows, 5);
        addColumn(dataSet, AbstractTaskAssigningQueryMapper.TASK_QUERY_COLUMN.PROCESS_ID.columnName(), dbRows, 6);
        addColumn(dataSet, AbstractTaskAssigningQueryMapper.TASK_QUERY_COLUMN.PROCESS_INSTANCE_ID.columnName(), dbRows, 7);
        addColumn(dataSet, AbstractTaskAssigningQueryMapper.TASK_QUERY_COLUMN.STATUS.columnName(), dbRows, 8);
        addColumn(dataSet, AbstractTaskAssigningQueryMapper.TASK_QUERY_COLUMN.LAST_MODIFICATION_DATE.columnName(), dbRows, 9);

        addColumn(dataSet, AbstractTaskAssigningQueryMapper.TASK_QUERY_COLUMN.PLANNING_TASK_TASK_ID.columnName(), dbRows, 10);
        addColumn(dataSet, AbstractTaskAssigningQueryMapper.TASK_QUERY_COLUMN.PLANNING_TASK_ASSIGNED_USER.columnName(), dbRows, 11);
        addColumn(dataSet, AbstractTaskAssigningQueryMapper.TASK_QUERY_COLUMN.PLANNING_TASK_INDEX.columnName(), dbRows, 12);
        addColumn(dataSet, AbstractTaskAssigningQueryMapper.TASK_QUERY_COLUMN.PLANNING_TASK_PUBLISHED.columnName(), dbRows, 13);

        addColumn(dataSet, AbstractTaskAssigningQueryMapper.TASK_QUERY_COLUMN.POTENTIAL_OWNER_ID.columnName(), dbRows, 14);
        addColumn(dataSet, AbstractTaskAssigningQueryMapper.TASK_QUERY_COLUMN.POTENTIAL_OWNER_TYPE.columnName(), dbRows, 15);

        when(dataSet.getRowCount()).thenReturn(dbRows.size());
        return dataSet;
    }

    protected void addColumn(DataSet dataSetMock, String columnId, List<List<Object>> dbRows, int columnIndex) {
        DataColumn dataColumn = mockColumn(columnId, dbRows, columnIndex);
        when(dataSetMock.getColumnById(columnId)).thenReturn(dataColumn);
    }

    protected DataColumn mockColumn(String columnId, List<List<Object>> dbRows, int columnIndex) {
        DataColumn column = mock(DataColumn.class);
        when(column.getId()).thenReturn(columnId);
        List<Object> columnValues = buildColumnValues(dbRows, columnIndex);
        when(column.getValues()).thenReturn(columnValues);
        return column;
    }

    protected List<Object> buildColumnValues(List<List<Object>> dbRows, int column) {
        return dbRows.stream().map(row -> row.get(column)).collect(Collectors.toList());
    }
}

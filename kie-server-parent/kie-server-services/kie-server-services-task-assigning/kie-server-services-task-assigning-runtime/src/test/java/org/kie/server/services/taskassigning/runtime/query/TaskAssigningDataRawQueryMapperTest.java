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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.kie.server.api.model.taskassigning.PlanningTask;

import static org.junit.Assert.assertEquals;

public class TaskAssigningDataRawQueryMapperTest extends AbstractTaskAssigningDataQueryMapperTest<TaskAssigningTaskDataRawQueryMapper> {

    @Override
    protected TaskAssigningTaskDataRawQueryMapper createQueryMapper() {
        return TaskAssigningTaskDataRawQueryMapper.get();
    }

    @Override
    protected String getExpectedName() {
        return TaskAssigningTaskDataRawQueryMapper.NAME;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void verifyResult(List<?> result) {
        List<List<Object>> rawResult = (List<List<Object>>) result;
        assertTask1IsPresent(rawResult, 0);
        assertTask2IsPresent(rawResult, 1);
        assertTask3IsPresent(rawResult, 2);
    }

    private void assertTask1IsPresent(List<List<Object>> rawResult, int index) {
        assertTaskIsPresent(rawResult, index, TASK1_ID, TASK1_CREATED_ON, TASK1_ACTUAL_OWNER, TASK1_DEPLOYMENT_ID,
                            TASK1_NAME, TASK1_PRIORITY, TASK1_PROCESS_ID, TASK1_PROCESS_INSTANCE_ID, TASK1_STATUS, TASK1_LAST_MODIFICATION_DATE,
                            PlanningTask.builder()
                                    .taskId(TASK1_PLANNING_TASK_ID)
                                    .published(TASK1_PLANNING_TASK_PUBLISHED)
                                    .assignedUser(TASK1_PLANNING_TASK_ASSIGNED_USER)
                                    .index(TASK1_PLANNING_TASK_INDEX)
                                    .build(),
                            Arrays.asList(Pair.of(TASK1_PO_1_ID, TASK1_PO_1_TYPE),
                                          Pair.of(TASK1_PO_2_ID, TASK1_PO_2_TYPE)));
    }

    private void assertTask2IsPresent(List<List<Object>> rawResult, int index) {
        assertTaskIsPresent(rawResult, index, TASK2_ID, TASK2_CREATED_ON, TASK2_ACTUAL_OWNER, TASK2_DEPLOYMENT_ID,
                            TASK2_NAME, TASK2_PRIORITY, TASK2_PROCESS_ID, TASK2_PROCESS_INSTANCE_ID, TASK2_STATUS, TASK2_LAST_MODIFICATION_DATE,
                            PlanningTask.builder()
                                    .taskId(TASK2_PLANNING_TASK_ID)
                                    .published(TASK2_PLANNING_TASK_PUBLISHED)
                                    .assignedUser(TASK2_PLANNING_TASK_ASSIGNED_USER)
                                    .index(TASK2_PLANNING_TASK_INDEX)
                                    .build(),
                            Collections.emptyList());
    }

    private void assertTask3IsPresent(List<List<Object>> rawResult, int index) {
        assertTaskIsPresent(rawResult, index, TASK3_ID, TASK3_CREATED_ON, TASK3_ACTUAL_OWNER, TASK3_DEPLOYMENT_ID,
                            TASK3_NAME, TASK3_PRIORITY, TASK3_PROCESS_ID, TASK3_PROCESS_INSTANCE_ID, TASK3_STATUS, TASK3_LAST_MODIFICATION_DATE,
                            null,
                            Arrays.asList(Pair.of(TASK3_PO_1_ID, TASK3_PO_1_TYPE),
                                          Pair.of(TASK3_PO_2_ID, TASK3_PO_2_TYPE)));
    }

    private void assertTaskIsPresent(List<List<Object>> rawDataList, int index, Long taskId, Date createdOn, String actualOwner,
                                     String deploymentId, String taskName, Integer priority, String processId,
                                     Long processInstanceId, String status, Date lastModificationDate, PlanningTask planningTask,
                                     List<Pair<String, String>> potentialOwners) {
        List<Object> expectedRow = new ArrayList<>();
        expectedRow.add(taskId);
        expectedRow.add(createdOn);
        expectedRow.add(actualOwner);
        expectedRow.add(deploymentId);
        expectedRow.add(taskName);
        expectedRow.add(priority);
        expectedRow.add(processId);
        expectedRow.add(processInstanceId);
        expectedRow.add(status);
        expectedRow.add(lastModificationDate);

        List<Object> planningTaskRow = new ArrayList<>();
        expectedRow.add(planningTaskRow);
        if (planningTask != null) {
            planningTaskRow.add(planningTask.getTaskId());
            planningTaskRow.add(planningTask.getAssignedUser());
            planningTaskRow.add(planningTask.getIndex());
            planningTaskRow.add(planningTask.getPublished());
        }
        List<List<Object>> potentialOwnersRow = new ArrayList<>();
        expectedRow.add(potentialOwnersRow);
        potentialOwners.forEach(potentialOwner -> {
            potentialOwnersRow.add(Arrays.asList(potentialOwner.getLeft(), potentialOwner.getRight()));
        });
        assertEquals(expectedRow, rawDataList.get(index));
    }
}

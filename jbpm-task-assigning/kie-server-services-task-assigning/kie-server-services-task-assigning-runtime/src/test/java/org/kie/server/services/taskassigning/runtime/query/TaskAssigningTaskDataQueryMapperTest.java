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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.kie.server.api.model.taskassigning.OrganizationalEntity;
import org.kie.server.api.model.taskassigning.PlanningTask;
import org.kie.server.api.model.taskassigning.TaskData;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TaskAssigningTaskDataQueryMapperTest extends AbstractTaskAssigningDataQueryMapperTest<TaskAssigningTaskDataQueryMapper> {

    @Override
    protected TaskAssigningTaskDataQueryMapper createQueryMapper() {
        return TaskAssigningTaskDataQueryMapper.get();
    }

    @Override
    protected String getExpectedName() {
        return TaskAssigningTaskDataQueryMapper.NAME;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void verifyResult(List<?> result) {
        List<TaskData> taskDataResult = (List<TaskData>) result;
        assertTask1IsPresent(taskDataResult, 0);
        assertTask2IsPresent(taskDataResult, 1);
        assertTask3IsPresent(taskDataResult, 2);
    }

    protected void assertTask1IsPresent(List<TaskData> result, int index) {
        assertTaskIsPresent(result, index,
                            TASK1_ID,
                            TASK1_CREATED_ON,
                            TASK1_ACTUAL_OWNER,
                            TASK1_DEPLOYMENT_ID,
                            TASK1_NAME,
                            TASK1_PRIORITY,
                            TASK1_PROCESS_ID,
                            TASK1_PROCESS_INSTANCE_ID,
                            TASK1_STATUS,
                            TASK1_LAST_MODIFICATION_DATE,
                            PlanningTask.builder()
                                    .taskId(TASK1_PLANNING_TASK_ID)
                                    .published(TASK1_PLANNING_TASK_PUBLISHED)
                                    .assignedUser(TASK1_PLANNING_TASK_ASSIGNED_USER)
                                    .index(TASK1_PLANNING_TASK_INDEX)
                                    .build(),
                            Arrays.asList(Pair.of(TASK1_PO_1_ID, TASK1_PO_1_TYPE),
                                          Pair.of(TASK1_PO_2_ID, TASK1_PO_2_TYPE)));
    }

    protected void assertTask2IsPresent(List<TaskData> result, int index) {
        assertTaskIsPresent(result, index,
                            TASK2_ID,
                            TASK2_CREATED_ON,
                            TASK2_ACTUAL_OWNER,
                            TASK2_DEPLOYMENT_ID,
                            TASK2_NAME,
                            TASK2_PRIORITY,
                            TASK2_PROCESS_ID,
                            TASK2_PROCESS_INSTANCE_ID,
                            TASK2_STATUS,
                            TASK2_LAST_MODIFICATION_DATE,
                            PlanningTask.builder()
                                    .taskId(TASK2_PLANNING_TASK_ID)
                                    .published(TASK2_PLANNING_TASK_PUBLISHED)
                                    .assignedUser(TASK2_PLANNING_TASK_ASSIGNED_USER)
                                    .index(TASK2_PLANNING_TASK_INDEX)
                                    .build(),
                            Collections.emptyList());
    }

    protected void assertTask3IsPresent(List<TaskData> result, int index) {
        assertTaskIsPresent(result, index,
                            TASK3_ID,
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
                            Arrays.asList(Pair.of(TASK3_PO_1_ID, TASK3_PO_1_TYPE),
                                          Pair.of(TASK3_PO_2_ID, TASK3_PO_2_TYPE)));
    }

    protected void assertTaskIsPresent(List<TaskData> taskDataList, int index, Long taskId, Date createdOn, String actualOwner,
                                       String deploymentId, String taskName, Integer priority, String processId,
                                       Long processInstanceId, String status, Date lastModificationDate, PlanningTask planningTask,
                                       List<Pair<String, String>> potentialOwners) {
        TaskData taskData = taskDataList.get(index);
        Set<OrganizationalEntity> potentialOwnersSet = new HashSet<>();
        potentialOwners.forEach(potentialOwner -> potentialOwnersSet.add(OrganizationalEntity.builder()
                                                                                 .name(potentialOwner.getLeft())
                                                                                 .type(potentialOwner.getRight())
                                                                                 .build())
        );

        TaskData expectedTaskData = TaskData.builder()
                .taskId(taskId)
                .createdOn(createdOn != null ? LocalDateTime.ofInstant(createdOn.toInstant(), ZoneId.systemDefault()) : null)
                .actualOwner(actualOwner)
                .containerId(deploymentId)
                .name(taskName)
                .priority(priority)
                .processId(processId)
                .processInstanceId(processInstanceId)
                .status(status)
                .lastModificationDate(lastModificationDate != null ? LocalDateTime.ofInstant(lastModificationDate.toInstant(), ZoneId.systemDefault()) : null)
                .planningTask(planningTask)
                .potentialOwners(potentialOwnersSet)
                .build();

        assertNotNull(taskData);
        assertEquals(expectedTaskData, taskData);
    }
}
